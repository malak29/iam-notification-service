package com.iam.notification.provider.aws;

import com.iam.notification.provider.TemplateStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "notification.template.storage", havingValue = "aws-s3")
@RequiredArgsConstructor
@Slf4j
public class AwsS3TemplateStorage implements TemplateStorageProvider {

    private final S3AsyncClient s3Client;
    private final String bucketName;

    @Override
    public Mono<String> getTemplate(String templateName, String language) {
        log.debug("Fetching template {} for language {} from S3", templateName, language);

        String key = String.format("templates/%s/%s.html", language, templateName);

        return Mono.fromFuture(() -> {
                    GetObjectRequest request = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    return s3Client.getObject(request, AsyncRequestBody.fromString(""));
                })
                .map(response -> response.toString())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(template -> log.debug("Template {} fetched successfully from S3", templateName))
                .onErrorResume(throwable -> {
                    log.warn("Failed to fetch template {} from S3: {}", templateName, throwable.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> saveTemplate(String templateName, String language, String content) {
        log.info("Saving template {} for language {} to S3", templateName, language);

        String key = String.format("templates/%s/%s.html", language, templateName);

        return Mono.fromFuture(() -> {
                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("text/html")
                            .build();

                    return s3Client.putObject(request,
                            AsyncRequestBody.fromString(content, StandardCharsets.UTF_8));
                })
                .then()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(unused -> log.info("Template {} saved successfully to S3", templateName))
                .onErrorResume(throwable -> {
                    log.error("Failed to save template {} to S3: {}", templateName, throwable.getMessage());
                    return Mono.error(throwable);
                });
    }

    @Override
    public Mono<Boolean> templateExists(String templateName, String language) {
        String key = String.format("templates/%s/%s.html", language, templateName);

        return Mono.fromFuture(() -> {
                    HeadObjectRequest request = HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    return s3Client.headObject(request);
                })
                .map(response -> true)
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public String getProviderName() {
        return "AWS_S3";
    }
}