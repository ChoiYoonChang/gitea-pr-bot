# Phase 4.1: 애플리케이션 시작

## 애플리케이션 실행
- `./mvnw spring-boot:run` 또는 `java -jar target/*.jar`
- 시작 로그에서 오류 없음 확인
- 포트 8080 바인딩 확인

## 헬스 체크
- `curl http://localhost:8080/api/webhook/health` → "OK" 응답
- `curl http://localhost:8080/actuator/health` → `{"status":"UP"}` 응답
- 애플리케이션 로그에서 Gitea service initialized 확인
