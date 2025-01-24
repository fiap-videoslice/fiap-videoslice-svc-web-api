package com.example.fiap.videosliceapi.adapters.externalsystem;

import com.example.fiap.videosliceapi.domain.entities.Job;
import com.example.fiap.videosliceapi.domain.external.NotificationSender;
import com.example.fiap.videosliceapi.domain.usecasedto.DownloadLink;
import com.example.fiap.videosliceapi.domain.utils.StringUtils;
import com.example.fiap.videosliceapi.presenters.EmailNotificationPresenter;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Concrete implementation of NotificationSender based on email
 */
@Service
public class EmailNotificationSender implements NotificationSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final CognitoUserRegistry cognitoUserRegistry;
    private final Session mailSession;
    private final String mailFrom;

    private final EmailNotificationPresenter presenter = new EmailNotificationPresenter();

    public EmailNotificationSender(CognitoUserRegistry cognitoUserRegistry, Environment environment) {
        this.cognitoUserRegistry = cognitoUserRegistry;

        String smtpEnabledEnv = environment.getProperty("videosliceapi.integration.smtp.enabled");
        if (StringUtils.isEmpty(smtpEnabledEnv))
            throw new IllegalStateException("videosliceapi.integration.smtp.enabled not set");

        boolean smtpEnabled = Boolean.parseBoolean(smtpEnabledEnv);

        if (smtpEnabled) {
            String smtpServer = environment.getProperty("videosliceapi.integration.smtp.server");
            if (StringUtils.isEmpty(smtpServer))
                throw new IllegalStateException("videosliceapi.integration.smtp.server not set");

            String smtpPort = environment.getProperty("videosliceapi.integration.smtp.port");
            if (StringUtils.isEmpty(smtpPort))
                throw new IllegalStateException("videosliceapi.integration.smtp.port not set");

            String smtpTls = environment.getProperty("videosliceapi.integration.smtp.starttls", "false");

            String smtpUser = environment.getProperty("videosliceapi.integration.smtp.user");
            String smtpPassword = environment.getProperty("videosliceapi.integration.smtp.password");
            boolean usingAuthentication = smtpUser != null && smtpPassword != null;

            this.mailFrom = environment.getProperty("videosliceapi.integration.smtp.mailFrom");
            if (StringUtils.isEmpty(mailFrom))
                throw new IllegalStateException("videosliceapi.integration.smtp.mailFrom not set");

            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpServer);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", String.valueOf(usingAuthentication));
            properties.put("mail.smtp.starttls.enable", smtpTls);

            if (usingAuthentication) {
                this.mailSession = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPassword);
                    }
                });
            } else {
                this.mailSession = Session.getInstance(properties);
            }
        } else {
            this.mailSession = null;
            this.mailFrom = null;
        }
    }

    @Override
    public void sendFinishedJobNotification(Job job, DownloadLink downloadLink) {
        String subject = presenter.finishedJobNotificationTitle(job);
        String body = presenter.finishedJobNotificationBody(job, downloadLink);

        if (mailSession != null) {
            try {
                String recipient = cognitoUserRegistry.getUserEmail(job.userId());

                MimeMessage message = new MimeMessage(mailSession);
                message.setFrom(mailFrom);
                message.setRecipients(Message.RecipientType.TO, recipient);
                message.setSubject(subject);
                message.setContent(body, "text/html; charset=utf-8");
                Transport.send(message);
            } catch (Exception e) {
                LOGGER.error("Failed to send email notification: {}", e, e);
            }

        } else {
            // SMTP-disabled setup intended to be used on developer environment
            LOGGER.info("""
                    -------------------------------------------------------
                    -- EMAIL NOTIFICATION FOR USER: {}
                    -- Subject: {}
                    {}""", job.userId(), subject, body);
        }
    }
}
