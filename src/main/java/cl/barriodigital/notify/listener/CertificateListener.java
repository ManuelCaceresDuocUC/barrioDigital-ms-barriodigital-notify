package cl.barriodigital.notify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.service.CertificateService;

/** Consumidor del dominio de certificados (q.cmd.certificate). */
@Component
public class CertificateListener {

    private final CertificateService certificateService;

    public CertificateListener(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @RabbitListener(queues = "${barriodigital.messaging.certificate.queue}")
    public void onMessage(CertificateEvent event) {
        certificateService.dispatch(event);
    }
}