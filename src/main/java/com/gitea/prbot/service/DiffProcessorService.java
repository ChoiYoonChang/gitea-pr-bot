package com.gitea.prbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiffProcessorService {

    private static final Logger log = LoggerFactory.getLogger(DiffProcessorService.class);

    @Value("${bot.review.chunk-size:100}")
    private int chunkSize;

    @Value("${bot.review.enable-static-analysis:true}")
    private boolean enableStaticAnalysis;

    private static final Pattern DIFF_FILE_PATTERN = Pattern.compile("^diff --git a/(.*) b/(.*)$");
    private static final Pattern HUNK_HEADER_PATTERN = Pattern.compile("^@@\\s*-\\d+,?\\d*\\s*\\+\\d+,?\\d*\\s*@@");

    public List<DiffChunk> processDiff(String diff) {
        if (diff == null || diff.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<DiffChunk> chunks = new ArrayList<>();
        String[] files = splitDiffByFiles(diff);

        for (String fileDiff : files) {
            chunks.addAll(processFileDiff(fileDiff));
        }

        if (enableStaticAnalysis) {
            chunks = filterWithStaticAnalysis(chunks);
        }

        log.info("Processed {} diff chunks", chunks.size());
        return chunks;
    }

    private String[] splitDiffByFiles(String diff) {
        List<String> files = new ArrayList<>();
        String[] lines = diff.split("\n");
        StringBuilder currentFile = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("diff --git") && currentFile.length() > 0) {
                files.add(currentFile.toString());
                currentFile = new StringBuilder();
            }
            currentFile.append(line).append("\n");
        }

        if (currentFile.length() > 0) {
            files.add(currentFile.toString());
        }

        return files.toArray(new String[0]);
    }

    private List<DiffChunk> processFileDiff(String fileDiff) {
        List<DiffChunk> chunks = new ArrayList<>();
        String[] lines = fileDiff.split("\n");

        String fileName = extractFileName(fileDiff);
        String fileExtension = getFileExtension(fileName);

        StringBuilder currentChunk = new StringBuilder();
        int lineCount = 0;
        int startLine = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 파일 헤더는 건너뛰기
            if (line.startsWith("diff --git") || line.startsWith("index ") ||
                line.startsWith("--- ") || line.startsWith("+++ ")) {
                continue;
            }

            // 새로운 hunk 시작
            if (HUNK_HEADER_PATTERN.matcher(line).matches()) {
                if (currentChunk.length() > 0) {
                    chunks.add(createDiffChunk(fileName, fileExtension, currentChunk.toString(), startLine));
                    currentChunk = new StringBuilder();
                    lineCount = 0;
                }
                startLine = extractLineNumber(line);
                continue;
            }

            currentChunk.append(line).append("\n");
            lineCount++;

            // chunk 크기 제한 확인
            if (lineCount >= chunkSize) {
                chunks.add(createDiffChunk(fileName, fileExtension, currentChunk.toString(), startLine));
                currentChunk = new StringBuilder();
                lineCount = 0;
                startLine = startLine + chunkSize;
            }
        }

        // 남은 chunk 처리
        if (currentChunk.length() > 0) {
            chunks.add(createDiffChunk(fileName, fileExtension, currentChunk.toString(), startLine));
        }

        return chunks;
    }

    private String extractFileName(String fileDiff) {
        String[] lines = fileDiff.split("\n");
        for (String line : lines) {
            Matcher matcher = DIFF_FILE_PATTERN.matcher(line);
            if (matcher.matches()) {
                return matcher.group(2); // b/ 이후의 파일명
            }
        }
        return "unknown";
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private int extractLineNumber(String hunkHeader) {
        // @@ -1,4 +1,4 @@ 형태에서 +1,4 부분의 1을 추출
        Pattern pattern = Pattern.compile("\\+\\d+");
        Matcher matcher = pattern.matcher(hunkHeader);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group().substring(1));
        }
        return 0;
    }

    private DiffChunk createDiffChunk(String fileName, String fileExtension, String content, int startLine) {
        return DiffChunk.builder()
                .fileName(fileName)
                .fileExtension(fileExtension)
                .content(content)
                .startLine(startLine)
                .language(detectLanguage(fileExtension))
                .hasSecurityConcerns(hasSecurityPatterns(content))
                .hasPerformanceConcerns(hasPerformancePatterns(content))
                .build();
    }

    private String detectLanguage(String extension) {
        return switch (extension.toLowerCase()) {
            case "java" -> "java";
            case "js", "jsx" -> "javascript";
            case "ts", "tsx" -> "typescript";
            case "py" -> "python";
            case "go" -> "go";
            case "rs" -> "rust";
            case "cpp", "cc", "cxx" -> "cpp";
            case "c" -> "c";
            default -> "text";
        };
    }

    private List<DiffChunk> filterWithStaticAnalysis(List<DiffChunk> chunks) {
        // 정적 분석 필터링: 명백한 문제가 있는 chunk만 남기기
        return chunks.stream()
                .filter(this::hasCodeQualityIssues)
                .toList();
    }

    private boolean hasCodeQualityIssues(DiffChunk chunk) {
        String content = chunk.getContent().toLowerCase();

        // 보안 패턴 체크
        if (hasSecurityPatterns(content)) return true;

        // 성능 패턴 체크
        if (hasPerformancePatterns(content)) return true;

        // 코드 스타일 패턴 체크
        if (hasStyleIssues(content)) return true;

        return false;
    }

    private boolean hasSecurityPatterns(String content) {
        List<String> securityPatterns = List.of(
            "password", "secret", "token", "api_key", "private_key",
            "exec(", "eval(", "system(", "shell_exec(",
            "sql", "query", "select", "insert", "update", "delete",
            "md5(", "sha1(", "base64_encode", "base64_decode"
        );

        return securityPatterns.stream().anyMatch(content::contains);
    }

    private boolean hasPerformancePatterns(String content) {
        List<String> performancePatterns = List.of(
            "for (", "while (", "foreach", ".stream()", ".map(", ".filter(",
            "n²", "o(n", "recursive", "loop", "nested",
            "database", "query", "connection", "transaction"
        );

        return performancePatterns.stream().anyMatch(content::contains);
    }

    private boolean hasStyleIssues(String content) {
        // 간단한 스타일 이슈 체크
        return content.contains("// todo") ||
               content.contains("// fixme") ||
               content.contains("system.out.println") ||
               content.length() > 1000; // 너무 긴 메서드
    }

    public boolean shouldSkipChunk(String chunk) {
        if (chunk == null || chunk.trim().isEmpty()) {
            return true;
        }

        // 빈 줄이나 주석만 있는 chunk 건너뛰기
        String[] lines = chunk.split("\n");
        boolean hasCode = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && 
                !trimmed.startsWith("//") && 
                !trimmed.startsWith("#") && 
                !trimmed.startsWith("/*") && 
                !trimmed.startsWith("*") &&
                !trimmed.startsWith("diff --git") &&
                !trimmed.startsWith("index ") &&
                !trimmed.startsWith("--- ") &&
                !trimmed.startsWith("+++ ")) {
                hasCode = true;
                break;
            }
        }

        return !hasCode;
    }

    public static class DiffChunk {
        private String fileName;
        private String fileExtension;
        private String content;
        private int startLine;
        private String language;
        private boolean hasSecurityConcerns;
        private boolean hasPerformanceConcerns;

        public static DiffChunkBuilder builder() {
            return new DiffChunkBuilder();
        }

        public static class DiffChunkBuilder {
            private DiffChunk chunk = new DiffChunk();

            public DiffChunkBuilder fileName(String fileName) {
                chunk.fileName = fileName;
                return this;
            }

            public DiffChunkBuilder fileExtension(String fileExtension) {
                chunk.fileExtension = fileExtension;
                return this;
            }

            public DiffChunkBuilder content(String content) {
                chunk.content = content;
                return this;
            }

            public DiffChunkBuilder startLine(int startLine) {
                chunk.startLine = startLine;
                return this;
            }

            public DiffChunkBuilder language(String language) {
                chunk.language = language;
                return this;
            }

            public DiffChunkBuilder hasSecurityConcerns(boolean hasSecurityConcerns) {
                chunk.hasSecurityConcerns = hasSecurityConcerns;
                return this;
            }

            public DiffChunkBuilder hasPerformanceConcerns(boolean hasPerformanceConcerns) {
                chunk.hasPerformanceConcerns = hasPerformanceConcerns;
                return this;
            }

            public DiffChunk build() {
                return chunk;
            }
        }

        // Getters
        public String getFileName() { return fileName; }
        public String getFileExtension() { return fileExtension; }
        public String getContent() { return content; }
        public int getStartLine() { return startLine; }
        public String getLanguage() { return language; }
        public boolean hasSecurityConcerns() { return hasSecurityConcerns; }
        public boolean hasPerformanceConcerns() { return hasPerformanceConcerns; }
    }
}