package com.example.fiap.videosliceapi.adapters.externalsystem;//import static org.junit.jupiter.api.Assertions.*;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.testUtils.StaticEnvironment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EmailNotificationSenderTest {

    /*
     * Testing only the "disabled" and failed config scenarios in Unit Test. The test with real message is in the Integration Test
     * (EmailNotificationSenderIT)
     */

    @Test
    void sendFinishedJobNotification_disable_messagePassThrough() {
        CognitoUserRegistry cognitoUserRegistry = mock();

        EmailNotificationSender authenticatedSender = new EmailNotificationSender(cognitoUserRegistry, new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "false"
        )));

        Job completeJob = Job.createJob(UUID.randomUUID(), "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());
        DownloadLink downloadLink = new DownloadLink("https://download.example.com/video1-frames.zip", 60);

        // Send is a no-op, our only expectation is that it does not fail trying some operation with mail server
        authenticatedSender.sendFinishedJobNotification(completeJob, downloadLink);

        verify(cognitoUserRegistry, never()).getUserEmail(anyString());
    }

    @Test
    void sendFinishedJobNotification_configFailure_missingServer() {
        CognitoUserRegistry cognitoUserRegistry = mock();

        StaticEnvironment environment = new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                //"videosliceapi.integration.smtp.server", "smtp.example.com",
                "videosliceapi.integration.smtp.port", "587",
                "videosliceapi.integration.smtp.mailFrom", "no-reply@example.com"
        ));

        assertThatThrownBy(() -> new EmailNotificationSender(cognitoUserRegistry, environment))
                .hasMessageContaining("videosliceapi.integration.smtp.server not set");
    }

    @Test
    void sendFinishedJobNotification_configFailure_missingEnabledFlag() {
        CognitoUserRegistry cognitoUserRegistry = mock();

        StaticEnvironment environment = new StaticEnvironment(Map.of(
//                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "smtp.example.com",
                "videosliceapi.integration.smtp.port", "587",
                "videosliceapi.integration.smtp.mailFrom", "no-reply@example.com"
        ));

        assertThatThrownBy(() -> new EmailNotificationSender(cognitoUserRegistry, environment))
                .hasMessageContaining("videosliceapi.integration.smtp.enabled not set");
    }

    @Test
    void sendFinishedJobNotification_configFailure_missingPort() {
        CognitoUserRegistry cognitoUserRegistry = mock();

        StaticEnvironment environment = new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "smtp.example.com",
//                "videosliceapi.integration.smtp.port", "587",
                "videosliceapi.integration.smtp.mailFrom", "no-reply@example.com"
        ));

        assertThatThrownBy(() -> new EmailNotificationSender(cognitoUserRegistry, environment))
                .hasMessageContaining("videosliceapi.integration.smtp.port not set");
    }

    @Test
    void sendFinishedJobNotification_configFailure_missingFrom() {
        CognitoUserRegistry cognitoUserRegistry = mock();

        StaticEnvironment environment = new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "smtp.example.com",
                "videosliceapi.integration.smtp.port", "587"
//                "videosliceapi.integration.smtp.mailFrom", "no-reply@example.com"
        ));

        assertThatThrownBy(() -> new EmailNotificationSender(cognitoUserRegistry, environment))
                .hasMessageContaining("videosliceapi.integration.smtp.mailFrom not set");
    }

    @Test
    void sendFinishedJobNotification_withEmailSend_exceptionHandling() {
        CognitoUserRegistry cognitoUserRegistry = mock();
        when(cognitoUserRegistry.getUserEmail("User_ABC")).thenReturn("user@example.com");

        EmailNotificationSender authenticatedSender = new EmailNotificationSender(cognitoUserRegistry, new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "smtp.example.com",
                "videosliceapi.integration.smtp.port", "587",
                "videosliceapi.integration.smtp.mailFrom", "no-reply@example.com"
        )));

        Job completeJob = Job.createJob(UUID.randomUUID(), "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());

        DownloadLink downloadLink = new DownloadLink("https://download.example.com/video1-frames.zip", 60);

        // Simulating exception during email sending
        doThrow(new RuntimeException("SMTP error")).when(cognitoUserRegistry).getUserEmail(anyString());

        // Attempting to send email won't throw, just log the exception
        authenticatedSender.sendFinishedJobNotification(completeJob, downloadLink);
    }
}