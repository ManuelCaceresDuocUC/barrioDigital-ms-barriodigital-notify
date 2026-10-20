package cl.barriodigital.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.IdempotencyProperties;
import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.EventEnvelope;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class AckHandlerTest {

    private static final String QUEUE = "q.cmd.email";

    private final Channel channel = mock(Channel.class);
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final AckHandler handler = new AckHandler(
            new RetryProperties(3, Duration.ofMillis(1), 2.0),
            new DlqReporter(registry),
            new ProcessedEventStore(new IdempotencyProperties(100, Duration.ofHours(1))));

    private EventEnvelope<String> envelope() {
        return envelope("e-1");
    }

    private EventEnvelope<String> envelope(String eventId) {
        return new EventEnvelope<>("TEST", eventId, "2026-10-20T20:00:00Z", "t-1", "15", "datos");
    }

    private double dlqCount() {
        var counter = registry.find("notify.dlq.total").tag("queue", QUEUE).counter();
        return counter == null ? 0.0 : counter.count();
    }

    @Test
    void procesoExitosoConfirmaConAckYNoCuentaDlq() throws Exception {
        handler.handle(envelope(), channel, 10L, QUEUE, payload -> { });

        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
        assertThat(dlqCount()).isZero();
    }

    @Test
    void errorNoRecuperableVaDirectoALaDlqSinReintentar() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, QUEUE, payload -> {
            calls.incrementAndGet();
            throw new IllegalArgumentException("sin destinatario");
        });

        assertThat(calls.get()).isEqualTo(1);
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        assertThat(dlqCount()).isEqualTo(1.0);
    }

    @Test
    void falloTransitorioSeReintentaYTerminaConAck() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, QUEUE, payload -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("servicio externo caído");
            }
        });

        assertThat(calls.get()).isEqualTo(3);
        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
        assertThat(dlqCount()).isZero();
    }

    @Test
    void sinExitoTrasLosIntentosMaximosVaALaDlq() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, QUEUE, payload -> {
            calls.incrementAndGet();
            throw new IllegalStateException("servicio externo caído");
        });

        assertThat(calls.get()).isEqualTo(3);
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        assertThat(dlqCount()).isEqualTo(1.0);
    }

    @Test
    void envelopeSinPayloadVaALaDlqSinProcesar() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        var sinPayload = new EventEnvelope<String>("TEST", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", null);

        handler.handle(sinPayload, channel, 10L, QUEUE, payload -> calls.incrementAndGet());

        assertThat(calls.get()).isZero();
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        assertThat(dlqCount()).isEqualTo(1.0);
    }

    @Test
    void unEventoDuplicadoSeConfirmaSinProcesarseDeNuevo() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope("e-1"), channel, 10L, QUEUE, payload -> calls.incrementAndGet());
        handler.handle(envelope("e-1"), channel, 11L, QUEUE, payload -> calls.incrementAndGet());

        assertThat(calls.get()).isEqualTo(1);
        verify(channel).basicAck(10L, false);
        verify(channel).basicAck(11L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
        assertThat(dlqCount()).isZero();
    }

    @Test
    void eventosConDistintoIdSeProcesanAmbos() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope("e-1"), channel, 10L, QUEUE, payload -> calls.incrementAndGet());
        handler.handle(envelope("e-2"), channel, 11L, QUEUE, payload -> calls.incrementAndGet());

        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void unEventoQueFueALaDlqNoSeMarcaComoProcesado() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope("e-1"), channel, 10L, QUEUE, payload -> {
            throw new IllegalArgumentException("sin destinatario");
        });
        handler.handle(envelope("e-1"), channel, 11L, QUEUE, payload -> calls.incrementAndGet());

        assertThat(calls.get()).isEqualTo(1);
        verify(channel).basicNack(10L, false, false);
        verify(channel).basicAck(11L, false);
        verify(channel, times(1)).basicAck(anyLong(), anyBoolean());
    }
}