# 개발 안내

## 코드 읽는 순서

| 수정할 내용 | 위치 (`src/main/java/com/youns/portfoliospr/` 기준) |
|---|---|
| URL·화면 연결 | `workspace/web/WorkspaceController.java` |
| 입력 항목·검증 | `workspace/web/form/` |
| 업무 상태·완료·삭제 규칙 | `workspace/service/WorkspaceService.java` |
| 데이터 필드·상태 | `workspace/domain/` |
| DB 조회 | `workspace/repository/` |
| 로그인·권한 | `workspace/config/WorkspaceSecurity.java` |
| 최초 시연 데이터 | `workspace/config/WorkspaceDemoData.java` |

화면: `src/main/resources/templates/workspace/` / CSS: `src/main/resources/static/workspace-assets/work.css`.
웹 화면은 GitHub·[Primer](https://primer.style/product/primitives/color/)를 참고한 흰색 본문·회색 헤더·파란색 링크를 사용합니다. 로그인·목록·등록·수정·상세와 기존 커뮤니티 화면에 적용합니다.
기존 커뮤니티는 Java·템플릿·스타일·테스트를 별도 폴더에 보존했습니다. 공통 HTTP 업무 예외는 `common/BusinessException.java`입니다.

요청은 컨트롤러 → 서비스 → Repository → H2 순서로 처리합니다. 서비스가 트랜잭션과 업무 규칙을 맡습니다. 사업·업무에는 `@Version`을 두고, 처리 이력의 사업 ID는 삭제 후 기록을 남기는 논리 참조입니다.

## 실행 화면

![사업 현황](images/workspace-1440.png)
![업무 처리와 이력](images/project-detail.png)

## 시연

1. `editor` 로그인 → 사업 등록 → 업무 등록.
2. 조치 메모와 함께 업무를 접수 → 처리 중 → 완료로 변경.
3. 미완료 업무가 있는 사업의 완료, 업무가 연결된 사업의 삭제 거절 확인.
4. 두 탭에서 같은 사업을 수정해 오래된 수정 요청의 충돌 안내 확인.
5. `viewer` 로그인 후 조회와 CSV 다운로드 확인.

## 추가 명령

저장소 루트에서 실행합니다.

```powershell
.\mvnw.cmd package
java -jar target/portfolio-spr-0.0.1-SNAPSHOT.jar
powershell -ExecutionPolicy Bypass -File scripts/test-browser.ps1
```

브라우저 검사는 실행 중인 서버, Chrome 또는 Edge, 기본 시연 데이터·비밀번호가 필요합니다. 별도 시연 DB는 다음과 같이 지정합니다.

```powershell
$env:APP_DB_URL='jdbc:h2:file:./data/browser-demo;DB_CLOSE_ON_EXIT=FALSE'
.\mvnw.cmd spring-boot:run
```

CI 설정은 [템플릿](ci/verify.yml)입니다. 자동 실행 워크플로로 등록되지 않았습니다.
운영 배포에는 사용자 저장소, HTTPS, 비밀값 관리, DB 마이그레이션·백업, 감사로그 통제와 동시성 검증이 추가로 필요합니다.
