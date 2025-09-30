package com.gitea.prbot.service;

import com.gitea.prbot.model.ReviewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

@Service
public class PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptService.class);

    private final Map<ReviewType, String> promptTemplates = new EnumMap<>(ReviewType.class);

    public PromptService() {
        loadPromptTemplates();
    }

    private void loadPromptTemplates() {
        try {
            promptTemplates.put(ReviewType.SECURITY, loadPrompt("prompts/security/security-review.md"));
            promptTemplates.put(ReviewType.PERFORMANCE, loadPrompt("prompts/performance/performance-review.md"));
            promptTemplates.put(ReviewType.STYLE, loadPrompt("prompts/style/code-style-review.md"));
            promptTemplates.put(ReviewType.GENERAL, loadPrompt("prompts/general/general-review.md"));

            log.info("Loaded {} prompt templates", promptTemplates.size());
        } catch (Exception e) {
            log.error("Error loading prompt templates", e);
        }
    }

    private String loadPrompt(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    public String getPrompt(ReviewType type) {
        return promptTemplates.getOrDefault(type, "코드를 검토해 주세요.");
    }
}