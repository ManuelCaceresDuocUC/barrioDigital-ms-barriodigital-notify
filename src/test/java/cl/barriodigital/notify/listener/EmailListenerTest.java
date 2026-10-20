package cl.barriodigital.notify.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.service.EmailService;

class EmailListenerTest {

    private final EmailService service = mock(EmailService.class);
    private final EmailListener listener = new EmailListener(service);

    @Test
    void delegaElCorreoAlServicio() {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "15");
        listener.onMessage(event);
        verify(service).send(event);
    }
}