# Phase 3.1: 환경 변수 설정

## 환경 파일 생성
- `cp .env.example .env`
- 환경 파일 권한 설정: `chmod 600 .env`

## LLM 설정
- `LLM_BASE_URL=http://localhost:11434/v1`
- `LLM_MODEL=starcoder2:3b`
- `LLM_FALLBACK_MODEL=deepseek-coder:1.3b`

## Gitea 연결 설정
- `GITEA_BASE_URL=https://your-gitea-server.com` (실제 주소로 변경)
- Gitea 토큰 생성 및 설정 (다음 단계에서)
- 웹훅 시크릿 생성 (32자 랜덤 문자열)
