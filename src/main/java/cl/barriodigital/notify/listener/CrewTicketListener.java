package cl.barriodigital.notify.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.service.CrewTicketService;

/** Consumidor del dominio de cuadrillas (q.cmd.crew). */
@Component
public class CrewTicketListener {

    private final CrewTicketService crewTicketService;
    private final AckHandler ackHandler;

    public CrewTicketListener(CrewTicketService crewTicketService, AckHandler ackHandler) {
        this.crewTicketService = crewTicketService;
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${barriodigital.messaging.crew.queue}")
    public void onMessage(EventEnvelope<CrewTicketEvent> envelope, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                          @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) throws IOException {
        ackHandler.handle(envelope, channel, deliveryTag, queue, crewTicketService::assign);
    }
}