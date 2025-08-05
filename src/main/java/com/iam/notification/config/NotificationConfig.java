package com.iam.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfig {

    private Email email = new Email();
    private Sms sms = new Sms();
    private Template template = new Template();
    private Queue queue = new Queue();
    private RealTime realTime = new RealTime();

    @Data
    public static class Email {
        private String provider = "smtp"; // smtp, aws-ses, sendgrid
        private String fromAddress = "noreply@iam-system.com";
        private String fromName = "IAM System";
        private boolean enabled = true;
        private int retryAttempts = 3;
        private String replyToAddress;
    }

    @Data
    public static class Sms {
        private String provider = "mock"; // mock, aws-sns, twilio
        private String fromNumber = "+1234567890";
        private boolean enabled = true;
        private int retryAttempts = 3;
    }

    @Data
    public static class Template {
        private String storage = "file"; // file, aws-s3, database
        private String defaultLanguage = "en";
        private boolean cacheEnabled = true;
        private int cacheTtlMinutes = 60;
    }

    @Data
    public static class Queue {
        private boolean enabled = true;
        private String provider = "redis"; // redis, aws-sqs
        private int maxRetries = 3;
        private int retryDelaySeconds = 30;
        private int batchSize = 10;
    }

    @Data
    public static class RealTime {
        private boolean enabled = true;
        private String provider = "websocket"; // websocket, aws-websocket, pusher
        private int connectionTimeoutSeconds = 30;
        private int maxConnections = 1000;
    }
}