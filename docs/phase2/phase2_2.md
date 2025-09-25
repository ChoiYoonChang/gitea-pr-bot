# Phase 2.2: StarCoder2 모델 설치

## 주 모델 다운로드
- `ollama pull starcoder2:3b` 실행
- 다운로드 완료 대기 (약 2GB, 10-15분)
- 모델 설치 확인: `ollama list`

## 대체 모델 설치 (선택사항)
- 경량화 버전: `ollama pull deepseek-coder:1.3b`
- 고성능 버전: `ollama pull starcoder2:7b` (고사양 서버만)

## 모델 테스트
- `ollama run starcoder2:3b "write hello world in java"`
- 정상 응답 확인
- 응답 시간 측정 (< 30초 권장)
