package cl.barriodigital.notify.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class IdempotencyPropertiesTest {

    @Test
    void aceptaValoresValidos() {
        var properties = new IdempotencyProperties(100, Duration.ofHours(1));

        assertThat(properties.maxEntries()).isEqualTo(100);
        assertThat(properties.ttl()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void rechazaUnTamanoNoPositivo() {
        assertThatThrownBy(() -> new IdempotencyProperties(0, Duration.ofHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-entries");
    }

    @Test
    void rechazaUnTtlNuloONoPositivo() {
        assertThatThrownBy(() -> new IdempotencyProperties(10, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ttl");
        assertThatThrownBy(() -> new IdempotencyProperties(10, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}