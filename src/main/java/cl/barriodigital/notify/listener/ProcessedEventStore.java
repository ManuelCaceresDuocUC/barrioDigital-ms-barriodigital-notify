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
 *
 * QUÉ ES: un "caché" simple con dos límites: tamaño máximo (se borra el más antiguo) y tiempo de vida (ttl).
 */
@Component
public class ProcessedEventStore {

    private final Duration ttl;
    // Reloj inyectable: en los tests se reemplaza por uno falso para simular el paso del tiempo.
    private final Clock clock;
    // eventId -> momento en que se procesó. LinkedHashMap conserva el orden de inserción.
    private final Map<String, Instant> entries;

    // Constructor que usa Spring (@Autowired) con el reloj real en UTC.
    @Autowired
    public ProcessedEventStore(IdempotencyProperties properties) {
        this(properties, Clock.systemUTC());
    }

    // Constructor visible solo dentro del paquete: lo usan los tests para entregar su propio reloj.
    ProcessedEventStore(IdempotencyProperties properties, Clock clock) {
        this.ttl = properties.ttl();
        this.clock = clock;
        int max = properties.maxEntries();
        // Mapa que elimina automáticamente su entrada más antigua al superar el máximo (política FIFO).
        this.entries = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                return size() > max;
            }
        };
    }

    // synchronized: varios hilos consumidores pueden consultar a la vez; el mapa no es seguro por sí solo.
    public synchronized boolean isProcessed(String eventId) {
        // Sin eventId no se puede deduplicar: se trata como no procesado.
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        Instant processedAt = entries.get(eventId);
        if (processedAt == null) {
            return false;
        }
        // Si el registro es más viejo que el ttl, se olvida y se trata como nuevo.
        if (processedAt.plus(ttl).isBefore(clock.instant())) {
            entries.remove(eventId);
            return false;
        }
        return true;
    }

    // Se llama solo después de procesar con éxito, justo antes del ACK.
    public synchronized void markProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        entries.put(eventId, clock.instant());
    }
}
