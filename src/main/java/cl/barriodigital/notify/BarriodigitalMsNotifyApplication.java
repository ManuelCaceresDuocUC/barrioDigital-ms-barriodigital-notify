package cl.barriodigital.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * QUÉ ES: punto de entrada del microservicio de notificaciones (ms-notify).
 *
 * QUÉ HACE: es el CONSUMIDOR de RabbitMQ. Escucha tres colas de comandos (correo, ticket de cuadrilla y
 * certificado), ejecuta la acción y confirma cada mensaje con ACK manual; si algo falla sin arreglo, el mensaje
 * se desvía a una cola de errores (DLQ). No tiene base de datos ni endpoints propios (solo health/metrics).
 */
@SpringBootApplication
public class BarriodigitalMsNotifyApplication {

	public static void main(String[] args) {
		// Arranca Spring, declara colas/exchanges en RabbitMQ y activa los listeners.
		SpringApplication.run(BarriodigitalMsNotifyApplication.class, args);
	}

}
