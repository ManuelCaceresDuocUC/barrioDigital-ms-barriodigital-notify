package cl.barriodigital.notify.listener;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.IdempotencyProperties;
import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.service.EmailService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class EmailListenerTest {

    private static final String QUEUE = "q.cmd.email";

    private final Channel channel = mock(Channel.class);
    private final EmailService service = mock(EmailService.class);
    private final EmailListener listener = new EmailListener(service,
            new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0),
        new DlqReporter(new SimpleMeterRegistry()),
        new ProcessedEventStore(new IdempotencyProperties(100, Duration.ofHours(1)))));

    private EventEnvelope<EmailNotificationEvent> envelope(EmailNotificationEvent event) {
        return new EventEnvelope<>("EMAIL_NOTIFICATION", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", event);
    }

    @Test
    void procesaElCorreoYConfirmaElMensaje() throws Exception {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "15");

        listener.onMessage(envelope(event), channel, 7L, QUEUE);

        verify(service).send(event);
        verify(channel).basicAck(7L, false);
    }

    @Test
    void unCorreoInvalidoVaDirectoALaDlq() throws Exception {
        var event = new EmailNotificationEvent("", "Asunto", "Cuerpo", "15");
        doThrow(new IllegalArgumentException("sin destinatario")).when(service).send(event);

        listener.onMessage(envelope(event), channel, 7L, QUEUE);

        verify(channel).basicNack(7L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
    @Test
    void unMensajeDuplicadoNoEnviaElCorreoDosVeces() throws Exception {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "15");

        listener.onMessage(envelope(event), channel, 7L, QUEUE);
        listener.onMessage(envelope(event), channel, 8L, QUEUE);

        verify(service, times(1)).send(event);
        verify(channel).basicAck(7L, false);
        verify(channel).basicAck(8L, false);
    }
}