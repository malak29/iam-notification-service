package com.iam.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
@Slf4j
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.s3.bucket-name:iam-notification-templates}")
    private String s3BucketName;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (accessKeyId != null && !accessKeyId.isEmpty() &&
                secretAccessKey != null && !secretAccessKey.isEmpty()) {

            log.info("Using static AWS credentials");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            );
        } else {
            log.info("Using default AWS credentials provider chain");
            return DefaultCredentialsProvider.create();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "notification.email.provider", havingValue = "aws-ses")
    public SesAsyncClient sesAsyncClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Configuring AWS SES client for region: {}", awsRegion);

        return SesAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.sms.provider", havingValue = "aws-sns")
    public SnsAsyncClient snsAsyncClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Configuring AWS SNS client for region: {}", awsRegion);

        return SnsAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.template.storage", havingValue = "aws-s3")
    public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentialsProvider) {
        log.info("Configuring AWS S3 client for region: {}", awsRegion);

        return S3AsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "notification.template.storage", havingValue = "aws-s3")
    public String s3BucketName() {
        return s3BucketName;
    }
}