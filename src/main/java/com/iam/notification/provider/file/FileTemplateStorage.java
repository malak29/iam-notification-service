package com.iam.notification.provider.file;

import com.iam.notification.provider.TemplateStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "notification.template.storage", havingValue = "file", matchIfMissing = true)
@Slf4j
public class FileTemplateStorage implements TemplateStorageProvider {

    private static final String TEMPLATE_BASE_PATH = "templates";

    @Override
    public Mono<String> getTemplate(String templateName, String language) {
        return Mono.fromCallable(() -> {
            try {
                String resourcePath = String.format("%s/%s/%s.html", TEMPLATE_BASE_PATH, language, templateName);
                ClassPathResource resource = new ClassPathResource(resourcePath);

                if (resource.exists()) {
                    return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
                } else {
                    // Fallback to English if language-specific template doesn't exist
                    String fallbackPath = String.format("%s/en/%s.html", TEMPLATE_BASE_PATH, templateName);
                    ClassPathResource fallbackResource = new ClassPathResource(fallbackPath);

                    if (fallbackResource.exists()) {
                        log.warn("Template {} not found for language {}, using English fallback", templateName, language);
                        return Files.readString(fallbackResource.getFile().toPath(), StandardCharsets.UTF_8);
                    } else {
                        throw new RuntimeException("Template not found: " + templateName);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read template: " + templateName, e);
            }
        });
    }

    @Override
    public Mono<Void> saveTemplate(String templateName, String language, String content) {
        return Mono.fromRunnable(() -> {
            try {
                Path templateDir = Paths.get("src/main/resources", TEMPLATE_BASE_PATH, language);
                Files.createDirectories(templateDir);

                Path templateFile = templateDir.resolve(templateName + ".html");
                Files.writeString(templateFile, content, StandardCharsets.UTF_8);

                log.info("Template {} saved to file system", templateName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save template: " + templateName, e);
            }
        });
    }

    @Override
    public Mono<Boolean> templateExists(String templateName, String language) {
        return Mono.fromCallable(() -> {
            String resourcePath = String.format("%s/%s/%s.html", TEMPLATE_BASE_PATH, language, templateName);
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return resource.exists();
        });
    }

    @Override
    public String getProviderName() {
        return "FILE_STORAGE";
    }
}