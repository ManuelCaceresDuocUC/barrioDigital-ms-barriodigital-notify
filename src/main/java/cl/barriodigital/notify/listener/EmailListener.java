package cl.barriodigital.notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.service.EmailService;

/** Consumidor del dominio de correos (q.cmd.email). */
@Component
public class EmailListener {

    private final EmailService emailService;

    public EmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${barriodigital.messaging.email.queue}")
    public void onMessage(EmailNotificationEvent event) {
        emailService.send(event);
    }
}