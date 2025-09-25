# Phase 4.2: Gitea 웹훅 설정

## 테스트 리포지토리 선택
- 코드 리뷰 대상 리포지토리 선택
- 또는 테스트 전용 리포지토리 생성

## 웹훅 생성
- 리포지토리 → Settings → Webhooks
- "Add Webhook" → "Gitea" 선택
- **Target URL**: `http://your-server:8080/api/webhook/gitea`
- **HTTP Method**: POST
- **Content Type**: application/json
- **Secret**: `.env` 파일의 `GITEA_WEBHOOK_SECRET` 값과 동일
- **Trigger Events**: "Pull Request" 체크
- "Active" 체크
- "Add Webhook" 클릭
