package cl.barriodigital.notify.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/** Política de reintentos del consumidor, centralizada en application.yaml. */
@ConfigurationProperties(prefix = "barriodigital.messaging.retry")
public record RetryProperties(int maxAttempts, Duration initialInterval, double multiplier) {

    public RetryProperties {
        Assert.isTrue(maxAttempts >= 1, "retry.max-attempts debe ser al menos 1");
        Assert.notNull(initialInterval, "retry.initial-interval es obligatorio");
        Assert.isTrue(!initialInterval.isZero() && !initialInterval.isNegative(),
                "retry.initial-interval debe ser positivo");
        Assert.isTrue(multiplier >= 1.0, "retry.multiplier debe ser al menos 1.0");
    }
}