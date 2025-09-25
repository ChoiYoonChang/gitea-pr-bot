package com.gitea.prbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResult {
    private ReviewType reviewType;
    private String content;
    private String overallGrade;
    private List<String> issues;
    private String repositoryName;
    private Long pullRequestNumber;
}