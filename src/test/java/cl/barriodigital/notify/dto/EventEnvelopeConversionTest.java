package cl.barriodigital.notify.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.core.ParameterizedTypeReference;

class EventEnvelopeConversionTest {

    @Test
    void elPayloadSeConvierteAlDtoTipado() {
        String json = """
                {"type":"EMAIL_NOTIFICATION","eventId":"e-1","timestamp":"2026-10-20T20:00:00Z",
                 "traceId":"t-1","correlationId":"15",
                 "payload":{"to":"ana@municipio.cl","subject":"Hola","body":"Cuerpo","procedureId":"15"}}
                """;
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setInferredArgumentType(
                new ParameterizedTypeReference<EventEnvelope<EmailNotificationEvent>>() { }.getType());

        Object result = new Jackson2JsonMessageConverter()
                .fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(result).isInstanceOf(EventEnvelope.class);
        EventEnvelope<?> envelope = (EventEnvelope<?>) result;
        assertThat(envelope.type()).isEqualTo("EMAIL_NOTIFICATION");
        assertThat(envelope.correlationId()).isEqualTo("15");
        assertThat(envelope.payload()).isInstanceOf(EmailNotificationEvent.class);
        assertThat(((EmailNotificationEvent) envelope.payload()).to()).isEqualTo("ana@municipio.cl");
    }
}