package cl.barriodigital.notify.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

class RabbitMQConfigTest {

    private final MessagingProperties props = new MessagingProperties(
            new MessagingProperties.Exchanges("cmd.direct", "cmd.topic", "cmd.dead.dlx"),
            new MessagingProperties.Domain("q.cmd.email", "q.cmd.email.dlq", "email.send", "email.*", "email.dlq"),
            new MessagingProperties.Domain("q.cmd.crew", "q.cmd.crew.dlq", "crew.ticket", "crew.#", "crew.dlq"),
            new MessagingProperties.Domain("q.cmd.certificate", "q.cmd.certificate.dlq",
                    "certificate.gen", "certificate.*", "certificate.dlq"));

    private final RabbitMQConfig config = new RabbitMQConfig(props);

    @Test
    void declaraLosTresExchanges() {
        assertThat(config.directExchange().getName()).isEqualTo("cmd.direct");
        assertThat(config.topicExchange().getName()).isEqualTo("cmd.topic");
        assertThat(config.deadLetterExchange().getName()).isEqualTo("cmd.dead.dlx");
    }

    @Test
    void colaPrincipalEsDuraderaYApuntaASuDlq() {
        Queue queue = config.crewQueue();
        assertThat(queue.getName()).isEqualTo("q.cmd.crew");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", "cmd.dead.dlx")
                .containsEntry("x-dead-letter-routing-key", "crew.dlq");
    }

    @Test
    void emailTieneBindingDirectYTopic() {
        assertBinding(config.emailDirectBinding(), "cmd.direct", "q.cmd.email", "email.send");
        assertBinding(config.emailTopicBinding(), "cmd.topic", "q.cmd.email", "email.*");
    }

    @Test
    void crewYCertificateTienenBindingDirectYTopic() {
        assertBinding(config.crewDirectBinding(), "cmd.direct", "q.cmd.crew", "crew.ticket");
        assertBinding(config.crewTopicBinding(), "cmd.topic", "q.cmd.crew", "crew.#");
        assertBinding(config.certificateDirectBinding(), "cmd.direct", "q.cmd.certificate", "certificate.gen");
        assertBinding(config.certificateTopicBinding(), "cmd.topic", "q.cmd.certificate", "certificate.*");
    }

    @Test
    void cadaDlqEstaEnlazadaAlDeadLetterExchange() {
        assertBinding(config.emailDlqBinding(), "cmd.dead.dlx", "q.cmd.email.dlq", "email.dlq");
        assertBinding(config.crewDlqBinding(), "cmd.dead.dlx", "q.cmd.crew.dlq", "crew.dlq");
        assertBinding(config.certificateDlqBinding(), "cmd.dead.dlx", "q.cmd.certificate.dlq", "certificate.dlq");
    }

    private void assertBinding(Binding binding, String exchange, String destination, String routingKey) {
        assertThat(binding.getExchange()).isEqualTo(exchange);
        assertThat(binding.getDestination()).isEqualTo(destination);
        assertThat(binding.getRoutingKey()).isEqualTo(routingKey);
    }
}