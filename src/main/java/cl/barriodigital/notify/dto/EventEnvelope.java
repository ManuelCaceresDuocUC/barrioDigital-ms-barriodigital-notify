package cl.barriodigital.notify.dto;

/**
 * Envoltorio común de los eventos que llegan por RabbitMQ.
 *
 * QUÉ ES: el "sobre" estándar de todos los mensajes; lo arma rabbitadmin al publicar. Es genérico (T):
 * el tipo del contenido cambia según la cola (correo, ticket o certificado).
 *
 * @param type          tipo de mensaje (EMAIL_NOTIFICATION, CREW_TICKET, ...)
 * @param eventId       identificador único: sirve para detectar duplicados
 * @param timestamp     momento de creación del mensaje
 * @param traceId       identificador para seguir una operación a través de varios servicios
 * @param correlationId identificador que une los logs de un mismo flujo
 * @param payload       el contenido real del comando
 */
public record EventEnvelope<T>(
        String type,
        String eventId,
        String timestamp,
        String traceId,
        String correlationId,
        T payload) {
}
