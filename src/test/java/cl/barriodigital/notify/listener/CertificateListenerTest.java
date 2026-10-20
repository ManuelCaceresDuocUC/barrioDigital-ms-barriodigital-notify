package cl.barriodigital.notify.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.Channel;

import cl.barriodigital.notify.config.IdempotencyProperties;
import cl.barriodigital.notify.config.RetryProperties;
import cl.barriodigital.notify.dto.CertificateEvent;
import cl.barriodigital.notify.dto.EventEnvelope;
import cl.barriodigital.notify.service.CertificateService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class CertificateListenerTest {

    private static final String QUEUE = "q.cmd.certificate";

    private final Channel channel = mock(Channel.class);
    private final CertificateService service = mock(CertificateService.class);
    private final CertificateListener listener = new CertificateListener(service,
            new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0),
        new DlqReporter(new SimpleMeterRegistry()),
        new ProcessedEventStore(new IdempotencyProperties(100, Duration.ofHours(1)))));

    @Test
    void procesaElCertificadoYConfirmaElMensaje() throws Exception {
        var event = new CertificateEvent("11111111-1", "RESIDENCIA", null);
        var envelope = new EventEnvelope<>("CERTIFICATE_REQUEST", "e-1", "2026-10-20T20:00:00Z", "t-1", "15", event);

        listener.onMessage(envelope, channel, 7L, QUEUE);

        verify(service).dispatch(event);
        verify(channel).basicAck(7L, false);
    }
}