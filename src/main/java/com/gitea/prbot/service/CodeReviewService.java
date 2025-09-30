package com.gitea.prbot.service;

import com.gitea.prbot.dto.PullRequestEvent;
import com.gitea.prbot.model.ReviewResult;
import com.gitea.prbot.model.ReviewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeReviewService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewService.class);

    private final ChatClient chatClient;
    private final GiteaService giteaService;
    private final PromptService promptService;
    private final DiffProcessorService diffProcessorService;

    public CodeReviewService(ChatClient chatClient, GiteaService giteaService, PromptService promptService, DiffProcessorService diffProcessorService) {
        this.chatClient = chatClient;
        this.giteaService = giteaService;
        this.promptService = promptService;
        this.diffProcessorService = diffProcessorService;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final int CHUNK_SIZE = 100;

    public void reviewPullRequest(PullRequestEvent event) {
        try {
            log.info("Starting review for PR #{} in {}",
                    event.getPullRequest().getNumber(),
                    event.getRepository().getFullName());

            String diff = giteaService.getPullRequestDiff(
                    event.getRepository().getFullName(),
                    event.getPullRequest().getNumber().intValue()
            );

            if (diff == null || diff.trim().isEmpty()) {
                log.warn("No diff content found for PR #{}", event.getPullRequest().getNumber());
                return;
            }

            List<String> chunks = chunkDiff(diff);
            List<CompletableFuture<ReviewResult>> futures = new ArrayList<>();

            for (String chunk : chunks) {
                if (shouldSkipChunk(chunk)) {
                    continue;
                }

                for (ReviewType type : ReviewType.values()) {
                    CompletableFuture<ReviewResult> future = CompletableFuture
                            .supplyAsync(() -> reviewChunk(chunk, type, event), executorService);
                    futures.add(future);
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenAccept(v -> {
                        List<ReviewResult> results = futures.stream()
                                .map(CompletableFuture::join)
                                .filter(result -> result != null && !result.getIssues().isEmpty())
                                .toList();

                        if (!results.isEmpty()) {
                            postReviewComments(event, results);
                        } else {
                            log.info("No issues found in PR #{}", event.getPullRequest().getNumber());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Error during parallel review processing", ex);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Error reviewing pull request", e);
        }
    }

    private ReviewResult reviewChunk(String chunk, ReviewType type, PullRequestEvent event) {
        try {
            String promptTemplate = promptService.getPrompt(type);
            String fullPrompt = promptTemplate + "\n\n코드:\n" + chunk;

            Prompt prompt = new Prompt(new UserMessage(fullPrompt));
            ChatResponse response = chatClient.call(prompt);
            String content = response.getResult().getOutput().getContent();

            return ReviewResult.builder()
                    .reviewType(type)
                    .content(content)
                    .issues(extractIssues(content))
                    .repositoryName(event.getRepository().getFullName())
                    .pullRequestNumber(event.getPullRequest().getNumber())
                    .build();

        } catch (Exception e) {
            log.error("Error reviewing chunk with type {}: {}", type, e.getMessage());
            return null;
        }
    }

    private List<String> chunkDiff(String diff) {
        List<String> chunks = new ArrayList<>();
        String[] lines = diff.split("\n");

        for (int i = 0; i < lines.length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, lines.length);
            String chunk = String.join("\n", Arrays.copyOfRange(lines, i, end));
            chunks.add(chunk);
        }

        return chunks;
    }

    private boolean shouldSkipChunk(String chunk) {
        return diffProcessorService.shouldSkipChunk(chunk);
    }

    private List<String> extractIssues(String content) {
        List<String> issues = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(HIGH|MED|LOW)\\](.+?)(?=\\n|$)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            issues.add(matcher.group().trim());
        }

        return issues;
    }

    private void postReviewComments(PullRequestEvent event, List<ReviewResult> results) {
        try {
            StringBuilder comment = new StringBuilder();
            comment.append("## 🤖 자동 코드 리뷰 결과\n\n");

            for (ReviewType type : ReviewType.values()) {
                List<ReviewResult> typeResults = results.stream()
                        .filter(r -> r.getReviewType() == type)
                        .toList();

                if (!typeResults.isEmpty()) {
                    comment.append("### ").append(getReviewTypeEmoji(type)).append(" ")
                            .append(type.name()).append(" 리뷰\n\n");

                    for (ReviewResult result : typeResults) {
                        for (String issue : result.getIssues()) {
                            comment.append("- ").append(issue).append("\n");
                        }
                    }
                    comment.append("\n");
                }
            }

            if (comment.length() > 100) {
                giteaService.addPullRequestComment(
                        event.getRepository().getFullName(),
                        event.getPullRequest().getNumber(),
                        comment.toString()
                );
                log.info("Posted review comments for PR #{}", event.getPullRequest().getNumber());
            }

        } catch (Exception e) {
            log.error("Error posting review comments", e);
        }
    }

    private String getReviewTypeEmoji(ReviewType type) {
        return switch (type) {
            case SECURITY -> "🔒";
            case PERFORMANCE -> "⚡";
            case STYLE -> "🎨";
            case GENERAL -> "📋";
        };
    }
}