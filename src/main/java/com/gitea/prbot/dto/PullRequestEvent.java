package com.gitea.prbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PullRequestEvent {
    private String action;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;

    private Repository repository;

    // Constructors
    public PullRequestEvent() {
    }

    public PullRequestEvent(String action, PullRequest pullRequest, Repository repository) {
        this.action = action;
        this.pullRequest = pullRequest;
        this.repository = repository;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public PullRequest getPullRequest() {
        return pullRequest;
    }

    public void setPullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    // Builder pattern
    public static PullRequestEventBuilder builder() {
        return new PullRequestEventBuilder();
    }

    public static class PullRequestEventBuilder {
        private String action;
        private PullRequest pullRequest;
        private Repository repository;

        public PullRequestEventBuilder action(String action) {
            this.action = action;
            return this;
        }

        public PullRequestEventBuilder pullRequest(PullRequest pullRequest) {
            this.pullRequest = pullRequest;
            return this;
        }

        public PullRequestEventBuilder repository(Repository repository) {
            this.repository = repository;
            return this;
        }

        public PullRequestEvent build() {
            return new PullRequestEvent(action, pullRequest, repository);
        }
    }

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

        // Constructors
        public PullRequest() {
        }

        public PullRequest(Long number, String title, String body, String state, String diffUrl, String htmlUrl, Head head, Base base) {
            this.number = number;
            this.title = title;
            this.body = body;
            this.state = state;
            this.diffUrl = diffUrl;
            this.htmlUrl = htmlUrl;
            this.head = head;
            this.base = base;
        }

        // Getters and Setters
        public Long getNumber() {
            return number;
        }

        public void setNumber(Long number) {
            this.number = number;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDiffUrl() {
            return diffUrl;
        }

        public void setDiffUrl(String diffUrl) {
            this.diffUrl = diffUrl;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public Head getHead() {
            return head;
        }

        public void setHead(Head head) {
            this.head = head;
        }

        public Base getBase() {
            return base;
        }

        public void setBase(Base base) {
            this.base = base;
        }

        // Builder pattern
        public static PullRequestBuilder builder() {
            return new PullRequestBuilder();
        }

        public static class PullRequestBuilder {
            private Long number;
            private String title;
            private String body;
            private String state;
            private String diffUrl;
            private String htmlUrl;
            private Head head;
            private Base base;

            public PullRequestBuilder number(Long number) {
                this.number = number;
                return this;
            }

            public PullRequestBuilder title(String title) {
                this.title = title;
                return this;
            }

            public PullRequestBuilder body(String body) {
                this.body = body;
                return this;
            }

            public PullRequestBuilder state(String state) {
                this.state = state;
                return this;
            }

            public PullRequestBuilder diffUrl(String diffUrl) {
                this.diffUrl = diffUrl;
                return this;
            }

            public PullRequestBuilder htmlUrl(String htmlUrl) {
                this.htmlUrl = htmlUrl;
                return this;
            }

            public PullRequestBuilder head(Head head) {
                this.head = head;
                return this;
            }

            public PullRequestBuilder base(Base base) {
                this.base = base;
                return this;
            }

            public PullRequest build() {
                return new PullRequest(number, title, body, state, diffUrl, htmlUrl, head, base);
            }
        }

        public static class Head {
            private String ref;
            private String sha;

            // Constructors
            public Head() {
            }

            public Head(String ref, String sha) {
                this.ref = ref;
                this.sha = sha;
            }

            // Getters and Setters
            public String getRef() {
                return ref;
            }

            public void setRef(String ref) {
                this.ref = ref;
            }

            public String getSha() {
                return sha;
            }

            public void setSha(String sha) {
                this.sha = sha;
            }

            // Builder pattern
            public static HeadBuilder builder() {
                return new HeadBuilder();
            }

            public static class HeadBuilder {
                private String ref;
                private String sha;

                public HeadBuilder ref(String ref) {
                    this.ref = ref;
                    return this;
                }

                public HeadBuilder sha(String sha) {
                    this.sha = sha;
                    return this;
                }

                public Head build() {
                    return new Head(ref, sha);
                }
            }
        }

        public static class Base {
            private String ref;
            private String sha;

            // Constructors
            public Base() {
            }

            public Base(String ref, String sha) {
                this.ref = ref;
                this.sha = sha;
            }

            // Getters and Setters
            public String getRef() {
                return ref;
            }

            public void setRef(String ref) {
                this.ref = ref;
            }

            public String getSha() {
                return sha;
            }

            public void setSha(String sha) {
                this.sha = sha;
            }

            // Builder pattern
            public static BaseBuilder builder() {
                return new BaseBuilder();
            }

            public static class BaseBuilder {
                private String ref;
                private String sha;

                public BaseBuilder ref(String ref) {
                    this.ref = ref;
                    return this;
                }

                public BaseBuilder sha(String sha) {
                    this.sha = sha;
                    return this;
                }

                public Base build() {
                    return new Base(ref, sha);
                }
            }
        }
    }

    public static class Repository {
        private String name;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("clone_url")
        private String cloneUrl;

        private Owner owner;

        // Constructors
        public Repository() {
        }

        public Repository(String name, String fullName, String cloneUrl, Owner owner) {
            this.name = name;
            this.fullName = fullName;
            this.cloneUrl = cloneUrl;
            this.owner = owner;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getCloneUrl() {
            return cloneUrl;
        }

        public void setCloneUrl(String cloneUrl) {
            this.cloneUrl = cloneUrl;
        }

        public Owner getOwner() {
            return owner;
        }

        public void setOwner(Owner owner) {
            this.owner = owner;
        }

        // Builder pattern
        public static RepositoryBuilder builder() {
            return new RepositoryBuilder();
        }

        public static class RepositoryBuilder {
            private String name;
            private String fullName;
            private String cloneUrl;
            private Owner owner;

            public RepositoryBuilder name(String name) {
                this.name = name;
                return this;
            }

            public RepositoryBuilder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public RepositoryBuilder cloneUrl(String cloneUrl) {
                this.cloneUrl = cloneUrl;
                return this;
            }

            public RepositoryBuilder owner(Owner owner) {
                this.owner = owner;
                return this;
            }

            public Repository build() {
                return new Repository(name, fullName, cloneUrl, owner);
            }
        }

        public static class Owner {
            private String login;

            // Constructors
            public Owner() {
            }

            public Owner(String login) {
                this.login = login;
            }

            // Getters and Setters
            public String getLogin() {
                return login;
            }

            public void setLogin(String login) {
                this.login = login;
            }

            // Builder pattern
            public static OwnerBuilder builder() {
                return new OwnerBuilder();
            }

            public static class OwnerBuilder {
                private String login;

                public OwnerBuilder login(String login) {
                    this.login = login;
                    return this;
                }

                public Owner build() {
                    return new Owner(login);
                }
            }
        }
    }
}