package cl.barriodigital.notify.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.service.EmailService;

/** Consumidor del dominio de correos (q.cmd.email). */
@Component
public class EmailListener {

    private final EmailService emailService;
    private final AckHandler ackHandler;

    public EmailListener(EmailService emailService, AckHandler ackHandler) {
        this.emailService = emailService;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${barriodigital.messaging.email.queue}")
    public void onMessage(EmailNotificationEvent event, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        ackHandler.handle(event, channel, deliveryTag, "email", emailService::send);
    }
}