package cl.barriodigital.notify.dto;

import java.io.Serializable;

public record EmailNotificationEvent(
    String to,
    String subject,
    String body,
    String procedureId
) implements Serializable {}