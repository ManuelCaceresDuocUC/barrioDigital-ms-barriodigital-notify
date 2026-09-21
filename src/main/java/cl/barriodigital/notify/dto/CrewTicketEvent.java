package cl.barriodigital.notify.dto;

import java.io.Serializable;

public record CrewTicketEvent(
    String ticketId,
    String category,
    String location,
    String priority
) implements Serializable {}