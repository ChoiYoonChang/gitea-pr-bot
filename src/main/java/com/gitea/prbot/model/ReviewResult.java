package com.gitea.prbot.model;

import java.util.List;

public class ReviewResult {
    private ReviewType reviewType;
    private String content;
    private String overallGrade;
    private List<String> issues;
    private String repositoryName;
    private Long pullRequestNumber;

    // Constructors
    public ReviewResult() {
    }

    public ReviewResult(ReviewType reviewType, String content, String overallGrade, List<String> issues, String repositoryName, Long pullRequestNumber) {
        this.reviewType = reviewType;
        this.content = content;
        this.overallGrade = overallGrade;
        this.issues = issues;
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
    }

    // Getters and Setters
    public ReviewType getReviewType() {
        return reviewType;
    }

    public void setReviewType(ReviewType reviewType) {
        this.reviewType = reviewType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOverallGrade() {
        return overallGrade;
    }

    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }

    public void setPullRequestNumber(Long pullRequestNumber) {
        this.pullRequestNumber = pullRequestNumber;
    }

    // Builder pattern
    public static ReviewResultBuilder builder() {
        return new ReviewResultBuilder();
    }

    public static class ReviewResultBuilder {
        private ReviewType reviewType;
        private String content;
        private String overallGrade;
        private List<String> issues;
        private String repositoryName;
        private Long pullRequestNumber;

        public ReviewResultBuilder reviewType(ReviewType reviewType) {
            this.reviewType = reviewType;
            return this;
        }

        public ReviewResultBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ReviewResultBuilder overallGrade(String overallGrade) {
            this.overallGrade = overallGrade;
            return this;
        }

        public ReviewResultBuilder issues(List<String> issues) {
            this.issues = issues;
            return this;
        }

        public ReviewResultBuilder repositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
            return this;
        }

        public ReviewResultBuilder pullRequestNumber(Long pullRequestNumber) {
            this.pullRequestNumber = pullRequestNumber;
            return this;
        }

        public ReviewResult build() {
            return new ReviewResult(reviewType, content, overallGrade, issues, repositoryName, pullRequestNumber);
        }
    }
}