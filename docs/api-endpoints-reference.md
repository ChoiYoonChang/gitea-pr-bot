# 🌐 Gitea PR Review Bot API 엔드포인트 참조 가이드

## 🎯 목적
이 문서는 프로젝트의 모든 API 엔드포인트와 외부 API 연동을 상세히 정리하여, API 관련 요청사항 분석 시 빠른 참조가 가능하도록 합니다.

---

## 🏠 내부 API 엔드포인트

### **기본 URL 구조**
```
Base URL: http://localhost:8080/api
Context Path: /api
```

---

## 🎮 GiteaWebhookController

### **1. Gitea 웹훅 수신**
```http
POST /api/webhook/gitea
```

#### **요청 정보**
- **Content-Type**: `application/json`
- **Body**: `PullRequestEvent` JSON
- **인증**: Gitea 웹훅 시크릿 검증

#### **요청 예시**
```json
{
  "action": "opened",
  "pull_request": {
    "number": 123,
    "title": "Add new feature",
    "body": "This PR adds a new feature",
    "state": "open",
    "diff_url": "https://gitea.example.com/repo/pulls/123.diff",
    "html_url": "https://gitea.example.com/repo/pulls/123",
    "head": {
      "ref": "feature-branch",
      "sha": "abc123def456"
    },
    "base": {
      "ref": "main",
      "sha": "def456ghi789"
    }
  },
  "repository": {
    "name": "my-repo",
    "full_name": "owner/my-repo",
    "clone_url": "https://gitea.example.com/owner/my-repo.git",
    "owner": {
      "login": "owner"
    }
  }
}
```

#### **응답 정보**
- **성공**: `200 OK`
- **실패**: `500 Internal Server Error`
- **Body**: 처리 결과 메시지

#### **응답 예시**
```json
"PR review initiated successfully"
```

#### **처리 로직**
1. 웹훅 페이로드 검증
2. PR 열기/동기화 이벤트만 처리
3. `CodeReviewService.reviewPullRequest()` 호출
4. 비동기 처리로 즉시 응답

---

### **2. 헬스 체크**
```http
GET /api/webhook/health
```

#### **요청 정보**
- **인증**: 불필요
- **파라미터**: 없음

#### **응답 정보**
- **성공**: `200 OK`
- **Body**: `"Gitea webhook endpoint is healthy"`

#### **사용 목적**
- 서비스 가용성 확인
- 로드 밸런서 헬스 체크
- 모니터링 시스템 연동

---

## 🔧 Spring Boot Actuator 엔드포인트

### **기본 URL**
```
Base URL: http://localhost:8080/api/actuator
```

---

### **1. 헬스 체크**
```http
GET /api/actuator/health
```

#### **응답 예시**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 378123456789,
        "threshold": 10485760
      }
    }
  }
}
```

---

### **2. 애플리케이션 정보**
```http
GET /api/actuator/info
```

#### **응답 예시**
```json
{
  "app": {
    "name": "gitea-pr-review-bot",
    "version": "0.0.1-SNAPSHOT"
  },
  "build": {
    "version": "0.0.1-SNAPSHOT"
  }
}
```

---

### **3. 메트릭**
```http
GET /api/actuator/metrics
```

#### **응답 예시**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "review.processing.time"
  ]
}
```

---

### **4. Prometheus 메트릭**
```http
GET /api/actuator/prometheus
```

#### **응답 예시**
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Survivor Space"} 1.048576E7

# HELP review_processing_time_seconds Time taken to process reviews
# TYPE review_processing_time_seconds summary
review_processing_time_seconds_count 42
review_processing_time_seconds_sum 125.5
```

---

## 🐙 Gitea API 연동

### **GiteaService에서 사용하는 API**

---

### **1. PR Diff 조회**
```http
GET {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/pulls/{index}.diff
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Accept**: `application/json`
- **파라미터**:
  - `owner`: 리포지토리 소유자
  - `repo`: 리포지토리 이름
  - `index`: PR 번호

#### **응답 정보**
- **성공**: `200 OK`
- **Body**: Git diff 형식의 텍스트

#### **사용 메서드**
```java
public String getPullRequestDiff(String repositoryFullName, int prNumber)
```

---

### **2. PR 정보 조회**
```http
GET {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/pulls/{index}
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Accept**: `application/json`

#### **응답 예시**
```json
{
  "id": 123,
  "number": 123,
  "title": "Add new feature",
  "body": "This PR adds a new feature",
  "state": "open",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z",
  "head": {
    "ref": "feature-branch",
    "sha": "abc123def456"
  },
  "base": {
    "ref": "main",
    "sha": "def456ghi789"
  }
}
```

#### **사용 메서드**
```java
public JsonNode getPullRequestInfo(String repositoryFullName, int prNumber)
```

---

### **3. 리포지토리 접근성 확인**
```http
GET {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Accept**: `application/json`

#### **응답 정보**
- **성공**: `200 OK` (접근 가능)
- **실패**: `404 Not Found` (접근 불가)

#### **사용 메서드**
```java
public boolean isRepositoryAccessible(String repositoryFullName)
```

---

### **4. PR 코멘트 생성**
```http
POST {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/issues/{index}/comments
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Content-Type**: `application/json`
- **Body**:
```json
{
  "body": "코드 리뷰 결과입니다.\n\n**보안 이슈**:\n- 하드코딩된 API 키 발견"
}
```

#### **응답 정보**
- **성공**: `201 Created`
- **Body**: 생성된 코멘트 정보

#### **사용 메서드**
```java
public void createReviewComment(String repositoryFullName, int prNumber, String comment)
```

---

### **5. PR 리뷰 생성**
```http
POST {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/pulls/{index}/reviews
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Content-Type**: `application/json`
- **Body**:
```json
{
  "body": "전체적인 코드 리뷰 결과",
  "event": "COMMENT"
}
```

#### **리뷰 이벤트 타입**
- `APPROVE`: 승인
- `REQUEST_CHANGES`: 변경 요청
- `COMMENT`: 일반 코멘트

#### **사용 메서드**
```java
public void createReview(String repositoryFullName, int prNumber, String body, String reviewType)
```

---

### **6. 라인별 코멘트 추가**
```http
POST {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/pulls/{index}/reviews
```

#### **요청 정보**
- **인증**: `Authorization: token {GITEA_TOKEN}`
- **Content-Type**: `application/json`
- **Body**:
```json
{
  "body": "Code review comment",
  "event": "COMMENT",
  "comments": [
    {
      "path": "src/main/java/Example.java",
      "body": "이 라인에 보안 이슈가 있습니다",
      "new_position": 15
    }
  ]
}
```

#### **사용 메서드**
```java
public void addLineComment(String repositoryFullName, int prNumber, String commitSha, String path, int line, String comment)
```

---

## 🤖 Ollama API 연동

### **Ollama Chat API**
```http
POST {LLM_BASE_URL}/chat/completions
```

#### **요청 정보**
- **Content-Type**: `application/json`
- **Body**:
```json
{
  "model": "starcoder2:3b",
  "messages": [
    {
      "role": "user",
      "content": "다음 코드를 보안 관점에서 검토해주세요:\n\n```java\npublic class Example {\n    private String apiKey = \"sk-123456789\";\n}\n```"
    }
  ],
  "temperature": 0.1,
  "max_tokens": 1000
}
```

#### **응답 예시**
```json
{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "starcoder2:3b",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "**보안 이슈 발견**:\n\n1. **하드코딩된 API 키**: `apiKey` 필드에 API 키가 하드코딩되어 있습니다.\n2. **민감정보 노출**: 이는 보안상 매우 위험합니다.\n\n**권장사항**:\n- 환경 변수나 설정 파일을 사용하세요.\n- API 키를 암호화하여 저장하세요."
      }
    }
  ],
  "usage": {
    "prompt_tokens": 50,
    "completion_tokens": 100,
    "total_tokens": 150
  }
}
```

#### **사용 클래스**
```java
ChatClient chatClient; // Spring AI ChatClient
```

---

## 📊 API 호출 플로우

### **1. 웹훅 수신 플로우**
```
Gitea → POST /api/webhook/gitea
     → GiteaWebhookController.handleGiteaWebhook()
     → CodeReviewService.reviewPullRequest()
```

### **2. 코드 리뷰 플로우**
```
CodeReviewService.reviewPullRequest()
├── GET {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/pulls/{index}.diff
├── DiffProcessorService.processDiff()
├── PromptService.getPrompt()
├── POST {LLM_BASE_URL}/chat/completions (병렬)
└── POST {GITEA_BASE_URL}/api/v1/repos/{owner}/{repo}/issues/{index}/comments
```

### **3. 병렬 처리 플로우**
```
각 DiffChunk × 4개 ReviewType
├── POST {LLM_BASE_URL}/chat/completions (4개 동시)
├── 응답 파싱 및 이슈 추출
└── 결과 집계
```

---

## 🔐 인증 및 보안

### **Gitea API 인증**
```http
Authorization: token {GITEA_TOKEN}
```
- **토큰 생성**: Gitea Settings → Applications → Generate New Token
- **필수 권한**: `repo`, `write:repository`

### **웹훅 보안**
- **시크릿 검증**: `GITEA_WEBHOOK_SECRET`
- **페이로드 검증**: HMAC-SHA256 서명 확인

### **Ollama API**
- **인증**: 불필요 (로컬 서버)
- **네트워크**: localhost만 접근 가능

---

## 📈 성능 특성

### **응답 시간**
- **웹훅 수신**: 즉시 응답 (비동기 처리)
- **코드 리뷰**: 평균 2분 (PR 크기에 따라 변동)
- **LLM 호출**: 평균 30초 (청크당)

### **동시 처리**
- **스레드 풀**: 4개 고정 스레드
- **병렬 LLM 호출**: 최대 4개 동시
- **청크 처리**: 100줄 단위

### **리소스 사용량**
- **메모리**: 청크당 약 10MB
- **네트워크**: LLM 호출당 약 1KB
- **CPU**: 병렬 처리로 4배 효율

---

## 🚨 에러 처리

### **HTTP 상태 코드**
- **200 OK**: 성공
- **400 Bad Request**: 잘못된 요청
- **401 Unauthorized**: 인증 실패
- **404 Not Found**: 리소스 없음
- **500 Internal Server Error**: 서버 오류

### **에러 응답 예시**
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error processing webhook: Failed to retrieve PR diff",
  "path": "/api/webhook/gitea"
}
```

### **재시도 로직**
- **LLM 호출**: 3회 재시도
- **Gitea API**: 3회 재시도
- **지수 백오프**: 1초, 2초, 4초 간격

---

## 🔧 API 테스트

### **웹훅 테스트**
```bash
curl -X POST http://localhost:8080/api/webhook/gitea \
  -H "Content-Type: application/json" \
  -d '{
    "action": "opened",
    "pull_request": {
      "number": 123,
      "title": "Test PR"
    },
    "repository": {
      "full_name": "test/repo"
    }
  }'
```

### **헬스 체크 테스트**
```bash
curl http://localhost:8080/api/webhook/health
curl http://localhost:8080/api/actuator/health
```

### **Gitea API 테스트**
```bash
curl -H "Authorization: token YOUR_TOKEN" \
  https://your-gitea-server.com/api/v1/repos/owner/repo/pulls/123.diff
```

---

이 문서는 모든 API 엔드포인트와 연동 정보를 상세히 설명하여, 
API 관련 요청사항 분석 시 빠른 참조가 가능하도록 합니다.
