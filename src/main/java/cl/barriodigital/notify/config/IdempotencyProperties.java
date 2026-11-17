package cl.barriodigital.notify.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Parámetros de la memoria de eventos ya procesados (descarte de duplicados).
 *
 * QUÉ ES: un record que enlaza el bloque barriodigital.messaging.idempotency de application.yaml
 * (tamaño máximo de la memoria y cuánto tiempo se recuerda un eventId).
 */
@ConfigurationProperties(prefix = "barriodigital.messaging.idempotency")
public record IdempotencyProperties(int maxEntries, Duration ttl) {

    // Constructor "compacto" de un record: valida los datos al arrancar. Si el yaml está mal, la app
    // falla de inmediato (fail-fast) con un mensaje claro en vez de comportarse raro más tarde.
    public IdempotencyProperties {
        Assert.isTrue(maxEntries > 0, "barriodigital.messaging.idempotency.max-entries debe ser mayor que 0");
        Assert.isTrue(ttl != null && !ttl.isZero() && !ttl.isNegative(),
                "barriodigital.messaging.idempotency.ttl debe ser una duración positiva");
    }
}
