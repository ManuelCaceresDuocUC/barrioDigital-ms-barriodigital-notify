package cl.barriodigital.notify.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = RetryPropertiesTest.TestConfig.class)
class RetryPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(RetryProperties.class)
    static class TestConfig { }

    @Autowired
    private RetryProperties retry;

    @Test
    void usaLaPoliticaDeReintentosDelYaml() {
        assertThat(retry.maxAttempts()).isEqualTo(3);
        assertThat(retry.initialInterval()).isEqualTo(Duration.ofSeconds(1));
        assertThat(retry.multiplier()).isEqualTo(2.0);
    }
}