package cl.barriodigital.notify.listener;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.barriodigital.notify.config.IdempotencyProperties;

/**
 * Memoria acotada de eventId ya procesados, para descartar duplicados (reenvíos o redeliveries).
 * Vive en memoria: se pierde al reiniciar y no se comparte entre instancias.
 */
@Component
public class ProcessedEventStore {

    private final Duration ttl;
    private final Clock clock;
    private final Map<String, Instant> entries;

    @Autowired
    public ProcessedEventStore(IdempotencyProperties properties) {
        this(properties, Clock.systemUTC());
    }

    ProcessedEventStore(IdempotencyProperties properties, Clock clock) {
        this.ttl = properties.ttl();
        this.clock = clock;
        int max = properties.maxEntries();
        this.entries = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                return size() > max;
            }
        };
    }

    public synchronized boolean isProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        Instant processedAt = entries.get(eventId);
        if (processedAt == null) {
            return false;
        }
        if (processedAt.plus(ttl).isBefore(clock.instant())) {
            entries.remove(eventId);
            return false;
        }
        return true;
    }

    public synchronized void markProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        entries.put(eventId, clock.instant());
    }
}