#!/bin/bash

# ===========================================
# Gitea PR Review Bot 실행 스크립트
# ===========================================

echo "🚀 Gitea PR Review Bot 시작 중..."

# Temurin JDK 21 환경 설정
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# ===========================================
# 필수 환경변수 설정
# ===========================================

# Gitea 연동 설정
export GITEA_BASE_URL="${GITEA_BASE_URL:-https://gitea.com}"
export GITEA_TOKEN="${GITEA_TOKEN:-your_gitea_token_here}"
export GITEA_WEBHOOK_SECRET="${GITEA_WEBHOOK_SECRET:-your_webhook_secret_here}"

# AI 모델 설정
export LLM_BASE_URL="${LLM_BASE_URL:-http://localhost:11434/v1}"
export LLM_MODEL="${LLM_MODEL:-starcoder2:3b}"
export LLM_FALLBACK_MODEL="${LLM_FALLBACK_MODEL:-deepseek-coder:1.3b}"
export LLM_API_KEY="${LLM_API_KEY:-dummy}"

# 서버 설정
export SERVER_PORT="${SERVER_PORT:-8080}"

# ===========================================
# 환경변수 검증
# ===========================================

echo "📋 환경변수 확인:"
echo "  GITEA_BASE_URL: $GITEA_BASE_URL"
echo "  GITEA_TOKEN: ${GITEA_TOKEN:0:10}..."
echo "  LLM_BASE_URL: $LLM_BASE_URL"
echo "  LLM_MODEL: $LLM_MODEL"
echo "  SERVER_PORT: $SERVER_PORT"

# ===========================================
# 사전 요구사항 확인
# ===========================================

echo "🔍 사전 요구사항 확인 중..."

# Java 버전 확인
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" != "21" ]; then
    echo "❌ Java 21이 필요합니다. 현재 버전: $java_version"
    exit 1
fi
echo "✅ Java 21 (Temurin) 확인됨"

# JAR 파일 확인
if [ ! -f "target/pr-review-bot-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다. 먼저 빌드를 실행하세요."
    echo "   mvn clean package"
    exit 1
fi
echo "✅ JAR 파일 확인됨"

# Ollama 서버 확인 (선택사항)
if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "✅ Ollama 서버 연결됨"
else
    echo "⚠️  Ollama 서버에 연결할 수 없습니다. AI 기능이 작동하지 않을 수 있습니다."
    echo "   Ollama 설치: brew install ollama"
    echo "   Ollama 시작: ollama serve"
fi

# ===========================================
# 애플리케이션 실행
# ===========================================

echo "🎯 애플리케이션 시작 중..."
echo "   서버 주소: http://localhost:$SERVER_PORT/api"
echo "   Health Check: http://localhost:$SERVER_PORT/api/actuator/health"
echo "   H2 Console: http://localhost:$SERVER_PORT/api/h2-console"
echo ""

# 애플리케이션 실행
java -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
