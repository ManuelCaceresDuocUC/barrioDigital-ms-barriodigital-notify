package cl.barriodigital.notify.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.barriodigital.notify.dto.EmailNotificationEvent;

class NotificationListenersTest {

    private NotificationListeners listeners;

    @BeforeEach
    void setUp() {
        listeners = new NotificationListeners();
    }

    @Test
    void handleEmailNotification_Success() {
        EmailNotificationEvent event = new EmailNotificationEvent("vecino@barriodigital.cl", "Aviso", "Texto", "PROC-01");
        assertDoesNotThrow(() -> listeners.handleEmailNotification(event));
    }

    @Test
    void handleEmailNotification_ShouldThrowException_WhenRecipientInvalid() {
        EmailNotificationEvent invalidEvent = new EmailNotificationEvent("", "Aviso", "Texto", "PROC-01");
        assertThrows(IllegalArgumentException.class, () -> listeners.handleEmailNotification(invalidEvent));
    }
}