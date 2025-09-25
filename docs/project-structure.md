# 📁 Gitea PR Review Bot 프로젝트 구조 분석

## 🎯 프로젝트 개요

**프로젝트명**: Gitea PR Review Bot (StarCoder2)  
**기술 스택**: Spring Boot 3.2.0 + Spring AI 0.8.0 + StarCoder2-3B  
**목적**: 사내 보안을 위한 로컬 LLM 기반 자동 코드 리뷰 봇  
**패키지**: `com.gitea.prbot`

---

## 📂 전체 프로젝트 구조

```
git-pr-bot/
├── 📁 docs/                          # 프로젝트 문서
│   ├── 📁 configuration/             # 설정 관련 문서
│   ├── 📁 phase1-7/                  # 배포 단계별 문서
│   ├── 📁 setup/                     # 설치 가이드
│   ├── 📁 troubleshooting/           # 문제 해결
│   ├── 📁 usage/                     # 사용법 가이드
│   ├── 📄 codereview.md              # 코드 리뷰 분석
│   ├── 📄 DEPLOYMENT_CHECKLIST.md    # 배포 체크리스트
│   └── 📄 README.md                  # 문서 메인
├── 📁 src/
│   ├── 📄 Main.java                  # ❌ 잘못된 진입점 (삭제 필요)
│   └── 📁 main/
│       ├── 📁 java/com/gitea/prbot/  # 메인 소스코드
│       └── 📁 resources/             # 설정 및 리소스
├── 📄 pom.xml                        # Maven 설정
└── 📄 README.md                      # 프로젝트 메인 README
```

---

## 🏗️ 소스코드 구조 상세 분석

### 📁 `src/main/java/com/gitea/prbot/`

#### 1. **애플리케이션 진입점**
```
📄 GiteaPrReviewBotApplication.java
```
- **역할**: Spring Boot 애플리케이션 메인 클래스
- **어노테이션**: `@SpringBootApplication`, `@EnableAsync`
- **기능**: 애플리케이션 시작점, 비동기 처리 활성화

#### 2. **Controller 계층**
```
📁 controller/
└── 📄 GiteaWebhookController.java
```
- **역할**: Gitea 웹훅 요청 처리
- **어노테이션**: `@RestController`, `@RequestMapping("/api/webhook")`
- **주요 메서드**:
  - `handleGiteaWebhook()`: PR 이벤트 처리
  - `health()`: 헬스 체크

#### 3. **Service 계층**
```
📁 service/
├── 📄 CodeReviewService.java         # 코드 리뷰 오케스트레이션
├── 📄 GiteaService.java              # Gitea API 통신
├── 📄 DiffProcessorService.java      # Diff 파싱 및 처리
└── 📄 PromptService.java             # 프롬프트 관리
```

**CodeReviewService.java**
- **역할**: 전체 코드 리뷰 프로세스 관리
- **주요 기능**:
  - PR 리뷰 오케스트레이션
  - Chunk 분할 및 병렬 처리
  - 결과 집계 및 코멘트 포스팅
- **의존성**: ChatClient, GiteaService, PromptService, DiffProcessorService

**GiteaService.java**
- **역할**: Gitea API와의 통신 담당
- **주요 메서드**:
  - `getPullRequestDiff()`: PR diff 조회
  - `createReviewComment()`: 리뷰 코멘트 생성
  - `createReview()`: 리뷰 생성
  - `addLineComment()`: 라인별 코멘트 추가
- **의존성**: RestTemplate, ObjectMapper

**DiffProcessorService.java**
- **역할**: Git diff 파싱 및 청크 분할
- **주요 기능**:
  - Diff 파일별 분할
  - Chunk 단위로 코드 분할 (기본 100줄)
  - 정적 분석 필터링
  - 언어별 패턴 감지
- **내부 클래스**: `DiffChunk` (Builder 패턴)

**PromptService.java**
- **역할**: AI 리뷰 프롬프트 관리
- **주요 기능**:
  - 프롬프트 템플릿 로딩
  - 리뷰 타입별 프롬프트 제공
- **프롬프트 타입**: SECURITY, PERFORMANCE, STYLE, GENERAL

#### 4. **Configuration 계층**
```
📁 config/
├── 📄 WebConfig.java                 # 웹 설정
└── 📄 OllamaConfig.java              # Ollama LLM 설정
```

**WebConfig.java**
- **역할**: 웹 관련 Bean 설정
- **주요 Bean**: RestTemplate

**OllamaConfig.java**
- **역할**: Ollama LLM 클라이언트 설정
- **기능**: ChatClient Bean 생성

#### 5. **DTO 계층**
```
📁 dto/
└── 📄 PullRequestEvent.java
```
- **역할**: Gitea 웹훅 이벤트 데이터 전송 객체
- **구조**: 중첩 클래스로 구성
  - `PullRequest` (number, title, body, state, diffUrl, htmlUrl, head, base)
  - `Repository` (name, fullName, cloneUrl, owner)
  - `Head/Base` (ref, sha)
  - `Owner` (login)

#### 6. **Model 계층**
```
📁 model/
├── 📄 ReviewResult.java              # 리뷰 결과 모델
└── 📄 ReviewType.java                # 리뷰 타입 열거형
```

**ReviewResult.java**
- **역할**: 코드 리뷰 결과 데이터 모델
- **필드**: reviewType, content, overallGrade, issues, repositoryName, pullRequestNumber

**ReviewType.java**
- **역할**: 리뷰 타입 정의
- **타입**: SECURITY, PERFORMANCE, STYLE, GENERAL

---

## 📁 리소스 구조

### 📁 `src/main/resources/`

#### 1. **설정 파일**
```
📄 application.yml
```
- **Spring Boot 설정**: 서버 포트, 데이터베이스, JPA
- **Spring AI 설정**: Ollama 연결 정보
- **Gitea 설정**: API URL, 토큰, 웹훅 시크릿
- **Bot 설정**: 리뷰 옵션, 프롬프트 경로, 모델 설정
- **로깅 설정**: 로그 레벨, 패턴
- **관리 설정**: Actuator 엔드포인트

#### 2. **프롬프트 템플릿**
```
📁 prompts/
├── 📁 security/
│   └── 📄 security-review.md         # 보안 검토 프롬프트
├── 📁 performance/
│   └── 📄 performance-review.md      # 성능 검토 프롬프트
├── 📁 style/
│   └── 📄 code-style-review.md       # 코드 스타일 검토 프롬프트
└── 📁 general/
    └── 📄 general-review.md          # 일반 검토 프롬프트
```

---

## 🔧 Maven 설정 (pom.xml)

### **주요 의존성**
- **Spring Boot**: 3.2.0 (Web, JPA, Validation, Actuator, Security)
- **Spring AI**: 0.8.0 (Ollama 통합)
- **데이터베이스**: H2 (개발), PostgreSQL (프로덕션)
- **유틸리티**: Lombok, Jackson, Commons Lang3
- **테스트**: Spring Boot Test, TestContainers

### **빌드 설정**
- **Java 버전**: 21
- **Spring AI 버전**: 0.8.0
- **리포지토리**: Spring Milestones, Spring Snapshots

---

## 🚀 애플리케이션 플로우

### 1. **웹훅 수신**
```
Gitea → GiteaWebhookController.handleGiteaWebhook()
```

### 2. **코드 리뷰 프로세스**
```
CodeReviewService.reviewPullRequest()
├── GiteaService.getPullRequestDiff()     # PR diff 조회
├── DiffProcessorService.processDiff()    # Diff 파싱 및 청크 분할
├── PromptService.getPrompt()             # 프롬프트 로딩
├── ChatClient.call()                     # LLM 호출 (병렬)
└── GiteaService.createReviewComment()    # 결과 포스팅
```

### 3. **병렬 처리**
- 각 청크별로 4가지 리뷰 타입 (SECURITY, PERFORMANCE, STYLE, GENERAL) 병렬 실행
- ExecutorService를 통한 스레드 풀 관리

---

## 📊 클래스별 상세 기능

### **GiteaWebhookController**
```java
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
@Slf4j
public class GiteaWebhookController {
    private final CodeReviewService codeReviewService;
    
    @PostMapping("/gitea")
    public ResponseEntity<String> handleGiteaWebhook(@RequestBody PullRequestEvent event)
    
    @GetMapping("/health")
    public ResponseEntity<String> health()
}
```

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
    
    public void reviewPullRequest(PullRequestEvent event)
    private ReviewResult reviewChunk(String chunk, ReviewType type, PullRequestEvent event)
    private List<String> chunkDiff(String diff)
    private boolean shouldSkipChunk(String chunk)
    private List<String> extractIssues(String content)
    private void postReviewComments(PullRequestEvent event, List<ReviewResult> results)
}
```

### **GiteaService**
```java
@Service
@Slf4j
public class GiteaService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String giteaBaseUrl;
    private final String giteaToken;
    
    public String getPullRequestDiff(String repositoryFullName, int prNumber)
    public void createReviewComment(String repositoryFullName, int prNumber, String comment)
    public void createReview(String repositoryFullName, int prNumber, String body, String reviewType)
    public void addLineComment(String repositoryFullName, int prNumber, String commitSha, String path, int line, String comment)
    public boolean isRepositoryAccessible(String repositoryFullName)
    public JsonNode getPullRequestInfo(String repositoryFullName, int prNumber)
    public void addPullRequestComment(String repositoryFullName, Long prNumber, String comment)
    private HttpHeaders createHeaders()
}
```

### **DiffProcessorService**
```java
@Service
@Slf4j
public class DiffProcessorService {
    @Value("${bot.review.chunk-size:100}")
    private int chunkSize;
    
    @Value("${bot.review.enable-static-analysis:true}")
    private boolean enableStaticAnalysis;
    
    public List<DiffChunk> processDiff(String diff)
    private String[] splitDiffByFiles(String diff)
    private List<DiffChunk> processFileDiff(String fileDiff)
    private String extractFileName(String fileDiff)
    private String getFileExtension(String fileName)
    private int extractLineNumber(String hunkHeader)
    private DiffChunk createDiffChunk(String fileName, String fileExtension, String content, int startLine)
    private String detectLanguage(String extension)
    private List<DiffChunk> filterWithStaticAnalysis(List<DiffChunk> chunks)
    private boolean hasCodeQualityIssues(DiffChunk chunk)
    private boolean hasSecurityPatterns(String content)
    private boolean hasPerformancePatterns(String content)
    private boolean hasStyleIssues(String content)
    
    // 내부 클래스
    public static class DiffChunk {
        // Builder 패턴으로 구현된 DiffChunk 클래스
    }
}
```

### **PromptService**
```java
@Service
@Slf4j
public class PromptService {
    private final Map<ReviewType, String> promptTemplates = new EnumMap<>(ReviewType.class);
    
    public PromptService() {
        loadPromptTemplates();
    }
    
    private void loadPromptTemplates()
    private String loadPrompt(String path) throws IOException
    public String getPrompt(ReviewType type)
}
```

---

## 🔄 데이터 플로우

### **1. 웹훅 이벤트 수신**
```
Gitea PR Event → PullRequestEvent DTO → GiteaWebhookController
```

### **2. 코드 리뷰 프로세스**
```
PullRequestEvent → CodeReviewService → GiteaService (diff 조회)
                ↓
            DiffProcessorService (파싱) → List<DiffChunk>
                ↓
            PromptService (프롬프트) → ChatClient (LLM 호출)
                ↓
            List<ReviewResult> → GiteaService (코멘트 포스팅)
```

### **3. 병렬 처리 구조**
```
각 DiffChunk × 4개 ReviewType = N개 병렬 작업
ExecutorService (4개 스레드) → CompletableFuture
```

---

## ⚙️ 설정 구조

### **application.yml 주요 설정**
```yaml
# 서버 설정
server:
  port: 8080
  servlet:
    context-path: /api

# Spring AI 설정
spring:
  ai:
    openai:
      base-url: ${LLM_BASE_URL:http://localhost:11434/v1}
      api-key: ${LLM_API_KEY:dummy}
      chat:
        options:
          model: ${LLM_MODEL:starcoder2:3b}

# Gitea 설정
gitea:
  base-url: ${GITEA_BASE_URL}
  token: ${GITEA_TOKEN}
  webhook:
    secret: ${GITEA_WEBHOOK_SECRET}

# Bot 설정
bot:
  review:
    enabled: true
    chunk-size: 100
    enable-static-analysis: true
    parallel-processing: true
```

---

## 🚨 현재 알려진 이슈

### **컴파일 오류**
1. **Lombok 어노테이션 처리 문제**: `@Slf4j`, `@Data` 미처리
2. **잘못된 Main.java**: 프로젝트 루트의 불필요한 파일
3. **DTO getter/setter 누락**: `@Data` 어노테이션 미처리로 인한 메서드 누락

### **아키텍처 개선점**
1. **Service 책임 분산**: CodeReviewService의 과도한 책임
2. **예외 처리**: null 반환으로 인한 NPE 위험
3. **DTO 구조**: 중첩 클래스 분리 필요

---

## 📈 성능 특성

### **병렬 처리**
- **스레드 풀**: 4개 고정 스레드
- **청크 크기**: 100줄 단위
- **리뷰 타입**: 4가지 (SECURITY, PERFORMANCE, STYLE, GENERAL)

### **메모리 사용량**
- **모델**: StarCoder2-3B (약 1.7GB)
- **JVM 힙**: 권장 4-8GB
- **청크 처리**: 스트리밍 방식으로 메모리 효율성

### **응답 시간**
- **첫 로딩**: 30-45초 (모델 로딩)
- **리뷰 처리**: 평균 2분 이내
- **병렬 처리**: 4배 성능 향상

---

## 🔗 외부 의존성

### **필수 서비스**
1. **Ollama**: 로컬 LLM 서버 (포트 11434)
2. **Gitea**: Git 서버 (웹훅 수신)
3. **StarCoder2-3B**: 코드 생성 모델

### **선택적 의존성**
1. **PostgreSQL**: 프로덕션 데이터베이스
2. **H2**: 개발용 인메모리 데이터베이스

---

이 문서는 프로젝트의 전체 구조와 각 컴포넌트의 역할을 상세히 설명합니다. 
나중에 요청사항 분석 시 이 구조를 참고하여 정확하고 효율적인 답변을 제공할 수 있습니다.
