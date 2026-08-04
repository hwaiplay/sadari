# 인증과 보안

## 문서 목적

- 목적: Sadari의 로그인 수명주기와 입력·외부 통신 보안 설계를 설명
- 적용 범위: Kakao OAuth, JWT, Redis, CSRF, 탈퇴 재인증, 파일 업로드, 표지 URL 검증
- 기준일: 2026-08-04

## Kakao OAuth와 사용자 식별값 보호

Kakao 로그인 성공 시 Provider 사용자 식별값을 내부 회원 조회 키로 사용한다. 이 값은 평문으로 저장하지 않고 `UserIdEncryptionService.encryptForStorage`를 거친 결정적 암호문으로 `TM_USERXM.USER_IDXX`에 저장한다.

결정적 암호화는 같은 식별값을 같은 값으로 조회할 수 있다는 장점이 있지만 패턴 노출 가능성이 있다. 현재 구현은 `AES/ECB/PKCS5Padding`을 사용하므로 포트폴리오에서는 평문 저장을 제거한 단계로 설명하고, 운영 보안 고도화 과제로 AES-GCM 또는 별도 결정적 암호화 전략을 제시하는 것이 정확하다.

구현 근거:

- `src/main/java/org/our/sadari/user/auth/service/AuthServiceImpl.java`
- `src/main/java/org/our/sadari/global/common/service/UserIdEncryptionService.java`

## JWT와 Redis의 역할 분리

### Access Token

- 사용자 번호와 권한을 담는다.
- 유효시간이 짧다.
- API 인증에 사용한다.
- 로그아웃하면 Token ID를 남은 TTL 동안 Redis 블랙리스트에 저장한다.

### Refresh Token

- Access Token 재발급에 사용한다.
- Access Token보다 긴 유효시간을 갖는다.
- Redis의 `auth:refresh:{userNumb}`에 저장해 서버에서 세션을 폐기할 수 있다.

### 로그인 사용자 메타데이터

로그인 시 다음 데이터를 Redis에 함께 저장한다.

- `auth:refresh:{userNumb}`: Refresh Token
- `auth:user:nick:{userNumb}`: 닉네임
- `auth:user:status:{userNumb}`: 회원 상태

`TokenRedisService`는 Lua 스크립트를 사용해 세 값을 같은 TTL로 원자 저장한다. 닉네임 수정 시에도 Lua 스크립트로 기존 TTL을 유지하며 Redis 닉네임을 갱신한다. 알림 발송자가 필요한 경우 DB를 다시 조회하지 않고 Redis 닉네임을 사용할 수 있다.

구현 근거:

- `src/main/java/org/our/sadari/global/security/jwt/TokenRedisService.java`
- `src/main/java/org/our/sadari/user/service/UserServiceImpl.java`

## 쿠키와 요청 인증

Access Token과 Refresh Token은 브라우저 JavaScript에서 읽을 수 없는 HttpOnly Cookie로 전달한다. `Secure`와 `SameSite`는 환경 설정으로 분리해 로컬과 운영 HTTPS 환경을 구분한다.

`JwtFilter`는 다음 순서로 요청을 검증한다.

1. Cookie에서 Access Token을 가져온다.
2. 토큰 서명과 만료시간을 확인한다.
3. Token ID가 Redis 블랙리스트에 있는지 확인한다.
4. 회원 상태가 영구 삭제 대기 상태인지 확인한다.
5. 정상인 경우 Spring Security 인증 객체를 구성한다.

구현 근거:

- `src/main/java/org/our/sadari/user/auth/controller/AuthLoginController.java`
- `src/main/java/org/our/sadari/global/security/jwt/JwtProvider.java`
- `src/main/java/org/our/sadari/global/security/jwt/JwtFilter.java`

## CSRF 보호

Cookie 인증은 브라우저가 다른 출처에서 시작된 요청에도 인증 정보를 자동으로 전송할 수 있으므로 상태 변경 API에 CSRF 보호를 적용한다.

1. `CookieCsrfTokenRepository`가 인증 Cookie와 같은 `Secure`, `SameSite`, `Path` 정책으로 CSRF Cookie를 발급한다.
2. 프론트엔드는 `GET /api/oauth/csrf`의 공통 응답으로 현재 브라우저의 Token을 조회한다.
3. 공통 Axios Request Interceptor가 `POST`, `PUT`, `PATCH`, `DELETE` 요청에 `X-XSRF-TOKEN` Header를 설정한다.
4. Service Worker의 알림 읽음과 Token 재발급 요청도 같은 Token 조회 및 Header 규칙을 사용한다.
5. Token 불일치로 `403`이 발생하면 새 Token을 조회하고 원 요청을 한 번만 재시도한다.

CSRF Token Cookie는 HttpOnly로 유지한다. 프런트와 API가 다른 출처인 운영 구성에서도 프런트가 API 호스트의 Cookie 원문을 직접 읽지 않고, CORS가 허용한 Token 조회 응답만 사용한다.

구현 근거:

- `src/main/java/org/our/sadari/global/security/config/SecurityConfig.java`
- `src/main/java/org/our/sadari/user/auth/controller/AuthLoginController.java`
- `src/main/frontend/src/app/api/axios.ts`
- `src/main/frontend/public/service-worker.js`

## 프론트 재발급 동시성 제어

Access Token이 만료된 상태에서 여러 API가 동시에 실패하면 각 요청이 Refresh API를 호출해 로그인 화면과 원래 화면을 반복할 수 있다.

`src/main/frontend/src/app/api/axios.ts`는 진행 중인 재발급 Promise를 `refreshRequest` 하나로 공유한다. 최초 요청만 Refresh API를 호출하고 나머지는 같은 Promise를 기다린 뒤 원래 요청을 재시도한다. Refresh 또는 Logout 요청 자체는 재발급 대상에서 제외해 순환 호출을 차단한다.

## 관리자 API 문서 보호

Swagger UI와 OpenAPI JSON은 일반 공개 경로가 아니다. `SecurityConfig`에서 다음 경로를 `ADMIN` 역할로 제한한다.

- `/api/admin/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

Controller의 `@Operation`, `@Parameter`, DTO의 `@Schema`로 API 계약을 문서화하면서 문서 자체의 접근 권한도 함께 관리한다.

구현 근거:

- `src/main/java/org/our/sadari/global/security/config/SecurityConfig.java`
- `src/main/java/org/our/sadari/global/common/config/OpenApiConfig.java`

## 관리자 서비스 인증과 권한

`sadari-admin`은 사용자 Kakao OAuth·JWT와 분리된 관리자 인증 체계를 사용한다.

1. `TM_ADMINX`에서 관리자 계정과 권한 코드를 조회한다.
2. 로그인 성공 시 임의 UUID 토큰을 발급한다.
3. 관리자 번호, 권한 코드·레벨, 이름과 부서를 Redis Hash에 저장한다.
4. HttpOnly·SameSite Lax Cookie로 토큰을 전달한다.
5. 요청마다 Redis TTL을 갱신해 활동 중인 세션을 연장한다.
6. `MenuPermissionInterceptor`가 관리 API를 화면 메뉴 URL에 매핑하고 HTTP Method에 따라 조회·쓰기·삭제 권한을 검사한다.

관리자 권한은 `TM_AUTHXM`, `TM_ADMENU`, `TB_AUTHMN`으로 관리하고 `TM_ADMINX.AUTH_CODE`로 관리자에게 그룹을 부여한다. 같은 그룹의 메뉴 권한 변경은 요청마다 DB에서 검사하므로 다음 요청부터 반영된다. 반면 특정 관리자의 권한 그룹 변경은 기존 Redis 세션에 이전 권한 코드가 남아 재로그인이나 세션 무효화가 필요하다.

구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/admin/service/impl/AdminRedisAuthServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/config/RedisAuthenticationFilter.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/config/MenuPermissionInterceptor.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/authgroup/service/impl/AuthGroupServiceImpl.java`

### 확인된 보안 개선점

- 관리자 비밀번호는 현재 단순 SHA-256 비교이므로 BCrypt 또는 Argon2로 교체해야 한다.
- 신규 관리 API는 Spring Security와 권한 인터셉터 경로에 모두 등록해야 한다. 기본 거부 정책과 누락 검증 테스트가 필요하다.
- 권한 그룹을 변경한 관리자의 Redis 세션을 즉시 삭제해야 한다.
- 운영 HTTPS에서는 관리자 인증 Cookie에 `Secure`를 적용해야 한다.

## 이미지 업로드 검증

`FileService`는 파일명 확장자와 클라이언트 Content-Type만 신뢰하지 않는다.

1. 업로드 바이트 크기를 제한한다.
2. JPEG와 PNG 선두 시그니처를 확인한다.
3. ImageIO Reader가 실제 이미지를 해석할 수 있는지 확인한다.
4. 시그니처와 디코더 판독 형식이 같은지 확인한다.
5. 가로·세로 크기와 전체 픽셀 수를 제한해 압축 폭탄 위험을 줄인다.
6. 디코딩한 이미지를 새 JPEG 또는 PNG 바이트로 재인코딩해 메타데이터와 위장 데이터를 제거한다.
7. UUID 기반 저장 파일명을 사용한다.
8. DB 트랜잭션이 롤백되면 생성한 실제 파일도 정리한다.

구현 근거:

- `src/main/java/org/our/sadari/global/file/service/FileService.java`
- `src/test/java/org/our/sadari/global/file/service/FileServiceTest.java`

## 외부 표지 URL의 SSRF 방어

도서 표지 대표색 분석은 서버가 외부 URL을 다운로드하므로 SSRF 위험이 있다. `BookCoverColorService`는 다음 조건을 만족하는 URL만 허용한다.

- HTTPS
- 카카오 도서 이미지 호스트 `search1.kakaocdn.net`과 기존 검색 결과 호환용 NAVER 호스트 `shopping-phinf.pstatic.net`
- 사용자 정보가 없는 URI
- 기본 포트 또는 443
- 최대 5MB 응답
- 최대 가로·세로 4096픽셀

HttpClient는 리다이렉트를 따르지 않으며 타임아웃을 사용한다.

구현 근거:

- `src/main/java/org/our/sadari/book/service/BookCoverColorService.java`
- `src/test/java/org/our/sadari/book/service/BookCoverColorServiceTest.java`

## 탈퇴 재인증과 데이터 정책

계정 비활성화와 영구 탈퇴는 Kakao 재인증 후 실행한다. 재인증 상태는 일회성 UUID로 Redis에 저장하고 10분 TTL을 적용한다. 콜백에서 같은 Kakao 계정인지 검증한 뒤 상태값을 소비해 재사용을 막는다.

- 계정 비활성화: 회원 상태 변경, 독후감 비공개, 댓글 삭제 상태, 알림 삭제, 푸시 중지
- 영구 탈퇴: `DELETE_PENDING`으로 전환하고 기본 30일 유예 후 스케줄러가 물리 삭제
- 영구 탈퇴 취소: 유예기간 동안 계정 복구 가능
- 로그인 이력: 감사 목적 보존
- 탈퇴 이력 OAuth 식별값: 복구할 수 없는 SHA-256 해시로 저장

정책 근거:

- `docs/policies/withdrawal-policy.md`
- `src/main/java/org/our/sadari/user/service/UserWithdrawalServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/service/UserHardDeleteServiceImpl.java`

## 향후 개선

- OAuth 식별값 저장 암호화를 인증된 암호화 방식으로 교체한다.
- 업로드 파일 검사에 악성코드 스캔과 객체 저장소 격리를 추가할 수 있다.
- 보안 테스트를 CI 필수 단계로 연결해야 한다.
- 관리자 계정의 적응형 비밀번호 해시, 로그인 실패 잠금, 세션 강제 만료와 관리 작업 감사 로그를 추가해야 한다.
