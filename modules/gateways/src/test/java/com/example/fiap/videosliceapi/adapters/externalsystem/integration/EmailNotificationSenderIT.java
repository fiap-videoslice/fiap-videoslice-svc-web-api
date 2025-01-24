package com.example.fiap.videosliceapi.adapters.externalsystem.integration;//import static org.junit.jupiter.api.Assertions.*;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.example.fiap.videosliceapi.adapters.externalsystem.CognitoUserRegistry;
import com.example.fiap.videosliceapi.adapters.externalsystem.EmailNotificationSender;
import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.testUtils.StaticEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailNotificationSenderIT {

    private CognitoUserRegistry cognitoUserRegistry;

    private SimpleSmtpServer dumbster;

    private EmailNotificationSender emailNotificationSender;

    @BeforeEach
    void setUp() throws IOException {
        cognitoUserRegistry = mock();

        dumbster = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);

        emailNotificationSender = new EmailNotificationSender(cognitoUserRegistry, new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "localhost",
                "videosliceapi.integration.smtp.port", String.valueOf(dumbster.getPort()),
                "videosliceapi.integration.smtp.starttls", "false",
                "videosliceapi.integration.smtp.mailFrom", "videosliceapi@localhost"
        )));
    }

    @AfterEach
    void tearDown() {
        if (dumbster != null) {
            dumbster.close();
        }
    }

    @Test
    void sendFinishedJobNotification_complete() {
        when(cognitoUserRegistry.getUserEmail("User_ABC")).thenReturn("user@fiap.example.com");
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");

        Job completeJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());

        emailNotificationSender.sendFinishedJobNotification(completeJob);

        List<SmtpMessage> receivedEmails = dumbster.getReceivedEmails();
        assertThat(receivedEmails).hasSize(1);

        SmtpMessage email = receivedEmails.getFirst();
        assertThat(email.getHeaderValue("To")).isEqualTo("user@fiap.example.com");
        assertThat(email.getHeaderValue("Subject")).isEqualTo("Your Video slice job is complete");

        assertThat(email.getBody()).contains("f4881488-1091-70b1-21fe-6dc5cce9c313");
        assertThat(email.getBody()).contains("has completed successfully");
    }

    @Test
    void sendFinishedJobNotification_failed() {
        when(cognitoUserRegistry.getUserEmail("User_ABC")).thenReturn("user@fiap.example.com");
        UUID id = UUID.fromString("f4881488-1091-70b1-21fe-6dc5cce9c313");

        Job failedJob = Job.createJob(id, "input-file.mp4", 10, Instant.now(), "User_ABC")
                .errorProcessing("Invalid video file", Instant.now());

        emailNotificationSender.sendFinishedJobNotification(failedJob);

        List<SmtpMessage> receivedEmails = dumbster.getReceivedEmails();
        assertThat(receivedEmails).hasSize(1);

        SmtpMessage email = receivedEmails.getFirst();
        assertThat(email.getHeaderValue("To")).isEqualTo("user@fiap.example.com");
        assertThat(email.getHeaderValue("Subject")).isEqualTo("YOUR VIDEO SLICE JOB HAS FAILED");

        assertThat(email.getBody()).contains("f4881488-1091-70b1-21fe-6dc5cce9c313");
        assertThat(email.getBody()).contains("has failed");
        assertThat(email.getBody()).contains("Invalid video file");
    }

    @Test
    void sendFinishedJobNotification_authenticationParams() {
        // This test is redundant but is here just for coverage,
        // to go through the construction using auth params

        when(cognitoUserRegistry.getUserEmail("User_ABC")).thenReturn("user@fiap.example.com");

        EmailNotificationSender authenticatedSender = new EmailNotificationSender(cognitoUserRegistry, new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.enabled", "true",
                "videosliceapi.integration.smtp.server", "localhost",
                "videosliceapi.integration.smtp.port", String.valueOf(dumbster.getPort()),
                "videosliceapi.integration.smtp.starttls", "false",
                "videosliceapi.integration.smtp.mailFrom", "videosliceapi@localhost",

                "videosliceapi.integration.smtp.user", "videosliceapi@localhost",
                "videosliceapi.integration.smtp.password", "1234"
        )));

        Job completeJob = Job.createJob(UUID.randomUUID(), "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());

        authenticatedSender.sendFinishedJobNotification(completeJob);
    }

    @Test @Disabled("This test is intended to be used manually, it depends on the communication with a real server")
    void sendFinishedJobNotification_real() {
        EmailNotificationSender realSender = new EmailNotificationSender(cognitoUserRegistry, new StaticEnvironment(Map.of(
                "videosliceapi.integration.smtp.server", "smtp.gmail.com",
                "videosliceapi.integration.smtp.port", "587",
                "videosliceapi.integration.smtp.starttls", "true",
                "videosliceapi.integration.smtp.mailFrom", "fiapvideoslice@gmail.com",

                "videosliceapi.integration.smtp.user", "fiapvideoslice@gmail.com",
                "videosliceapi.integration.smtp.password", "<password>"
        )));

        when(cognitoUserRegistry.getUserEmail("User_ABC")).thenReturn("gomesrodrigo0481@gmail.com");

        Job completeJob = Job.createJob(UUID.randomUUID(), "input-file.mp4", 10, Instant.now(), "User_ABC")
                .completeProcessing("output-file.zip", Instant.now());

        realSender.sendFinishedJobNotification(completeJob);

        System.out.println("===============================================\nReal mail sent. Check inbox");
    }
}