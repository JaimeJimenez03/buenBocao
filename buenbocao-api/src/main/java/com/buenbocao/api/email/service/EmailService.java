package com.buenbocao.api.email.service;

import com.buenbocao.api.email.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Servicio de envío de emails.
 * Los emails se envían de forma asíncrona para no bloquear la petición HTTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Envía el email de verificación de cuenta.
     */
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationUrl", verificationUrl);

        String htmlContent = templateEngine.process("email/verify-email", context);

        sendHtmlEmail(toEmail, "Verifica tu cuenta en Buen Bocao", htmlContent);
    }

    /**
     * Envía el email de bienvenida tras verificar la cuenta.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);

        String htmlContent = templateEngine.process("email/welcome", context);

        sendHtmlEmail(toEmail, "¡Bienvenido a Buen Bocao!", htmlContent);
    }

    /**
     * Envía el código de restablecimiento de contraseña.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String code) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("code", code);

        String htmlContent = templateEngine.process("email/reset-password", context);

        sendHtmlEmail(toEmail, "Código de recuperación de contraseña", htmlContent);
    }

    /**
     * Envía el email de cambio de contraseña exitoso.
     */
    @Async
    public void sendPasswordChangedEmail(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);

        String htmlContent = templateEngine.process("email/password-changed", context);

        sendHtmlEmail(toEmail, "Tu contraseña ha sido actualizada", htmlContent);
    }


    /**
     * Envía email detallado de ban al usuario.
     */
    @Async
    public void sendBanEmail(String toEmail, String username, String reason, Integer durationDays) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("reason", reason != null ? reason : "Violación de las normas de la comunidad");
        context.setVariable("permanent", durationDays == null);
        context.setVariable("durationDays", durationDays);

        String htmlContent = templateEngine.process("email/account-banned", context);
        String subject = durationDays == null
                ? "Tu cuenta en Buen Bocao ha sido suspendida permanentemente"
                : "Tu cuenta en Buen Bocao ha sido suspendida por " + durationDays + " días";
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // ---- Método base de envío ----

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email enviado a: {} — Asunto: {}", to, subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }
}
