# Phase 6.2: 로그 관리 설정

## 로그 로테이션 설정
```bash
# /etc/logrotate.d/pr-review-bot
sudo tee /etc/logrotate.d/pr-review-bot << EOF
/path/to/logs/pr-review-bot.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    create 644 app app
    postrotate
        systemctl reload pr-review-bot
    endscript
}
EOF
```

## 로그 레벨 조정 (프로덕션)
```yaml
# application-production.yml
logging:
  level:
    root: INFO
    com.gitea.prbot: INFO
```
