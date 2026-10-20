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

import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.service.CertificateService;

class CertificateListenerTest {

    // CertificateListenerTest
    private final Channel channel = mock(Channel.class);
    private final CertificateService service = mock(CertificateService.class);
    private final CertificateListener listener = new CertificateListener(service,
            new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0)));

    @Test
    void procesaElCertificadoYConfirmaElMensaje() throws Exception {
        var event = new CertificateEvent("11111111-1", "RESIDENCIA", null);
        listener.onMessage(event, channel, 7L);
        verify(service).dispatch(event);
        verify(channel).basicAck(7L, false);
    }
}