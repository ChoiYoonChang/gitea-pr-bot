# Phase 5.1: JVM 성능 튜닝

## 힙 메모리 최적화
- 시스템 메모리의 50-75% 할당
- 8GB 시스템: `-Xmx4g -Xms2g`
- 16GB 시스템: `-Xmx8g -Xms4g`

## GC 최적화
```bash
export JAVA_OPTS="-Xmx6g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication"
```
