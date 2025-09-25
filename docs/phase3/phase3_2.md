# Phase 3.2: Gitea 토큰 생성

## Personal Access Token 생성
- Gitea 로그인 → Settings → Applications
- "Generate New Token" 클릭
- Token Name: `pr-review-bot`
- 필수 권한 선택:
  - `repo` (리포지토리 접근)
  - `write:repository` (PR 코멘트 작성)
- 토큰 생성 및 안전한 곳에 보관

## 토큰 검증
- `.env` 파일에 `GITEA_TOKEN=your_token_here` 추가
- 토큰 테스트: `curl -H "Authorization: token $GITEA_TOKEN" $GITEA_BASE_URL/api/v1/user`
