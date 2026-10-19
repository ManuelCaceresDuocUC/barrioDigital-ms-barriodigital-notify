package cl.barriodigital.notify.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = MessagingPropertiesTest.TestConfig.class)
class MessagingPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(MessagingProperties.class)
    static class TestConfig { }

    @Autowired
    private MessagingProperties messaging;

    @Test
    void exchangesCoincidenConElCaso() {
        assertThat(messaging.exchanges().direct()).isEqualTo("cmd.direct");
        assertThat(messaging.exchanges().topic()).isEqualTo("cmd.topic");
        assertThat(messaging.exchanges().deadLetter()).isEqualTo("cmd.dead.dlx");
    }

    @Test
    void dominioEmailCoincideConElCaso() {
        var email = messaging.email();
        assertThat(email.queue()).isEqualTo("q.cmd.email");
        assertThat(email.deadLetterQueue()).isEqualTo("q.cmd.email.dlq");
        assertThat(email.directKey()).isEqualTo("email.send");
        assertThat(email.topicKey()).isEqualTo("email.*");
        assertThat(email.deadLetterKey()).isEqualTo("email.dlq");
    }

    @Test
    void dominiosCrewYCertificateCoincidenConElCaso() {
        assertThat(messaging.crew().queue()).isEqualTo("q.cmd.crew");
        assertThat(messaging.crew().topicKey()).isEqualTo("crew.#");
        assertThat(messaging.certificate().queue()).isEqualTo("q.cmd.certificate");
        assertThat(messaging.certificate().directKey()).isEqualTo("certificate.gen");
    }
}