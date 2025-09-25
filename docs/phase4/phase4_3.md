# Phase 4.3: 첫 번째 리뷰 테스트

## 테스트용 문제 코드 작성
```java
// UserService.java - 의도적인 문제 포함
public class UserService {
    private static final String API_KEY = "sk-123456789"; // 보안 이슈

    public User findUser(String name) {
        String sql = "SELECT * FROM users WHERE name = '" + name + "'"; // SQL 인젝션
        // ...
    }
}
```

## PR 생성 및 테스트
- 새 브랜치 생성: `git checkout -b test/code-review`
- 테스트 파일 추가 및 커밋
- 브랜치 푸시: `git push origin test/code-review`
- Gitea에서 Pull Request 생성
- 웹훅 수신 로그 확인: `tail -f logs/pr-review-bot.log`

## 리뷰 결과 확인
- 2분 내에 자동 코멘트 생성 확인
- 보안 이슈 검출 확인 (API_KEY 하드코딩)
- SQL 인젝션 위험 지적 확인
- 전체 품질 등급 확인 (C 또는 D 예상)
