package com.iam.notification.provider.smtp;

import com.iam.notification.provider.EmailProvider;
import com.iam.notification.dto.EmailRequest;
import com.iam.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;

    @Override
    public Mono<NotificationResponse> sendEmail(EmailRequest request) {
        log.info("Sending email via SMTP to: {}", request.getTo());

        return Mono.fromCallable(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(request.getTo());
                helper.setFrom(request.getFrom());
                helper.setSubject(request.getSubject());

                if (request.getBodyHtml() != null && !request.getBodyHtml().isEmpty()) {
                    helper.setText(request.getBodyText(), request.getBodyHtml());
                } else {
                    helper.setText(request.getBodyText(), false);
                }

                mailSender.send(message);

                return NotificationResponse.builder()
                        .success(true)
                        .provider("SMTP")
                        .providerId(UUID.randomUUID().toString())
                        .message("Email sent successfully via SMTP")
                        .build();

            } catch (Exception e) {
                log.error("Failed to send email via SMTP: {}", e.getMessage(), e);
                return NotificationResponse.builder()
                        .success(false)
                        .provider("SMTP")
                        .errorMessage(e.getMessage())
                        .build();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> verifyEmailAddress(String email) {
        // Basic email format validation for SMTP
        return Mono.fromCallable(() -> {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            return email.matches(emailRegex);
        });
    }

    @Override
    public String getProviderName() {
        return "SMTP";
    }
}