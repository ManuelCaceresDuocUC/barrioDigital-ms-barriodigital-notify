package cl.barriodigital.notify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Nombres de exchanges, colas y routing keys, centralizados en application.yaml.
 *
 * QUÉ ES: record que enlaza el bloque barriodigital.messaging del yaml. Evita escribir nombres de colas
 * "a mano" en el código: se cambian en un solo lugar.
 * VOCABULARIO RabbitMQ: exchange = buzón de entrada que reparte; queue = cola donde esperan los mensajes;
 * routing key = etiqueta que decide a qué cola va cada mensaje; DLQ = cola de mensajes fallidos.
 */
@ConfigurationProperties(prefix = "barriodigital.messaging")
public record MessagingProperties(Exchanges exchanges, Domain email, Domain crew, Domain certificate) {

    // Valida que los cuatro bloques existan en el yaml.
    public MessagingProperties {
        Assert.notNull(exchanges, "barriodigital.messaging.exchanges es obligatorio");
        Assert.notNull(email, "barriodigital.messaging.email es obligatorio");
        Assert.notNull(crew, "barriodigital.messaging.crew es obligatorio");
        Assert.notNull(certificate, "barriodigital.messaging.certificate es obligatorio");
    }

    // Los tres exchanges: direct (clave exacta), topic (clave con comodines) y dead-letter (errores).
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
