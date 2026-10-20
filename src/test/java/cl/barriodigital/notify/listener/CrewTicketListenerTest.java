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
import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.service.CrewTicketService;

class CrewTicketListenerTest {

    // CrewTicketListenerTest
    private final Channel channel = mock(Channel.class);
    private final CrewTicketService service = mock(CrewTicketService.class);
    private final CrewTicketListener listener = new CrewTicketListener(service,
            new AckHandler(new RetryProperties(3, Duration.ofMillis(1), 2.0)));

    @Test
    void procesaElTicketYConfirmaElMensaje() throws Exception {
        var event = new CrewTicketEvent("15", "ALUMBRADO", "Plaza Central", "NORMAL");
        listener.onMessage(event, channel, 7L);
        verify(service).assign(event);
        verify(channel).basicAck(7L, false);
    }
}