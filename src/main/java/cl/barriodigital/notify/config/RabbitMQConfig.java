package cl.barriodigital.notify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QUÉ ES: la "topología" de RabbitMQ declarada como código.
 *
 * QUÉ HACE: al arrancar, Spring AMQP lee estos beans y crea en el broker (si no existen) los 3 exchanges,
 * las 3 colas principales, sus 3 DLQ y los bindings que las conectan. Resultado:
 *
 *   cmd.direct --(email.send / crew.ticket / certificate.gen)--> q.cmd.email / q.cmd.crew / q.cmd.certificate
 *   cmd.topic  --(email.* / crew.# / certificate.*)------------> las mismas colas (enrutado por patrón)
 *   Si un mensaje es rechazado (NACK sin reencolar) en una cola principal:
 *   cmd.dead.dlx --(email.dlq / crew.dlq / certificate.dlq)----> q.cmd.*.dlq
 */
@Configuration
// Registra los tres records de propiedades como beans (leen application.yaml).
@EnableConfigurationProperties({ MessagingProperties.class, RetryProperties.class, IdempotencyProperties.class })
public class RabbitMQConfig {

    private final MessagingProperties messaging;

    public RabbitMQConfig(MessagingProperties messaging) {
        this.messaging = messaging;
    }

    // --- Exchanges ---
    // Direct: enruta por clave EXACTA (es el que usa hoy rabbitadmin al publicar).
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(messaging.exchanges().direct());
    }

    // Topic: enruta por patrones (* = una palabra, # = cero o más palabras). Declarado para uso futuro.
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(messaging.exchanges().topic());
    }

    // Dead-letter exchange: recibe los mensajes que las colas principales rechazan.
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(messaging.exchanges().deadLetter());
    }

    // --- Colas principales (cada una apunta a su DLQ) ---
    @Bean
    public Queue emailQueue() {
        return mainQueue(messaging.email());
    }

    @Bean
    public Queue crewQueue() {
        return mainQueue(messaging.crew());
    }

    @Bean
    public Queue certificateQueue() {
        return mainQueue(messaging.certificate());
    }

    // --- Colas DLQ ---
    // Durables: sobreviven a un reinicio del broker, así no se pierden los mensajes fallidos.
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(messaging.email().deadLetterQueue()).build();
    }

    @Bean
    public Queue crewDlq() {
        return QueueBuilder.durable(messaging.crew().deadLetterQueue()).build();
    }

    @Bean
    public Queue certificateDlq() {
        return QueueBuilder.durable(messaging.certificate().deadLetterQueue()).build();
    }

    // --- Bindings direct (routing key exacta) ---
    // Un binding = regla "los mensajes de este exchange con esta clave van a esta cola".
    @Bean
    public Binding emailDirectBinding() {
        return directBinding(emailQueue(), messaging.email());
    }

    @Bean
    public Binding crewDirectBinding() {
        return directBinding(crewQueue(), messaging.crew());
    }

    @Bean
    public Binding certificateDirectBinding() {
        return directBinding(certificateQueue(), messaging.certificate());
    }

    // --- Bindings topic (patrón con comodines) ---
    @Bean
    public Binding emailTopicBinding() {
        return topicBinding(emailQueue(), messaging.email());
    }

    @Bean
    public Binding crewTopicBinding() {
        return topicBinding(crewQueue(), messaging.crew());
    }

    @Bean
    public Binding certificateTopicBinding() {
        return topicBinding(certificateQueue(), messaging.certificate());
    }

    // --- Bindings de las DLQ ---
    @Bean
    public Binding emailDlqBinding() {
        return dlqBinding(emailDlq(), messaging.email());
    }

    @Bean
    public Binding crewDlqBinding() {
        return dlqBinding(crewDlq(), messaging.crew());
    }

    @Bean
    public Binding certificateDlqBinding() {
        return dlqBinding(certificateDlq(), messaging.certificate());
    }

    // Convierte JSON a DTOs Java automáticamente
    // (el mensaje llega como JSON y el listener recibe directamente un EventEnvelope<...>).
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Cola principal durable cuyo "dead letter" apunta al exchange de errores con la clave de su dominio.
    // Esa configuración hace que un basicNack(requeue=false) mande el mensaje a la DLQ correspondiente.
    private Queue mainQueue(MessagingProperties.Domain domain) {
        return QueueBuilder.durable(domain.queue())
                .deadLetterExchange(messaging.exchanges().deadLetter())
                .deadLetterRoutingKey(domain.deadLetterKey())
                .build();
    }

    // Helpers que construyen los tres tipos de binding para no repetir código.
    private Binding directBinding(Queue queue, MessagingProperties.Domain domain) {
        return BindingBuilder.bind(queue).to(directExchange()).with(domain.directKey());
    }

    private Binding topicBinding(Queue queue, MessagingProperties.Domain domain) {
        return BindingBuilder.bind(queue).to(topicExchange()).with(domain.topicKey());
    }

    private Binding dlqBinding(Queue dlq, MessagingProperties.Domain domain) {
        return BindingBuilder.bind(dlq).to(deadLetterExchange()).with(domain.deadLetterKey());
    }
}
