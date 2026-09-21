package cl.barriodigital.notify.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String MAIN_EXCHANGE = "ex.barriodigital.direct";
    public static final String DLX_EXCHANGE = "ex.barriodigital.dlx";

    // Queues Principales
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_CREW = "q.cmd.crew";
    public static final String QUEUE_CERTIFICATE = "q.cmd.certificate";

    // Queues DLQ
    public static final String DLQ_EMAIL = "q.cmd.email.dlq";
    public static final String DLQ_CREW = "q.cmd.crew.dlq";
    public static final String DLQ_CERTIFICATE = "q.cmd.certificate.dlq";

    // Routing Keys DLQ
    public static final String RK_EMAIL_DLQ = "rk.cmd.email.dlq";
    public static final String RK_CREW_DLQ = "rk.cmd.crew.dlq";
    public static final String RK_CERTIFICATE_DLQ = "rk.cmd.certificate.dlq";

    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(MAIN_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // --- Definición de Colas Principales asociadas a su DLQ ---
    @Bean
    public Queue emailQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RK_EMAIL_DLQ);
        return QueueBuilder.durable(QUEUE_EMAIL).withArguments(args).build();
    }

    @Bean
    public Queue crewQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RK_CREW_DLQ);
        return QueueBuilder.durable(QUEUE_CREW).withArguments(args).build();
    }

    @Bean
    public Queue certificateQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RK_CERTIFICATE_DLQ);
        return QueueBuilder.durable(QUEUE_CERTIFICATE).withArguments(args).build();
    }

    // --- Definición de Colas DLQ ---
    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(DLQ_EMAIL).build();
    }

    @Bean
    public Queue crewDlq() {
        return QueueBuilder.durable(DLQ_CREW).build();
    }

    @Bean
    public Queue certificateDlq() {
        return QueueBuilder.durable(DLQ_CERTIFICATE).build();
    }

    // --- Bindings para DLQ ---
    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq()).to(deadLetterExchange()).with(RK_EMAIL_DLQ);
    }

    @Bean
    public Binding crewDlqBinding() {
        return BindingBuilder.bind(crewDlq()).to(deadLetterExchange()).with(RK_CREW_DLQ);
    }

    @Bean
    public Binding certificateDlqBinding() {
        return BindingBuilder.bind(certificateDlq()).to(deadLetterExchange()).with(RK_CERTIFICATE_DLQ);
    }

    // Converter para transformar JSON a DTOs Java automáticamente
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}