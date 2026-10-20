package cl.barriodigital.notify.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.dto.EmailNotificationEvent;

class NotificationServicesTest {

    private final EmailService emailService = new EmailService();
    private final CrewTicketService crewTicketService = new CrewTicketService();
    private final CertificateService certificateService = new CertificateService();

    @Test
    void correoValidoSeProcesaSinError() {
        var event = new EmailNotificationEvent("a@b.cl", "Asunto", "Cuerpo", "P-1");
        assertThatCode(() -> emailService.send(event)).doesNotThrowAnyException();
    }

    @Test
    void correoSinDestinatarioLanzaExcepcion() {
        var event = new EmailNotificationEvent(" ", "Asunto", "Cuerpo", "P-1");
        assertThatThrownBy(() -> emailService.send(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ticketSinIdentificadorLanzaExcepcion() {
        var event = new CrewTicketEvent(null, "ALUMBRADO", "Plaza", "ALTA");
        assertThatThrownBy(() -> crewTicketService.assign(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void certificadoSinRutLanzaExcepcion() {
        var event = new CertificateEvent("", "RESIDENCIA", "http://docs/cert.pdf");
        assertThatThrownBy(() -> certificateService.dispatch(event)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ticketYCertificadoValidosSeProcesanSinError() {
        assertThatCode(() -> crewTicketService.assign(new CrewTicketEvent("T-1", "ALUMBRADO", "Plaza", "ALTA")))
                .doesNotThrowAnyException();
        assertThatCode(() -> certificateService.dispatch(new CertificateEvent("11111111-1", "RESIDENCIA", "u")))
                .doesNotThrowAnyException();
    }
}