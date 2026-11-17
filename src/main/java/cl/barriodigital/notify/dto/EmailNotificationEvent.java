package cl.barriodigital.notify.dto;

import java.io.Serializable;

/**
 * QUÉ ES: carga útil del comando "enviar correo" (routing key email.send).
 *
 * @param to          destinatario (obligatorio)
 * @param subject     asunto (si falta se usa uno por defecto)
 * @param body        texto del correo (si falta se usa el asunto)
 * @param procedureId identificador del trámite relacionado (solo para trazabilidad en los logs)
 */
public record EmailNotificationEvent(
    String to,
    String subject,
    String body,
    String procedureId
) implements Serializable {}
