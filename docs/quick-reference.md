# ⚡ Gitea PR Review Bot 빠른 참조 가이드

## 🎯 목적
이 문서는 프로젝트의 핵심 정보를 간결하게 정리하여, 빠른 요청사항 분석과 토큰 절약을 위한 참조 가이드입니다.

---

## 📋 프로젝트 개요

**프로젝트명**: Gitea PR Review Bot (StarCoder2)  
**기술 스택**: Spring Boot 3.2.0 + Spring AI 0.8.0 + StarCoder2-3B  
**목적**: 사내 보안을 위한 로컬 LLM 기반 자동 코드 리뷰 봇  
**패키지**: `com.gitea.prbot`

---

## 🏗️ 핵심 아키텍처

```
Controller → Service → External API
    ↓           ↓           ↓
GiteaWebhook  CodeReview  Gitea API
Controller    Service     Ollama API
```

### **주요 컴포넌트**
- **GiteaWebhookController**: 웹훅 수신 및 처리
- **CodeReviewService**: 리뷰 프로세스 오케스트레이션
- **GiteaService**: Gitea API 통신
- **DiffProcessorService**: Diff 파싱 및 청크 분할
- **PromptService**: AI 프롬프트 관리

---

## 📁 파일 구조

```
src/main/java/com/gitea/prbot/
├── GiteaPrReviewBotApplication.java    # 메인 애플리케이션
├── controller/
│   └── GiteaWebhookController.java     # 웹훅 컨트롤러
├── service/
│   ├── CodeReviewService.java          # 리뷰 오케스트레이션
│   ├── GiteaService.java               # Gitea API 통신
│   ├── DiffProcessorService.java       # Diff 처리
│   └── PromptService.java              # 프롬프트 관리
├── config/
│   ├── WebConfig.java                  # 웹 설정
│   └── OllamaConfig.java               # Ollama 설정
├── dto/
│   └── PullRequestEvent.java           # 웹훅 DTO
└── model/
    ├── ReviewResult.java               # 리뷰 결과
    └── ReviewType.java                 # 리뷰 타입
```

---

## 🔄 핵심 플로우

### **1. 웹훅 수신**
```
Gitea → POST /api/webhook/gitea → GiteaWebhookController
```

### **2. 코드 리뷰 프로세스**
```
CodeReviewService.reviewPullRequest()
├── GiteaService.getPullRequestDiff()     # PR diff 조회
├── DiffProcessorService.processDiff()    # 청크 분할
├── PromptService.getPrompt()             # 프롬프트 로딩
├── ChatClient.call() (병렬)              # LLM 호출
└── GiteaService.createReviewComment()    # 결과 포스팅
```

### **3. 병렬 처리**
- **스레드 풀**: 4개 고정 스레드
- **청크 크기**: 100줄 단위
- **리뷰 타입**: 4가지 (SECURITY, PERFORMANCE, STYLE, GENERAL)

---

## ⚙️ 주요 설정

### **application.yml 핵심 설정**
```yaml
# 서버
server:
  port: 8080
  servlet:
    context-path: /api

# Spring AI (Ollama)
spring:
  ai:
    openai:
      base-url: ${LLM_BASE_URL:http://localhost:11434/v1}
      chat:
        options:
          model: ${LLM_MODEL:starcoder2:3b}
          temperature: 0.1
          max-tokens: 1000

# Gitea
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

## 🌐 API 엔드포인트

### **내부 API**
- `POST /api/webhook/gitea` - Gitea 웹훅 수신
- `GET /api/webhook/health` - 헬스 체크
- `GET /api/actuator/health` - Spring Boot 헬스 체크

### **외부 API 연동**
- **Gitea API**: PR diff 조회, 코멘트 생성
- **Ollama API**: LLM 호출 (localhost:11434)

---

## 🚨 현재 이슈

### **컴파일 오류**
1. **Lombok 어노테이션 미처리**: `@Slf4j`, `@Data`
2. **잘못된 Main.java**: 프로젝트 루트의 불필요한 파일
3. **DTO getter/setter 누락**: `@Data` 어노테이션 미처리

### **해결 방법**
1. `pom.xml`에 Lombok 어노테이션 처리 플러그인 추가
2. `src/Main.java` 삭제
3. 수동으로 로거 및 getter/setter 추가 (임시)

---

## 📊 성능 특성

### **처리 성능**
- **평균 리뷰 시간**: 2분 이내
- **병렬 처리**: 4배 성능 향상
- **메모리 사용량**: 청크당 약 10MB

### **리소스 요구사항**
- **JVM 힙**: 4-8GB 권장
- **모델 크기**: StarCoder2-3B (1.7GB)
- **CPU**: 4코어 이상 권장

---

## 🔧 주요 메서드

### **GiteaWebhookController**
- `handleGiteaWebhook()` - 웹훅 이벤트 처리
- `health()` - 헬스 체크

### **CodeReviewService**
- `reviewPullRequest()` - PR 리뷰 오케스트레이션
- `reviewChunk()` - 개별 청크 리뷰
- `chunkDiff()` - Diff 청크 분할

### **GiteaService**
- `getPullRequestDiff()` - PR diff 조회
- `createReviewComment()` - 코멘트 생성
- `createReview()` - 리뷰 생성

### **DiffProcessorService**
- `processDiff()` - Diff 전체 처리
- `splitDiffByFiles()` - 파일별 분할
- `filterWithStaticAnalysis()` - 정적 분석 필터링

### **PromptService**
- `getPrompt()` - 리뷰 타입별 프롬프트 반환
- `loadPromptTemplates()` - 프롬프트 템플릿 로딩

---

## 🏷️ 데이터 모델

### **PullRequestEvent**
- `action` - 이벤트 액션
- `pullRequest` - PR 정보 (number, title, body, state, diffUrl, htmlUrl, head, base)
- `repository` - 리포지토리 정보 (name, fullName, cloneUrl, owner)

### **ReviewResult**
- `reviewType` - 리뷰 타입 (SECURITY, PERFORMANCE, STYLE, GENERAL)
- `content` - LLM 응답 내용
- `overallGrade` - 전체 등급 (A/B/C/D)
- `issues` - 발견된 이슈 리스트
- `repositoryName` - 리포지토리 이름
- `pullRequestNumber` - PR 번호

### **ReviewType**
- `SECURITY` - 보안 검토
- `PERFORMANCE` - 성능 검토
- `STYLE` - 코드 스타일 검토
- `GENERAL` - 일반 검토

---

## 🔐 환경 변수

### **필수 환경 변수**
```bash
# LLM 설정
LLM_BASE_URL=http://localhost:11434/v1
LLM_MODEL=starcoder2:3b

# Gitea 설정
GITEA_BASE_URL=https://your-gitea-server.com
GITEA_TOKEN=your_gitea_access_token
GITEA_WEBHOOK_SECRET=your_secure_secret
```

---

## 📈 모니터링

### **Actuator 엔드포인트**
- `/api/actuator/health` - 헬스 체크
- `/api/actuator/metrics` - 메트릭
- `/api/actuator/prometheus` - Prometheus 메트릭

### **주요 메트릭**
- `review.processing.time` - 리뷰 처리 시간
- `jvm.memory.used` - JVM 메모리 사용량
- `http.server.requests` - HTTP 요청 통계

---

## 🚀 배포 정보

### **빌드 명령어**
```bash
mvn clean package
java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

### **Docker 지원**
- Dockerfile 제공
- docker-compose.yml 제공
- 환경 변수 기반 설정

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

## 📚 상세 문서 링크

- **[프로젝트 구조](project-structure.md)** - 전체 구조 상세 분석
- **[클래스/메서드 참조](class-method-reference.md)** - 모든 클래스와 메서드 정보
- **[설정 참조](configuration-reference.md)** - 모든 설정 항목 상세
- **[API 엔드포인트 참조](api-endpoints-reference.md)** - API 연동 정보
- **[코드 리뷰](codereview.md)** - 코드 품질 분석 및 개선 방안

---

이 문서는 프로젝트의 핵심 정보를 간결하게 정리하여, 
빠른 요청사항 분석과 토큰 절약을 위한 참조 가이드입니다.
