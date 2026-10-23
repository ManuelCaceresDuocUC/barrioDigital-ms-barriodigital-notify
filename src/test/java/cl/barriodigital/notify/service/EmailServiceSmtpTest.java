package cl.barriodigital.notify.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import cl.barriodigital.notify.dto.EmailNotificationEvent;

class EmailServiceSmtpTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    private static EmailNotificationEvent event(String to, String subject, String body) {
        return new EmailNotificationEvent(to, subject, body, "87");
    }

    @Test
    void conEnvioDesactivadoSoloSimulaYNoUsaElServidor() {
        EmailService service = new EmailService(mailSender, false, "");

        service.send(event("vecino@correo.cl", "Tu trámite fue ingresado", "Hola"));

        verifyNoInteractions(mailSender);
    }

    @Test
    void conEnvioActivadoEnviaElCorreoConLosDatosDelEvento() {
        EmailService service = new EmailService(mailSender, true, "app@gmail.com");

        service.send(event("  vecino@correo.cl ", "Tu trámite fue ingresado", "Recibimos tu solicitud"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getFrom()).isEqualTo("app@gmail.com");
        assertThat(sent.getTo()).containsExactly("vecino@correo.cl");
        assertThat(sent.getSubject()).isEqualTo("Tu trámite fue ingresado");
        assertThat(sent.getText()).isEqualTo("Recibimos tu solicitud");
    }

    @Test
    void sinCuerpoUsaElAsuntoComoTexto() {
        EmailService service = new EmailService(mailSender, true, "app@gmail.com");

        service.send(event("vecino@correo.cl", "Tu trámite fue admitido", null));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).isEqualTo("Tu trámite fue admitido");
    }

    @Test
    void sinAsuntoUsaUnoPorDefecto() {
        EmailService service = new EmailService(mailSender, true, "app@gmail.com");

        service.send(event("vecino@correo.cl", " ", "Hola"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Notificación BarrioDigital");
    }

    @Test
    void sinDestinatarioSeRechazaYNoSeIntentaEnviar() {
        EmailService service = new EmailService(mailSender, true, "app@gmail.com");

        assertThatThrownBy(() -> service.send(event(null, "Asunto", "Cuerpo")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.send(event("  ", "Asunto", "Cuerpo")))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(mailSender);
    }

    @Test
    void unFalloDelServidorSePropagaParaQueElConsumidorReintenteOEnvieALaDlq() {
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));
        EmailService service = new EmailService(mailSender, true, "app@gmail.com");

        assertThatThrownBy(() -> service.send(event("vecino@correo.cl", "Asunto", "Cuerpo")))
                .isInstanceOf(MailSendException.class);
    }

    @Test
    void activarElEnvioSinRemitenteFallaAlArrancar() {
        assertThatThrownBy(() -> new EmailService(mailSender, true, " "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MAIL_USERNAME");
    }

    @Test
    void enmascaraElCorreoParaLosLogs() {
        assertThat(EmailService.maskEmail("vecino@correo.cl")).isEqualTo("v***@correo.cl");
        assertThat(EmailService.maskEmail("sin-arroba")).isEqualTo("***");
        assertThat(EmailService.maskEmail(null)).isEqualTo("(sin correo)");
    }
}
