package cl.barriodigital.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import cl.barriodigital.notify.dto.EventEnvelope;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(OutputCaptureExtension.class)
class DlqReporterTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final DlqReporter reporter = new DlqReporter(registry);

    private EventEnvelope<String> envelope() {
        return new EventEnvelope<>("EMAIL_NOTIFICATION", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", "datos");
    }

    @Test
    void registraElLogConTodosLosDatosYEnmascaraLosPersonales(CapturedOutput output) {
        var cause = new IllegalArgumentException("Destinatario ana.perez@municipio.cl con RUT 11.111.111-1 inválido");

        reporter.report("q.cmd.email", envelope(), 1, "error no recuperable", cause);

        assertThat(output.getAll())
                .contains("queue=q.cmd.email")
                .contains("eventId=e-1")
                .contains("correlationId=15")
                .contains("intentos=1")
                .contains("motivo=error no recuperable")
                .contains("a***@municipio.cl")
                .contains("[RUT]")
                .doesNotContain("ana.perez@municipio.cl")
                .doesNotContain("11.111.111-1");
    }

    @Test
    void incrementaElContadorPorColaDeOrigen() {
        reporter.report("q.cmd.email", envelope(), 1, "error no recuperable", new IllegalArgumentException("x"));
        reporter.report("q.cmd.email", envelope(), 3, "reintentos agotados", new IllegalStateException("y"));
        reporter.report("q.cmd.crew", envelope(), 0, "envelope sin payload", new IllegalArgumentException("z"));

        assertThat(registry.get("notify.dlq.total").tag("queue", "q.cmd.email").counter().count()).isEqualTo(2.0);
        assertThat(registry.get("notify.dlq.total").tag("queue", "q.cmd.crew").counter().count()).isEqualTo(1.0);
    }

    @Test
    void soportaUnMensajeSinEnvelope(CapturedOutput output) {
        reporter.report("q.cmd.email", null, 0, "envelope sin payload", new IllegalArgumentException("x"));

        assertThat(output.getAll()).contains("eventId=n/a").contains("correlationId=n/a");
    }
}