# Phase 2.3: 애플리케이션 설치 및 빌드

Phase 2의 마지막 단계로 PR Review Bot 애플리케이션을 설치하고 빌드 환경을 구성합니다.

## 🎯 목표
- Maven 개발 환경 구성
- 애플리케이션 소스코드 구조 확인
- 의존성 다운로드 및 기본 컴파일 테스트
- Gitea/Ollama 아키텍처로의 전환 완료

## 📋 사전 요구사항
- Phase 2.1, 2.2 완료
- Java 21 이상 설치됨
- 프로젝트 소스코드 준비됨

## 🛠️ 설치 과정

### 1. Maven 설치

**명령어:**
```bash
brew install maven
```

**실행 과정 (5-10분 소요):**
```
==> Fetching dependencies for maven: glib, xorgproto, libxau, libxdmcp, libxcb, libx11, libxext, libxrender, lzo, pixman, cairo, graphite2, harfbuzz, jpeg-turbo, libtiff, little-cms2 and openjdk
...
==> Installing maven
==> Pouring maven--3.9.11.all.bottle.2.tar.gz
🍺  /opt/homebrew/Cellar/maven/3.9.11: 100 files, 10.3MB
```

### 2. Maven 설치 확인

**명령어:**
```bash
mvn -version
```

**성공 시 출력:**
```
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /opt/homebrew/Cellar/maven/3.9.11/libexec
Java version: 25, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/25/libexec/openjdk.jdk/Contents/Home
Default locale: ko_KR, platform encoding: UTF-8
OS name: "mac os x", version: "15.6.1", arch: "aarch64", family: "mac"
```

### 3. 프로젝트 구조 확인

**명령어:**
```bash
ls -la /Users/rooky/IdeaProjects/git-pr-bot/
```

**확인해야 할 파일들:**
```
-rw-r--r--  1 rooky  staff  4.2K pom.xml
drwxr-xr-x  4 rooky  staff   128B src/
drwxr-xr-x  8 rooky  staff   256B docs/
```

### 4. Java 소스 구조 확인

**명령어:**
```bash
find src/main/java -name "*.java"
```

**성공 시 출력:**
```
src/main/java/com/gitea/prbot/config/WebConfig.java
src/main/java/com/gitea/prbot/GiteaPrReviewBotApplication.java
src/main/java/com/gitea/prbot/service/DiffProcessorService.java
src/main/java/com/gitea/prbot/service/GiteaService.java
src/main/java/com/gitea/prbot/controller/GiteaWebhookController.java
src/main/java/com/gitea/prbot/dto/PullRequestEvent.java
src/main/java/com/gitea/prbot/model/ReviewResult.java
src/main/java/com/gitea/prbot/model/ReviewType.java
src/main/java/com/gitea/prbot/service/CodeReviewService.java
src/main/java/com/gitea/prbot/service/PromptService.java
src/main/java/com/gitea/prbot/config/OllamaConfig.java
```

### 5. 의존성 다운로드 및 컴파일 테스트

**명령어:**
```bash
mvn compile -q
```

**실행 과정:**
- 처음 실행 시 의존성 다운로드로 3-5분 소요
- Spring Boot, Spring AI, Ollama 관련 라이브러리 다운로드
- Lombok 등 추가 라이브러리 다운로드

**현재 상태 (예상되는 결과):**
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
...

[ERROR] COMPILATION ERROR :
[ERROR] .../PromptService.java:[31,13] cannot find symbol
  symbol:   variable log
  location: class com.gitea.prbot.service.PromptService
...
```

### 6. pom.xml 구성 확인

**명령어:**
```bash
grep -A 3 -B 1 "ollama\|lombok\|gitea" pom.xml
```

**확인해야 할 내용:**
```xml
<groupId>com.gitea</groupId>
<artifactId>pr-review-bot</artifactId>
<name>Gitea PR Review Bot</name>
...
<artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
...
<artifactId>lombok</artifactId>
```

## ✅ 검증 체크리스트

다음 모든 항목이 통과해야 Phase 2.3 기본 완료:

- [ ] `mvn -version` 실행 시 Maven 3.9.11 이상 버전 표시됨
- [ ] `find src/main/java -name "*.java"` 실행 시 모든 Java 파일 확인됨
- [ ] `mvn compile` 실행 시 의존성 다운로드가 완료됨
- [ ] pom.xml에서 Gitea/Ollama 관련 의존성 확인됨
- [ ] 프로젝트가 com.gitea.prbot 패키지로 구성됨

## 🚨 현재 알려진 이슈 및 해결 예정

### 이슈 1: Lombok 어노테이션 처리 문제
**증상:** `cannot find symbol: variable log` 등의 컴파일 에러

**원인:** Lombok 어노테이션 처리기가 제대로 동작하지 않음

**해결 예정:** Phase 3에서 어노테이션 처리 설정 수정 또는 수동 코드 생성

### 이슈 2: Spring AI Ollama API 변경
**증상:** OllamaChatClient 생성자 관련 컴파일 에러

**해결 예정:** Phase 3에서 최신 API에 맞춰 코드 수정

## 📊 의존성 다운로드 상세 로그

### 성공적인 의존성 다운로드:
```bash
$ mvn compile -q
Downloading from spring-milestones: https://repo.spring.io/milestone/org/springframework/ai/spring-ai-ollama-spring-boot-starter/0.8.0/...
...
Downloaded from spring-milestones: .../spring-ai-ollama-spring-boot-starter-0.8.0.jar (12 kB at 15 kB/s)
...
Downloaded from central: .../lombok-1.18.30.jar (1.9 MB at 2.1 MB/s)
...
```

### 주요 다운로드된 라이브러리:
- Spring Boot 3.2.0 관련 라이브러리
- Spring AI 0.8.0 및 Ollama 통합
- Lombok 1.18.30
- Jackson JSON 처리 라이브러리
- WebFlux (Gitea API 호출용)
- H2/PostgreSQL 데이터베이스 드라이버

## 📝 실행 결과 로그

### 성공적인 설치 과정 로그:
```bash
# 1. Maven 설치
$ brew install maven
==> Fetching dependencies for maven: ...
🍺  /opt/homebrew/Cellar/maven/3.9.11: 100 files, 10.3MB

# 2. Maven 확인
$ mvn -version
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: /opt/homebrew/Cellar/maven/3.9.11/libexec
Java version: 25, vendor: Homebrew

# 3. 프로젝트 구조 확인
$ ls -la
total 16
drwxr-xr-x@ 8 rooky  staff   256 Sep 25 20:28 .
-rw-r--r--@ 1 rooky  staff  4218 Sep 25 21:45 pom.xml
drwxr-xr-x@ 4 rooky  staff   128 Sep 25 20:22 src
drwx------@ 9 rooky  staff   288 Sep 25 21:50 docs

# 4. Java 파일 구조 확인
$ find src/main/java -name "*.java"
src/main/java/com/gitea/prbot/GiteaPrReviewBotApplication.java
src/main/java/com/gitea/prbot/controller/GiteaWebhookController.java
... (11개 파일 확인)

# 5. 의존성 다운로드 시도
$ mvn compile -q
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
[INFO] Scanning for projects...
Downloading from spring-milestones: https://repo.spring.io/milestone/...
... (수많은 의존성 다운로드)

[ERROR] COMPILATION ERROR :
[ERROR] .../PromptService.java:[31,13] cannot find symbol
  symbol:   variable log
... (예상되는 Lombok 관련 에러들)
```

## ⚙️ 아키텍처 전환 완료 확인

### GitHub → Gitea 전환:
- ✅ 패키지명: `com.github.prbot` → `com.gitea.prbot`
- ✅ 컨트롤러: `GitHubWebhookController` → `GiteaWebhookController`
- ✅ 서비스: `GitHubService` → `GiteaService`
- ✅ DTO: GitHub API → Gitea API 구조

### OpenAI → Ollama 전환:
- ✅ 의존성: `spring-ai-openai-spring-boot-starter` → `spring-ai-ollama-spring-boot-starter`
- ✅ 설정: `OpenAiConfig` → `OllamaConfig`
- ✅ 클라이언트: `OpenAiChatClient` → `OllamaChatClient`

## ⏱️ 예상 소요 시간
- Maven 설치: 5-10분
- 의존성 다운로드: 3-5분
- 구조 확인 및 테스트: 2-3분
- **총 소요 시간: 약 15-20분**

## 💡 Phase 3 준비사항

Phase 2.3에서 발견된 이슈들이 Phase 3에서 해결될 예정:
1. Lombok 어노테이션 처리 문제 해결
2. Spring AI Ollama API 호환성 수정
3. 컴파일 성공 및 JAR 파일 생성
4. 기본 설정 파일 구성

## 🔗 다음 단계
Phase 2.3 완료 후 **Phase 3: 기본 설정**으로 진행하여 컴파일 문제를 해결하고 설정 파일을 구성하게 됩니다.

## 📋 Phase 2 전체 완료 상태

**✅ 완료된 항목:**
- Ollama 0.12.2 설치 및 서비스 실행
- StarCoder2-3B 모델 다운로드 (1.7GB) 및 테스트
- Maven 3.9.11 설치 및 환경 구성
- 애플리케이션 소스코드 Gitea/Ollama 아키텍처로 전환
- 모든 필요 의존성 다운로드 완료

**⚠️ Phase 3에서 해결 예정:**
- Lombok 어노테이션 처리 최적화
- 컴파일 에러 수정 및 JAR 빌드 성공
- 기본 설정 파일 구성