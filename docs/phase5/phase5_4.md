# Phase 5.4: 모니터링 설정

## 메트릭 수집 활성화
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

## 성능 벤치마크 실행
```bash
# 성능 테스트 스크립트 실행
./scripts/performance-benchmark.sh
# 목표: 평균 리뷰 시간 < 2분, 메모리 사용량 < 4GB
```
