package com.iam.notification.provider.sendgrid;

import com.iam.notification.provider.EmailProvider;
import com.iam.notification.dto.EmailRequest;
import com.iam.notification.dto.NotificationResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@ConditionalOnProperty(name = "notification.email.provider", havingValue = "sendgrid")
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailProvider implements EmailProvider {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Override
    public Mono<NotificationResponse> sendEmail(EmailRequest request) {
        log.info("Sending email via SendGrid to: {}", request.getTo());

        return Mono.fromCallable(() -> {
            try {
                Email from = new Email(request.getFrom());
                Email to = new Email(request.getTo());
                Content content = new Content("text/html", request.getBodyHtml());

                Mail mail = new Mail(from, request.getSubject(), to, content);

                SendGrid sg = new SendGrid(apiKey);
                Request sgRequest = new Request();
                sgRequest.setMethod(Method.POST);
                sgRequest.setEndpoint("mail/send");
                sgRequest.setBody(mail.build());

                Response response = sg.api(sgRequest);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    return NotificationResponse.builder()
                            .success(true)
                            .provider("SENDGRID")
                            .providerId(response.getHeaders().get("X-Message-Id"))
                            .message("Email sent successfully via SendGrid")
                            .build();
                } else {
                    throw new RuntimeException("SendGrid API error: " + response.getBody());
                }

            } catch (Exception e) {
                log.error("Failed to send email via SendGrid: {}", e.getMessage(), e);
                return NotificationResponse.builder()
                        .success(false)
                        .provider("SENDGRID")
                        .errorMessage(e.getMessage())
                        .build();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> verifyEmailAddress(String email) {
        // SendGrid email validation logic
        return Mono.fromCallable(() -> {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            return email.matches(emailRegex);
        });
    }

    @Override
    public String getProviderName() {
        return "SENDGRID";
    }
}