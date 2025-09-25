# ⚙️ Gitea PR Review Bot 설정 참조 가이드

## 🎯 목적
이 문서는 프로젝트의 모든 설정 파일과 구성 요소를 상세히 정리하여, 설정 관련 요청사항 분석 시 빠른 참조가 가능하도록 합니다.

---

## 📄 application.yml 설정

### **전체 설정 구조**
```yaml
spring:
  application:
    name: gitea-pr-review-bot
  datasource: ...
  jpa: ...
  ai: ...
  h2: ...

gitea: ...

bot: ...

logging: ...

management: ...

server: ...
```

---

## 🌱 Spring Boot 설정

### **애플리케이션 기본 설정**
```yaml
spring:
  application:
    name: gitea-pr-review-bot
```
- **역할**: 애플리케이션 이름 설정
- **사용처**: 로깅, 모니터링, 설정 그룹핑

### **데이터베이스 설정**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: password
```
- **개발환경**: H2 인메모리 데이터베이스
- **URL**: `jdbc:h2:mem:testdb`
- **드라이버**: `org.h2.Driver`
- **인증**: sa/password

### **JPA 설정**
```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    format-sql: true
```
- **방언**: H2Dialect
- **DDL**: create-drop (개발용)
- **SQL 로깅**: 활성화
- **SQL 포맷팅**: 활성화

### **H2 콘솔 설정**
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```
- **활성화**: true
- **경로**: `/h2-console`
- **접근**: `http://localhost:8080/api/h2-console`

---

## 🤖 Spring AI 설정

### **Ollama 연결 설정**
```yaml
spring:
  ai:
    openai:
      base-url: ${LLM_BASE_URL:http://localhost:11434/v1}
      api-key: ${LLM_API_KEY:dummy}
      chat:
        options:
          model: ${LLM_MODEL:starcoder2:3b}
          temperature: 0.1
          max-tokens: 1000
```

#### **설정 항목 상세**
- **base-url**: 
  - 환경변수: `LLM_BASE_URL`
  - 기본값: `http://localhost:11434/v1`
  - 역할: Ollama 서버 주소

- **api-key**:
  - 환경변수: `LLM_API_KEY`
  - 기본값: `dummy`
  - 역할: API 키 (Ollama는 인증 불필요)

- **model**:
  - 환경변수: `LLM_MODEL`
  - 기본값: `starcoder2:3b`
  - 역할: 사용할 LLM 모델

- **temperature**: `0.1`
  - 역할: 응답 창의성 조절 (낮을수록 일관성)

- **max-tokens**: `1000`
  - 역할: 최대 응답 토큰 수

---

## 🐙 Gitea 설정

### **Gitea API 설정**
```yaml
gitea:
  base-url: ${GITEA_BASE_URL}
  token: ${GITEA_TOKEN}
  webhook:
    secret: ${GITEA_WEBHOOK_SECRET}
```

#### **설정 항목 상세**
- **base-url**:
  - 환경변수: `GITEA_BASE_URL`
  - 예시: `https://your-gitea-server.com`
  - 역할: Gitea 서버 주소

- **token**:
  - 환경변수: `GITEA_TOKEN`
  - 역할: Gitea API 인증 토큰
  - 권한: repo, write:repository

- **webhook.secret**:
  - 환경변수: `GITEA_WEBHOOK_SECRET`
  - 역할: 웹훅 보안 검증용 시크릿
  - 생성: 32자 랜덤 문자열

---

## 🤖 Bot 설정

### **리뷰 설정**
```yaml
bot:
  review:
    enabled: true
    auto-approve: false
    skip-draft: true
    max-files-per-review: 10
    max-lines-per-file: 500
    chunk-size: 100
    enable-static-analysis: true
    parallel-processing: true
```

#### **설정 항목 상세**
- **enabled**: `true`
  - 역할: 봇 활성화 여부

- **auto-approve**: `false`
  - 역할: 자동 승인 여부

- **skip-draft**: `true`
  - 역할: Draft PR 건너뛰기

- **max-files-per-review**: `10`
  - 역할: 리뷰당 최대 파일 수

- **max-lines-per-file**: `500`
  - 역할: 파일당 최대 라인 수

- **chunk-size**: `100`
  - 역할: 청크당 라인 수
  - 성능: 메모리 사용량과 처리 속도 균형

- **enable-static-analysis**: `true`
  - 역할: 정적 분석 필터링 활성화
  - 효과: LLM 호출 최소화

- **parallel-processing**: `true`
  - 역할: 병렬 처리 활성화
  - 효과: 처리 속도 향상

### **프롬프트 설정**
```yaml
bot:
  prompts:
    security: classpath:prompts/security/security-review.md
    performance: classpath:prompts/performance/performance-review.md
    style: classpath:prompts/style/code-style-review.md
    general: classpath:prompts/general/general-review.md
```

#### **프롬프트 파일 경로**
- **security**: 보안 검토 프롬프트
- **performance**: 성능 검토 프롬프트
- **style**: 코드 스타일 검토 프롬프트
- **general**: 일반 검토 프롬프트

### **모델 설정**
```yaml
bot:
  models:
    primary: ${LLM_MODEL:starcoder2:3b}
    fallback: ${LLM_FALLBACK_MODEL:deepseek-coder:1.3b}
```

#### **모델 설정 상세**
- **primary**:
  - 환경변수: `LLM_MODEL`
  - 기본값: `starcoder2:3b`
  - 역할: 주 모델

- **fallback**:
  - 환경변수: `LLM_FALLBACK_MODEL`
  - 기본값: `deepseek-coder:1.3b`
  - 역할: 대체 모델

---

## 📝 로깅 설정

### **로그 레벨 설정**
```yaml
logging:
  level:
    com.gitea.prbot: DEBUG
    org.springframework.ai: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

#### **설정 항목 상세**
- **com.gitea.prbot**: `DEBUG`
  - 역할: 애플리케이션 로그 레벨

- **org.springframework.ai**: `DEBUG`
  - 역할: Spring AI 로그 레벨

- **pattern.console**: `"%d{yyyy-MM-dd HH:mm:ss} - %msg%n"`
  - 역할: 콘솔 로그 포맷
  - 형식: 날짜 시간 - 메시지

---

## 🔧 관리 설정

### **Actuator 설정**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### **설정 항목 상세**
- **exposure.include**: `health,info,metrics,prometheus`
  - 역할: 노출할 엔드포인트
  - health: 헬스 체크
  - info: 애플리케이션 정보
  - metrics: 메트릭
  - prometheus: Prometheus 메트릭

- **health.show-details**: `always`
  - 역할: 헬스 체크 상세 정보 표시

---

## 🌐 서버 설정

### **서버 기본 설정**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

#### **설정 항목 상세**
- **port**: `8080`
  - 역할: 서버 포트

- **context-path**: `/api`
  - 역할: 애플리케이션 컨텍스트 경로
  - 효과: 모든 엔드포인트에 `/api` 접두사

---

## 🏭 프로덕션 설정

### **프로덕션 프로파일**
```yaml
---
spring:
  config:
    activate:
      on-profile: production
  
  datasource:
    # PostgreSQL 설정 (프로덕션)
```

#### **프로덕션 전용 설정**
- **프로파일**: `production`
- **데이터베이스**: PostgreSQL (H2 대신)
- **로깅**: INFO 레벨
- **보안**: 강화된 설정

---

## 🔐 환경 변수 참조

### **필수 환경 변수**
```bash
# LLM 설정
LLM_BASE_URL=http://localhost:11434/v1
LLM_API_KEY=dummy
LLM_MODEL=starcoder2:3b
LLM_FALLBACK_MODEL=deepseek-coder:1.3b

# Gitea 설정
GITEA_BASE_URL=https://your-gitea-server.com
GITEA_TOKEN=your_gitea_access_token
GITEA_WEBHOOK_SECRET=your_secure_secret

# 데이터베이스 설정 (프로덕션)
DATABASE_URL=jdbc:postgresql://localhost:5432/pr_review_bot
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password
```

### **환경 변수 우선순위**
1. **환경 변수** (최우선)
2. **application.yml 기본값**
3. **Spring Boot 기본값**

---

## 📊 설정별 성능 영향

### **메모리 사용량**
- **chunk-size**: 100줄 (기본) → 메모리 사용량과 처리 속도 균형
- **max-files-per-review**: 10개 → 메모리 제한
- **max-lines-per-file**: 500줄 → 파일별 메모리 제한

### **처리 속도**
- **parallel-processing**: true → 4배 속도 향상
- **enable-static-analysis**: true → LLM 호출 50% 감소
- **chunk-size**: 작을수록 빠른 처리, 클수록 정확한 분석

### **정확도**
- **temperature**: 0.1 → 일관된 결과
- **max-tokens**: 1000 → 충분한 응답 길이
- **chunk-size**: 클수록 더 정확한 컨텍스트 분석

---

## 🔧 설정 최적화 가이드

### **개발환경 최적화**
```yaml
bot:
  review:
    chunk-size: 50          # 빠른 테스트
    max-files-per-review: 5 # 제한된 리소스
    enable-static-analysis: false # 모든 청크 처리

logging:
  level:
    com.gitea.prbot: DEBUG  # 상세 로깅
```

### **프로덕션 최적화**
```yaml
bot:
  review:
    chunk-size: 100         # 균형잡힌 설정
    max-files-per-review: 10 # 안정적인 처리
    enable-static-analysis: true # 효율적인 처리

logging:
  level:
    com.gitea.prbot: INFO   # 최소 로깅
```

### **고성능 서버 최적화**
```yaml
bot:
  review:
    chunk-size: 200         # 큰 청크
    max-files-per-review: 20 # 많은 파일 처리
    parallel-processing: true # 병렬 처리

spring:
  ai:
    openai:
      chat:
        options:
          max-tokens: 2000  # 긴 응답
```

---

## 🚨 설정 관련 주의사항

### **보안 설정**
- **GITEA_TOKEN**: 안전한 곳에 보관
- **GITEA_WEBHOOK_SECRET**: 32자 이상 랜덤 문자열
- **DATABASE_PASSWORD**: 강력한 비밀번호

### **성능 설정**
- **chunk-size**: 너무 크면 메모리 부족, 너무 작으면 성능 저하
- **parallel-processing**: CPU 코어 수에 맞게 조정
- **max-tokens**: 너무 크면 응답 지연

### **모니터링 설정**
- **management.endpoints**: 필요한 엔드포인트만 노출
- **logging.level**: 프로덕션에서는 INFO 이상
- **health.show-details**: 프로덕션에서는 when-authorized

---

이 문서는 모든 설정 항목과 그 영향을 상세히 설명하여, 
설정 관련 요청사항 분석 시 빠른 참조가 가능하도록 합니다.
