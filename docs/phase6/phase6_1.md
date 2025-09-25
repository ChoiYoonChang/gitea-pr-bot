# Phase 6.1: 서비스 데몬화

## systemd 서비스 생성 (Linux)
```bash
# /etc/systemd/system/pr-review-bot.service
sudo tee /etc/systemd/system/pr-review-bot.service << EOF
[Unit]
Description=PR Review Bot
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/path/to/pr-review-bot
Environment=JAVA_OPTS=-Xmx6g -Xms2g
ExecStart=/usr/bin/java \$JAVA_OPTS -jar target/pr-review-bot-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
```

## 서비스 등록 및 테스트
- `sudo systemctl daemon-reload`
- `sudo systemctl enable pr-review-bot`
- `sudo systemctl start pr-review-bot`
- `sudo systemctl status pr-review-bot` → Active (running) 확인
