package cl.barriodigital.notify.listener;

import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.logging.SensitiveDataMasker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/** Deja constancia (log + métrica) de cada mensaje que se envía a una DLQ. */
@Slf4j
@Component
public class DlqReporter {

    static final String METRIC = "notify.dlq.total";

    private final MeterRegistry registry;

    public DlqReporter(MeterRegistry registry) {
        this.registry = registry;
    }

    public void report(String queue, EventEnvelope<?> envelope, int attempts, String reason, Throwable cause) {
        Counter.builder(METRIC)
                .description("Mensajes enviados a la DLQ, por cola de origen")
                .tag("queue", queue)
                .register(registry)
                .increment();

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