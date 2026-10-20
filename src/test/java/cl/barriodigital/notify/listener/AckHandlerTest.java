package cl.barriodigital.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.EventEnvelope;

class AckHandlerTest {

    private final Channel channel = mock(Channel.class);
    private final AckHandler handler = new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0));

    private EventEnvelope<String> envelope() {
        return new EventEnvelope<>("TEST", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", "datos");
    }

    @Test
    void procesoExitosoConfirmaConAck() throws Exception {
        handler.handle(envelope(), channel, 10L, "email", payload -> { });

        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void errorNoRecuperableVaDirectoALaDlqSinReintentar() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, "email", payload -> {
            calls.incrementAndGet();
            throw new IllegalArgumentException("sin destinatario");
        });

        assertThat(calls.get()).isEqualTo(1);
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void falloTransitorioSeReintentaYTerminaConAck() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, "email", payload -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("servicio externo caído");
            }
        });

        assertThat(calls.get()).isEqualTo(3);
        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void sinExitoTrasLosIntentosMaximosVaALaDlq() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle(envelope(), channel, 10L, "email", payload -> {
            calls.incrementAndGet();
            throw new IllegalStateException("servicio externo caído");
        });

        assertThat(calls.get()).isEqualTo(3);
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void envelopeSinPayloadVaALaDlqSinProcesar() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        var sinPayload = new EventEnvelope<String>("TEST", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", null);

        handler.handle(sinPayload, channel, 10L, "email", payload -> calls.incrementAndGet());

        assertThat(calls.get()).isZero();
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}