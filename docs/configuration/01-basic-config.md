# 기본 설정 가이드

Gitea PR Review Bot의 기본 설정을 구성하는 방법을 설명합니다.

## 📝 환경 변수 설정

### .env 파일 구성

`.env` 파일을 프로젝트 루트에 생성하고 다음 설정을 추가합니다:

```bash
# =================================
# Local LLM 설정
# =================================
LLM_BASE_URL=http://localhost:11434/v1
LLM_API_KEY=dummy
LLM_MODEL=starcoder2:3b
LLM_FALLBACK_MODEL=deepseek-coder:1.3b

# =================================
# Gitea 서버 설정
# =================================
GITEA_BASE_URL=https://gitea.your-company.com
GITEA_TOKEN=gitea_access_token_here
GITEA_WEBHOOK_SECRET=webhook_secret_key_here

# =================================
# 데이터베이스 설정 (프로덕션)
# =================================
DATABASE_URL=jdbc:postgresql://localhost:5432/pr_review_bot
DATABASE_USERNAME=review_user
DATABASE_PASSWORD=secure_password

# =================================
# 애플리케이션 설정
# =================================
SERVER_PORT=8080
LOGGING_LEVEL=DEBUG
```

### 환경 변수 설명

| 변수명 | 필수 | 기본값 | 설명 |
|--------|------|--------|------|
| `LLM_BASE_URL` | ✅ | - | Ollama 서버 주소 |
| `LLM_MODEL` | ✅ | starcoder2:3b | 주 사용 모델 |
| `GITEA_BASE_URL` | ✅ | - | Gitea 서버 URL |
| `GITEA_TOKEN` | ✅ | - | Gitea 액세스 토큰 |
| `GITEA_WEBHOOK_SECRET` | ✅ | - | 웹훅 보안 키 |

## ⚙️ application.yml 설정

### 기본 설정

```yaml
spring:
  application:
    name: gitea-pr-review-bot

  profiles:
    active: ${SPRING_PROFILE:local}

# LLM 설정
  ai:
    openai:
      base-url: ${LLM_BASE_URL:http://localhost:11434/v1}
      api-key: ${LLM_API_KEY:dummy}
      chat:
        options:
          model: ${LLM_MODEL:starcoder2:3b}
          temperature: 0.1
          max-tokens: 1000

# Gitea 설정
gitea:
  base-url: ${GITEA_BASE_URL}
  token: ${GITEA_TOKEN}
  webhook:
    secret: ${GITEA_WEBHOOK_SECRET}

# Bot 동작 설정
bot:
  review:
    enabled: true
    skip-draft: true
    max-files-per-review: 10
    max-lines-per-file: 500
    chunk-size: 100
    enable-static-analysis: true
    parallel-processing: true

  models:
    primary: ${LLM_MODEL:starcoder2:3b}
    fallback: ${LLM_FALLBACK_MODEL:deepseek-coder:1.3b}
```

### 프로파일별 설정

#### 로컬 개발 환경 (local)
```yaml
spring:
  config:
    activate:
      on-profile: local

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

  h2:
    console:
      enabled: true

logging:
  level:
    com.gitea.prbot: DEBUG
    org.springframework.ai: DEBUG
```

#### 프로덕션 환경 (production)
```yaml
spring:
  config:
    activate:
      on-profile: production

  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.gitea.prbot: INFO
```

## 🔧 봇 동작 설정 상세

### 리뷰 설정 옵션

```yaml
bot:
  review:
    # 기본 동작
    enabled: true                    # 리뷰 기능 활성화
    skip-draft: true                 # 드래프트 PR 건너뛰기
    auto-approve: false              # 자동 승인 (보안상 비추천)

    # 성능 제한
    max-files-per-review: 10         # PR당 최대 리뷰 파일 수
    max-lines-per-file: 500         # 파일당 최대 라인 수
    chunk-size: 100                  # Diff chunk 크기 (라인 단위)

    # 최적화 기능
    enable-static-analysis: true     # 정적 분석 필터링
    parallel-processing: true        # 병렬 처리 활성화
    cache-enabled: true              # 결과 캐싱

    # 타임아웃 설정
    llm-timeout: 30s                 # LLM 응답 타임아웃
    webhook-timeout: 10s             # 웹훅 처리 타임아웃
```

### 프롬프트 설정

```yaml
bot:
  prompts:
    security: classpath:prompts/security/security-review.md
    performance: classpath:prompts/performance/performance-review.md
    style: classpath:prompts/style/code-style-review.md
    general: classpath:prompts/general/general-review.md

    # 커스텀 프롬프트 (파일 경로)
    custom-rules: file:///path/to/team-specific-rules.md
```

### 모델 설정

```yaml
bot:
  models:
    primary: starcoder2:3b           # 주 모델
    fallback: deepseek-coder:1.3b    # 대체 모델 (주 모델 실패시)

    # 모델별 설정
    model-config:
      starcoder2:3b:
        temperature: 0.1
        max-tokens: 1000
        timeout: 30s
      deepseek-coder:1.3b:
        temperature: 0.2
        max-tokens: 800
        timeout: 20s
```

## 🔐 보안 설정

### Gitea 토큰 생성

1. Gitea 로그인 → Settings → Applications
2. "Generate New Token" 클릭
3. Token Name: `pr-review-bot`
4. Scopes 선택:
   - `repo` (리포지토리 접근)
   - `write:repository` (PR 코멘트 작성)
5. Generate Token → 토큰 복사
6. `.env` 파일에 `GITEA_TOKEN` 설정

### 웹훅 시크릿 생성

```bash
# 랜덤 시크릿 생성
openssl rand -hex 32

# 또는 간단한 방법
echo "your-secure-webhook-secret-$(date +%s)" | sha256sum
```

### SSL/TLS 설정 (프로덕션)

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

## 📊 로깅 설정

### 상세 로깅 설정

```yaml
logging:
  level:
    # 애플리케이션 로그
    com.gitea.prbot: DEBUG
    com.gitea.prbot.service.CodeReviewService: TRACE

    # Spring AI 로그
    org.springframework.ai: INFO

    # 웹 요청 로그
    org.springframework.web: INFO

    # 데이터베이스 로그 (개발시만)
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

  file:
    name: logs/pr-review-bot.log
    max-size: 100MB
    max-history: 30
```

### 로그 파일 설정 (프로덕션)

```yaml
logging:
  config: classpath:logback-spring.xml
```

`logback-spring.xml` 파일:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="production">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/pr-review-bot.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/pr-review-bot.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE" />
        </root>
    </springProfile>
</configuration>
```

## ✅ 설정 검증

### 1. 설정 파일 구문 검사

```bash
# YAML 파일 구문 검사
./mvnw spring-boot:run --debug
# 시작 로그에서 설정 값 확인
```

### 2. 연결성 테스트

```bash
# Ollama 연결 테스트
curl http://localhost:11434/api/tags

# Gitea API 테스트
curl -H "Authorization: token $GITEA_TOKEN" \
     $GITEA_BASE_URL/api/v1/user

# 애플리케이션 헬스 체크
curl http://localhost:8080/actuator/health
```

### 3. 설정값 확인

```bash
# 환경 변수 확인
curl http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name == "systemEnvironment")'

# 애플리케이션 설정 확인
curl http://localhost:8080/actuator/configprops
```

## 🔄 설정 변경 적용

### 런타임 설정 변경 (일부만 가능)

```bash
# 로깅 레벨 변경
curl -X POST http://localhost:8080/actuator/loggers/com.gitea.prbot \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel": "TRACE"}'
```

### 설정 파일 변경 후 재시작

```bash
# 설정 변경 후
./mvnw spring-boot:run

# 또는 프로덕션 환경에서
systemctl restart pr-review-bot
```

## 🚨 일반적인 설정 문제

### 1. Gitea 연결 실패
```
원인: GITEA_BASE_URL 또는 GITEA_TOKEN 오류
해결: curl로 Gitea API 직접 테스트
```

### 2. LLM 모델 로드 실패
```
원인: 모델이 Ollama에 설치되지 않음
해결: ollama list 확인 후 ollama pull 실행
```

### 3. 웹훅 인증 실패
```
원인: GITEA_WEBHOOK_SECRET 불일치
해결: Gitea 웹훅 설정과 환경 변수 값 일치 확인
```

## 📚 다음 단계

기본 설정이 완료되면:
1. [Gitea 웹훅 설정](02-gitea-webhook.md)
2. [프롬프트 커스터마이징](03-prompt-customization.md)
3. [성능 튜닝](04-performance-tuning.md)