# 성능 튜닝 가이드

PR Review Bot의 성능을 최적화하여 빠르고 정확한 코드 리뷰를 제공하는 방법을 설명합니다.

## 📊 성능 기준점

### 목표 성능 지표

| 지표 | 목표 값 | 우수 | 보통 | 개선 필요 |
|------|---------|------|------|-----------|
| 평균 리뷰 시간 | < 2분 | < 1분 | 2-5분 | > 5분 |
| LLM 응답 시간 | < 30초 | < 15초 | 30-60초 | > 60초 |
| 메모리 사용량 | < 4GB | < 2GB | 4-6GB | > 6GB |
| CPU 사용률 | < 70% | < 50% | 70-85% | > 85% |
| 동시 PR 처리 | 5개 | 10개+ | 3-5개 | 1-2개 |

### 현재 성능 측정

```bash
# 성능 측정 스크립트
#!/bin/bash
# performance-benchmark.sh

echo "=== PR Review Bot 성능 벤치마크 ==="

# 1. 시스템 리소스
echo "1. 시스템 리소스:"
free -h
echo "CPU 사용률:"
top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1

# 2. 애플리케이션 메트릭
echo "2. 애플리케이션 성능:"
curl -s http://localhost:8080/actuator/metrics/review.processing.time | jq .

# 3. LLM 모델 성능
echo "3. LLM 모델 응답 시간:"
time ollama run starcoder2:3b "analyze this java code: public class Test {}"

# 4. 데이터베이스 성능
echo "4. 데이터베이스 응답 시간:"
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.usage | jq .
```

## ⚡ LLM 모델 최적화

### 모델 선택 가이드

```yaml
# 환경별 권장 모델
production:
  high-performance-server:    # 16GB+ RAM, GPU
    primary: starcoder2:7b
    fallback: starcoder2:3b

  standard-server:            # 8-16GB RAM
    primary: starcoder2:3b
    fallback: deepseek-coder:1.3b

  resource-limited:           # < 8GB RAM
    primary: deepseek-coder:1.3b
    fallback: codellama:7b-code
```

### 모델 최적화 설정

```yaml
# application.yml
spring:
  ai:
    openai:
      chat:
        options:
          model: starcoder2:3b
          temperature: 0.1        # 일관성 중시
          max-tokens: 800         # 1000 → 800 (응답 단축)
          top-p: 0.9             # 품질 vs 속도 균형
          frequency-penalty: 0.1  # 반복 줄이기

# Ollama 서버 최적화
ollama-config:
  num-ctx: 4096               # 컨텍스트 윈도우 크기
  num-gpu: 1                  # GPU 사용 (가능한 경우)
  num-thread: 4               # CPU 스레드 수
```

### 모델 캐싱 최적화

```bash
# Ollama 모델 사전 로딩
ollama run starcoder2:3b ""   # 빈 프롬프트로 모델 워밍업

# 시스템 시작시 자동 로딩
# /etc/systemd/system/ollama-warmup.service
[Unit]
Description=Ollama Model Warmup
After=ollama.service

[Service]
Type=oneshot
ExecStart=/usr/local/bin/ollama run starcoder2:3b ""
RemainAfterExit=true

[Install]
WantedBy=multi-user.target
```

## 🔧 애플리케이션 성능 튜닝

### JVM 최적화

```bash
# 프로덕션 JVM 옵션
export JAVA_OPTS="-Xmx6g -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Djava.net.preferIPv4Stack=true"

# GC 로깅 (성능 분석용)
export GC_OPTS="-Xloggc:logs/gc.log \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=5 \
  -XX:GCLogFileSize=100M"

java $JAVA_OPTS $GC_OPTS -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
```

### 스레드 풀 최적화

```yaml
# application.yml - 비동기 처리 최적화
spring:
  task:
    execution:
      pool:
        core-size: 4
        max-size: 8
        queue-capacity: 50
        keep-alive: 60s
      thread-name-prefix: review-task-

# 커스텀 스레드 풀 설정
bot:
  review:
    parallel-processing: true
    thread-pool:
      core-size: 2              # CPU 코어 수의 1/2
      max-size: 4               # CPU 코어 수
      queue-size: 10            # 대기열 크기
      keep-alive: 30s
```

### 데이터베이스 연결 풀 튜닝

```yaml
spring:
  datasource:
    hikari:
      # 연결 풀 크기 (CPU 코어 수 * 2 + 1)
      maximum-pool-size: 10
      minimum-idle: 5

      # 타임아웃 설정
      connection-timeout: 20000   # 20초
      idle-timeout: 300000        # 5분
      max-lifetime: 1800000       # 30분
      leak-detection-threshold: 60000  # 1분

      # 성능 최적화
      auto-commit: true
      cache-prep-stmts: true
      prep-stmt-cache-size: 250
      prep-stmt-cache-sql-limit: 2048
```

## 📝 코드 처리 최적화

### Chunk 처리 전략

```yaml
# PR 크기별 동적 청크 조정
bot:
  review:
    dynamic-chunking: true

    size-strategies:
      small:          # < 50 lines
        chunk-size: 50
        overlap: 5
        parallel: true

      medium:         # 50-200 lines
        chunk-size: 80
        overlap: 10
        parallel: true

      large:          # 200-500 lines
        chunk-size: 100
        overlap: 15
        parallel: false  # 순차 처리로 안정성 확보

      extra-large:    # > 500 lines
        chunk-size: 50   # 작은 청크로 분할
        overlap: 20
        parallel: false
        summary-only: true  # 요약만 제공
```

### 정적 분석 필터링 최적화

```java
// DiffProcessorService 성능 개선
@Service
public class OptimizedDiffProcessorService {

    // 패턴 매칭 최적화 - 미리 컴파일된 정규식 사용
    private static final Pattern SECURITY_PATTERN = Pattern.compile(
        "(?i)(password|secret|token|api_key|private_key)",
        Pattern.COMPILED
    );

    private static final Set<String> PERFORMANCE_KEYWORDS = Set.of(
        "for (", "while (", ".stream()", "foreach", "nested"
    );

    // Bloom Filter 사용으로 빠른 사전 필터링
    private final BloomFilter<String> processedChunks =
        BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000);

    @Cacheable(value = "diff-analysis", key = "#diffHash")
    public List<DiffChunk> processDiff(String diff, String diffHash) {
        // 캐시를 통한 중복 처리 방지
        if (processedChunks.mightContain(diffHash)) {
            return getCachedResult(diffHash);
        }

        // 병렬 스트림으로 처리 속도 향상
        return diff.lines()
            .parallel()
            .filter(this::hasCodeQualityIssues)
            .map(this::createDiffChunk)
            .collect(toList());
    }
}
```

### 캐싱 전략

```yaml
# Redis 기반 분산 캐시
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

# 캐시 설정
spring:
  cache:
    type: redis
    redis:
      time-to-live: 1h
      cache-null-values: false

# 멀티레벨 캐시 구성
bot:
  cache:
    # L1: 로컬 메모리 캐시 (Caffeine)
    local:
      max-size: 1000
      expire-after-write: 10m

    # L2: 분산 캐시 (Redis)
    distributed:
      expire-after-write: 1h

    # 캐시 키 전략
    strategies:
      diff-hash: "diff:{hash}"
      prompt-result: "prompt:{model}:{hash}"
      user-preferences: "user:{userId}:prefs"
```

## 🚀 시스템 레벨 최적화

### 운영체제 튜닝

```bash
# /etc/sysctl.conf - 네트워크 및 메모리 최적화
# 네트워크 버퍼 크기 증가
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.ipv4.tcp_rmem = 4096 12582912 16777216
net.ipv4.tcp_wmem = 4096 12582912 16777216

# 파일 디스크립터 제한 증가
fs.file-max = 65536

# 메모리 스와핑 최소화
vm.swappiness = 10
vm.vfs_cache_pressure = 50

# 설정 적용
sudo sysctl -p
```

### 파일 시스템 최적화

```bash
# SSD 최적화 (디스크 스케줄러 변경)
echo noop | sudo tee /sys/block/sda/queue/scheduler

# 로그 파일 저장용 별도 볼륨 마운트 (선택사항)
# 빠른 SSD를 로그 전용으로 사용
sudo mount /dev/sdb1 /var/log/pr-review-bot -o noatime,nodiratime

# 임시 파일용 tmpfs 설정
echo "tmpfs /tmp/pr-review-bot tmpfs size=1G,noexec,nodev,nosuid 0 0" >> /etc/fstab
```

### 네트워크 최적화

```yaml
# 타임아웃 및 재시도 최적화
bot:
  network:
    # Gitea API 호출 최적화
    gitea:
      connection-timeout: 5s
      read-timeout: 30s
      connection-pool-size: 10
      max-connections-per-route: 5

    # LLM API 호출 최적화
    llm:
      connection-timeout: 3s
      read-timeout: 120s        # 모델 응답 대기 시간
      connection-pool-size: 5
      keep-alive: true
```

## 📊 모니터링 및 프로파일링

### 실시간 성능 모니터링

```yaml
# Micrometer 메트릭 설정
management:
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
    tags:
      application: pr-review-bot
      environment: production

# 커스텀 메트릭 수집
bot:
  metrics:
    enabled: true
    collect:
      - review-processing-time
      - llm-response-time
      - diff-chunk-size
      - memory-usage
      - cache-hit-ratio
```

### Grafana 대시보드 설정

```json
// grafana-dashboard.json 예시
{
  "dashboard": {
    "title": "PR Review Bot Performance",
    "panels": [
      {
        "title": "Average Review Time",
        "type": "graph",
        "targets": [{
          "expr": "avg(review_processing_time_seconds)"
        }]
      },
      {
        "title": "LLM Response Time",
        "type": "graph",
        "targets": [{
          "expr": "histogram_quantile(0.95, llm_response_time_seconds_bucket)"
        }]
      },
      {
        "title": "Memory Usage",
        "type": "graph",
        "targets": [{
          "expr": "jvm_memory_used_bytes / jvm_memory_max_bytes * 100"
        }]
      }
    ]
  }
}
```

### APM 통합 (Application Performance Monitoring)

```yaml
# New Relic 통합
newrelic:
  app_name: PR Review Bot
  license_key: ${NEW_RELIC_LICENSE_KEY}

  transaction_tracer:
    enabled: true
    threshold: 1.0

  slow_sql:
    enabled: true
    threshold: 2.0
```

## 🎯 성능 시나리오별 최적화

### 1. 높은 동시성 처리

```yaml
# 많은 PR이 동시에 생성되는 환경
bot:
  review:
    # 큐 기반 처리
    queue:
      enabled: true
      max-size: 100
      worker-threads: 8

    # 우선순위 기반 처리
    priority-queue: true
    priorities:
      hotfix: 1         # 최우선
      main: 2           # 높음
      develop: 3        # 보통
      feature: 4        # 낮음

    # 부하 분산
    load-balancing:
      enabled: true
      max-concurrent-reviews: 5
      backpressure-threshold: 10
```

### 2. 대용량 PR 처리

```yaml
# 큰 PR (1000+ lines) 처리 최적화
bot:
  review:
    large-pr-strategy:
      # 점진적 처리
      incremental: true
      batch-size: 100

      # 요약 중심 리뷰
      summary-mode: true
      focus-on-critical: true

      # 비동기 처리
      async-processing: true
      notify-when-complete: true
```

### 3. 리소스 제한 환경

```yaml
# 메모리/CPU가 제한된 환경
bot:
  review:
    resource-limited-mode: true

    # 순차 처리
    parallel-processing: false

    # 작은 청크 크기
    chunk-size: 30
    max-chunks-per-session: 5

    # 공격적인 캐싱
    aggressive-caching: true
    cache-everything: true
```

## 🔧 성능 테스트 및 벤치마크

### 부하 테스트 스크립트

```bash
#!/bin/bash
# load-test.sh - PR Review Bot 부하 테스트

CONCURRENT_PRS=10
TEST_DURATION=300  # 5분

echo "시작: $CONCURRENT_PRS개 동시 PR 처리 테스트"

# 동시 PR 이벤트 생성
for i in $(seq 1 $CONCURRENT_PRS); do
  {
    curl -X POST http://localhost:8080/api/webhook/gitea \
      -H "Content-Type: application/json" \
      -d "{\"action\":\"opened\",\"pull_request\":{\"number\":$i}}" &
  } &
done

# 테스트 실행
sleep $TEST_DURATION

# 결과 수집
echo "완료된 리뷰 수:"
curl -s http://localhost:8080/actuator/metrics/review.completed | jq .measurements[0].value

echo "평균 처리 시간:"
curl -s http://localhost:8080/actuator/metrics/review.processing.time | jq .measurements[0].value
```

### 성능 회귀 테스트

```bash
#!/bin/bash
# performance-regression-test.sh

# 기준선 성능 측정
BASELINE_TIME=$(measure_review_time baseline-pr.json)

# 새 버전 성능 측정
NEW_TIME=$(measure_review_time test-pr.json)

# 회귀 검사 (20% 이상 느려지면 실패)
THRESHOLD=$(echo "$BASELINE_TIME * 1.2" | bc)

if (( $(echo "$NEW_TIME > $THRESHOLD" | bc -l) )); then
  echo "FAIL: 성능 회귀 감지 ($NEW_TIME > $THRESHOLD)"
  exit 1
else
  echo "PASS: 성능 유지 ($NEW_TIME <= $THRESHOLD)"
fi
```

## 📈 성능 최적화 체크리스트

### 설치 및 설정 최적화
- [ ] 적절한 하드웨어 사양 확보 (8GB+ RAM, 4+ CPU cores)
- [ ] SSD 스토리지 사용
- [ ] JVM 힙 메모리 최적화 (시스템 메모리의 50-75%)
- [ ] G1GC 사용 및 GC 파라미터 튜닝

### 모델 및 LLM 최적화
- [ ] 환경에 맞는 적절한 모델 크기 선택
- [ ] Ollama 서버 워밍업 설정
- [ ] 모델 응답 토큰 수 제한
- [ ] 온도 설정 최적화 (0.1 권장)

### 애플리케이션 최적화
- [ ] 동적 청크 크기 조정
- [ ] 정적 분석 필터링 활성화
- [ ] 멀티레벨 캐싱 구현
- [ ] 비동기 처리 및 스레드 풀 최적화

### 모니터링 및 유지보수
- [ ] Prometheus + Grafana 모니터링 구축
- [ ] 성능 메트릭 수집 및 알람 설정
- [ ] 정기적인 성능 벤치마크 실행
- [ ] 로그 로테이션 및 정리 자동화

이러한 최적화를 통해 PR Review Bot의 성능을 크게 향상시킬 수 있습니다. 환경에 맞게 단계적으로 적용하여 최적의 성능을 달성하세요.