package com.gitea.prbot.controller;

import com.gitea.prbot.dto.PullRequestEvent;
import com.gitea.prbot.service.CodeReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook")
public class GiteaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GiteaWebhookController.class);

    private final CodeReviewService codeReviewService;

    public GiteaWebhookController(CodeReviewService codeReviewService) {
        this.codeReviewService = codeReviewService;
    }

    @PostMapping("/gitea")
    public ResponseEntity<String> handleGiteaWebhook(@RequestBody PullRequestEvent event) {
        try {
            log.info("Received Gitea webhook for PR #{} in repository {}",
                    event.getPullRequest().getNumber(),
                    event.getRepository().getFullName());

            if ("opened".equals(event.getAction()) || "synchronize".equals(event.getAction())) {
                codeReviewService.reviewPullRequest(event);
                return ResponseEntity.ok("PR review initiated successfully");
            }

            return ResponseEntity.ok("Event ignored (not a PR open/sync event)");
        } catch (Exception e) {
            log.error("Error processing Gitea webhook", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Gitea webhook endpoint is healthy");
    }
}