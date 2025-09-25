# Phase 2.1: Ollama 설치 및 설정

## Ollama 설치
- macOS: `brew install ollama`
- Linux: `curl -fsSL https://ollama.com/install.sh | sh`
- Windows: 공식 사이트에서 다운로드
- 설치 확인: `ollama --version`

## Ollama 서비스 시작
- 서비스 시작: `ollama serve`
- 백그라운드 실행 확인
- 포트 11434 리스닝 확인: `curl http://localhost:11434`
