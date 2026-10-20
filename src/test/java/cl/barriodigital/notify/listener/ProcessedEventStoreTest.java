package cl.barriodigital.notify.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.config.IdempotencyProperties;

class ProcessedEventStoreTest {

    private final MutableClock clock = new MutableClock();

    private ProcessedEventStore store(int max, Duration ttl) {
        return new ProcessedEventStore(new IdempotencyProperties(max, ttl), clock);
    }

    @Test
    void unEventoNuevoNoEstaProcesado() {
        assertThat(store(10, Duration.ofHours(1)).isProcessed("e-1")).isFalse();
    }

    @Test
    void unEventoMarcadoSeReconoceComoProcesado() {
        var store = store(10, Duration.ofHours(1));

        store.markProcessed("e-1");

        assertThat(store.isProcessed("e-1")).isTrue();
        assertThat(store.isProcessed("e-2")).isFalse();
    }

    @Test
    void elEventoSeOlvidaTrasElTtl() {
        var store = store(10, Duration.ofMinutes(10));
        store.markProcessed("e-1");

        clock.advance(Duration.ofMinutes(11));

        assertThat(store.isProcessed("e-1")).isFalse();
    }

    @Test
    void descartaLosMasAntiguosAlSuperarElMaximo() {
        var store = store(2, Duration.ofHours(1));

        store.markProcessed("e-1");
        store.markProcessed("e-2");
        store.markProcessed("e-3");

        assertThat(store.isProcessed("e-1")).isFalse();
        assertThat(store.isProcessed("e-2")).isTrue();
        assertThat(store.isProcessed("e-3")).isTrue();
    }

    @Test
    void ignoraEventIdsNulosOVacios() {
        var store = store(10, Duration.ofHours(1));

        store.markProcessed(null);
        store.markProcessed(" ");

        assertThat(store.isProcessed(null)).isFalse();
        assertThat(store.isProcessed(" ")).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-20T20:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}