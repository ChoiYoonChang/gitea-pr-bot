# 일반적인 문제 해결 가이드

PR Review Bot 운영 중 자주 발생하는 문제들과 해결 방법을 정리했습니다.

## 🚨 긴급 문제 해결

### 1. 봇이 완전히 응답하지 않음

**증상:**
- 웹훅은 수신되지만 리뷰 코멘트가 전혀 생성되지 않음
- 애플리케이션은 실행 중이지만 작업 처리 안됨

**즉시 확인 사항:**
```bash
# 1. 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health
# 응답이 없으면 애플리케이션 재시작 필요

# 2. Ollama 서비스 상태 확인
ollama ps
# 모델이 로드되어 있는지 확인

# 3. 메모리 사용량 확인
free -h
# 메모리 부족시 시스템 재시작 고려
```

**해결 방법:**
```bash
# Step 1: Ollama 재시작
sudo systemctl restart ollama
ollama pull starcoder2:3b

# Step 2: 애플리케이션 재시작
pkill -f "pr-review-bot"
nohup java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &

# Step 3: 연결 테스트
curl -X POST http://localhost:8080/api/webhook/gitea \
  -H "Content-Type: application/json" \
  -d '{"action":"opened","pull_request":{"number":999}}'
```

### 2. 극도로 느린 응답 (> 5분)

**증상:**
- 리뷰는 생성되지만 매우 오래 걸림
- 시스템 리소스 사용률이 높음

**빠른 해결책:**
```yaml
# application.yml 임시 조정
bot:
  review:
    chunk-size: 20          # 100 → 20으로 감소
    max-files-per-review: 3 # 10 → 3으로 감소
    enable-static-analysis: true

    # 타임아웃 단축
    llm-timeout: 30s
```

**시스템 최적화:**
```bash
# 1. 불필요한 프로세스 종료
sudo systemctl stop unnecessary-services

# 2. Swap 확인 및 최적화
sudo swapon --show
# Swap 사용률이 높으면 메모리 증설 필요

# 3. 경량 모델로 임시 전환
ollama pull deepseek-coder:1.3b
# 환경변수 LLM_MODEL=deepseek-coder:1.3b로 변경
```

## 🔧 설정 관련 문제

### 3. Gitea 연결 실패

**에러 로그:**
```
ERROR c.g.p.service.GiteaService - Repository user/repo is not accessible
```

**진단 및 해결:**
```bash
# 1. Gitea 토큰 유효성 확인
curl -H "Authorization: token $GITEA_TOKEN" \
     $GITEA_BASE_URL/api/v1/user
# 401 응답시 토큰 재발급 필요

# 2. Gitea URL 접근성 확인
curl -I $GITEA_BASE_URL
# 타임아웃시 네트워크/방화벽 문제

# 3. 토큰 권한 확인
curl -H "Authorization: token $GITEA_TOKEN" \
     $GITEA_BASE_URL/api/v1/repos/user/repo
# 403 응답시 리포지토리 권한 부족
```

**토큰 재발급 방법:**
1. Gitea → Settings → Applications
2. 기존 토큰 삭제
3. 새 토큰 생성 (scopes: repo, write:repository)
4. `.env` 파일 업데이트
5. 애플리케이션 재시작

### 4. 웹훅 인증 실패

**에러 로그:**
```
WARN c.g.p.controller.GiteaWebhookController - Invalid webhook signature
```

**해결 방법:**
```bash
# 1. 시크릿 키 일치 확인
echo "Current secret: $GITEA_WEBHOOK_SECRET"

# 2. Gitea 웹훅 설정에서 동일한 값 사용 확인

# 3. 시그니처 생성 테스트
echo -n '{"test":"payload"}' | \
  openssl dgst -sha256 -hmac "$GITEA_WEBHOOK_SECRET"
```

### 5. LLM 모델 로딩 실패

**에러 로그:**
```
ERROR o.s.ai.openai.OpenAiChatModel - Model starcoder2:3b not found
```

**해결 단계:**
```bash
# 1. 설치된 모델 확인
ollama list
# starcoder2:3b가 없으면 설치 필요

# 2. 모델 재설치
ollama rm starcoder2:3b
ollama pull starcoder2:3b

# 3. 모델 테스트
ollama run starcoder2:3b "print hello world in java"

# 4. 애플리케이션 재시작
```

## 🐛 런타임 에러

### 6. 메모리 부족 에러 (OutOfMemoryError)

**에러 로그:**
```
java.lang.OutOfMemoryError: Java heap space
```

**즉시 대응:**
```bash
# 1. JVM 힙 메모리 증가
export JAVA_OPTS="-Xmx6g -Xms2g"
java $JAVA_OPTS -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar

# 2. 처리 청크 크기 감소
# application.yml 수정
bot:
  review:
    chunk-size: 30
    max-files-per-review: 5
```

**근본적 해결:**
```yaml
# 메모리 효율적인 설정
bot:
  review:
    # 배치 처리 비활성화
    parallel-processing: false

    # GC 튜닝
    gc-strategy: G1GC

    # 캐시 크기 제한
    cache:
      max-size: 100
      expire-after-write: 1h
```

### 7. 데이터베이스 연결 에러

**에러 로그:**
```
HikariPool-1 - Connection is not available
```

**해결 방법:**
```yaml
# application.yml 연결 풀 조정
spring:
  datasource:
    hikari:
      maximum-pool-size: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000

    # H2 사용시 (개발)
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
```

### 8. JSON 파싱 에러

**에러 로그:**
```
JsonParseException: Unexpected character at position 123
```

**디버깅:**
```bash
# 1. 웹훅 페이로드 로깅 활성화
logging:
  level:
    com.gitea.prbot.controller.GiteaWebhookController: TRACE

# 2. 실제 페이로드 확인
tail -f logs/pr-review-bot.log | grep "Webhook payload"

# 3. Gitea 버전별 호환성 확인
curl $GITEA_BASE_URL/api/v1/version
```

## 🔍 성능 문제

### 9. LLM 응답 시간 초과

**에러 로그:**
```
TimeoutException: LLM request timed out after 30 seconds
```

**단계별 해결:**
```bash
# 1. 모델 상태 확인
ollama ps
# GPU 사용률, 메모리 사용률 확인

# 2. 경량 모델로 전환
ollama pull deepseek-coder:1.3b

# 3. 타임아웃 증가 (임시)
bot:
  review:
    llm-timeout: 120s

# 4. 병렬 처리 비활성화 (리소스 부족시)
    parallel-processing: false
```

### 10. 디스크 공간 부족

**증상:**
- 로그 파일 증가 중단
- 모델 로딩 실패
- 애플리케이션 크래시

**해결:**
```bash
# 1. 디스크 사용량 확인
df -h
du -sh logs/
du -sh ~/.ollama/

# 2. 로그 파일 정리
find logs/ -name "*.log" -mtime +30 -delete
logrotate /etc/logrotate.d/pr-review-bot

# 3. 불필요한 모델 제거
ollama list
ollama rm unused-model:latest

# 4. 로그 로테이션 설정
# /etc/logrotate.d/pr-review-bot
/path/to/logs/pr-review-bot.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    create 644 app app
}
```

## 🔄 리뷰 품질 문제

### 11. 부정확한 리뷰 결과 (False Positive)

**증상:**
- 명백히 정상인 코드를 문제로 지적
- 같은 패턴을 반복적으로 잘못 검출

**튜닝 방법:**
```yaml
# 프롬프트 개선
bot:
  prompts:
    # 더 구체적인 규칙 추가
    team-specific: |
      다음은 우리 팀의 허용된 패턴입니다:
      - System.out.println() 디버깅용 사용 OK
      - TODO 주석 단기간 허용
      - 테스트 코드의 매직 넘버 허용
```

**정적 분석 필터 조정:**
```yaml
bot:
  review:
    static-analysis:
      # 임계값 상향 조정
      confidence-threshold: 0.8  # 0.5 → 0.8

      # 특정 패턴 예외 처리
      ignore-patterns:
        - "test/**/*.java"
        - "**/generated/**"
```

### 12. 놓치는 문제들 (False Negative)

**원인 분석:**
```bash
# 1. 처리된 청크 확인
grep "Processing chunk" logs/pr-review-bot.log

# 2. 모델 응답 품질 확인
grep "ChatResponse" logs/pr-review-bot.log

# 3. 청크 크기 최적화
bot:
  review:
    chunk-size: 80      # 너무 크거나 작지 않게
    context-lines: 5    # 주변 컨텍스트 포함
```

## 🛠️ 디버깅 도구

### 13. 상세 로깅 설정

```yaml
# 문제 상황별 로깅 레벨
logging:
  level:
    # 웹훅 문제
    com.gitea.prbot.controller: DEBUG

    # LLM 통신 문제
    org.springframework.ai: DEBUG

    # 데이터베이스 문제
    org.hibernate: DEBUG
    org.hibernate.SQL: DEBUG

    # 네트워크 문제
    org.springframework.web.client: DEBUG
```

### 14. 헬스 체크 스크립트

```bash
#!/bin/bash
# health-check.sh

echo "=== PR Review Bot 헬스 체크 ==="

# 1. 애플리케이션 상태
echo "1. 애플리케이션 상태:"
curl -s http://localhost:8080/actuator/health | jq .

# 2. Ollama 상태
echo "2. Ollama 모델 상태:"
ollama ps

# 3. 메모리 사용량
echo "3. 메모리 사용량:"
free -h

# 4. 디스크 사용량
echo "4. 디스크 사용량:"
df -h

# 5. Gitea 연결성
echo "5. Gitea 연결 테스트:"
curl -s -H "Authorization: token $GITEA_TOKEN" \
     $GITEA_BASE_URL/api/v1/user | jq .login

echo "=== 체크 완료 ==="
```

### 15. 성능 프로파일링

```bash
# JVM 성능 모니터링
java -XX:+UseG1GC \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     -Xloggc:gc.log \
     -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar

# 애플리케이션 메트릭 수집
curl http://localhost:8080/actuator/metrics > metrics.json

# 힙 덤프 생성 (메모리 문제시)
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
jcmd <PID> GC.dump /tmp/heapdump.hprof
```

## 🚑 응급 대응 플레이북

### 프로덕션 장애 대응

**1단계: 즉시 대응 (< 5분)**
```bash
# 서비스 상태 확인
systemctl status pr-review-bot
curl http://localhost:8080/actuator/health

# 로그에서 최근 에러 확인
tail -100 logs/pr-review-bot.log | grep ERROR

# 필요시 서비스 재시작
systemctl restart pr-review-bot
```

**2단계: 임시 복구 (< 15분)**
```bash
# 경량 설정으로 전환
echo "bot.review.chunk-size=20" >> application-emergency.properties
echo "bot.review.max-files-per-review=3" >> application-emergency.properties

# 경량 모델로 전환
export LLM_MODEL=deepseek-coder:1.3b

# 애플리케이션 재시작
java -Dspring.profiles.active=emergency \
     -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

**3단계: 근본 원인 분석 (< 1시간)**
```bash
# 전체 시스템 진단
./scripts/full-system-diagnosis.sh

# 성능 메트릭 분석
curl http://localhost:8080/actuator/metrics > incident-metrics.json

# 로그 분석 및 보고서 생성
./scripts/generate-incident-report.sh
```

## 📞 지원 요청

### 내부 에스컬레이션 기준

**Level 1 (팀 자체 해결):**
- 설정 문제
- 간단한 연결 오류
- 성능 튜닝

**Level 2 (시스템 관리자):**
- 인프라 관련 문제
- 네트워크 연결성
- 리소스 부족

**Level 3 (개발팀 지원):**
- 애플리케이션 버그
- 모델 관련 이슈
- 기능 개선 요청

### 문제 보고 템플릿

```
## 🚨 PR Review Bot 문제 보고

### 기본 정보
- 발생 시간: YYYY-MM-DD HH:MM:SS
- 심각도: [HIGH/MEDIUM/LOW]
- 영향 범위: [전체/특정 리포지토리/특정 사용자]

### 증상 설명
[구체적인 증상과 예상 동작과의 차이점]

### 재현 단계
1.
2.
3.

### 환경 정보
- OS:
- Java 버전:
- 애플리케이션 버전:
- Ollama 버전:
- 사용 중인 모델:

### 첨부 파일
- [ ] 로그 파일
- [ ] 설정 파일
- [ ] 에러 스크린샷
- [ ] 성능 메트릭

### 긴급도
- [ ] 프로덕션 서비스 중단
- [ ] 기능 제한적 사용 가능
- [ ] 불편하지만 우회 방법 있음
- [ ] 개선 요청
```

이 가이드는 가장 일반적인 문제들을 다루고 있습니다. 특정 상황에 대한 추가 지원이 필요하면 개발팀에 문의하세요.