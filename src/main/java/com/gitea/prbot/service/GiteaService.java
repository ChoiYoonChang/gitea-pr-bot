package com.gitea.prbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GiteaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String giteaBaseUrl;
    private final String giteaToken;

    public GiteaService(RestTemplate restTemplate,
                        ObjectMapper objectMapper,
                        @Value("${gitea.base-url}") String giteaBaseUrl,
                        @Value("${gitea.token}") String giteaToken) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.giteaBaseUrl = giteaBaseUrl;
        this.giteaToken = giteaToken;
        log.info("Gitea service initialized with base URL: {}", giteaBaseUrl);
    }

    public String getPullRequestDiff(String repositoryFullName, int prNumber) {
        try {
            String url = String.format("%s/api/v1/repos/%s/pulls/%d.diff",
                    giteaBaseUrl, repositoryFullName, prNumber);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Failed to get PR diff for {}/#{}, status: {}",
                         repositoryFullName, prNumber, response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("Error getting PR diff for {}/#{}", repositoryFullName, prNumber, e);
            return null;
        }
    }

    public void createReviewComment(String repositoryFullName, int prNumber, String comment) {
        try {
            String url = String.format("%s/api/v1/repos/%s/issues/%d/comments",
                    giteaBaseUrl, repositoryFullName, prNumber);

            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("body", comment);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Posted review comment to PR {}/#{}", repositoryFullName, prNumber);
            } else {
                log.error("Failed to post comment to PR {}/#{}, status: {}",
                         repositoryFullName, prNumber, response.getStatusCode());
                throw new RuntimeException("Failed to post comment");
            }

        } catch (Exception e) {
            log.error("Error posting comment to PR {}/#{}", repositoryFullName, prNumber, e);
            throw new RuntimeException("Failed to post comment", e);
        }
    }

    public void createReview(String repositoryFullName, int prNumber, String body, String reviewType) {
        try {
            String url = String.format("%s/api/v1/repos/%s/pulls/%d/reviews",
                    giteaBaseUrl, repositoryFullName, prNumber);

            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("body", body);
            requestBody.put("event", reviewType); // APPROVE, REQUEST_CHANGES, COMMENT

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Created review for PR {}/#{} with type {}", repositoryFullName, prNumber, reviewType);
            } else {
                log.error("Failed to create review for PR {}/#{}, status: {}",
                         repositoryFullName, prNumber, response.getStatusCode());
                throw new RuntimeException("Failed to create review");
            }

        } catch (Exception e) {
            log.error("Error creating review for PR {}/#{}", repositoryFullName, prNumber, e);
            throw new RuntimeException("Failed to create review", e);
        }
    }

    public void addLineComment(String repositoryFullName, int prNumber, String commitSha,
                              String path, int line, String comment) {
        try {
            String url = String.format("%s/api/v1/repos/%s/pulls/%d/reviews",
                    giteaBaseUrl, repositoryFullName, prNumber);

            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> commentData = new HashMap<>();
            commentData.put("path", path);
            commentData.put("body", comment);
            commentData.put("new_position", line);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("body", "Code review comment");
            requestBody.put("event", "COMMENT");
            requestBody.put("comments", new Object[]{commentData});

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Added line comment to PR {}/#{} at {}:{}", repositoryFullName, prNumber, path, line);
            } else {
                log.error("Failed to add line comment to PR {}/#{}, status: {}",
                         repositoryFullName, prNumber, response.getStatusCode());
                throw new RuntimeException("Failed to add line comment");
            }

        } catch (Exception e) {
            log.error("Error adding line comment to PR {}/#{}", repositoryFullName, prNumber, e);
            throw new RuntimeException("Failed to add line comment", e);
        }
    }

    public boolean isRepositoryAccessible(String repositoryFullName) {
        try {
            String url = String.format("%s/api/v1/repos/%s", giteaBaseUrl, repositoryFullName);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            log.warn("Repository {} is not accessible", repositoryFullName);
            return false;
        }
    }

    public JsonNode getPullRequestInfo(String repositoryFullName, int prNumber) {
        try {
            String url = String.format("%s/api/v1/repos/%s/pulls/%d",
                    giteaBaseUrl, repositoryFullName, prNumber);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            }

            return null;

        } catch (Exception e) {
            log.error("Error getting PR info for {}/#{}", repositoryFullName, prNumber, e);
            return null;
        }
    }

    public void addPullRequestComment(String repositoryFullName, Long prNumber, String comment) {
        createReviewComment(repositoryFullName, prNumber.intValue(), comment);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + giteaToken);
        headers.set("Accept", "application/json");
        return headers;
    }
}