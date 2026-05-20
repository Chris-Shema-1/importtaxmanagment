package com.importtax.server.service;

import com.importtax.server.config.EmailConfig;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Sends transactional email via Brevo SMTP (Jakarta Mail).
 */
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String OTP_SUBJECT = "Your Import Tax Management System OTP Code";

    private final Session mailSession;

    public EmailService() {
        this.mailSession = createMailSession();
    }

    /**
     * Sends a one-time password to the recipient's inbox.
     *
     * @param recipientEmail address from the users table
     * @param otpCode        six-digit OTP (not logged)
     */
    public void sendOtpEmail(String recipientEmail, String otpCode) throws MessagingException {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new MessagingException("Recipient email is required");
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw new MessagingException("OTP code is required");
        }

        String normalizedEmail = recipientEmail.trim();
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
        message.setReplyTo(new InternetAddress[]{new InternetAddress(EmailConfig.FROM_EMAIL)});
        message.setHeader("X-Mailer", EmailConfig.FROM_NAME);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(normalizedEmail, false));
        message.setSubject(OTP_SUBJECT, "UTF-8");
        message.setContent(buildHtmlBody(otpCode.trim()), "text/html; charset=UTF-8");

        Transport.send(message);
        LOGGER.info("OTP email sent successfully to {}", maskEmail(normalizedEmail));
    }

    private static String buildHtmlBody(String otpCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Segoe UI,Arial,sans-serif;background:#f5f7fa;margin:0;padding:24px;">
              <div style="max-width:520px;margin:0 auto;background:#ffffff;border-radius:8px;padding:32px;border:1px solid #dce0e6;">
                <h2 style="color:#1e2430;margin:0 0 16px;">Import Tax Management System</h2>
                <p style="color:#5a6478;font-size:15px;line-height:1.5;">Hello,</p>
                <p style="color:#5a6478;font-size:15px;line-height:1.5;">
                  Use the verification code below to complete your sign-in. This code is valid for
                  <strong>5 minutes</strong>.
                </p>
                <p style="text-align:center;margin:28px 0;">
                  <span style="display:inline-block;font-size:32px;font-weight:bold;letter-spacing:8px;
                    color:#0078d7;background:#f0f6fc;padding:16px 28px;border-radius:8px;">%s</span>
                </p>
                <p style="color:#5a6478;font-size:14px;line-height:1.5;">
                  If you did not request this code, please ignore this email or contact your system administrator.
                  Never share this code with anyone.
                </p>
                <hr style="border:none;border-top:1px solid #eceff3;margin:24px 0;">
                <p style="color:#a0a8b4;font-size:12px;margin:0;">ITMS Security — automated message, do not reply.</p>
              </div>
            </body>
            </html>
            """.formatted(otpCode);
    }

    private static Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(EmailConfig.SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.SMTP_USERNAME, EmailConfig.SMTP_PASSWORD);
            }
        });
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
