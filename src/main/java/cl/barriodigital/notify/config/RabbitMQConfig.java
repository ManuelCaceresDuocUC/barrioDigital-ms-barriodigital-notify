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

@Configuration
@EnableConfigurationProperties({ MessagingProperties.class, RetryProperties.class, IdempotencyProperties.class })
public class RabbitMQConfig {

    private final MessagingProperties messaging;

    public RabbitMQConfig(MessagingProperties messaging) {
        this.messaging = messaging;
    }

    // --- Exchanges ---
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(messaging.exchanges().direct());
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(messaging.exchanges().topic());
    }

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
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Queue mainQueue(MessagingProperties.Domain domain) {
        return QueueBuilder.durable(domain.queue())
                .deadLetterExchange(messaging.exchanges().deadLetter())
                .deadLetterRoutingKey(domain.deadLetterKey())
                .build();
    }

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