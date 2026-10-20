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
    private final DlqReporter dlqReporter;

    public AckHandler(RetryProperties retry, DlqReporter dlqReporter) {
        this.retry = retry;
        this.dlqReporter = dlqReporter;
    }

    public <T> void handle(EventEnvelope<T> envelope, Channel channel, long deliveryTag,
                           String queue, Consumer<T> work) throws IOException {
        if (envelope == null || envelope.payload() == null) {
            rejectToDlq(channel, deliveryTag, queue, envelope, 0, "envelope sin payload",
                    new IllegalArgumentException("El mensaje no trae payload"));
            return;
        }

        Duration wait = retry.initialInterval();

        for (int attempt = 1; ; attempt++) {
            try {
                work.accept(envelope.payload());
            } catch (IllegalArgumentException ex) {
                rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "error no recuperable", ex);
                return;
            } catch (RuntimeException ex) {
                if (attempt >= retry.maxAttempts()) {
                    rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "reintentos agotados", ex);
                    return;
                }
                log.warn("Fallo procesando mensaje de {} (eventId={}, correlationId={}) (intento {}/{}). Reintento en {} ms",
                        queue, envelope.eventId(), envelope.correlationId(),
                        attempt, retry.maxAttempts(), wait.toMillis());
                if (!pause(wait)) {
                    rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "proceso interrumpido", ex);
                    return;
                }
                wait = Duration.ofMillis(Math.round(wait.toMillis() * retry.multiplier()));
                continue;
            }
            channel.basicAck(deliveryTag, false);
            return;
        }
    }

    private void rejectToDlq(Channel channel, long deliveryTag, String queue, EventEnvelope<?> envelope,
                             int attempts, String reason, RuntimeException ex) throws IOException {
        channel.basicNack(deliveryTag, false, false);
        dlqReporter.report(queue, envelope, attempts, reason, ex);
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