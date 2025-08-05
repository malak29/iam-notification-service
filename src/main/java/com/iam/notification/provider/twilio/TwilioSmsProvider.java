package com.iam.notification.provider.twilio;

import com.iam.notification.provider.SmsProvider;
import com.iam.notification.dto.SmsRequest;
import com.iam.notification.dto.NotificationResponse;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "notification.sms.provider", havingValue = "twilio")
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsProvider implements SmsProvider {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS provider initialized");
    }

    @Override
    public Mono<NotificationResponse> sendSms(SmsRequest request) {
        log.info("Sending SMS via Twilio to: {}", request.getPhoneNumber());

        return Mono.fromCallable(() -> {
            try {
                Message message = Message.creator(
                        new PhoneNumber(request.getPhoneNumber()),
                        new PhoneNumber(fromNumber),
                        request.getMessage()
                ).create();

                return NotificationResponse.builder()
                        .success(true)
                        .provider("TWILIO")
                        .providerId(message.getSid())
                        .message("SMS sent successfully via Twilio")
                        .build();

            } catch (Exception e) {
                log.error("Failed to send SMS via Twilio: {}", e.getMessage(), e);
                return NotificationResponse.builder()
                        .success(false)
                        .provider("TWILIO")
                        .errorMessage(e.getMessage())
                        .build();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> validatePhoneNumber(String phoneNumber) {
        return Mono.fromCallable(() -> {
            try {
                // Use Twilio's phone number validation
                PhoneNumber phone = new PhoneNumber(phoneNumber);
                return phone.getEndpoint() != null;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }
}