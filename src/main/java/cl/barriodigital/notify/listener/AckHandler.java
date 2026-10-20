package cl.barriodigital.notify.listener;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.EventEnvelope;
import lombok.extern.slf4j.Slf4j;

/**
 * Confirmación manual de mensajes: ACK si se procesó, reintentos con backoff si falla algo
 * recuperable, y NACK sin reencolar (el mensaje va a la DLQ) si el error no tiene arreglo
 * o se agotaron los intentos.
 */
@Slf4j
@Component
public class AckHandler {

    private final RetryProperties retry;

    public AckHandler(RetryProperties retry) {
        this.retry = retry;
    }

    public <T> void handle(EventEnvelope<T> envelope, Channel channel, long deliveryTag,
                           String domain, Consumer<T> work) throws IOException {
        if (envelope == null || envelope.payload() == null) {
            rejectToDlq(channel, deliveryTag, domain, envelope, 0, "envelope sin payload",
                    new IllegalArgumentException("El mensaje no trae payload"));
            return;
        }

        Duration wait = retry.initialInterval();

        for (int attempt = 1; ; attempt++) {
            try {
                work.accept(envelope.payload());
            } catch (IllegalArgumentException ex) {
                rejectToDlq(channel, deliveryTag, domain, envelope, attempt, "error no recuperable", ex);
                return;
            } catch (RuntimeException ex) {
                if (attempt >= retry.maxAttempts()) {
                    rejectToDlq(channel, deliveryTag, domain, envelope, attempt, "reintentos agotados", ex);
                    return;
                }
                log.warn("Fallo procesando mensaje de {} ({}) (intento {}/{}): {}. Reintento en {} ms",
                        domain, describe(envelope), attempt, retry.maxAttempts(), ex.toString(), wait.toMillis());
                if (!pause(wait)) {
                    rejectToDlq(channel, deliveryTag, domain, envelope, attempt, "proceso interrumpido", ex);
                    return;
                }
                wait = Duration.ofMillis(Math.round(wait.toMillis() * retry.multiplier()));
                continue;
            }
            channel.basicAck(deliveryTag, false);
            return;
        }
    }

    private void rejectToDlq(Channel channel, long deliveryTag, String domain, EventEnvelope<?> envelope,
                             int attempts, String reason, RuntimeException ex) throws IOException {
        channel.basicNack(deliveryTag, false, false);
        log.error("Mensaje de {} ({}) enviado a la DLQ ({}) tras {} intento(s): {}",
                domain, describe(envelope), reason, attempts, ex.toString());
    }

    private static String describe(EventEnvelope<?> envelope) {
        if (envelope == null) {
            return "sin envelope";
        }
        return "eventId=%s, type=%s, correlationId=%s"
                .formatted(envelope.eventId(), envelope.type(), envelope.correlationId());
    }

    private boolean pause(Duration wait) {
        try {
            Thread.sleep(wait.toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}