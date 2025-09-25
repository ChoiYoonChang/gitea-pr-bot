# 첫 번째 PR 리뷰 테스트

PR Review Bot을 설치하고 설정한 후, 첫 번째 코드 리뷰를 수행하는 방법을 안내합니다.

## 🎯 테스트 준비

### 전제 조건 확인

다음 항목들이 정상 작동하는지 확인하세요:

- [ ] Ollama 서버 실행 중 (`ollama ps`)
- [ ] StarCoder2 모델 설치됨 (`ollama list`)
- [ ] PR Review Bot 애플리케이션 실행 중
- [ ] Gitea 웹훅 설정 완료
- [ ] 봇 서버 접근 가능 (`curl http://localhost:8080/api/webhook/health`)

### 테스트 환경 확인

```bash
# 1. Ollama 상태 확인
ollama ps
# NAME            ID       SIZE    MODIFIED
# starcoder2:3b   abc123   1.7GB   2 hours ago

# 2. 봇 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health
# {"status":"UP"}

# 3. Gitea 연결 확인
curl -H "Authorization: token $GITEA_TOKEN" \
     $GITEA_BASE_URL/api/v1/user
# 성공시 사용자 정보 반환
```

## 🧪 테스트용 PR 생성

### 1. 테스트 리포지토리 생성

Gitea에서 테스트용 리포지토리를 만들거나 기존 리포지토리를 사용합니다.

### 2. 테스트 코드 작성

다음과 같은 의도적인 코드 품질 이슈가 포함된 파일을 생성합니다:

#### `UserService.java` (보안 + 성능 이슈 포함)

```java
package com.example.service;

import java.sql.*;

public class UserService {

    // 보안 이슈: 하드코딩된 DB 정보
    private static final String DB_PASSWORD = "admin123";
    private static final String API_KEY = "sk-1234567890abcdef";

    private Connection connection;

    public UserService() throws SQLException {
        // 보안 이슈: 하드코딩된 연결 정보
        this.connection = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/testdb",
            "admin",
            DB_PASSWORD
        );
    }

    // 보안 이슈: SQL 인젝션 취약점
    public User findUserByName(String name) throws SQLException {
        String sql = "SELECT * FROM users WHERE name = '" + name + "'";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return new User(rs.getString("name"), rs.getString("email"));
        }
        return null;
    }

    // 성능 이슈: N+1 쿼리 문제
    public void printAllUsersWithOrders() throws SQLException {
        String usersSql = "SELECT id, name FROM users";
        Statement stmt = connection.createStatement();
        ResultSet users = stmt.executeQuery(usersSql);

        while (users.next()) {
            int userId = users.getInt("id");
            System.out.println("User: " + users.getString("name"));

            // 각 사용자마다 별도 쿼리 실행 (N+1 문제)
            String ordersSql = "SELECT * FROM orders WHERE user_id = " + userId;
            ResultSet orders = stmt.executeQuery(ordersSql);
            while (orders.next()) {
                System.out.println("  Order: " + orders.getString("product"));
            }
        }
    }

    // 스타일 이슈: 의미없는 변수명, 매직넘버
    public boolean checkUserStatus(int id) throws SQLException {
        String sql = "SELECT status FROM users WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int s = rs.getInt("status");
            return s == 1;  // 매직넘버
        }
        return false;
    }
}
```

#### `User.java` (스타일 이슈 포함)

```java
package com.example.model;

// 스타일 이슈: public 필드, final 키워드 누락
public class User {
    public String name;    // private이어야 함
    public String email;   // private이어야 함

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // 스타일 이슈: @Override 어노테이션 누락
    public String toString() {
        return "User{name='" + name + "', email='" + email + "'}";
    }

    // TODO: hashCode() 구현 필요
    // FIXME: equals() 메서드 수정 필요
}
```

### 3. Pull Request 생성

1. 새 브랜치 생성: `git checkout -b feature/user-service`
2. 파일 추가: `git add .`
3. 커밋: `git commit -m "Add user service with intentional issues for testing"`
4. 푸시: `git push origin feature/user-service`
5. Gitea에서 Pull Request 생성

## 📊 리뷰 결과 확인

### 1. 웹훅 수신 로그 확인

```bash
# 실시간 로그 모니터링
tail -f logs/pr-review-bot.log

# 웹훅 수신 확인
grep "Received Gitea webhook" logs/pr-review-bot.log
# 2024-01-20 14:30:15 - Received Gitea webhook event: pull_request
```

### 2. 리뷰 처리 과정 로그

```bash
# 처리 과정 상세 로그
grep "Processing PR event" logs/pr-review-bot.log
# 2024-01-20 14:30:16 - Processing PR event: opened for PR #1

grep "Starting code review" logs/pr-review-bot.log
# 2024-01-20 14:30:17 - Starting code review for PR #1: Add user service

grep "Completed code review" logs/pr-review-bot.log
# 2024-01-20 14:30:25 - Completed code review for PR #1
```

### 3. 예상 리뷰 결과

다음과 같은 리뷰 코멘트가 PR에 자동으로 추가됩니다:

```
## 🤖 StarCoder2 코드 리뷰 결과

### 🔒 보안 검증
[HIGH] UserService.java:8 - 민감정보: DB_PASSWORD 하드코딩 발견
[HIGH] UserService.java:9 - 민감정보: API_KEY 하드코딩 발견
[HIGH] UserService.java:22 - 인젝션: 문자열 연결 SQL 쿼리

### ⚡ 성능 검증
[HIGH] UserService.java:33 - 반복: N+1 쿼리 문제 - JOIN 사용 권장
[MED] UserService.java:33 - DB: 반복문 내 쿼리 실행

### 📝 스타일 검증
[MED] UserService.java:51 - 네이밍: 변수명 's' → 'userStatus' 권장
[LOW] UserService.java:53 - 중복: 매직넘버 1 → 상수 정의 권장
[MED] User.java:5 - 구조: public 필드 → private + getter/setter
[LOW] User.java:13 - 컨벤션: @Override 어노테이션 누락

### 🎯 전체 품질 평가
전반적 품질: C
권장: REQUEST_CHANGES

---
*StarCoder2-3B 로컬 모델로 분석됨 - 소스코드 외부 전송 없음*
```

## 🔧 리뷰 결과 해석

### 심각도 레벨 이해

- **HIGH**: 즉시 수정 필요 (보안 취약점, 성능 저하)
- **MED**: 우선순위 있는 개선사항
- **LOW**: 코드 품질 향상을 위한 제안

### 카테고리별 해석

#### 🔒 보안 검증
- 하드코딩된 민감정보 검출
- SQL 인젝션 취약점 발견
- 권한 검사 누락 등

#### ⚡ 성능 검증
- N+1 쿼리 문제
- 비효율적인 알고리즘
- 메모리 누수 가능성

#### 📝 스타일 검증
- 코딩 컨벤션 위반
- 네이밍 규칙 미준수
- 코드 중복 등

#### 🎯 전체 품질 평가
- **A**: 우수한 코드
- **B**: 양호한 코드
- **C**: 개선 필요
- **D**: 대대적 수정 필요

## 🎛️ 리뷰 동작 확인

### 1. 다양한 시나리오 테스트

#### 클린 코드 테스트
문제가 없는 깨끗한 코드로 PR 생성:

```java
package com.example.service;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CleanUserService {

    private static final int ACTIVE_STATUS = 1;
    private final UserRepository userRepository;

    public CleanUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == ACTIVE_STATUS)
                .orElse(false);
    }
}
```

예상 결과:
```
## 🤖 StarCoder2 코드 리뷰 결과

✅ **검토 완료**

코드를 검토한 결과 특별한 이슈가 발견되지 않았습니다.

전반적 품질: A
권장: APPROVE
```

#### Draft PR 테스트
Draft 상태의 PR 생성하여 건너뛰기 동작 확인:

```bash
# 로그에서 Draft PR 건너뛰기 확인
grep "Skipping draft PR" logs/pr-review-bot.log
```

### 2. 성능 테스트

대용량 diff로 처리 성능 확인:

```bash
# 처리 시간 측정
grep "code review.*completed in" logs/pr-review-bot.log
# 2024-01-20 14:30:25 - Code review for PR #1 completed in 8.3s
```

### 3. 에러 처리 테스트

의도적으로 에러 상황 생성:

```bash
# Ollama 서비스 중지
sudo systemctl stop ollama

# PR 생성 후 에러 처리 확인
grep "Error during code review" logs/pr-review-bot.log
```

## 🚨 일반적인 첫 리뷰 문제

### 1. 리뷰 코멘트가 생성되지 않음

**점검 항목:**
```bash
# 웹훅 수신 확인
grep "webhook" logs/pr-review-bot.log

# LLM 모델 응답 확인
grep "ChatResponse" logs/pr-review-bot.log

# Gitea API 호출 확인
grep "Posted review comment" logs/pr-review-bot.log
```

### 2. 부정확한 리뷰 결과

**원인 및 해결:**
- 모델 버전 확인: `ollama list`
- 프롬프트 템플릿 확인
- chunk 크기 조정 (너무 클 경우)

### 3. 처리 시간이 오래 걸림

**최적화 방법:**
```yaml
# chunk 크기 감소
bot:
  review:
    chunk-size: 50  # 기본 100에서 감소

# 병렬 처리 활성화 확인
    parallel-processing: true
```

## ✅ 성공적인 첫 리뷰 체크리스트

- [ ] 웹훅이 정상 수신됨
- [ ] PR 정보가 정확히 파싱됨
- [ ] LLM 모델이 응답함
- [ ] 코드 이슈가 정확히 검출됨
- [ ] Gitea PR에 코멘트가 작성됨
- [ ] 심각도와 카테고리가 올바름
- [ ] 전체 처리 시간이 합리적임 (< 30초)

## 📈 리뷰 품질 향상

### 1. 팀 규칙 프롬프트 커스터마이징

팀의 특정 코딩 규칙을 추가:

```markdown
# 추가 팀 규칙
- 모든 public 메서드에 JavaDoc 필수
- 예외 처리시 로깅 필수
- 매직 넘버 금지
- Builder 패턴 사용 권장
```

### 2. False Positive 최소화

```yaml
bot:
  review:
    # 정적 분석 임계값 조정
    static-analysis-threshold: 0.7

    # 컨텍스트 윈도우 증가
    context-lines: 10
```

## 📚 다음 단계

첫 번째 리뷰 테스트가 성공적으로 완료되면:

1. [프롬프트 커스터마이징](../configuration/03-prompt-customization.md)으로 팀 규칙 반영
2. [성능 튜닝 가이드](../configuration/04-performance-tuning.md)로 최적화
3. [실제 개발 워크플로우에 통합](02-workflow-integration.md)
4. [팀 온보딩 가이드](03-team-onboarding.md)로 팀원들에게 소개