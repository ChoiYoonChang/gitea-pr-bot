# Phase 2.2: StarCoder2 모델 설치

Phase 2의 두 번째 단계로 StarCoder2-3B 모델을 설치하고 테스트합니다.

## 🎯 목표
- StarCoder2-3B 모델 다운로드 및 설치
- 모델 정상 작동 확인
- 추론 성능 테스트

## 📋 사전 요구사항
- Phase 2.1 완료 (Ollama 서비스 실행 중)
- 최소 4GB 이상의 사용 가능한 디스크 공간
- 안정적인 인터넷 연결

## 🛠️ 설치 과정

### 1. 모델 다운로드

**명령어:**
```bash
ollama pull starcoder2:3b
```

**실행 과정 (약 5-10분 소요):**
```
pulling manifest ⠙
pulling 28bfdfaeba9f:  36% ▕██████            ▏ 615 MB/1.7 GB  3.0 MB/s   5m58s
...
pulling 28bfdfaeba9f: 100% ▕██████████████████▏ 1.7 GB/1.7 GB  6.4 MB/s
verifying sha256 digest
writing manifest
success
```

### 2. 설치 확인

**명령어:**
```bash
ollama list
```

**성공 시 출력:**
```
NAME            ID              SIZE    MODIFIED
starcoder2:3b   28bfdfaeba9f    1.7 GB  2 minutes ago
```

### 3. 모델 테스트 (기본 기능 확인)

**명령어:**
```bash
ollama run starcoder2:3b "write hello world in java"
```

**실행 과정:**
- 첫 실행시 모델 로딩에 30-60초 소요
- CPU 환경에서 추론 시간 30-45초 정도
- 스피너(⠙⠹⠸⠼) 표시되며 모델이 작업 중임을 의미

**성공 시 출력 예시:**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### 4. 모델 상태 확인

**명령어:**
```bash
ollama ps
```

**모델 실행 중일 때 출력:**
```
NAME            ID              SIZE      PROCESSOR    CONTEXT    UNTIL
starcoder2:3b   28bfdfaeba9f    1.3 GB    100% CPU     2048       4 minutes from now
```

**모델 유휴 상태일 때 출력:**
```
NAME    ID    SIZE    PROCESSOR    CONTEXT    UNTIL
```

## ✅ 검증 체크리스트

다음 모든 항목이 통과해야 Phase 2.2 완료:

- [ ] `ollama list` 실행 시 starcoder2:3b 모델이 목록에 표시됨
- [ ] 모델 크기가 약 1.7GB로 표시됨
- [ ] `ollama run starcoder2:3b "simple test"` 실행 시 모델이 응답함
- [ ] 첫 실행 후 모델 로딩 시간이 30초 이내로 단축됨

## 🚨 문제 해결

### 문제 1: 다운로드 속도가 너무 느림
**증상:** 다운로드가 1MB/s 이하로 매우 느림

**해결책:**
```bash
# 다운로드 중단
Ctrl+C
# 재시도 (이어받기 됨)
ollama pull starcoder2:3b
```

### 문제 2: 디스크 공간 부족
**증상:** "no space left on device" 에러

**해결책:**
```bash
# 디스크 사용량 확인
df -h
# 불필요한 파일 정리 후 재시도
ollama pull starcoder2:3b
```

### 문제 3: 모델 실행이 매우 느림
**증상:** 간단한 질문에도 2분 이상 소요

**원인 및 해결책:**
- **원인:** CPU만 사용하는 환경에서 정상적인 현상
- **해결책:** 더 작은 모델 사용 고려
```bash
# 더 빠른 경량 모델 설치 (선택사항)
ollama pull deepseek-coder:1.3b
```

### 문제 4: 모델이 응답하지 않음
**증상:** 무한 로딩 상태

**해결책:**
```bash
# Ollama 서비스 재시작
killall ollama
ollama serve &
# 모델 재시도
ollama run starcoder2:3b "test"
```

## 📊 성능 벤치마크

### CPU 환경 (Apple M1/M2)
- **첫 로딩 시간:** 30-45초
- **이후 추론 시간:** 15-30초 (간단한 질문)
- **메모리 사용량:** 약 3-4GB
- **추천 사용 방식:** 짧은 코드 리뷰, 단순 질문

### CPU 환경 (Intel x64)
- **첫 로딩 시간:** 45-60초
- **이후 추론 시간:** 30-60초
- **메모리 사용량:** 약 4-5GB
- **추천 사용 방식:** 배치 처리로 여러 질문 동시 처리

## 📝 실행 결과 로그

### 성공적인 설치 과정 로그:
```bash
# 1. 모델 다운로드
$ ollama pull starcoder2:3b
pulling manifest
pulling 28bfdfaeba9f:  36% ▕██████            ▏ 615 MB/1.7 GB
pulling 28bfdfaeba9f:  37% ▕██████            ▏ 624 MB/1.7 GB  4.6 MB/s   3m54s
...
pulling 28bfdfaeba9f: 100% ▕██████████████████▏ 1.7 GB/1.7 GB
verifying sha256 digest
writing manifest
success

# 2. 설치 확인
$ ollama list
NAME            ID              SIZE    MODIFIED
starcoder2:3b   28bfdfaeba9f    1.7 GB  2 minutes ago

# 3. 모델 테스트
$ ollama run starcoder2:3b "write hello world in java"
⠙ (로딩 중... 약 30-45초)

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}

# 4. 모델 상태 확인
$ ollama ps
NAME            ID              SIZE      PROCESSOR    CONTEXT    UNTIL
starcoder2:3b   28bfdfaeba9f    1.3 GB    100% CPU     2048       4 minutes from now
```

## ⏱️ 예상 소요 시간
- 모델 다운로드: 5-15분 (네트워크 속도에 따라)
- 첫 모델 테스트: 1-2분
- **총 소요 시간: 약 10-20분**

## 💡 최적화 팁

### 1. 모델 예열 (Warm-up)
```bash
# 서버 시작 시 모델을 미리 로드
ollama run starcoder2:3b "warmup" > /dev/null
```

### 2. 배치 처리를 위한 스크립트
```bash
#!/bin/bash
# batch-review.sh
echo "Starting batch code review..."
ollama run starcoder2:3b "$(cat code-to-review.txt)"
```

### 3. 메모리 모니터링
```bash
# 메모리 사용량 실시간 모니터링
watch -n 1 "ps aux | grep ollama | head -1"
```

## 🔗 다음 단계
Phase 2.2 완료 후 [Phase 2.3: 애플리케이션 설치](phase2-3-application-installation.md)로 진행하세요.