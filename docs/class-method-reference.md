# 📚 Gitea PR Review Bot 클래스 및 메서드 참조 가이드

## 🎯 목적
이 문서는 프로젝트의 모든 클래스와 메서드를 상세히 정리하여, 나중에 요청사항 분석 시 빠른 참조가 가능하도록 합니다.

---

## 📁 패키지 구조

```
com.gitea.prbot/
├── GiteaPrReviewBotApplication.java    # 메인 애플리케이션
├── config/                             # 설정 클래스들
├── controller/                         # 웹 컨트롤러
├── dto/                               # 데이터 전송 객체
├── model/                             # 도메인 모델
└── service/                           # 비즈니스 로직 서비스
```

---

## 🚀 메인 애플리케이션

### **GiteaPrReviewBotApplication**
```java
@SpringBootApplication
@EnableAsync
public class GiteaPrReviewBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiteaPrReviewBotApplication.class, args);
    }
}
```
- **역할**: Spring Boot 애플리케이션 진입점
- **어노테이션**: `@SpringBootApplication`, `@EnableAsync`
- **기능**: 애플리케이션 시작, 비동기 처리 활성화

---

## 🎮 Controller 계층

### **GiteaWebhookController**
```java
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class GiteaWebhookController {
    private final CodeReviewService codeReviewService;
}
```

#### **메서드 목록**

**1. handleGiteaWebhook()**
```java
@PostMapping("/gitea")
public ResponseEntity<String> handleGiteaWebhook(@RequestBody PullRequestEvent event)
```
- **기능**: Gitea 웹훅 이벤트 처리
- **파라미터**: `PullRequestEvent event` - 웹훅 페이로드
- **반환값**: `ResponseEntity<String>` - 처리 결과
- **로직**:
  - PR 열기/동기화 이벤트만 처리
  - `codeReviewService.reviewPullRequest()` 호출
  - 예외 처리 및 로깅

**2. health()**
```java
@GetMapping("/health")
public ResponseEntity<String> health()
```
- **기능**: 헬스 체크 엔드포인트
- **반환값**: `ResponseEntity<String>` - "Gitea webhook endpoint is healthy"

---

## 🔧 Service 계층

### **CodeReviewService**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewService {
    private final ChatClient chatClient;
    private final GiteaService giteaService;
    private final PromptService promptService;
    private final DiffProcessorService diffProcessorService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final int CHUNK_SIZE = 100;
}
```

#### **메서드 목록**

**1. reviewPullRequest()**
```java
public void reviewPullRequest(PullRequestEvent event)
```
- **기능**: PR 리뷰 프로세스 오케스트레이션
- **파라미터**: `PullRequestEvent event` - PR 이벤트
- **로직**:
  - Gitea에서 PR diff 조회
  - Diff를 청크로 분할
  - 각 청크별로 병렬 리뷰 실행
  - 결과 집계 및 코멘트 포스팅

**2. reviewChunk()**
```java
private ReviewResult reviewChunk(String chunk, ReviewType type, PullRequestEvent event)
```
- **기능**: 개별 청크 리뷰 실행
- **파라미터**: 
  - `String chunk` - 리뷰할 코드 청크
  - `ReviewType type` - 리뷰 타입
  - `PullRequestEvent event` - PR 이벤트
- **반환값**: `ReviewResult` - 리뷰 결과
- **로직**:
  - 프롬프트 템플릿 로딩
  - LLM 호출
  - 결과 파싱 및 반환

**3. chunkDiff()**
```java
private List<String> chunkDiff(String diff)
```
- **기능**: Diff를 청크 단위로 분할
- **파라미터**: `String diff` - Git diff 내용
- **반환값**: `List<String>` - 청크 리스트
- **로직**: 100줄 단위로 diff 분할

**4. shouldSkipChunk()**
```java
private boolean shouldSkipChunk(String chunk)
```
- **기능**: 청크 스킵 여부 판단
- **파라미터**: `String chunk` - 청크 내용
- **반환값**: `boolean` - 스킵 여부
- **로직**: 빈 청크, 주석만 있는 청크 등 스킵

**5. extractIssues()**
```java
private List<String> extractIssues(String content)
```
- **기능**: LLM 응답에서 이슈 추출
- **파라미터**: `String content` - LLM 응답
- **반환값**: `List<String>` - 이슈 리스트
- **로직**: 정규표현식으로 이슈 파싱

**6. postReviewComments()**
```java
private void postReviewComments(PullRequestEvent event, List<ReviewResult> results)
```
- **기능**: 리뷰 결과를 Gitea에 포스팅
- **파라미터**:
  - `PullRequestEvent event` - PR 이벤트
  - `List<ReviewResult> results` - 리뷰 결과 리스트
- **로직**: GiteaService를 통해 코멘트 생성

---

### **GiteaService**
```java
@Service
@Slf4j
public class GiteaService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String giteaBaseUrl;
    private final String giteaToken;
}
```

#### **메서드 목록**

**1. getPullRequestDiff()**
```java
public String getPullRequestDiff(String repositoryFullName, int prNumber)
```
- **기능**: PR의 diff 내용 조회
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `int prNumber` - PR 번호
- **반환값**: `String` - diff 내용
- **API**: `GET /api/v1/repos/{owner}/{repo}/pulls/{index}.diff`

**2. createReviewComment()**
```java
public void createReviewComment(String repositoryFullName, int prNumber, String comment)
```
- **기능**: PR에 일반 코멘트 생성
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `int prNumber` - PR 번호
  - `String comment` - 코멘트 내용
- **API**: `POST /api/v1/repos/{owner}/{repo}/issues/{index}/comments`

**3. createReview()**
```java
public void createReview(String repositoryFullName, int prNumber, String body, String reviewType)
```
- **기능**: PR에 리뷰 생성
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `int prNumber` - PR 번호
  - `String body` - 리뷰 내용
  - `String reviewType` - 리뷰 타입 (APPROVE, REQUEST_CHANGES, COMMENT)
- **API**: `POST /api/v1/repos/{owner}/{repo}/pulls/{index}/reviews`

**4. addLineComment()**
```java
public void addLineComment(String repositoryFullName, int prNumber, String commitSha, String path, int line, String comment)
```
- **기능**: 특정 라인에 코멘트 추가
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `int prNumber` - PR 번호
  - `String commitSha` - 커밋 SHA
  - `String path` - 파일 경로
  - `int line` - 라인 번호
  - `String comment` - 코멘트 내용
- **API**: `POST /api/v1/repos/{owner}/{repo}/pulls/{index}/reviews`

**5. isRepositoryAccessible()**
```java
public boolean isRepositoryAccessible(String repositoryFullName)
```
- **기능**: 리포지토리 접근 가능 여부 확인
- **파라미터**: `String repositoryFullName` - 리포지토리 전체 이름
- **반환값**: `boolean` - 접근 가능 여부
- **API**: `GET /api/v1/repos/{owner}/{repo}`

**6. getPullRequestInfo()**
```java
public JsonNode getPullRequestInfo(String repositoryFullName, int prNumber)
```
- **기능**: PR 상세 정보 조회
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `int prNumber` - PR 번호
- **반환값**: `JsonNode` - PR 정보
- **API**: `GET /api/v1/repos/{owner}/{repo}/pulls/{index}`

**7. addPullRequestComment()**
```java
public void addPullRequestComment(String repositoryFullName, Long prNumber, String comment)
```
- **기능**: PR에 코멘트 추가 (Long 타입 오버로드)
- **파라미터**:
  - `String repositoryFullName` - 리포지토리 전체 이름
  - `Long prNumber` - PR 번호
  - `String comment` - 코멘트 내용

**8. createHeaders()**
```java
private HttpHeaders createHeaders()
```
- **기능**: Gitea API 호출용 헤더 생성
- **반환값**: `HttpHeaders` - 인증 헤더 포함
- **로직**: Authorization 토큰, Accept 헤더 설정

---

### **DiffProcessorService**
```java
@Service
@Slf4j
public class DiffProcessorService {
    @Value("${bot.review.chunk-size:100}")
    private int chunkSize;
    
    @Value("${bot.review.enable-static-analysis:true}")
    private boolean enableStaticAnalysis;
    
    private static final Pattern DIFF_FILE_PATTERN = Pattern.compile("^diff --git a/(.*) b/(.*)$");
    private static final Pattern HUNK_HEADER_PATTERN = Pattern.compile("^@@\\s*-\\d+,?\\d*\\s*\\+\\d+,?\\d*\\s*@@");
}
```

#### **메서드 목록**

**1. processDiff()**
```java
public List<DiffChunk> processDiff(String diff)
```
- **기능**: Diff 전체 처리 및 청크 생성
- **파라미터**: `String diff` - Git diff 내용
- **반환값**: `List<DiffChunk>` - 처리된 청크 리스트
- **로직**:
  - 파일별 diff 분할
  - 각 파일별 청크 생성
  - 정적 분석 필터링 (옵션)

**2. splitDiffByFiles()**
```java
private String[] splitDiffByFiles(String diff)
```
- **기능**: Diff를 파일별로 분할
- **파라미터**: `String diff` - Git diff 내용
- **반환값**: `String[]` - 파일별 diff 배열
- **로직**: "diff --git" 패턴으로 파일 분할

**3. processFileDiff()**
```java
private List<DiffChunk> processFileDiff(String fileDiff)
```
- **기능**: 개별 파일 diff 처리
- **파라미터**: `String fileDiff` - 파일 diff 내용
- **반환값**: `List<DiffChunk>` - 파일의 청크 리스트
- **로직**:
  - 파일명 추출
  - hunk 헤더 파싱
  - 청크 크기 제한 적용

**4. extractFileName()**
```java
private String extractFileName(String fileDiff)
```
- **기능**: diff에서 파일명 추출
- **파라미터**: `String fileDiff` - 파일 diff 내용
- **반환값**: `String` - 파일명
- **로직**: "diff --git a/... b/..." 패턴에서 파일명 추출

**5. getFileExtension()**
```java
private String getFileExtension(String fileName)
```
- **기능**: 파일명에서 확장자 추출
- **파라미터**: `String fileName` - 파일명
- **반환값**: `String` - 확장자
- **로직**: 마지막 점 이후 문자열 추출

**6. extractLineNumber()**
```java
private int extractLineNumber(String hunkHeader)
```
- **기능**: hunk 헤더에서 라인 번호 추출
- **파라미터**: `String hunkHeader` - hunk 헤더
- **반환값**: `int` - 라인 번호
- **로직**: "@@ -1,4 +1,4 @@" 패턴에서 +1,4 부분의 1 추출

**7. createDiffChunk()**
```java
private DiffChunk createDiffChunk(String fileName, String fileExtension, String content, int startLine)
```
- **기능**: DiffChunk 객체 생성
- **파라미터**:
  - `String fileName` - 파일명
  - `String fileExtension` - 확장자
  - `String content` - 청크 내용
  - `int startLine` - 시작 라인
- **반환값**: `DiffChunk` - 생성된 청크 객체
- **로직**: Builder 패턴으로 DiffChunk 생성

**8. detectLanguage()**
```java
private String detectLanguage(String extension)
```
- **기능**: 확장자로 프로그래밍 언어 감지
- **파라미터**: `String extension` - 파일 확장자
- **반환값**: `String` - 언어명
- **지원 언어**: java, javascript, typescript, python, go, rust, cpp, c

**9. filterWithStaticAnalysis()**
```java
private List<DiffChunk> filterWithStaticAnalysis(List<DiffChunk> chunks)
```
- **기능**: 정적 분석으로 청크 필터링
- **파라미터**: `List<DiffChunk> chunks` - 원본 청크 리스트
- **반환값**: `List<DiffChunk>` - 필터링된 청크 리스트
- **로직**: 코드 품질 이슈가 있는 청크만 남김

**10. hasCodeQualityIssues()**
```java
private boolean hasCodeQualityIssues(DiffChunk chunk)
```
- **기능**: 청크에 코드 품질 이슈 여부 확인
- **파라미터**: `DiffChunk chunk` - 청크 객체
- **반환값**: `boolean` - 이슈 존재 여부
- **로직**: 보안, 성능, 스타일 패턴 체크

**11. hasSecurityPatterns()**
```java
private boolean hasSecurityPatterns(String content)
```
- **기능**: 보안 관련 패턴 감지
- **파라미터**: `String content` - 코드 내용
- **반환값**: `boolean` - 보안 패턴 존재 여부
- **패턴**: password, secret, token, exec(), eval(), sql 등

**12. hasPerformancePatterns()**
```java
private boolean hasPerformancePatterns(String content)
```
- **기능**: 성능 관련 패턴 감지
- **파라미터**: `String content` - 코드 내용
- **반환값**: `boolean` - 성능 패턴 존재 여부
- **패턴**: for, while, stream(), map(), filter(), database 등

**13. hasStyleIssues()**
```java
private boolean hasStyleIssues(String content)
```
- **기능**: 코드 스타일 이슈 감지
- **파라미터**: `String content` - 코드 내용
- **반환값**: `boolean` - 스타일 이슈 존재 여부
- **패턴**: // todo, // fixme, system.out.println, 긴 메서드 등

#### **내부 클래스: DiffChunk**
```java
public static class DiffChunk {
    private String fileName;
    private String fileExtension;
    private String content;
    private int startLine;
    private String language;
    private boolean hasSecurityConcerns;
    private boolean hasPerformanceConcerns;
    
    // Builder 패턴 구현
    public static DiffChunkBuilder builder()
    
    // Getter 메서드들
    public String getFileName()
    public String getFileExtension()
    public String getContent()
    public int getStartLine()
    public String getLanguage()
    public boolean hasSecurityConcerns()
    public boolean hasPerformanceConcerns()
}
```

---

### **PromptService**
```java
@Service
@Slf4j
public class PromptService {
    private final Map<ReviewType, String> promptTemplates = new EnumMap<>(ReviewType.class);
}
```

#### **메서드 목록**

**1. PromptService()**
```java
public PromptService() {
    loadPromptTemplates();
}
```
- **기능**: 생성자에서 프롬프트 템플릿 로딩
- **로직**: `loadPromptTemplates()` 호출

**2. loadPromptTemplates()**
```java
private void loadPromptTemplates()
```
- **기능**: 모든 프롬프트 템플릿 로딩
- **로직**:
  - SECURITY: prompts/security/security-review.md
  - PERFORMANCE: prompts/performance/performance-review.md
  - STYLE: prompts/style/code-style-review.md
  - GENERAL: prompts/general/general-review.md

**3. loadPrompt()**
```java
private String loadPrompt(String path) throws IOException
```
- **기능**: 개별 프롬프트 파일 로딩
- **파라미터**: `String path` - 프롬프트 파일 경로
- **반환값**: `String` - 프롬프트 내용
- **로직**: ClassPathResource로 파일 읽기

**4. getPrompt()**
```java
public String getPrompt(ReviewType type)
```
- **기능**: 리뷰 타입별 프롬프트 반환
- **파라미터**: `ReviewType type` - 리뷰 타입
- **반환값**: `String` - 프롬프트 템플릿
- **기본값**: "코드를 검토해 주세요."

---

## ⚙️ Configuration 계층

### **WebConfig**
```java
@Configuration
public class WebConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```
- **역할**: 웹 관련 Bean 설정
- **Bean**: RestTemplate - HTTP 클라이언트

### **OllamaConfig**
```java
@Configuration
public class OllamaConfig {
    // ChatClient Bean 설정 (구체적 구현은 확인 필요)
}
```
- **역할**: Ollama LLM 클라이언트 설정
- **Bean**: ChatClient - LLM 통신 클라이언트

---

## 📦 DTO 계층

### **PullRequestEvent**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestEvent {
    private String action;
    
    @JsonProperty("pull_request")
    private PullRequest pullRequest;
    
    private Repository repository;
}
```

#### **중첩 클래스들**

**1. PullRequest**
```java
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
}
```

**2. Head**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class Head {
    private String ref;
    private String sha;
}
```

**3. Base**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class Base {
    private String ref;
    private String sha;
}
```

**4. Repository**
```java
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
}
```

**5. Owner**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class Owner {
    private String login;
}
```

---

## 🏷️ Model 계층

### **ReviewResult**
```java
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
```
- **역할**: 코드 리뷰 결과 데이터 모델
- **필드**:
  - `reviewType`: 리뷰 타입 (SECURITY, PERFORMANCE, STYLE, GENERAL)
  - `content`: LLM 응답 내용
  - `overallGrade`: 전체 등급 (A/B/C/D)
  - `issues`: 발견된 이슈 리스트
  - `repositoryName`: 리포지토리 이름
  - `pullRequestNumber`: PR 번호

### **ReviewType**
```java
public enum ReviewType {
    SECURITY,
    PERFORMANCE,
    STYLE,
    GENERAL
}
```
- **역할**: 리뷰 타입 열거형
- **타입**:
  - `SECURITY`: 보안 검토
  - `PERFORMANCE`: 성능 검토
  - `STYLE`: 코드 스타일 검토
  - `GENERAL`: 일반 검토

---

## 🔄 메서드 호출 플로우

### **1. 웹훅 수신 플로우**
```
Gitea → GiteaWebhookController.handleGiteaWebhook()
     → CodeReviewService.reviewPullRequest()
```

### **2. 코드 리뷰 플로우**
```
CodeReviewService.reviewPullRequest()
├── GiteaService.getPullRequestDiff()
├── DiffProcessorService.processDiff()
│   ├── splitDiffByFiles()
│   ├── processFileDiff()
│   └── filterWithStaticAnalysis()
├── PromptService.getPrompt()
├── ChatClient.call() (병렬)
└── GiteaService.createReviewComment()
```

### **3. 병렬 처리 플로우**
```
각 DiffChunk × 4개 ReviewType
├── CodeReviewService.reviewChunk()
│   ├── PromptService.getPrompt()
│   ├── ChatClient.call()
│   └── extractIssues()
└── CompletableFuture.allOf()
```

---

## 📊 성능 특성

### **스레드 풀 설정**
- **크기**: 4개 고정 스레드
- **사용처**: CodeReviewService.executorService

### **청크 처리**
- **기본 크기**: 100줄
- **설정**: `bot.review.chunk-size`

### **정적 분석**
- **활성화**: `bot.review.enable-static-analysis`
- **필터링**: 보안/성능/스타일 패턴 기반

---

이 문서는 모든 클래스와 메서드의 상세 정보를 제공하여, 
나중에 요청사항 분석 시 빠른 참조가 가능하도록 합니다.
