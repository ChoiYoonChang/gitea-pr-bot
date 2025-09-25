# Phase 5.2: 애플리케이션 성능 설정

## Chunk 처리 최적화
```yaml
# application.yml 수정
bot:
  review:
    chunk-size: 100                    # 기본값, 메모리 부족시 50으로 감소
    max-files-per-review: 10          # 대용량 PR 제한
    enable-static-analysis: true       # 정적 분석 필터링 활성화
    parallel-processing: true          # 병렬 처리 활성화
```

## 스레드 풀 조정
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 4                   # CPU 코어 수
        max-size: 8                    # 최대 스레드
        queue-capacity: 50
```
