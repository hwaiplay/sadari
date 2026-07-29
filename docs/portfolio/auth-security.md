# 인증과 보안

## 문서 목적

- 목적: Sadari의 로그인 수명주기와 입력·외부 통신 보안 설계를 설명
- 적용 범위: Kakao OAuth, JWT, Redis, 탈퇴 재인증, 파일 업로드, 표지 URL 검증
- 기준일: 2026-07-30

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
- NAVER 도서 이미지 호스트 `shopping-phinf.pstatic.net`
- 사용자 정보가 없는 URI
- 기본 포트 또는 443
- 최대 5MB 응답
- 최대 가로·세로 4096픽셀

HttpClient는 리다이렉트를 따르지 않으며 타임아웃을 사용한다.

구현 근거:

- `src/main/java/org/our/sadari/book/service/BookCoverColorService.java`
- `src/test/java/org/our/sadari/book/service/BookCoverColorServiceTest.java`

## 탈퇴 재인증과 데이터 정책

서비스 탈퇴와 영구 탈퇴는 Kakao 재인증 후 실행한다. 재인증 상태는 일회성 UUID로 Redis에 저장하고 10분 TTL을 적용한다. 콜백에서 같은 Kakao 계정인지 검증한 뒤 상태값을 소비해 재사용을 막는다.

- 서비스 탈퇴: 회원 상태 변경, 독후감 비공개, 댓글 삭제 상태, 알림 삭제, 푸시 중지
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
- Access Token을 Cookie로 사용하는 구조에서 CSRF 위협 모델을 다시 평가하고 필요한 보호를 적용한다.
- 업로드 파일 검사에 악성코드 스캔과 객체 저장소 격리를 추가할 수 있다.
- 보안 테스트를 CI 필수 단계로 연결해야 한다.

