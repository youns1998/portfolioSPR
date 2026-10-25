# 사업·업무 관리

사업 등록부터 업무 처리·이력 조회·CSV 인계까지 구현한 웹 애플리케이션입니다. 시연 데이터는 가상 사업입니다.

**Java 21 · Spring Boot 4.1.2 · Spring Security · JPA · Thymeleaf · H2**

## 실행

Java 21 이상을 준비하고 저장소 루트에서 실행합니다. 최초 의존성 다운로드에는 인터넷이 필요합니다.

```powershell
.\mvnw.cmd spring-boot:run
```

접속: **http://127.0.0.1:8080/**. macOS·Linux에서는 `bash mvnw spring-boot:run`을 사용합니다.

| 계정 | 비밀번호 | 권한 |
|---|---|---|
| `editor` | `Portfolio-2026!` | 조회·편집·업무 처리·CSV |
| `viewer` | `View-2026!` | 조회·검색·CSV |

로컬 시연 계정이며 `APP_EDITOR_PASSWORD`, `APP_VIEWER_PASSWORD`로 변경합니다. 첫 실행에 사업 4개·업무 12개를 등록하고, `data/portfolio.mv.db`에 저장합니다.

## 기능

- 사업 CRUD, 담당·기간·상태 관리, 검색·페이지·현황 집계
- 업무 접수 → 처리 중 → 완료, 필수 조치 메모와 처리 이력
- 조회·편집 권한 분리, CSRF, 입력 검증, 오래된 수정 요청 거절
- 미완료 업무가 있는 사업의 완료 거절, 연결 업무가 있는 사업의 삭제 거절, CSV 출력

## 코드 구조

`src/main/java/com/youns/portfoliospr/` 기준입니다.

```text
workspace/          # 주력 사업·업무 관리
  web/              # 요청·화면, web/form/ 입력 DTO
  service/          # 업무 규칙·트랜잭션
  domain/           # 엔티티·상태
  repository/       # DB 조회
  config/           # 로그인 정책·시연 데이터
community/          # 보존한 기존 커뮤니티 모듈
common/             # 공통 예외
```

화면은 `src/main/resources/templates/workspace/`, 스타일은 `static/workspace-assets/`에 있습니다. 테스트도 `workspace/`와 `community/`로 구분합니다.

처음 읽을 코드: [컨트롤러](src/main/java/com/youns/portfoliospr/workspace/web/WorkspaceController.java) → [서비스](src/main/java/com/youns/portfoliospr/workspace/service/WorkspaceService.java) → [테스트](src/test/java/com/youns/portfoliospr/workspace/WorkspaceTests.java).

## 검증

```powershell
.\mvnw.cmd test
```

**Java 테스트 32개**, 실제 브라우저 검사 13개 통과. 실행 JAR 재시작 후 데이터 유지도 확인했습니다. 테스트 DB는 별도 인메모리 DB입니다.

기존 `/community`는 별도 학습 모듈이며 `/workspace/**` 로그인 정책의 적용 대상이 아닙니다. 운영용 사용자 관리·첨부파일·전자결재는 포함하지 않습니다.

[실행 화면·개발 안내](docs/development.md) · [검증 기록](docs/verification.md)
