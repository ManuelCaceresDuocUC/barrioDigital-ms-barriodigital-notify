package cl.barriodigital.notify.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.CrewTicketEvent;
import cl.barriodigital.notify.service.CrewTicketService;

class CrewTicketListenerTest {

    private final CrewTicketService service = mock(CrewTicketService.class);
    private final CrewTicketListener listener = new CrewTicketListener(service);

    @Test
    void delegaElTicketAlServicio() {
        var event = new CrewTicketEvent("15", "ALUMBRADO", "Plaza Central", "NORMAL");
        listener.onMessage(event);
        verify(service).assign(event);
    }
}