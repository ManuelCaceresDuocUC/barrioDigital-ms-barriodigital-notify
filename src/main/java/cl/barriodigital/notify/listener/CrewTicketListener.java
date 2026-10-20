package cl.barriodigital.notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.service.CrewTicketService;

/** Consumidor del dominio de cuadrillas (q.cmd.crew). */
@Component
public class CrewTicketListener {

    private final CrewTicketService crewTicketService;

    public CrewTicketListener(CrewTicketService crewTicketService) {
        this.crewTicketService = crewTicketService;
    }

    @RabbitListener(queues = "${barriodigital.messaging.crew.queue}")
    public void onMessage(CrewTicketEvent event) {
        crewTicketService.assign(event);
    }
}