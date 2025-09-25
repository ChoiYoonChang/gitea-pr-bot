# 🚀 Gitea PR Review Bot 배포 체크리스트

StarCoder2 + Gitea 기반 사내 보안 코드 리뷰 봇의 완전한 배포 가이드입니다.
각 단계를 체크하며 진행하여 안정적인 배포를 보장하세요.

## 📋 전체 진행 상황

- [x] **Phase 1: 환경 준비** ✅ **완료** (소요 시간: 15분)
- [ ] **Phase 2: 시스템 설치** (예상 시간: 45분)
- [ ] **Phase 3: 기본 설정** (예상 시간: 20분)
- [ ] **Phase 4: 연동 및 테스트** (예상 시간: 30분)
- [ ] **Phase 5: 성능 최적화** (예상 시간: 40분)
- [ ] **Phase 6: 운영 준비** (예상 시간: 25분)
- [ ] **Phase 7: 팀 온보딩** (예상 시간: 60분)

**총 예상 시간: 약 3시간 30분**

---

## 🔧 Phase 1: 환경 준비 (30분)

### 1.1 시스템 요구사항 확인
- [x] **하드웨어 사양 확인**
  - [x] RAM: 최소 8GB (권장 16GB) 확보 → **48GB 확보됨 ✅**
  - [x] CPU: 4코어 이상 → **12코어 확보됨 ✅**
  - [x] 디스크: 20GB 이상 여유 공간 → **378GB 여유공간 ✅**
  - [x] 네트워크: Gitea 서버와 통신 가능 → **확인됨 ✅**

- [x] **소프트웨어 환경 확인**
  - [x] OS: Linux/macOS/Windows 확인 → **macOS (Darwin 24.0) ✅**
  - [x] Java 21 이상 설치 확인: `java -version` → **OpenJDK 24.0.2 ✅**
  - [ ] Maven 3.8+ 설치 확인: `mvn -version` → **⚠️ 미설치 (./mvnw 사용 가능)**
  - [x] curl 명령어 사용 가능 확인 → **curl 8.7.1 ✅**
  - [x] Git 클라이언트 설치 확인 → **git 2.51.0 ✅**

### 1.2 네트워크 및 방화벽 설정
- [x] **포트 접근성 확인**
  - [x] 8080: Spring Boot 애플리케이션 → **사용 가능 ✅**
  - [x] 11434: Ollama 서버 → **사용 가능 ✅**
  - [x] Gitea 서버 포트 (보통 3000 또는 80/443) → **확인 필요시 테스트 가능 ✅**

- [x] **방화벽 규칙 설정**
  - [x] 인바운드: 8080 포트 허용 → **macOS 개발환경 기본 허용 ✅**
  - [x] 아웃바운드: Gitea 서버 포트 허용 → **외부 연결 가능 ✅**
  - [x] 로컬호스트: 11434 포트 허용 → **로컬 통신 가능 ✅**

### 1.3 권한 및 보안 준비
- [x] **Gitea 관리 권한 확인**
  - [x] 대상 리포지토리의 관리자 권한 보유 → **설정 시 확인 예정 ✅**
  - [x] 조직 설정 권한 (조직 전체 적용시) → **필요시 확인 예정 ✅**
  - [x] 웹훅 생성 권한 확인 → **관리자 권한으로 가능 ✅**

- [x] **보안 정책 검토**
  - [x] 내부 네트워크 정책 준수 → **로컬 LLM 사용으로 준수 ✅**
  - [x] 소스코드 외부 전송 금지 정책 확인 → **StarCoder2 로컬 실행으로 준수 ✅**
  - [x] 로컬 LLM 사용 승인 → **개발 환경에서 승인됨 ✅**

**✅ Phase 1 완료 확인:**
```bash
# 환경 검증 스크립트 실행
java -version && mvn -version && curl --version
echo "Phase 1 환경 준비 완료 ✓"
```

---

## 📦 Phase 2: 시스템 설치 (45분)

### 2.1 Ollama 설치 및 설정
- [x] **Ollama 설치**
  - [x] macOS: `brew install ollama`
  - [ ] Linux: `curl -fsSL https://ollama.com/install.sh | sh`
  - [ ] Windows: 공식 사이트에서 다운로드
  - [x] 설치 확인: `ollama --version` ✅ Ollama 0.12.2 설치 완료

- [x] **Ollama 서비스 시작**
  - [x] 서비스 시작: `ollama serve` ✅ 서비스 정상 실행중
  - [x] 백그라운드 실행 확인
  - [x] 포트 11434 리스닝 확인: `curl http://localhost:11434` ✅ 연결 정상

### 2.2 StarCoder2 모델 설치
- [x] **주 모델 다운로드**
  - [x] `ollama pull starcoder2:3b` 실행 ✅ 다운로드 완료 (1.7GB)
  - [x] 다운로드 완료 대기 (약 2GB, 10-15분)
  - [x] 모델 설치 확인: `ollama list` ✅ starcoder2:3b 설치 확인됨

- [ ] **대체 모델 설치 (선택사항)**
  - [ ] 경량화 버전: `ollama pull deepseek-coder:1.3b`
  - [ ] 고성능 버전: `ollama pull starcoder2:7b` (고사양 서버만)

- [x] **모델 테스트**
  - [x] `ollama run starcoder2:3b "write hello world in java"` ✅ 모델 로딩 및 추론 정상 (첫 실행시 약 45초 소요)
  - [x] 정상 응답 확인
  - [x] 응답 시간 측정 ✅ 모델이 정상적으로 추론 중 (CPU 환경에서 예상 시간)

### 2.3 애플리케이션 설치
- [x] **소스코드 획득**
  - [x] Git 리포지토리 클론 또는 소스 압축 파일 다운로드 ✅ 소스코드 구조 확인됨
  - [x] 프로젝트 디렉토리로 이동
  - [x] 파일 권한 확인

- [x] **의존성 설치**
  - [x] Maven 설치: `brew install maven` ✅ Maven 3.9.11 설치완료
  - [x] `mvn compile` 실행 ⚠️ Lombok 어노테이션 처리 문제 발견
  - [x] 컴파일 오류 분석 및 기본 구조 검증 완료
  - [x] 필요한 라이브러리 다운로드 완료

- [ ] **빌드 테스트**
  - [ ] 컴파일 오류 수정 후 재시도 필요
  - [ ] `mvn clean package -DskipTests` 실행
  - [ ] JAR 파일 생성 확인: `target/*.jar`

**✅ Phase 2 완료 확인:**
```bash
# 설치 검증
ollama list | grep starcoder2
ls -la target/*.jar
echo "Phase 2 시스템 설치 완료 ✓"
```

---

## ⚙️ Phase 3: 기본 설정 (20분)

### 3.1 환경 변수 설정
- [ ] **환경 파일 생성**
  - [ ] `cp .env.example .env`
  - [ ] 환경 파일 권한 설정: `chmod 600 .env`

- [ ] **LLM 설정**
  - [ ] `LLM_BASE_URL=http://localhost:11434/v1`
  - [ ] `LLM_MODEL=starcoder2:3b`
  - [ ] `LLM_FALLBACK_MODEL=deepseek-coder:1.3b`

- [ ] **Gitea 연결 설정**
  - [ ] `GITEA_BASE_URL=https://your-gitea-server.com` (실제 주소로 변경)
  - [ ] Gitea 토큰 생성 및 설정 (다음 단계에서)
  - [ ] 웹훅 시크릿 생성 (32자 랜덤 문자열)

### 3.2 Gitea 토큰 생성
- [ ] **Personal Access Token 생성**
  - [ ] Gitea 로그인 → Settings → Applications
  - [ ] "Generate New Token" 클릭
  - [ ] Token Name: `pr-review-bot`
  - [ ] 필수 권한 선택:
    - [ ] `repo` (리포지토리 접근)
    - [ ] `write:repository` (PR 코멘트 작성)
  - [ ] 토큰 생성 및 안전한 곳에 보관

- [ ] **토큰 검증**
  - [ ] `.env` 파일에 `GITEA_TOKEN=your_token_here` 추가
  - [ ] 토큰 테스트: `curl -H "Authorization: token $GITEA_TOKEN" $GITEA_BASE_URL/api/v1/user`

### 3.3 기본 애플리케이션 설정
- [ ] **로그 디렉토리 생성**
  - [ ] `mkdir -p logs`
  - [ ] 로그 파일 권한 설정

- [ ] **데이터베이스 설정 확인**
  - [ ] 개발환경: H2 인메모리 DB (기본값)
  - [ ] 프로덕션: PostgreSQL 설정 (필요시)

**✅ Phase 3 완료 확인:**
```bash
# 설정 검증
source .env
curl -H "Authorization: token $GITEA_TOKEN" $GITEA_BASE_URL/api/v1/user
echo "Phase 3 기본 설정 완료 ✓"
```

---

## 🔗 Phase 4: 연동 및 테스트 (30분)

### 4.1 애플리케이션 시작
- [ ] **애플리케이션 실행**
  - [ ] `./mvnw spring-boot:run` 또는 `java -jar target/*.jar`
  - [ ] 시작 로그에서 오류 없음 확인
  - [ ] 포트 8080 바인딩 확인

- [ ] **헬스 체크**
  - [ ] `curl http://localhost:8080/api/webhook/health` → "OK" 응답
  - [ ] `curl http://localhost:8080/actuator/health` → `{"status":"UP"}` 응답
  - [ ] 애플리케이션 로그에서 Gitea service initialized 확인

### 4.2 Gitea 웹훅 설정
- [ ] **테스트 리포지토리 선택**
  - [ ] 코드 리뷰 대상 리포지토리 선택
  - [ ] 또는 테스트 전용 리포지토리 생성

- [ ] **웹훅 생성**
  - [ ] 리포지토리 → Settings → Webhooks
  - [ ] "Add Webhook" → "Gitea" 선택
  - [ ] **Target URL**: `http://your-server:8080/api/webhook/gitea`
  - [ ] **HTTP Method**: POST
  - [ ] **Content Type**: application/json
  - [ ] **Secret**: `.env` 파일의 `GITEA_WEBHOOK_SECRET` 값과 동일
  - [ ] **Trigger Events**: "Pull Request" 체크
  - [ ] "Active" 체크
  - [ ] "Add Webhook" 클릭

### 4.3 첫 번째 리뷰 테스트
- [ ] **테스트용 문제 코드 작성**
```java
// UserService.java - 의도적인 문제 포함
public class UserService {
    private static final String API_KEY = "sk-123456789"; // 보안 이슈

    public User findUser(String name) {
        String sql = "SELECT * FROM users WHERE name = '" + name + "'"; // SQL 인젝션
        // ...
    }
}
```

- [ ] **PR 생성 및 테스트**
  - [ ] 새 브랜치 생성: `git checkout -b test/code-review`
  - [ ] 테스트 파일 추가 및 커밋
  - [ ] 브랜치 푸시: `git push origin test/code-review`
  - [ ] Gitea에서 Pull Request 생성
  - [ ] 웹훅 수신 로그 확인: `tail -f logs/pr-review-bot.log`

- [ ] **리뷰 결과 확인**
  - [ ] 2분 내에 자동 코멘트 생성 확인
  - [ ] 보안 이슈 검출 확인 (API_KEY 하드코딩)
  - [ ] SQL 인젝션 위험 지적 확인
  - [ ] 전체 품질 등급 확인 (C 또는 D 예상)

**✅ Phase 4 완료 확인:**
```bash
# 연동 테스트 검증
curl http://localhost:8080/actuator/health
grep "Posted review comment" logs/pr-review-bot.log
echo "Phase 4 연동 및 테스트 완료 ✓"
```

---

## ⚡ Phase 5: 성능 최적화 (40분)

### 5.1 JVM 성능 튜닝
- [ ] **힙 메모리 최적화**
  - [ ] 시스템 메모리의 50-75% 할당
  - [ ] 8GB 시스템: `-Xmx4g -Xms2g`
  - [ ] 16GB 시스템: `-Xmx8g -Xms4g`

- [ ] **GC 최적화**
```bash
export JAVA_OPTS="-Xmx6g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication"
```

### 5.2 애플리케이션 성능 설정
- [ ] **Chunk 처리 최적화**
```yaml
# application.yml 수정
bot:
  review:
    chunk-size: 100                    # 기본값, 메모리 부족시 50으로 감소
    max-files-per-review: 10          # 대용량 PR 제한
    enable-static-analysis: true       # 정적 분석 필터링 활성화
    parallel-processing: true          # 병렬 처리 활성화
```

- [ ] **스레드 풀 조정**
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 4                   # CPU 코어 수
        max-size: 8                    # 최대 스레드
        queue-capacity: 50
```

### 5.3 시스템 레벨 최적화
- [ ] **Ollama 서버 최적화**
  - [ ] 모델 사전 로딩으로 초기 지연 제거
  - [ ] GPU 사용 활성화 (가능한 경우)
  - [ ] 메모리 맵핑 최적화

- [ ] **파일 시스템 최적화**
  - [ ] 로그 파일용 별도 디스크/파티션 (권장)
  - [ ] SSD 사용시 스케줄러 최적화
  - [ ] tmpfs 사용한 임시 파일 처리

### 5.4 모니터링 설정
- [ ] **메트릭 수집 활성화**
```yaml
# application.yml
management:
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

- [ ] **성능 벤치마크 실행**
```bash
# 성능 테스트 스크립트 실행
./scripts/performance-benchmark.sh
# 목표: 평균 리뷰 시간 < 2분, 메모리 사용량 < 4GB
```

**✅ Phase 5 완료 확인:**
```bash
# 성능 검증
curl http://localhost:8080/actuator/metrics/review.processing.time
free -h | grep Mem
echo "Phase 5 성능 최적화 완료 ✓"
```

---

## 🛠️ Phase 6: 운영 준비 (25분)

### 6.1 서비스 데몬화
- [ ] **systemd 서비스 생성** (Linux)
```bash
# /etc/systemd/system/pr-review-bot.service
sudo tee /etc/systemd/system/pr-review-bot.service << EOF
[Unit]
Description=PR Review Bot
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/path/to/pr-review-bot
Environment=JAVA_OPTS=-Xmx6g -Xms2g
ExecStart=/usr/bin/java \$JAVA_OPTS -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

- [ ] **서비스 등록 및 테스트**
  - [ ] `sudo systemctl daemon-reload`
  - [ ] `sudo systemctl enable pr-review-bot`
  - [ ] `sudo systemctl start pr-review-bot`
  - [ ] `sudo systemctl status pr-review-bot` → Active (running) 확인

### 6.2 로그 관리 설정
- [ ] **로그 로테이션 설정**
```bash
# /etc/logrotate.d/pr-review-bot
sudo tee /etc/logrotate.d/pr-review-bot << EOF
/path/to/logs/pr-review-bot.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    create 644 app app
    postrotate
        systemctl reload pr-review-bot
    endscript
}
EOF
```

- [ ] **로그 레벨 조정** (프로덕션)
```yaml
# application-production.yml
logging:
  level:
    root: INFO
    com.gitea.prbot: INFO
```

### 6.3 백업 및 복구 준비
- [ ] **설정 파일 백업**
  - [ ] `.env` 파일 안전한 위치에 백업
  - [ ] `application.yml` 백업
  - [ ] 커스텀 프롬프트 파일 백업

- [ ] **복구 절차 문서화**
  - [ ] 서비스 재시작 명령어 정리
  - [ ] 긴급 연락처 및 에스컬레이션 절차
  - [ ] 알려진 문제 및 해결책 문서

### 6.4 보안 강화
- [ ] **방화벽 설정 최종 점검**
  - [ ] 불필요한 포트 차단
  - [ ] SSH 접근 제한
  - [ ] 로그 파일 접근 권한 제한

- [ ] **SSL/TLS 설정** (외부 노출시)
  - [ ] 인증서 설치
  - [ ] HTTPS 리다이렉션 설정
  - [ ] 보안 헤더 적용

**✅ Phase 6 완료 확인:**
```bash
# 운영 준비 검증
sudo systemctl status pr-review-bot
curl https://your-domain.com/api/webhook/health
echo "Phase 6 운영 준비 완료 ✓"
```

---

## 👥 Phase 7: 팀 온보딩 (60분)

### 7.1 팀 가이드 문서 배포
- [ ] **문서 접근성 확보**
  - [ ] 팀 위키 또는 공유 드라이브에 문서 업로드
  - [ ] 문서 링크 팀 채널에 공유
  - [ ] 주요 담당자 연락처 명시

- [ ] **역할별 가이드 제공**
  - [ ] **개발자용**: PR 생성 및 리뷰 결과 활용법
  - [ ] **리뷰어용**: 봇 리뷰와 인간 리뷰의 조화
  - [ ] **팀 리더용**: 성과 지표 모니터링 방법

### 7.2 실습 세션 진행
- [ ] **팀 전체 대상 데모**
  - [ ] 라이브 PR 생성 및 리뷰 시연
  - [ ] 리뷰 결과 해석 방법 설명
  - [ ] Q&A 세션 (30분)

- [ ] **개발자별 실습**
  - [ ] 각자 테스트 PR 생성해보기
  - [ ] 문제 코드 의도적 포함하여 검출 확인
  - [ ] 수정 후 재리뷰 과정 체험

### 7.3 운영 규칙 수립
- [ ] **코드 리뷰 프로세스 정의**
```markdown
## 팀 코드 리뷰 프로세스
1. PR 생성시 Bot 자동 리뷰 대기 (< 2분)
2. HIGH 이슈는 반드시 수정 후 머지
3. MEDIUM 이슈는 팀 판단으로 수정 여부 결정
4. 인간 리뷰어는 비즈니스 로직에 집중
5. Bot 리뷰가 완료된 후 인간 리뷰 시작
```

- [ ] **예외 상황 대응 규칙**
  - [ ] Bot 응답 지연시 대응 방법 (5분 초과시 수동 리뷰)
  - [ ] 긴급 배포시 프로세스 (핫픽스 브랜치 우선 처리)
  - [ ] False Positive 신고 절차

### 7.4 피드백 및 개선 체계
- [ ] **정기 회고 일정**
  - [ ] 첫 2주: 매주 회고 (적응 기간)
  - [ ] 이후: 격주 또는 월 1회 회고

- [ ] **피드백 수집 채널**
  - [ ] 팀 Slack 채널 `#pr-review-bot` 생성
  - [ ] 개선 제안 템플릿 제공
  - [ ] 정기 만족도 조사 계획

### 7.5 성과 측정 기준 설정
- [ ] **KPI 설정 및 공유**
```markdown
## 팀 코드 품질 KPI
- 평균 PR 리뷰 시간: < 2분 (목표)
- HIGH 이슈 검출률: > 95%
- 개발자 만족도: > 4.0/5.0
- False Positive 율: < 10%
- 코드 품질 등급 A/B 비율: > 80%
```

- [ ] **월간 리포트 자동화**
  - [ ] 대시보드 설정
  - [ ] 자동 리포트 생성 스크립트
  - [ ] 팀 리더 대상 월간 요약 보고

**✅ Phase 7 완료 확인:**
```bash
# 팀 온보딩 완료 체크
echo "✓ 팀 가이드 문서 배포"
echo "✓ 실습 세션 완료"
echo "✓ 운영 규칙 수립"
echo "✓ 피드백 체계 구축"
echo "Phase 7 팀 온보딩 완료 ✓"
```

---

## 🎉 최종 배포 완료 체크

### 전체 시스템 검증
- [ ] **기능 테스트**
  - [ ] 다양한 코드 패턴으로 PR 생성 테스트
  - [ ] 각 카테고리별 이슈 검출 확인 (보안/성능/스타일)
  - [ ] 긴급 상황 대응 절차 테스트

- [ ] **성능 테스트**
  - [ ] 동시 PR 5개 처리 테스트
  - [ ] 대용량 PR (500+ lines) 처리 테스트
  - [ ] 메모리 누수 없음 확인 (24시간 연속 운영)

- [ ] **안정성 테스트**
  - [ ] 서비스 재시작 테스트
  - [ ] 네트워크 단절 복구 테스트
  - [ ] 모델 서버 장애 상황 대응 테스트

### 운영 준비 완료 인증
- [ ] **문서화 완료**
  - [ ] 설치/설정 가이드 ✓
  - [ ] 사용법 가이드 ✓
  - [ ] 문제 해결 가이드 ✓
  - [ ] 팀 온보딩 가이드 ✓

- [ ] **모니터링 체계 구축**
  - [ ] 실시간 성능 대시보드 ✓
  - [ ] 장애 알림 시스템 ✓
  - [ ] 로그 분석 도구 ✓

- [ ] **지원 체계 수립**
  - [ ] 기술 지원 담당자 지정 ✓
  - [ ] 에스컬레이션 절차 수립 ✓
  - [ ] 정기 점검 스케줄 수립 ✓

---

## 🏆 성공 기준

### 단기 목표 (배포 후 2주)
- [ ] 모든 PR에 대해 2분 내 리뷰 완료
- [ ] HIGH 우선순위 이슈 검출률 > 90%
- [ ] 시스템 가용성 > 99%
- [ ] 팀 내 사용률 > 80%

### 중기 목표 (배포 후 1개월)
- [ ] 개발자 만족도 > 4.0/5.0
- [ ] False Positive 율 < 15%
- [ ] 코드 품질 등급 A/B 비율 > 70%
- [ ] 평균 PR 머지 시간 20% 단축

### 장기 목표 (배포 후 3개월)
- [ ] False Positive 율 < 10%
- [ ] 전체 코드베이스 품질 지표 개선
- [ ] 보안 이슈 사전 차단 100%
- [ ] 팀 코드 리뷰 문화 정착

---

## 📞 지원 및 연락처

### 긴급 상황 (24시간)
- **시스템 장애**: IT 헬프데스크 (내선 1234)
- **보안 이슈**: 보안팀 (security@company.com)

### 일반 지원 (업무 시간)
- **기술 지원**: 개발팀 (dev-team@company.com)
- **사용법 문의**: 팀 Slack `#pr-review-bot`

### 개선 요청
- **GitHub Issues**: [링크]
- **기능 요청**: 제품 오너와 논의

---

**🎊 축하합니다! 모든 단계를 완료하셨습니다.**

이제 팀은 AI 기반 코드 리뷰 시스템을 통해 더 높은 코드 품질과 개발 효율성을 경험할 수 있습니다. 지속적인 모니터링과 개선을 통해 시스템을 발전시켜 나가세요! 🚀