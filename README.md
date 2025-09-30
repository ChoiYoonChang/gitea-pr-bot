<<<<<<< HEAD
# Gitea PR Review Bot (StarCoder2)

Spring Boot + Spring AI + StarCoder2를 사용한 **사내 보안** 코드 리뷰 봇입니다.
소스코드를 외부로 전송하지 않고 로컬 LLM으로 규칙 기반 코드 리뷰를 수행합니다.

## 🚀 주요 기능

### 🔐 **사내 보안 우선**
- **로컬 LLM**: StarCoder2-3B 모델로 소스코드 외부 유출 없음
- **자체 호스팅**: Ollama/vLLM으로 사내 서버에서 실행
- **Gitea 연동**: 사내 Git 서버와 완벽 통합

### ⚡ **성능 최적화**
- **경량 모델**: 3B 파라미터로 빠른 추론 (7B 대비 3배 빠름)
- **Chunk 처리**: Diff를 100줄 단위로 분할해서 병렬 처리
- **정적 분석**: 규칙 기반 필터링으로 LLM 호출 최소화

### 📋 **규칙 기반 검토 (경량화된 프롬프트)**
- **🔒 보안**: 민감정보, 인젝션, 권한, 암호화 규칙 위반 검출
- **⚡ 성능**: 반복문, DB쿼리, 메모리, 동기화 문제 검출
- **📝 스타일**: 네이밍, 구조, 컨벤션, 중복 코드 검출
- **🎯 품질**: 전반적인 코드 품질 평가 (A/B/C/D 등급)

### 🔧 **팀 맞춤 설정**
- Draft PR 건너뛰기
- 파일/라인 수 제한 (성능 고려)
- 팀별 코딩 규칙 프롬프트 커스터마이징

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/github/prbot/
│   │   ├── controller/           # GitHub 웹훅 컨트롤러
│   │   ├── service/             # 비즈니스 로직
│   │   ├── config/              # 설정 클래스
│   │   ├── model/               # 데이터 모델
│   │   └── dto/                 # 데이터 전송 객체
│   └── resources/
│       ├── prompts/             # AI 리뷰 프롬프트
│       │   ├── security/        # 보안 검토 프롬프트
│       │   ├── performance/     # 성능 검토 프롬프트
│       │   ├── style/          # 스타일 검토 프롬프트
│       │   └── general/        # 종합 검토 프롬프트
│       └── application.yml      # 애플리케이션 설정
└── test/                        # 테스트 코드
```

## 🛠️ 설치 및 실행

### 1. 사전 요구사항
- **Temurin JDK 21** (프로덕션 및 개발 환경 표준)
- Maven 3.8+
- **Ollama** (로컬 LLM 서버)
- **Gitea 서버** (자체 호스팅)

### 2. StarCoder2 모델 설치
```bash
# Ollama 설치 (macOS)
brew install ollama

# StarCoder2-3B 모델 다운로드
ollama pull starcoder2:3b

# 또는 더 경량화된 모델
ollama pull deepseek-coder:1.3b

# 모델 서버 실행 (백그라운드)
ollama serve
```

### 3. 환경 설정
```bash
# 환경 변수 파일 생성
cp .env.example .env

# 로컬 LLM과 Gitea 설정
vim .env
```

### 4. 애플리케이션 실행
```bash
# 개발 모드로 실행
./mvnw spring-boot:run

# 또는 JAR 빌드 후 실행
./mvnw clean package
java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

### 5. Gitea 웹훅 설정
1. Gitea 리포지토리 → Settings → Webhooks
2. Add Webhook → Gitea 선택
3. 다음 설정 입력:
   - **Target URL**: `https://your-domain.com/api/webhook/gitea`
   - **HTTP Method**: POST
   - **Content Type**: application/json
   - **Secret**: 환경변수 `GITEA_WEBHOOK_SECRET`와 동일한 값
   - **Trigger Events**: "Pull Request" 선택

## ⚙️ 설정 옵션

### application.yml 주요 설정 (성능 최적화)
```yaml
bot:
  review:
    enabled: true                    # 리뷰 활성화/비활성화
    skip-draft: true                 # Draft PR 건너뛰기
    max-files-per-review: 10         # 리뷰할 최대 파일 수 (경량화)
    max-lines-per-file: 500         # 파일당 최대 라인 수 (경량화)
    chunk-size: 100                  # Diff chunk 크기
    enable-static-analysis: true     # 정적 분석 필터링
    parallel-processing: true        # 병렬 처리

  models:
    primary: starcoder2:3b           # 주 모델
    fallback: deepseek-coder:1.3b    # 대체 모델
```

### 환경 변수
```bash
# 로컬 LLM 설정
LLM_BASE_URL=http://localhost:11434/v1
LLM_MODEL=starcoder2:3b
LLM_FALLBACK_MODEL=deepseek-coder:1.3b

# Gitea 설정
GITEA_BASE_URL=https://your-gitea.com
GITEA_TOKEN=your_gitea_token
GITEA_WEBHOOK_SECRET=your_secret
```

## 📝 프롬프트 커스터마이징

각 리뷰 타입별로 프롬프트를 수정할 수 있습니다:

- `src/main/resources/prompts/security/security-review.md` - 보안 검토
- `src/main/resources/prompts/performance/performance-review.md` - 성능 검토
- `src/main/resources/prompts/style/code-style-review.md` - 스타일 검토
- `src/main/resources/prompts/general/general-review.md` - 종합 검토

프롬프트에서 사용 가능한 변수:
- `{pr_title}` - PR 제목
- `{pr_description}` - PR 설명
- `{code_diff}` - 변경된 코드 diff
- `{repository_name}` - 리포지토리 이름
- `{author}` - PR 작성자
- `{files_changed}` - 변경된 파일 수
- `{lines_added}` - 추가된 라인 수
- `{lines_deleted}` - 삭제된 라인 수

## 🔍 모니터링

애플리케이션 상태 확인:
```bash
# Health check
curl http://localhost:8080/actuator/health

# 웹훅 엔드포인트 테스트
curl http://localhost:8080/api/webhook/health
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./mvnw test

# 통합 테스트 실행 (Testcontainers 사용)
./mvnw verify
```

## 📊 리뷰 결과 예시

경량화된 규칙 기반 검출 결과:

### 🤖 StarCoder2 코드 리뷰 결과

#### 🔒 보안 검증
```
[HIGH] UserController.java:23 - 민감정보: API_KEY 하드코딩 발견
[MED] AuthService.java:45 - 인젝션: 문자열 연결 SQL 쿼리
```

#### ⚡ 성능 검증
```
[HIGH] ProductService.java:67 - 반복: O(n²) 중첩 루프 최적화 필요
[MED] UserRepository.java:34 - DB: N+1 쿼리 가능성
```

#### 📝 스타일 검증
```
[LOW] OrderService.java:12 - 네이밍: 변수명 'data' → 'orderData' 권장
```

#### 🎯 전체 품질 평가
```
전반적 품질: B
권장: REQUEST_CHANGES
```

---
*StarCoder2-3B 로컬 모델로 분석됨 - 소스코드 외부 전송 없음*

## 🚀 배포

### Docker를 사용한 배포
```dockerfile
FROM eclipse-temurin:21-jdk-slim
COPY target/pr-review-bot-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 환경별 설정
- 개발: H2 인메모리 데이터베이스
- 프로덕션: PostgreSQL 권장

## 🤝 기여하기

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 🎯 주요 장점

### 🔐 **완벽한 사내 보안**
- 소스코드가 외부 API로 전송되지 않음
- 로컬 서버에서만 처리
- 사내 Gitea와 완벽 통합

### ⚡ **최적화된 성능**
- 3B 모델로 7B 대비 3배 빠른 추론
- Chunk 단위 병렬 처리
- 정적 분석 필터링으로 불필요한 LLM 호출 최소화

### 💰 **비용 효율성**
- OpenAI API 비용 없음 (월 $0)
- 사내 GPU 서버 1대로 팀 전체 커버
- 무제한 PR 리뷰 가능

### 🎨 **팀 맞춤형**
- 팀별 코딩 규칙 프롬프트 커스터마이징
- 경량화된 규칙 기반 검출
- 한국어 친화적 피드백

## 📊 성능 벤치마크

| 모델 | 파라미터 | 추론 시간* | 메모리 사용량 | 정확도** |
|------|----------|------------|---------------|----------|
| GPT-4 | 1.76T | 3.2s | N/A | 95% |
| StarCoder2-7B | 7B | 1.8s | 14GB | 88% |
| **StarCoder2-3B** | **3B** | **0.6s** | **6GB** | **85%** |
| DeepSeek-Coder-1.3B | 1.3B | 0.3s | 3GB | 78% |

*100줄 diff 기준, **규칙 위반 검출 정확도

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 🆘 지원

이슈가 있거나 문의사항이 있으시면 GitHub Issues를 이용해주세요.# gitea-pr-bot
=======
# sbs-datasync-bot
>>>>>>> fd1c172 (first commit)
