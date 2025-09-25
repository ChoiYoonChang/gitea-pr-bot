# 설치 가이드

Gitea PR Review Bot을 설치하는 단계별 가이드입니다.

## 📋 사전 요구사항

### 시스템 요구사항
- **운영체제**: Linux, macOS, Windows
- **Java**: 21 이상
- **Maven**: 3.8 이상
- **메모리**: 최소 8GB (StarCoder2-3B 모델용)
- **디스크**: 10GB 이상 (모델 파일 포함)

### 필수 소프트웨어
- [Ollama](https://ollama.com/) - 로컬 LLM 서버
- [Gitea](https://gitea.io/) - Git 서버 (이미 구축된 상태)
- Docker (선택사항, 컨테이너 배포시)

## 🚀 단계별 설치

### 1. Ollama 설치

#### macOS
```bash
brew install ollama
```

#### Linux
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Windows
[Ollama 공식 사이트](https://ollama.com/download)에서 다운로드

### 2. StarCoder2 모델 설치

```bash
# Ollama 서비스 시작
ollama serve

# 새 터미널에서 모델 다운로드
ollama pull starcoder2:3b

# 대체 모델 (더 경량화)
ollama pull deepseek-coder:1.3b

# 모델 테스트
ollama run starcoder2:3b
# >>> 간단한 자바 함수를 작성해보세요
```

### 3. 프로젝트 클론

```bash
git clone <your-git-repository>
cd git-pr-bot
```

### 4. 환경 설정

```bash
# 환경 변수 파일 생성
cp .env.example .env

# 환경 변수 편집
vim .env
```

필수 환경 변수:
```bash
# Local LLM 설정
LLM_BASE_URL=http://localhost:11434/v1
LLM_API_KEY=dummy
LLM_MODEL=starcoder2:3b

# Gitea 설정
GITEA_BASE_URL=https://your-gitea-instance.com
GITEA_TOKEN=your_gitea_access_token
GITEA_WEBHOOK_SECRET=your_secure_secret

# 데이터베이스 (선택사항, 기본값은 H2)
DATABASE_URL=jdbc:postgresql://localhost:5432/pr_review_bot
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password
```

### 5. 애플리케이션 빌드 및 실행

```bash
# 의존성 설치 및 빌드
./mvnw clean compile

# 개발 모드로 실행
./mvnw spring-boot:run

# 또는 JAR 파일로 빌드 후 실행
./mvnw clean package
java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

### 6. 서비스 상태 확인

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/api/webhook/health
# 응답: OK

# 액추에이터 헬스 체크
curl http://localhost:8080/actuator/health
# 응답: {"status":"UP"}
```

## 🐳 Docker 설치 (선택사항)

### Docker Compose 방식

1. `docker-compose.yml` 파일 생성:
```yaml
version: '3.8'

services:
  pr-review-bot:
    build: .
    ports:
      - "8080:8080"
    environment:
      - LLM_BASE_URL=http://ollama:11434/v1
      - GITEA_BASE_URL=${GITEA_BASE_URL}
      - GITEA_TOKEN=${GITEA_TOKEN}
      - GITEA_WEBHOOK_SECRET=${GITEA_WEBHOOK_SECRET}
    depends_on:
      - ollama
      - postgres

  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    environment:
      - OLLAMA_MODELS=starcoder2:3b

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=pr_review_bot
      - POSTGRES_USER=review_user
      - POSTGRES_PASSWORD=review_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  ollama_data:
  postgres_data:
```

2. 실행:
```bash
# 백그라운드 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f pr-review-bot

# 종료
docker-compose down
```

## 🔧 설치 후 검증

### 1. Ollama 모델 확인
```bash
ollama list
# NAME                 ID           SIZE     MODIFIED
# starcoder2:3b        abc123...    1.7GB    2 hours ago
```

### 2. 애플리케이션 로그 확인
```bash
# 애플리케이션 시작 로그에서 다음 확인:
# - Spring Boot 정상 시작
# - Gitea service initialized
# - H2 database connection established
```

### 3. API 엔드포인트 테스트
```bash
# 웹훅 엔드포인트
curl -X POST http://localhost:8080/api/webhook/gitea \
  -H "Content-Type: application/json" \
  -d '{"test": "webhook"}'
```

## 🚨 일반적인 설치 문제

### Ollama 모델 다운로드 실패
```bash
# 문제: 네트워크 또는 디스크 용량 부족
# 해결:
ollama pull starcoder2:3b --insecure
# 또는 더 작은 모델 사용
ollama pull deepseek-coder:1.3b
```

### Java 버전 문제
```bash
# 문제: Java 21 미만 사용
# 해결:
java -version
# Java 21 설치 필요

# SDKMAN 사용 (권장)
sdk install java 21-tem
sdk use java 21-tem
```

### 메모리 부족
```bash
# 문제: OutOfMemoryError
# 해결: JVM 힙 메모리 증가
java -Xmx4g -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar

# 또는 application.yml에서 chunk-size 감소
bot:
  review:
    chunk-size: 50  # 기본값 100에서 50으로 감소
```

## 🔄 업그레이드 가이드

### 새 버전 배포
```bash
# 1. 최신 코드 가져오기
git pull origin main

# 2. 애플리케이션 중지
pkill -f "pr-review-bot"

# 3. 리빌드
./mvnw clean package

# 4. 재시작
java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

### 모델 업그레이드
```bash
# 새 모델 다운로드
ollama pull starcoder2:7b

# 환경 변수 업데이트
LLM_MODEL=starcoder2:7b

# 애플리케이션 재시작
```

## ✅ 다음 단계

설치가 완료되면:
1. [환경 설정 가이드](../configuration/01-basic-config.md) 확인
2. [Gitea 웹훅 설정](../configuration/02-gitea-webhook.md) 진행
3. [첫 번째 리뷰 테스트](../usage/01-first-review.md) 수행