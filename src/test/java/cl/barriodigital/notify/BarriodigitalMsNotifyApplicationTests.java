package cl.barriodigital.notify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Los listeners no arrancan: el test valida el contexto sin depender de un broker RabbitMQ.
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class BarriodigitalMsNotifyApplicationTests {

    @Test
    void contextLoads() {
    }
}