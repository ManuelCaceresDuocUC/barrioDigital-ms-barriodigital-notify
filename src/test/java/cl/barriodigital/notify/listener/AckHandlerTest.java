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

class AckHandlerTest {

    private final Channel channel = mock(Channel.class);
    private final AckHandler handler = new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0));

    @Test
    void procesoExitosoConfirmaConAck() throws Exception {
        handler.handle("evento", channel, 10L, "email", event -> { });

        verify(channel).basicAck(10L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void errorNoRecuperableVaDirectoALaDlqSinReintentar() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        handler.handle("evento", channel, 10L, "email", event -> {
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

        handler.handle("evento", channel, 10L, "email", event -> {
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

        handler.handle("evento", channel, 10L, "email", event -> {
            calls.incrementAndGet();
            throw new IllegalStateException("servicio externo caído");
        });

        assertThat(calls.get()).isEqualTo(3);
        verify(channel).basicNack(10L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}