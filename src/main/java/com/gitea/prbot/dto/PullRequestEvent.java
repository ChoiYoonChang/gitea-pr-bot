package com.gitea.prbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestEvent {
    private String action;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;

    private Repository repository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PullRequest {
        private Long number;
        private String title;
        private String body;
        private String state;

        @JsonProperty("diff_url")
        private String diffUrl;

        @JsonProperty("html_url")
        private String htmlUrl;

        private Head head;
        private Base base;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Head {
            private String ref;
            private String sha;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Base {
            private String ref;
            private String sha;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Repository {
        private String name;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("clone_url")
        private String cloneUrl;

        private Owner owner;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Owner {
            private String login;
        }
    }
}