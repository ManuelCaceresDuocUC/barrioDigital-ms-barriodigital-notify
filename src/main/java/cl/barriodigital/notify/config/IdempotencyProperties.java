package cl.barriodigital.notify.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/** Parámetros de la memoria de eventos ya procesados (descarte de duplicados). */
@ConfigurationProperties(prefix = "barriodigital.messaging.idempotency")
public record IdempotencyProperties(int maxEntries, Duration ttl) {

    public IdempotencyProperties {
        Assert.isTrue(maxEntries > 0, "barriodigital.messaging.idempotency.max-entries debe ser mayor que 0");
        Assert.isTrue(ttl != null && !ttl.isZero() && !ttl.isNegative(),
                "barriodigital.messaging.idempotency.ttl debe ser una duración positiva");
    }
}