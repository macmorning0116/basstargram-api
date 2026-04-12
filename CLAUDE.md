# CLAUDE.md

## 목적
- 이 문서는 `/Users/kim-yechan/Desktop/fishing-api` 에서 작업하는 AI 코딩 에이전트를 위한 작업 기준서입니다.
- 이 프로젝트는 Spring Boot + Java 17 + PostgreSQL 기반의 낚시 서비스 백엔드 API 서버입니다.
- 프론트엔드는 `/Users/kim-yechan/Desktop/fishing-frontend` (React 19 + TypeScript + Vite)에 있습니다.

## 제품 맥락
- 소셜 로그인(카카오/구글) 기반 OAuth 2.0 인증
- 조행 기록 커뮤니티 (게시글, 댓글, 좋아요, 신고)
- 네이버 지도 기반 위치 서비스 (역지오코딩)
- OpenWeatherMap 날씨 API 연동
- GPT 기반 조행 분석
- OpenSearch 기반 게시글 검색

## 기술 스택
- Java 17, Spring Boot 3.4
- PostgreSQL 16 (Docker: `fishing-crawler-postgres` 컨테이너, 포트 5433, DB: `fishing_api`)
- Flyway 마이그레이션 (V1~V5)
- JPA (Hibernate 6) + PostGIS
- JWT 인증 (access 30분 + refresh 14일, HttpOnly 쿠키)
- Gradle 9, Spotless 코드 포맷터
- Lombok, Jackson

## 자주 쓰는 명령어
- 빌드: `./gradlew build`
- 컴파일만: `./gradlew compileJava`
- 코드 포맷: `./gradlew spotlessApply`
- 테스트: `./gradlew test`
- 포맷 + 빌드: `./gradlew spotlessApply build`

## 디렉터리 가이드
- `src/main/java/.../domain/auth/` — 인증 도메인
  - `controller/AuthController.java` — OAuth 로그인, 토큰 갱신, 로그아웃
  - `service/AuthService.java` — 소셜 로그인 흐름, JWT 발급
  - `entity/User.java` — 유저 엔티티 (PENDING → ACTIVE 상태 전이)
  - `jwt/JwtTokenProvider.java` — JWT 생성/검증
  - `security/JwtAuthenticationFilter.java` — Bearer 토큰 필터
  - `security/CurrentUserArgumentResolver.java` — @CurrentUser 어노테이션 처리
  - `client/` — 카카오/구글 소셜 로그인 클라이언트
  - `cookie/RefreshTokenCookieManager.java` — HttpOnly 쿠키 관리
- `src/main/java/.../domain/user/` — 유저 프로필 관리
  - `controller/UserController.java` — 프로필 조회/수정, 닉네임 변경/중복체크
  - `service/UserService.java` — 프로필 완성, 닉네임 30일 쿨다운, 프로필 사진
- `src/main/java/.../domain/community/` — 커뮤니티 도메인
  - `controller/CommunityController.java` — 게시글/댓글/좋아요/신고 CRUD
  - `service/CommunityService.java` — 비즈니스 로직, 카운터 atomic update
  - `entity/` — CommunityPost, CommunityComment, CommunityPostLike 등
  - `storage/` — 이미지 저장 (로컬 파일시스템)
- `src/main/java/.../domain/map/` — 네이버 역지오코딩
- `src/main/java/.../global/` — 공통 설정
  - `exception/ErrorCode.java` — 에러 코드 (httpStatus 포함)
  - `exception/GlobalExceptionHandler.java` — 예외 → HTTP 응답 매핑
  - `response/ApiResponse.java` — 응답 래퍼 {success, data, error}
  - `config/WebConfig.java` — CORS, ArgumentResolver, 필터 등록
- `src/main/resources/db/migration/` — Flyway SQL 마이그레이션

## 인증 흐름
1. `GET /v1/auth/{provider}/authorize-url` → OAuth URL + state 토큰
2. 프론트에서 OAuth 리다이렉트 → 콜백 → `POST /v1/auth/{provider}/code`
3. 신규 유저: `status=PENDING`, `needsProfileSetup=true`
4. `POST /v1/users/me/profile` → 닉네임 설정 → `status=ACTIVE`
5. Access Token: Authorization Bearer 헤더, Refresh Token: HttpOnly 쿠키

## API 응답 형식
```json
{
  "success": true,
  "data": { ... }
}
```
에러 시:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "한국어 에러 메시지"
  }
}
```

## 동시성 제어
- 좋아요/댓글/신고 카운터는 DB atomic update 사용 (Repository @Query)
- 엔티티의 increment/decrement 메서드 대신 `communityPostRepository.incrementLikeCount(postId)` 사용
- 좋아요 멱등성: `UNIQUE (post_id, user_id)` + existsBy 체크
- 신고 멱등성: `UNIQUE (reporter_user_id, target_type, target_id)` + 중복 시 409

## 코드 작성 원칙
- ErrorCode에 httpStatus 필드 포함 (3인자 생성자: httpStatus, code, message)
- GlobalExceptionHandler가 ResponseEntity<ApiResponse<Void>>로 적절한 HTTP 상태 반환
- 새 에러 코드 추가 시 반드시 httpStatus 지정
- 코드 수정 후 반드시 `./gradlew spotlessApply` 실행
- PENDING 유저도 일부 엔드포인트 접근 가능 (CurrentUserArgumentResolver에서 처리)

## DB 접근 (개발)
```bash
docker exec fishing-crawler-postgres psql -U fishing fishing_api
```
- 유저 테이블 초기화: `TRUNCATE users, user_refresh_tokens RESTART IDENTITY CASCADE;`

## 수정 시 주의사항
- Flyway 마이그레이션은 한번 적용되면 수정 불가. 새 버전으로 추가
- @ConditionalOnBean은 JPA 리포지토리와 함께 쓰면 빈 등록 타이밍 이슈 발생 가능
- 카운터 업데이트는 반드시 Repository의 atomic @Query 사용 (엔티티 메서드 X)
- uploads/ 디렉터리는 gitignore 대상

## 커밋 메시지 규칙
- 예시: `feat: 피드 authorId 필터링 지원`
- 예시: `fix: 좋아요 카운터 동시성 제어 적용`
- `했습니다` 같은 서술형보다 `추가`, `수정`, `개선` 형태를 선호합니다.
