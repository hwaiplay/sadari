# 계정 및 인증 정책

## 적용 범위

- 기준일은 2026년 8월 11일입니다.
- 사용자 로그인, 인증 토큰, 회원 상태별 접근과 관리자 현재 사용자 조회에 적용합니다.

## 로그인

- 인증 제공자는 Kakao OAuth입니다.
- 로그인 제공자 코드는 축약형 대신 `KAKAO`, `NAVER`, `GOOGLE` 풀네임을 사용하며 로그인 제공자 코드 공통코드로 관리합니다.
- 회원 원본의 제공자 코드는 사용자 계정과 프로필의 로그인 제공자 코드, 로그인 이력의 제공자 코드는 사용자 로그인 이력의 로그인 제공자 코드에 같은 값으로 저장합니다.
- Kakao 사용자 식별값은 암호화하여 사용자 계정과 프로필의 OAuth 사용자 식별값 암호문에 저장합니다.
- 로그인 성공 시 Access Token과 Refresh Token을 발급합니다.
- 두 토큰은 JavaScript에서 직접 읽을 수 없는 HttpOnly 쿠키로 전달합니다.
- 운영 환경에서는 `app.cookie.secure=true`를 사용해 HTTPS에서만 인증 쿠키를 전송합니다.
- 기기별 `sid` 적용 전 발급된 기존 JWT에는 세션 식별자가 없으므로 배포 후 한 번 다시 로그인해야 합니다.

## JWT

- Access Token은 API 인증에 사용하며 회원 번호, 역할, 토큰 식별자 `jti`, 기기별 세션 식별자 `sid`를 포함합니다.
- Refresh Token은 Access Token 재발급에만 사용하며 Access Token과 같은 `sid`를 포함합니다.
- 각 토큰의 유효시간은 `jwt.access-token-validity-in-seconds`와 `jwt.refresh-token-validity-in-seconds` 설정을 기준으로 합니다.
- 쿠키의 Max-Age도 같은 초 단위 설정값을 사용합니다.
- Access Token이 만료됐지만 유효한 Refresh Token이 있으면 재발급을 시도합니다.
- Refresh Token이 없거나 유효하지 않으면 로그인 화면으로 이동합니다.
- 재발급 실패를 반복 호출하지 않도록 프론트엔드는 인증 실패 흐름을 한 번만 수행합니다.
- 여러 탭과 서비스 워커가 동시에 같은 Refresh Token으로 재발급하면 Redis 원자 회전이 최초 결과 하나를 저장하고, `jwt.refresh-rotation-grace-in-seconds` 동안 나머지 요청에도 같은 최신 Refresh Token을 반환합니다.

## Redis 로그인 정보

로그인 시 아래 정보를 Redis에 저장합니다. 세션 종속 키와 계정 상태 키는 로그아웃·만료 정책이 서로 다릅니다.

| 키 형식 | 자료형과 값 | 만료·삭제 기준 |
| --- | --- | --- |
| `auth:session:{sid}` | Hash, 기기별 현재·직전 Refresh Token, 회원 번호와 동시 회전 유예시각 | 로그인 시 Refresh Token 유효시간을 적용하고 재발급 시 갱신하며 현재·전체 로그아웃 시 대상 세션 삭제 |
| `auth:user:sessions:{userNumb}` | Set, 회원에게 연결된 기기별 `sid` 목록 | 로그인·재발급 시 Refresh Token 유효시간을 적용하고 현재 로그아웃 시 해당 `sid`, 전체 로그아웃 시 Set 삭제 |
| `auth:user:nick:{userNumb}` | String, 로그인 사용자 닉네임 | 로그인 또는 닉네임 수정 시 활성 세션 TTL을 적용하고 마지막 세션 또는 전체 로그아웃 시 삭제하며 인증 필수 데이터로 사용하지 않음 |
| `auth:user:status:{userNumb}` | String, 회원 상태 | TTL 없이 유지하고 회원 물리 삭제 시 삭제 |

- 로그인할 때마다 새로운 `sid`를 생성하므로 다른 디바이스 로그인은 기존 세션을 덮어쓰지 않습니다.
- 같은 브라우저 프로필의 탭은 인증 Cookie를 공유하므로 일반적으로 하나의 `sid` 세션을 함께 사용합니다.
- Access Token은 로그인 시 Redis에 저장하지 않으며, 로그아웃된 `jti`만 남은 유효시간 동안 `auth:blacklist:access:{jti}`에 저장합니다.
- 알림 생성 시 발신자 닉네임은 추가 DB 조회 없이 Redis 값을 사용합니다.
- 닉네임 수정 시 DB와 Redis 닉네임을 함께 변경합니다.
- Redis 로그인 정보가 없으면 알림 같은 부가 처리는 생략할 수 있지만 핵심 업무 결과를 임의로 실패시키지 않습니다.
- 회원 상태 캐시는 로그인 세션 삭제와 분리하고, 캐시가 없으면 `활성`로 추정하지 않고 DB 원본 상태를 조회해 보정합니다.
- 구형 `auth:refresh:{userNumb}` 키는 현재 인증과 로그아웃에서 사용하지 않습니다. 모든 구버전 서버 종료 후 남은 TTL 만료를 기다리거나 운영 절차에 따라 정리합니다.

## 로그아웃

- Access Token의 `jti`를 Redis 블랙리스트에 등록합니다.
- 블랙리스트 TTL은 해당 Access Token의 남은 유효시간만큼 설정합니다.
- 로그아웃 Alert에서 `현재 디바이스 로그아웃`, `전체 디바이스 로그아웃`, `취소` 중 하나를 선택합니다.
- 현재 디바이스 로그아웃은 현재 `sid` 세션과 현재 브라우저 푸시 구독만 제거합니다. 같은 브라우저 프로필의 탭은 인증 Cookie를 공유하므로 함께 로그아웃됩니다.
- 전체 디바이스 로그아웃은 회원의 모든 `sid` 세션과 모든 푸시 구독을 비활성화합니다.
- 전체 디바이스 로그아웃을 실행한 다른 디바이스는 다음 인증 요청에서 세션 무효화가 확인되어 로그아웃됩니다.
- 일반 로그아웃은 회원 상태 캐시를 삭제하지 않습니다.
- Access Token과 Refresh Token 쿠키를 즉시 만료시킵니다.
- 블랙리스트에 등록된 Access Token은 만료 전이라도 인증에 사용할 수 없습니다.
- 로그아웃 완료는 `BroadcastChannel`과 `storage` 이벤트로 같은 브라우저의 다른 탭에 전달해 인증 Store와 Query 캐시를 즉시 정리합니다.

## 회원 상태별 접근

| 상태 | 접근 정책 |
| --- | --- |
| `활성` | 정상 서비스 이용 |
| `비활성화` | 계정 비활성화 상태, 일반 서비스 이용 제한, 재로그인 시 재활성화 흐름 적용 |
| `이용정지` | 관리자 이용 정지 상태, 정지 안내·로그아웃 및 최소 인증 API만 허용 |
| 영구 탈퇴 대기 | 영구 삭제 대기 화면, 탈퇴 취소, 로그아웃 및 최소 인증 API만 허용 |

## 관리자 이용 정지 계정 로그인

- 이용 정지 중에는 사용자 계정과 프로필 원본 행과 암호화된 OAuth 사용자 식별값 암호문을 유지합니다.
- 같은 Kakao 계정으로 로그인하면 기존 회원 행을 조회하므로 신규 가입으로 우회하지 못하고 `이용정지` 상태를 계속 적용합니다.
- 관리자는 사용자 Redis에 직접 연결하지 않고 회원 상태 변경과 사용자 상태 변경 Outbox 이벤트를 같은 DB 트랜잭션으로 저장합니다.
- 사용자 서버는 `사용자 계정 상태사용자 상태 동기화` 스케줄러를 5분마다 실행해 이벤트 대상의 현재 DB 상태를 `auth:user:status:{userNumb}`에 반영합니다.
- 따라서 이미 로그인한 세션의 관리자 정지·해제 반영은 정상 운영 시 최대 5분이 걸릴 수 있습니다.
- Redis 반영에 실패한 사용자 상태 변경 Outbox 이벤트는 삭제하지 않고 다음 5분 주기에 재시도합니다.
- Redis 반영에 성공하면 이용정지의 사용자 서버 반영 상태를 완료로 변경한 뒤 임시 전달 이벤트를 삭제합니다.
- 회원 상태 동기화를 위해 FCM 푸시를 발송하거나 사용자 브라우저의 수신 여부에 의존하지 않습니다.
- 기간 정지는 종료 시각이 지난 뒤 정지 상태 조회 또는 로그인 시 만료 처리하고 정지 직전 회원 상태로 복구합니다.
- 다른 Kakao 계정을 새로 만든 사용자는 기존 계정과 동일인임을 신뢰성 있게 식별할 수 없으므로 자동 차단 범위에 포함하지 않습니다.
- 다른 Kakao 계정까지 연결하려고 기기 지문, IP 또는 이름을 강제 식별자로 사용하지 않습니다.

## 비활성화 계정 복귀

- `비활성화` 사용자가 같은 Kakao 계정으로 다시 로그인하면 회원 상태를 `활성`로 변경합니다.
- OAuth 콜백은 이번 로그인에서 계정이 다시 활성화됐는지 일회성 표시로 프론트엔드에 전달합니다.
- 인증 확인이 끝나면 “다시 돌아와서 반가워요” 팝업과 함께 비활성화 정책을 다시 안내합니다.
- 비활성화 과정에서 비공개로 전환된 독후감 공개 설정, 삭제 상태가 된 댓글과 알림, 중지된 푸시 구독은 자동 복원하지 않습니다.
- 복귀 안내를 닫은 뒤 정상 서비스 화면으로 이동하며, 일반 로그인에는 해당 팝업을 표시하지 않습니다.

## 관리자 현재 사용자 조회

- 관리자 `현 사용자 관리`는 사용자 계정과 프로필에 현재 원본 행이 존재하는 회원만 조회합니다.
- 목록 기본 조건은 `활성`이며 검색 시 `비활성화`, `이용정지`, 영구 탈퇴 대기를 포함한 전체 상태를 선택할 수 있습니다.
- 로그인 제공자는 로그인 제공자 코드 공통코드 선택값으로 검색하며 목록·상세·로그인 이력의 표시명은 `공통코드 이름 조회 함수`으로 조회합니다.
- 관리자 기능은 목록, 검색, 상세, 로그인 이력, 계정 처리 이력 및 이용 정지 이력을 제공합니다.
- 쓰기 권한이 있는 관리자는 기간 정지를 등록·해제할 수 있고, `SUPER` 권한만 무기한 정지를 등록할 수 있습니다.
- 비활성화 복구, 영구 탈퇴 취소, 강제 로그아웃 및 일반 회원 데이터 수정 기능은 제공하지 않습니다.
- 암호화된 외부 사용자 식별값인 사용자 계정과 프로필의 OAuth 사용자 식별값 암호문은 관리자 API 응답과 화면에 제공하지 않습니다.
- 로그인 이력의 IP는 서버 SQL에서 일부 마스킹한 값만 제공하며 원문 IP는 관리자 API에 제공하지 않습니다.
- 계정 처리 이력은 코드화된 처리 방식, 사유, 상태와 처리 일시만 제공하며 자유 입력 사유와 내부 오류문구는 제공하지 않습니다.
- 영구 삭제가 완료되어 사용자 계정과 프로필 원본 행이 제거된 회원은 현재 사용자 목록과 상세에서 조회하지 않습니다.

## 구현 근거

- `src/main/java/org/our/sadari/global/security/jwt/JwtProvider.java`
- `src/main/java/org/our/sadari/global/security/jwt/JwtFilter.java`
- `src/main/java/org/our/sadari/global/security/jwt/TokenRedisService.java`
- `src/main/java/org/our/sadari/global/scheduler/service/UserStatusEventServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/UserStatusEventMapper.xml`
- `src/main/java/org/our/sadari/global/security/dto/TokenDto.java`
- `src/main/java/org/our/sadari/user/auth/controller/AuthLoginController.java`
- `src/main/java/org/our/sadari/user/auth/dto/AuthLogoutDto.java`
- `src/main/java/org/our/sadari/user/auth/service/AuthServiceImpl.java`
- `src/main/java/org/our/sadari/push/service/PushServiceImpl.java`
- `src/main/java/org/our/sadari/user/service/UserSuspensionServiceImpl.java`
- `src/main/java/org/our/sadari/user/mapper/UserSuspensionMapper.xml`
- `src/main/frontend/src/pages/Settings/SuspensionPage.tsx`
- `src/main/frontend/src/pages/Oauth/Oauth.tsx`
- `src/main/frontend/src/features/Auth/lib/logoutFlow.ts`
- `src/main/frontend/src/features/Auth/lib/authEvents.ts`
- `src/main/frontend/src/features/Auth/components/AuthSyncProvider.tsx`
- `sadari-admin` 저장소 `src/main/java/org/sadari/admin/sadariadmin/currentuser/controller/CurrentUserController.java`
- `sadari-admin` 저장소 `src/main/java/org/sadari/admin/sadariadmin/currentuser/mapper/CurrentUserMapper.xml`
- `sadari-admin` 저장소 `src/main/frontend/src/pages/currentUser/CurrentUserListPage.tsx`
- `sadari-admin` 저장소 `src/main/frontend/src/pages/currentUser/CurrentUserDetailPage.tsx`