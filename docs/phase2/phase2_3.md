# Phase 2.3: 애플리케이션 설치

## 소스코드 획득
- Git 리포지토리 클론 또는 소스 압축 파일 다운로드
- 프로젝트 디렉토리로 이동
- 파일 권한 확인

## 의존성 설치
- `./mvnw clean compile` 실행
- 컴파일 오류 없이 완료 확인
- 필요한 라이브러리 다운로드 완료

## 빌드 테스트
- `./mvnw clean package -DskipTests` 실행
- JAR 파일 생성 확인: `target/*.jar`
