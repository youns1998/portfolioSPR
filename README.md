# portfolioSPR — 무명광장

회원가입 없이 관심 있는 게시판에 글과 댓글을 작성하는 Spring Boot 커뮤니티입니다.

## 주요 기능

- 기본 게시판 4개: 인기글, 게임, 음악, 운동 (게시판 생성 화면과 요청 처리는 임시 주석 처리)
- 시작 시 각 게시판에 더미 게시글 45개 자동 등록 (총 180개, 인기글도 독립 게시판)
- 게시글 생성·조회·수정·삭제, 검색, 20개 단위 페이지네이션, 조회 수
- 비회원 댓글 생성·수정·삭제
- 게시판·게시글·댓글은 작성 시 설정한 비밀번호로 수정·삭제 (4~64자, 해시로 저장)
- 댓글 권한은 IP가 아닌 비밀번호로 확인하므로 같은 IP를 공유해도 다른 비밀번호의 댓글은 수정·삭제할 수 없음
- IP 원문은 저장하지 않으며 화면에는 마스킹된 값과 익명 별칭만 표시
- 서버 렌더링 UI와 모바일 반응형 레이아웃

## 기술 구성

- Java 21
- Spring Boot 4.1.2
- Spring MVC, Thymeleaf, Spring Data JPA, Bean Validation
- H2 인메모리 데이터베이스
- Maven Wrapper

## 실행

Java 21 이상이 필요합니다.

```powershell
.\mvnw.cmd spring-boot:run
```

브라우저에서 `http://localhost:8080`을 엽니다.

테스트:

```powershell
.\mvnw.cmd test
```

## 데이터베이스 정책

`jdbc:h2:mem:portfolio`를 사용하므로 데이터베이스 파일은 생성되지 않습니다. 애플리케이션을 종료하면 게시판·게시글·댓글 데이터가 모두 초기화됩니다.

재시작하면 기본 게시판과 더미 글이 다시 준비됩니다. 더미 게시판 관리 비밀번호는 `board-demo-1234`, 더미 게시글 수정·삭제 비밀번호는 `post-demo-1234`입니다.

운영 환경에서는 댓글 IP 해시에 사용할 비밀값을 반드시 변경하세요.

```powershell
$env:APP_IP_HASH_SECRET = "충분히-길고-무작위인-문자열"
.\mvnw.cmd spring-boot:run
```

프록시 환경에서는 애플리케이션에 전달되는 원격 주소가 실제 방문자 IP가 되도록 신뢰할 수 있는 프록시에서 설정해야 합니다. 임의의 `X-Forwarded-For` 헤더를 애플리케이션이 직접 신뢰하지 않도록 구성되어 있습니다.

## 코드 위치

- `domain/` — 게시판, 게시글, 댓글 엔티티
- `repository/` — JPA 저장소와 검색 쿼리
- `service/` — CRUD, 비밀번호 확인 및 익명 IP 표시 처리
- `web/` — MVC 컨트롤러, 입력 폼, 오류 처리
- `templates/` — Thymeleaf 화면
- `static/css/app.css` — 전체 스타일
