# Gitea 웹훅 설정 가이드

Gitea에서 PR Review Bot으로 웹훅을 설정하는 방법을 설명합니다.

## 🔗 웹훅 개요

웹훅(Webhook)은 Gitea에서 Pull Request 이벤트가 발생할 때 자동으로 Review Bot에게 알림을 보내는 메커니즘입니다.

### 지원하는 이벤트

- **opened**: 새 PR 생성시
- **synchronize**: PR에 새 커밋 푸시시
- **ready_for_review**: 드래프트에서 리뷰 준비 완료로 변경시
- **reopened**: 닫힌 PR 재오픈시

## 📋 웹훅 설정 단계

### 1. Gitea 관리자/소유자 권한 확인

웹훅 설정을 위해서는 다음 권한이 필요합니다:
- 리포지토리 관리자 권한
- 또는 조직 소유자 권한

### 2. 웹훅 생성

#### 리포지토리별 웹훅 설정

1. **Gitea 리포지토리** → **Settings** → **Webhooks** 이동
2. **Add Webhook** → **Gitea** 선택
3. 웹훅 설정 입력:

```
Target URL: https://your-bot-server.com/api/webhook/gitea
HTTP Method: POST
Post Content Type: application/json
Secret: your_webhook_secret_here
```

4. **Trigger On** 섹션에서 다음 이벤트 선택:
   - ✅ Push Events (선택사항)
   - ✅ **Pull Request Events** (필수)
   - ✅ Issue Events (선택사항)

5. **Active** 체크박스 활성화
6. **Add Webhook** 버튼 클릭

#### 조직 전체 웹훅 설정

모든 리포지토리에 일괄 적용하려면:

1. **조직 페이지** → **Settings** → **Webhooks**
2. 위와 동일한 방법으로 설정
3. 모든 하위 리포지토리에 자동 적용

### 3. 웹훅 설정 세부사항

#### URL 구성
```
https://[your-domain]/api/webhook/gitea
```

예시:
```
# 로컬 개발 (ngrok 사용)
https://abc123.ngrok.io/api/webhook/gitea

# 프로덕션
https://pr-bot.your-company.com/api/webhook/gitea

# 내부 네트워크
http://192.168.1.100:8080/api/webhook/gitea
```

#### 시크릿 키 설정

`.env` 파일의 `GITEA_WEBHOOK_SECRET`와 동일한 값 사용:

```bash
# .env 파일
GITEA_WEBHOOK_SECRET=super_secure_webhook_secret_2024

# 웹훅 설정의 Secret 필드
super_secure_webhook_secret_2024
```

## 🔧 고급 웹훅 설정

### 조건부 웹훅 (Branch Filter)

특정 브랜치만 처리하고 싶은 경우:

```yaml
# application.yml
bot:
  review:
    target-branches:
      - main
      - develop
      - release/*
    exclude-branches:
      - feature/experimental-*
      - hotfix/temp-*
```

### 사용자 필터링

특정 사용자의 PR만 리뷰:

```yaml
bot:
  review:
    target-users:
      - developer1
      - developer2
    exclude-users:
      - bot-user
      - ci-system
```

### 파일 패턴 필터링

특정 파일 타입만 리뷰:

```yaml
bot:
  review:
    include-patterns:
      - "*.java"
      - "*.kt"
      - "*.js"
      - "*.ts"
    exclude-patterns:
      - "*.md"
      - "*.txt"
      - "test/**"
      - "**/generated/**"
```

## 🧪 웹훅 테스트

### 1. 수동 웹훅 테스트

Gitea 웹훅 설정 페이지에서:

1. 설정한 웹훅 클릭
2. **Test Delivery** 버튼 클릭
3. 테스트 결과 확인

### 2. curl을 사용한 테스트

```bash
# 테스트용 웹훅 페이로드
curl -X POST http://localhost:8080/api/webhook/gitea \
  -H "Content-Type: application/json" \
  -H "X-Gitea-Delivery: 12345678-1234-1234-1234-123456789012" \
  -H "X-Gitea-Event: pull_request" \
  -H "X-Hub-Signature-256: sha256=YOUR_SIGNATURE" \
  -d '{
    "action": "opened",
    "pull_request": {
      "id": 1,
      "number": 1,
      "title": "Test PR",
      "body": "This is a test pull request",
      "state": "open",
      "draft": false,
      "html_url": "https://gitea.example.com/user/repo/pulls/1",
      "diff_url": "https://gitea.example.com/user/repo/pulls/1.diff",
      "head": {
        "ref": "feature-branch",
        "sha": "abc123def456"
      },
      "base": {
        "ref": "main",
        "sha": "def456abc123"
      },
      "user": {
        "id": 1,
        "login": "testuser",
        "avatar_url": "https://gitea.example.com/avatars/1",
        "html_url": "https://gitea.example.com/testuser"
      }
    },
    "repository": {
      "id": 1,
      "name": "test-repo",
      "full_name": "user/test-repo",
      "html_url": "https://gitea.example.com/user/test-repo",
      "clone_url": "https://gitea.example.com/user/test-repo.git",
      "owner": {
        "id": 1,
        "login": "user",
        "avatar_url": "https://gitea.example.com/avatars/1"
      }
    }
  }'
```

### 3. 웹훅 시그니처 생성 (보안 테스트용)

```bash
#!/bin/bash
SECRET="your_webhook_secret"
PAYLOAD='{"test": "payload"}'

SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" | sed 's/^.*= //')
echo "X-Hub-Signature-256: sha256=$SIGNATURE"
```

## 📊 웹훅 모니터링

### 웹훅 로그 확인

```bash
# 애플리케이션 로그에서 웹훅 이벤트 확인
tail -f logs/pr-review-bot.log | grep "webhook"

# 또는 실시간 로그
curl -N http://localhost:8080/actuator/logfile | grep "webhook"
```

### 웹훅 통계 확인

애플리케이션에서 제공하는 메트릭스:

```bash
# 웹훅 수신 통계
curl http://localhost:8080/actuator/metrics/webhook.received

# 처리 완료 통계
curl http://localhost:8080/actuator/metrics/webhook.processed

# 실패 통계
curl http://localhost:8080/actuator/metrics/webhook.failed
```

## 🚨 웹훅 문제 해결

### 1. 웹훅이 전송되지 않음

**원인 및 해결:**

```bash
# Gitea 웹훅 로그 확인
# Gitea 관리자 → Site Administration → Operations → Webhooks

# 봇 서버 연결 확인
curl -I https://your-bot-server.com/api/webhook/health

# 방화벽/포트 확인
telnet your-bot-server.com 8080
```

### 2. 웹훅 인증 실패 (401/403 오류)

**원인:** 시크릿 키 불일치

```bash
# 시크릿 키 확인
echo $GITEA_WEBHOOK_SECRET

# Gitea 웹훅 설정에서 동일한 값 사용 확인
```

### 3. 웹훅 페이로드 파싱 오류

**원인:** JSON 형식 오류 또는 예상하지 못한 필드

```bash
# 로그에서 상세 오류 확인
grep "JSON parse error" logs/pr-review-bot.log

# 웹훅 페이로드 로그 활성화
logging:
  level:
    com.gitea.prbot.controller.GiteaWebhookController: TRACE
```

### 4. 타임아웃 오류

**원인:** LLM 모델 응답 지연

```yaml
# application.yml에서 타임아웃 증가
bot:
  review:
    llm-timeout: 60s
    webhook-timeout: 30s
```

## 🔄 웹훅 재전송 메커니즘

실패한 웹훅 이벤트 처리:

### 1. 수동 재처리

```bash
# 특정 PR 수동 리뷰 트리거
curl -X POST http://localhost:8080/api/manual/review \
  -H "Content-Type: application/json" \
  -d '{
    "repository": "user/repo",
    "pr_number": 123,
    "force": true
  }'
```

### 2. 자동 재시도 설정

```yaml
bot:
  review:
    retry:
      max-attempts: 3
      delay: 5s
      backoff-multiplier: 2
```

## 🌐 다중 인스턴스 웹훅

여러 Bot 인스턴스 운영시:

### 로드 밸런싱 설정

```
# nginx 설정 예시
upstream pr-review-bots {
    server bot1.internal:8080;
    server bot2.internal:8080;
    server bot3.internal:8080;
}

server {
    listen 443 ssl;
    server_name pr-bot.your-company.com;

    location /api/webhook/ {
        proxy_pass http://pr-review-bots;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 웹훅 중복 처리 방지

```yaml
bot:
  review:
    deduplication:
      enabled: true
      window: 5m        # 5분 내 동일한 PR 이벤트 중복 제거
      redis-url: redis://localhost:6379
```

## ✅ 웹훅 설정 체크리스트

- [ ] Gitea에서 웹훅 URL 정확히 입력
- [ ] POST 메서드 선택
- [ ] application/json 콘텐츠 타입 설정
- [ ] 시크릿 키 일치 확인
- [ ] Pull Request Events 활성화
- [ ] Active 체크박스 활성화
- [ ] 테스트 전송으로 연결 확인
- [ ] 봇 서버에서 웹훅 수신 로그 확인
- [ ] 첫 번째 PR 생성으로 전체 플로우 테스트

## 📚 다음 단계

웹훅 설정이 완료되면:
1. [프롬프트 커스터마이징](03-prompt-customization.md)
2. [첫 번째 PR 리뷰 테스트](../usage/01-first-review.md)
3. [성능 튜닝 가이드](04-performance-tuning.md)