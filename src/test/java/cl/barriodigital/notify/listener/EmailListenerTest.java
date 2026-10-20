package cl.barriodigital.notify.listener;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.service.EmailService;

class EmailListenerTest {

    private final Channel channel = mock(Channel.class);
    private final EmailService service = mock(EmailService.class);
    private final EmailListener listener = new EmailListener(service,
            new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0)));

    private EventEnvelope<EmailNotificationEvent> envelope(EmailNotificationEvent event) {
        return new EventEnvelope<>("EMAIL_NOTIFICATION", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", event);
    }

    @Test
    void procesaElCorreoYConfirmaElMensaje() throws Exception {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "15");

        listener.onMessage(envelope(event), channel, 7L);

        verify(service).send(event);
        verify(channel).basicAck(7L, false);
    }

    @Test
    void unCorreoInvalidoVaDirectoALaDlq() throws Exception {
        var event = new EmailNotificationEvent("", "Asunto", "Cuerpo", "15");
        doThrow(new IllegalArgumentException("sin destinatario")).when(service).send(event);

        listener.onMessage(envelope(event), channel, 7L);

        verify(channel).basicNack(7L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}