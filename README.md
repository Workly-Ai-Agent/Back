# Workly Backend

Workly의 Kotlin/Spring Boot API 서버입니다. 사용자 인증, 워크스페이스와 프로젝트 권한, Task 및 제안 저장, 실시간 채팅을 담당하고 Python AI Agent와 내부 HTTP 요청으로 연결됩니다.

## 기술 구성

- Kotlin, Java 21, Spring Boot
- Spring Security와 JWT 인증
- Spring Data JPA
- H2 로컬 개발 데이터베이스, PostgreSQL 배포 데이터베이스
- WebSocket/STOMP 채팅

## 로컬 실행

Java 21이 필요합니다. 기본 `application.yaml` 설정은 로컬 H2를 사용합니다.

```bash
./gradlew bootRun
```

기본 API 주소는 `http://localhost:8080/api`이며 헬스 체크는 `/api/health`입니다. AI 기능을 사용하려면 AI Agent 서버도 실행하고 아래 환경 변수를 설정하세요.

## 환경 변수

| 변수 | 용도 | 기본값 또는 예시 |
| --- | --- | --- |
| `AGENT_BASE_URL` | AI Agent 서비스의 루트 주소 | `http://localhost:8000` |
| `AGENT_REQUEST_TIMEOUT_SECONDS` | Agent 응답 대기 제한(초) | `600` |
| `JWT_SECRET` | JWT 서명 키 | 로컬 개발 기본값 사용 가능, 배포에서는 강한 비밀 값 필수 |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트엔드 출처 | 로컬 개발은 `http://localhost:5173` |
| `SPRING_DATASOURCE_URL` | 배포 PostgreSQL JDBC 주소 | 배포 환경에서 설정 |
| `SPRING_DATASOURCE_USERNAME` | 데이터베이스 사용자 | 배포 환경에서 설정 |
| `SPRING_DATASOURCE_PASSWORD` | 데이터베이스 비밀번호 | 배포 환경에서 설정 |

`AGENT_BASE_URL`에는 Agent 서버의 루트 주소를 넣습니다. `/api/agent/workflow` 경로는 백엔드가 자동으로 붙입니다. AI 제안 처리는 여러 모델 호출로 오래 걸릴 수 있어 기본 제한은 10분입니다. 필요하면 `AGENT_REQUEST_TIMEOUT_SECONDS`로 조정할 수 있습니다.

## AI 제안과 Task 반영

`POST /api/projects/{projectId}/agent/generate`는 AI 워크플로우를 실행하고 대기 중인 제안을 저장합니다. 제안의 Task는 바로 업무 테이블에 추가되지 않습니다. 프로젝트 Leader가 `POST /api/agent/proposals/{proposalId}/approve`를 호출해야 승인된 내용이 Task에 반영됩니다.

## 관련 저장소

- [Front](https://github.com/Workly-Ai-Agent/Front) — React 사용자 인터페이스
- [AI-Agent](https://github.com/Workly-Ai-Agent/AI-Agent) — FastAPI 기반 AI 워크플로우
