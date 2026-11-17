package cl.barriodigital.notify.listener;

import java.util.Arrays;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.AsyncConsumerStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Deja en el log las colas a las que cada consumidor quedó realmente suscrito.
 *
 * QUÉ ES: un oyente de eventos de Spring (no de RabbitMQ): reacciona cuando un consumidor arranca.
 * PARA QUÉ: confirmar en los logs, al iniciar, que los tres listeners quedaron conectados a sus colas.
 */
@Slf4j
@Component
public class ConsumerStartupLogger implements ApplicationListener<AsyncConsumerStartedEvent> {

    @Override
    public void onApplicationEvent(AsyncConsumerStartedEvent event) {
        // El origen del evento es el contenedor del listener, que conoce los nombres de sus colas.
        if (event.getSource() instanceof AbstractMessageListenerContainer container) {
            log.info("Consumidor suscrito a las colas {}", Arrays.toString(container.getQueueNames()));
        }
    }
}
