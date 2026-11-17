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
 * Confirmación manual de mensajes: ACK si se procesó (o si ya se había procesado antes),
 * reintentos con backoff si falla algo recuperable, y NACK sin reencolar (el mensaje va a la
 * DLQ) si el error no tiene arreglo o se agotaron los intentos.
 *
 * QUÉ ES: la pieza central del servicio; concentra TODA la lógica de ACK/NACK, reintentos e idempotencia
 * para que los listeners por dominio queden mínimos y la lógica de negocio no conozca RabbitMQ.
 *
 * VOCABULARIO: basicAck = "listo, bórralo de la cola"; basicNack(requeue=false) = "no pude, no lo
 * devuelvas a la cola: mándalo a la DLQ"; deliveryTag = número que identifica la entrega en el canal.
 */
@Slf4j
@Component
public class AckHandler {

    private final RetryProperties retry;
    private final DlqReporter dlqReporter;
    private final ProcessedEventStore processedEvents;

    public AckHandler(RetryProperties retry, DlqReporter dlqReporter, ProcessedEventStore processedEvents) {
        this.retry = retry;
        this.dlqReporter = dlqReporter;
        this.processedEvents = processedEvents;
    }

    /**
     * @param envelope    mensaje recibido
     * @param channel     canal de RabbitMQ por el que se confirma
     * @param deliveryTag identificador de esta entrega
     * @param queue       cola de origen (solo para logs y métricas)
     * @param work        la acción de negocio a ejecutar con el contenido (enviar correo, asignar ticket...)
     */
    public <T> void handle(EventEnvelope<T> envelope, Channel channel, long deliveryTag,
                           String queue, Consumer<T> work) throws IOException {
        // Caso 1: mensaje sin contenido -> no tiene arreglo -> a la DLQ de inmediato.
        if (envelope == null || envelope.payload() == null) {
            rejectToDlq(channel, deliveryTag, queue, envelope, 0, "envelope sin payload",
                    new IllegalArgumentException("El mensaje no trae payload"));
            return;
        }

        // Caso 2: duplicado -> ya se hizo antes; se confirma (ACK) sin repetir el efecto (idempotencia).
        if (processedEvents.isProcessed(envelope.eventId())) {
            log.info("Mensaje duplicado descartado en {} (eventId={}, correlationId={})",
                    queue, envelope.eventId(), envelope.correlationId());
            channel.basicAck(deliveryTag, false);
            return;
        }

        // Espera inicial entre reintentos (1 s por defecto).
        Duration wait = retry.initialInterval();

        // Bucle de intentos: 1, 2, ... hasta maxAttempts. Sale con return en cada caso final.
        for (int attempt = 1; ; attempt++) {
            try {
                // Ejecuta la acción de negocio (EmailService.send, CrewTicketService.assign, ...).
                work.accept(envelope.payload());
            } catch (IllegalArgumentException ex) {
                // Dato inválido (por ejemplo ticket sin id): reintentar no lo arreglaría -> DLQ ya.
                rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "error no recuperable", ex);
                return;
            } catch (RuntimeException ex) {
                // Fallo posiblemente temporal (por ejemplo SMTP caído): se reintenta con espera creciente.
                if (attempt >= retry.maxAttempts()) {
                    // Se acabaron los intentos -> DLQ.
                    rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "reintentos agotados", ex);
                    return;
                }
                log.warn("Fallo procesando mensaje de {} (eventId={}, correlationId={}) (intento {}/{}). Reintento en {} ms",
                        queue, envelope.eventId(), envelope.correlationId(),
                        attempt, retry.maxAttempts(), wait.toMillis());
                // Espera; si el hilo fue interrumpido (apagado de la app) se desvía a la DLQ.
                if (!pause(wait)) {
                    rejectToDlq(channel, deliveryTag, queue, envelope, attempt, "proceso interrumpido", ex);
                    return;
                }
                // Backoff exponencial: 1 s, 2 s, 4 s... (espera * multiplicador).
                wait = Duration.ofMillis(Math.round(wait.toMillis() * retry.multiplier()));
                continue;
            }
            // Éxito: se recuerda el eventId (para descartar futuros duplicados) y se confirma con ACK.
            processedEvents.markProcessed(envelope.eventId());
            channel.basicAck(deliveryTag, false);
            return;
        }
    }

    // NACK sin reencolar: RabbitMQ envía el mensaje a la DLQ; además se deja constancia en log y métrica.
    private void rejectToDlq(Channel channel, long deliveryTag, String queue, EventEnvelope<?> envelope,
                             int attempts, String reason, RuntimeException ex) throws IOException {
        channel.basicNack(deliveryTag, false, false);
        dlqReporter.report(queue, envelope, attempts, reason, ex);
    }

    // Duerme el hilo; devuelve false si fue interrumpido (y restaura la marca de interrupción).
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
