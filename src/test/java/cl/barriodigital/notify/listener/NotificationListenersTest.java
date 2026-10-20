package cl.barriodigital.notify.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.dto.EmailNotificationEvent;
import cl.barriodigital.notify.service.CertificateService;
import cl.barriodigital.notify.service.CrewTicketService;
import cl.barriodigital.notify.service.EmailService;

class NotificationListenersTest {

    private final EmailService emailService = mock(EmailService.class);
    private final CrewTicketService crewTicketService = mock(CrewTicketService.class);
    private final CertificateService certificateService = mock(CertificateService.class);
    private final NotificationListeners listeners =
            new NotificationListeners(emailService, crewTicketService, certificateService);

    @Test
    void delegaElCorreoAlServicio() {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "P-1");
        listeners.handleEmailNotification(event);
        verify(emailService).send(event);
    }

    @Test
    void delegaElTicketAlServicio() {
        var event = new CrewTicketEvent("T-1", "ALUMBRADO", "Plaza Central", "ALTA");
        listeners.handleCrewTicket(event);
        verify(crewTicketService).assign(event);
    }

    @Test
    void delegaElCertificadoAlServicio() {
        var event = new CertificateEvent("11111111-1", "RESIDENCIA", "http://docs/cert.pdf");
        listeners.handleCertificateRequest(event);
        verify(certificateService).dispatch(event);
    }
}