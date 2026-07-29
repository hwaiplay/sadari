# 계정 및 인증 정책

## 로그인

- 인증 제공자는 Kakao OAuth입니다.
- Kakao 사용자 식별값은 암호화하여 `TM_USERXM.USER_IDXX`에 저장합니다.
- 로그인 성공 시 Access Token과 Refresh Token을 발급합니다.
- 두 토큰은 JavaScript에서 직접 읽을 수 없는 HttpOnly 쿠키로 전달합니다.
- 운영 환경에서는 `app.cookie.secure=true`를 사용해 HTTPS에서만 인증 쿠키를 전송합니다.

## JWT

- Access Token은 API 인증에 사용하며 회원 번호, 역할, 토큰 식별자 `jti`를 포함합니다.
- Refresh Token은 Access Token 재발급에만 사용합니다.
- 각 토큰의 유효시간은 `jwt.access-token-validity-in-seconds`와 `jwt.refresh-token-validity-in-seconds` 설정을 기준으로 합니다.
- 쿠키의 Max-Age도 같은 초 단위 설정값을 사용합니다.
- Access Token이 만료됐지만 유효한 Refresh Token이 있으면 재발급을 시도합니다.
- Refresh Token이 없거나 유효하지 않으면 로그인 화면으로 이동합니다.
- 재발급 실패를 반복 호출하지 않도록 프론트엔드는 인증 실패 흐름을 한 번만 수행합니다.

## Redis 로그인 정보

로그인 시 아래 정보를 Refresh Token과 같은 세션 수명으로 Redis에 저장합니다.

| 키 형식 | 값 |
| --- | --- |
| `auth:refresh:{userNumb}` | Refresh Token |
| `auth:user:nick:{userNumb}` | 로그인 사용자 닉네임 |
| `auth:user:status:{userNumb}` | 회원 상태 |

- 알림 생성 시 발신자 닉네임은 추가 DB 조회 없이 Redis 값을 사용합니다.
- 닉네임 수정 시 DB와 Redis 닉네임을 함께 변경합니다.
- Redis 로그인 정보가 없으면 알림 같은 부가 처리는 생략할 수 있지만 핵심 업무 결과를 임의로 실패시키지 않습니다.

## 로그아웃

- Access Token의 `jti`를 Redis 블랙리스트에 등록합니다.
- 블랙리스트 TTL은 해당 Access Token의 남은 유효시간만큼 설정합니다.
- Refresh Token, 닉네임, 회원 상태 캐시를 Redis에서 제거합니다.
- Access Token과 Refresh Token 쿠키를 즉시 만료시킵니다.
- 블랙리스트에 등록된 Access Token은 만료 전이라도 인증에 사용할 수 없습니다.

## 회원 상태별 접근

| 상태 | 접근 정책 |
| --- | --- |
| `ACTIVE` | 정상 서비스 이용 |
| `WITHDRAWN` | 일반 서비스 이용 제한, 재로그인 시 복구 흐름 적용 |
| `DELETE_PENDING` | 영구 삭제 대기 화면, 탈퇴 취소, 로그아웃 및 최소 인증 API만 허용 |

## 구현 근거

- `global/security/jwt/JwtProvider.java`
- `global/security/jwt/JwtFilter.java`
- `global/security/jwt/TokenRedisService.java`
- `user/auth/controller/AuthLoginController.java`
- `user/auth/service/AuthServiceImpl.java`
