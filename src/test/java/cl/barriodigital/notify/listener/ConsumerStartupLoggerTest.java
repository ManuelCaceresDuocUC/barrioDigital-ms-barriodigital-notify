package cl.barriodigital.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.listener.AsyncConsumerStartedEvent;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class ConsumerStartupLoggerTest {

    @Test
    void registraLasColasDelConsumidor(CapturedOutput output) {
        var container = mock(SimpleMessageListenerContainer.class);
        when(container.getQueueNames()).thenReturn(new String[] { "q.cmd.email" });

        new ConsumerStartupLogger().onApplicationEvent(new AsyncConsumerStartedEvent(container, new Object()));

        assertThat(output.getAll()).contains("Consumidor suscrito a las colas [q.cmd.email]");
    }
}