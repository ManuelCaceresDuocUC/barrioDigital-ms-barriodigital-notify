package cl.barriodigital.notify.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.service.CertificateService;

/** Consumidor del dominio de certificados (q.cmd.certificate). */
@Component
public class CertificateListener {

    private final CertificateService certificateService;
    private final AckHandler ackHandler;

    public CertificateListener(CertificateService certificateService, AckHandler ackHandler) {
        this.certificateService = certificateService;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${barriodigital.messaging.certificate.queue}")
    public void onMessage(EventEnvelope<CertificateEvent> envelope, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        ackHandler.handle(envelope, channel, deliveryTag, "certificate", certificateService::dispatch);
    }
}