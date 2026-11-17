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

/**
 * Consumidor del dominio de certificados (q.cmd.certificate).
 *
 * QUÉ ES: adaptador delgado entre RabbitMQ y la lógica de negocio. No decide nada: entrega el mensaje
 * a AckHandler junto con la acción a ejecutar (CertificateService.dispatch).
 */
@Component
public class CertificateListener {

    private final CertificateService certificateService;
    private final AckHandler ackHandler;

    public CertificateListener(CertificateService certificateService, AckHandler ackHandler) {
        this.certificateService = certificateService;
        this.ackHandler = ackHandler;
    }

    // Spring crea un consumidor suscrito a la cola (el nombre sale del yaml). Cada mensaje llega ya
    // convertido de JSON a EventEnvelope<CertificateEvent>; el canal y el deliveryTag permiten el ACK manual.
    @RabbitListener(queues = "${barriodigital.messaging.certificate.queue}")
    public void onMessage(EventEnvelope<CertificateEvent> envelope, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                          @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) throws IOException {
        // certificateService::dispatch es una referencia a método: la acción que AckHandler ejecutará.
        ackHandler.handle(envelope, channel, deliveryTag, queue, certificateService::dispatch);
    }
}
