package cl.barriodigital.notify.dto;

import java.io.Serializable;

/**
 * QUÉ ES: carga útil del comando "ticket de visita a cuadrilla" (routing key crew.ticket).
 *
 * @param ticketId identificador del ticket (obligatorio; si viene vacío, el mensaje va a la DLQ)
 * @param category categoría del problema (por ejemplo BACHE)
 * @param location dirección o lugar de la atención
 * @param priority prioridad (por ejemplo ALTA)
 */
public record CrewTicketEvent(
    String ticketId,
    String category,
    String location,
    String priority
) implements Serializable {}
