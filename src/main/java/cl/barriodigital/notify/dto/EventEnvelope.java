package cl.barriodigital.notify.dto;

/** Envoltorio común de los eventos que llegan por RabbitMQ. */
public record EventEnvelope<T>(
        String type,
        String eventId,
        String timestamp,
        String traceId,
        String correlationId,
        T payload) {
}