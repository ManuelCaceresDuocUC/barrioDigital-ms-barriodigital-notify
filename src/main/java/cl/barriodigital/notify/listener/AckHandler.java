package cl.barriodigital.notify.listener;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.RetryProperties;
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

    public <T> void handle(T event, Channel channel, long deliveryTag, String domain, Consumer<T> work)
            throws IOException {
        Duration wait = retry.initialInterval();

        for (int attempt = 1; ; attempt++) {
            try {
                work.accept(event);
            } catch (IllegalArgumentException ex) {
                rejectToDlq(channel, deliveryTag, domain, attempt, "error no recuperable", ex);
                return;
            } catch (RuntimeException ex) {
                if (attempt >= retry.maxAttempts()) {
                    rejectToDlq(channel, deliveryTag, domain, attempt, "reintentos agotados", ex);
                    return;
                }
                log.warn("Fallo procesando mensaje de {} (intento {}/{}): {}. Reintento en {} ms",
                        domain, attempt, retry.maxAttempts(), ex.toString(), wait.toMillis());
                if (!pause(wait)) {
                    rejectToDlq(channel, deliveryTag, domain, attempt, "proceso interrumpido", ex);
                    return;
                }
                wait = Duration.ofMillis(Math.round(wait.toMillis() * retry.multiplier()));
                continue;
            }
            channel.basicAck(deliveryTag, false);
            return;
        }
    }

    private void rejectToDlq(Channel channel, long deliveryTag, String domain, int attempts,
                             String reason, RuntimeException ex) throws IOException {
        channel.basicNack(deliveryTag, false, false);
        log.error("Mensaje de {} enviado a la DLQ ({}) tras {} intento(s): {}",
                domain, reason, attempts, ex.toString());
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