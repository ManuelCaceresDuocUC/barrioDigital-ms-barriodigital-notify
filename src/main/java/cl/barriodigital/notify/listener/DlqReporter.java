package cl.barriodigital.notify.listener;

import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.logging.SensitiveDataMasker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Deja constancia (log + métrica) de cada mensaje que se envía a una DLQ.
 *
 * QUÉ ES: utilitario de observabilidad. Cada vez que AckHandler rechaza un mensaje, esta clase registra
 * un error en el log y suma 1 a la métrica notify.dlq.total (visible en /actuator/metrics).
 */
@Slf4j
@Component
public class DlqReporter {

    // Nombre de la métrica en Micrometer (la librería de métricas de Spring).
    static final String METRIC = "notify.dlq.total";

    private final MeterRegistry registry;

    public DlqReporter(MeterRegistry registry) {
        this.registry = registry;
    }

    public void report(String queue, EventEnvelope<?> envelope, int attempts, String reason, Throwable cause) {
        // Contador etiquetado por cola de origen: permite ver cuántos fallos tuvo cada cola.
        Counter.builder(METRIC)
                .description("Mensajes enviados a la DLQ, por cola de origen")
                .tag("queue", queue)
                .register(registry)
                .increment();

        // Log de error con los identificadores para rastrear el mensaje. El detalle de la excepción pasa
        // por SensitiveDataMasker para no escribir correos ni RUT completos. "n/a" si no hay envelope.
        log.error("Mensaje enviado a la DLQ: queue={}, eventId={}, type={}, correlationId={}, intentos={}, motivo={}, detalle={}",
                queue,
                envelope == null ? "n/a" : envelope.eventId(),
                envelope == null ? "n/a" : envelope.type(),
                envelope == null ? "n/a" : envelope.correlationId(),
                attempts,
                reason,
                SensitiveDataMasker.mask(String.valueOf(cause)));
    }
}
