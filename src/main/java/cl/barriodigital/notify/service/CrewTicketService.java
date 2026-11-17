package cl.barriodigital.notify.service;

import org.springframework.stereotype.Service;

import cl.barriodigital.notify.dto.CrewTicketEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * QUÉ ES: lógica de negocio del dominio "ticket de cuadrilla".
 * ESTADO: simula la asignación (solo log). Es el servicio que se usa en la demo de la DLQ: un ticket
 * con ticketId vacío lanza IllegalArgumentException y el mensaje termina en q.cmd.crew.dlq.
 */
@Slf4j
@Service
public class CrewTicketService {

    public void assign(CrewTicketEvent event) {
        // Validación: sin identificador no hay ticket. Error no recuperable -> DLQ sin reintentos.
        if (event.ticketId() == null || event.ticketId().isBlank()) {
            throw new IllegalArgumentException("El ticket debe tener un identificador");
        }
        log.info("Procesando ticket de cuadrilla {} (categoría {}, prioridad {})",
                event.ticketId(), event.category(), event.priority());
        // Simulación de la asignación a una cuadrilla.
        log.info("Ticket {} asignado para atención en {}", event.ticketId(), event.location());
    }
}
