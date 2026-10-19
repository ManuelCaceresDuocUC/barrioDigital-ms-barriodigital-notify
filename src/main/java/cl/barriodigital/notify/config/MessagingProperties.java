package cl.barriodigital.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/** Nombres de exchanges, colas y routing keys, centralizados en application.yaml. */
@ConfigurationProperties(prefix = "barriodigital.messaging")
public record MessagingProperties(Exchanges exchanges, Domain email, Domain crew, Domain certificate) {

    public MessagingProperties {
        Assert.notNull(exchanges, "barriodigital.messaging.exchanges es obligatorio");
        Assert.notNull(email, "barriodigital.messaging.email es obligatorio");
        Assert.notNull(crew, "barriodigital.messaging.crew es obligatorio");
        Assert.notNull(certificate, "barriodigital.messaging.certificate es obligatorio");
    }

    public record Exchanges(String direct, String topic, String deadLetter) {
        public Exchanges {
            Assert.hasText(direct, "exchanges.direct es obligatorio");
            Assert.hasText(topic, "exchanges.topic es obligatorio");
            Assert.hasText(deadLetter, "exchanges.dead-letter es obligatorio");
        }
    }

    /** Nombres asociados a un dominio funcional (email, crew, certificate). */
    public record Domain(String queue, String deadLetterQueue, String directKey,
                         String topicKey, String deadLetterKey) {
        public Domain {
            Assert.hasText(queue, "queue es obligatorio");
            Assert.hasText(deadLetterQueue, "dead-letter-queue es obligatorio");
            Assert.hasText(directKey, "direct-key es obligatorio");
            Assert.hasText(topicKey, "topic-key es obligatorio");
            Assert.hasText(deadLetterKey, "dead-letter-key es obligatorio");
        }
    }
}