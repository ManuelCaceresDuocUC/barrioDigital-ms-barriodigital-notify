package cl.barriodigital.notify.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.service.CertificateService;

class CertificateListenerTest {

    private final CertificateService service = mock(CertificateService.class);
    private final CertificateListener listener = new CertificateListener(service);

    @Test
    void delegaElCertificadoAlServicio() {
        var event = new CertificateEvent("11111111-1", "RESIDENCIA", null);
        listener.onMessage(event);
        verify(service).dispatch(event);
    }
}