# Phase 2.1: Ollama 설치 및 설정

Phase 2의 첫 번째 단계로 Ollama를 설치하고 서비스를 시작합니다.

## 🎯 목표
- Ollama 설치 및 서비스 실행
- 포트 11434에서 정상 동작 확인

## 📋 사전 요구사항
- macOS 시스템 (본 가이드는 macOS 기준)
- Homebrew 설치됨
- 관리자 권한

## 🛠️ 설치 과정

### 1. Ollama 설치

**명령어:**
```bash
brew install ollama
```

**실행 결과 예시:**
```
==> Fetching ollama
==> Downloading https://ghcr.io/v2/homebrew/core/ollama/manifests/0.12.2
...
🍺  /opt/homebrew/Cellar/ollama/0.12.2: 6 files, 176.1MB
```

### 2. 설치 확인

**명령어:**
```bash
ollama --version
```

**성공 시 출력:**
```
ollama version is 0.12.2
```

### 3. Ollama 서비스 시작

**명령어 (백그라운드 실행):**
```bash
nohup ollama serve > /tmp/claude/ollama.log 2>&1 &
```

**또는 포그라운드 실행 (테스트용):**
```bash
ollama serve
```

**성공 시 출력:**
```
Ollama is running on http://127.0.0.1:11434
```

### 4. 서비스 상태 확인

**명령어:**
```bash
curl http://localhost:11434
```

**성공 시 출력:**
```
Ollama is running
```

### 5. 프로세스 확인

**명령어:**
```bash
ps aux | grep ollama
```

**성공 시 출력 예시:**
```
rooky    12345   0.0  0.5 408240272  85648   ??  S     8:28PM   0:00.45 ollama serve
```

## ✅ 검증 체크리스트

다음 모든 항목이 통과해야 Phase 2.1 완료:

- [ ] `ollama --version` 명령어 실행 시 버전 정보 출력됨
- [ ] `curl http://localhost:11434` 명령어 실행 시 "Ollama is running" 응답
- [ ] `ps aux | grep ollama` 실행 시 ollama serve 프로세스 확인됨
- [ ] 포트 11434가 리스닝 상태임

## 🚨 문제 해결

### 문제 1: brew install ollama 실패
**증상:** Homebrew에서 패키지를 찾을 수 없음

**해결책:**
```bash
# Homebrew 업데이트
brew update
# 다시 시도
brew install ollama
```

### 문제 2: 포트 11434가 이미 사용 중
**증상:** "bind: address already in use" 에러

**해결책:**
```bash
# 포트 사용 프로세스 확인
lsof -i :11434
# 기존 프로세스 종료
killall ollama
# 서비스 재시작
ollama serve
```

### 문제 3: 권한 에러
**증상:** 권한 부족으로 서비스 시작 실패

**해결책:**
```bash
# 홈 디렉토리에서 실행
cd ~
ollama serve
```

## 📝 실행 결과 로그

### 성공적인 설치 과정 로그:
```bash
# 1. Ollama 설치
$ brew install ollama
==> Fetching ollama
==> Downloading https://ghcr.io/v2/homebrew/core/ollama/manifests/0.12.2
Already downloaded: /Users/rooky/Library/Caches/Homebrew/downloads/...
==> Downloading https://ghcr.io/v2/homebrew/core/ollama/blobs/sha256:...
Already downloaded: /Users/rooky/Library/Caches/Homebrew/downloads/...
==> Pouring ollama--0.12.2.arm64_sequoia.bottle.tar.gz
🍺  /opt/homebrew/Cellar/ollama/0.12.2: 6 files, 176.1MB

# 2. 설치 확인
$ ollama --version
ollama version is 0.12.2

# 3. 서비스 시작
$ ollama serve
Ollama is running on http://127.0.0.1:11434

# 4. 연결 테스트 (다른 터미널에서)
$ curl http://localhost:11434
Ollama is running
```

## ⏱️ 예상 소요 시간
- 설치: 2-3분
- 설정 및 확인: 1-2분
- **총 소요 시간: 약 5분**

## 🔗 다음 단계
Phase 2.1 완료 후 [Phase 2.2: StarCoder2 모델 설치](phase2-2-starcoder2-installation.md)로 진행하세요.