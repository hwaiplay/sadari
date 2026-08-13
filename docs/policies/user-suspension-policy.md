# 관리자 회원 이용 정지 정책

## 적용 범위

- 기준일은 2026년 7월 30일입니다.
- 관리자 이용 정지 등록·해제, 사용자 로그인 제한, 기간 만료, 탈퇴와의 상태 우선순위에 적용합니다.

## 권한과 정지 유형

| 유형 | 종료일 | 등록 권한 |
| --- | --- | --- |
| `PERIOD` | 현재보다 미래인 종료일 필수 | 현 사용자 관리 쓰기 권한 |
| `INDEFINITE` | 종료일 없음 | 현 사용자 관리 쓰기 권한을 가진 `SUPER` |

- 프론트엔드 선택지뿐 아니라 서버 서비스에서도 `SUPER` 권한을 검증합니다.
- 한 회원에게 `ACTIVE` 상태의 정지 이력은 하나만 허용합니다.
- 정지 등록과 해제는 회원 행 잠금과 활성 이력 고유 인덱스로 동시 요청을 직렬화합니다.

## 정지 사유와 공개 범위

- 정지 사유는 `SPND_RSON` 공통코드에서 선택합니다.
- 사용자에게는 정지 유형, 코드화된 사유명, 시작일과 종료 예정일만 공개합니다.
- 관리자 내부 처리 메모 `SPND_CNTN`과 해제 메모 `RLES_CNTN`은 사용자 API에 포함하지 않습니다.
- 내부 메모는 UTF-8 기준 각각 최대 1,000바이트까지 저장합니다.

## 회원 상태와 접근

- 정지 등록 시 현재 회원 상태를 `PREV_STAT`에 보관하고 `TM_USERXM.USER_STAT`을 `SUSPENDED`로 변경합니다.
- 동일 Kakao 계정의 암호화 식별값을 보존하므로 같은 계정으로 재로그인하거나 가입을 다시 시도해도 기존 정지 회원으로 처리합니다.
- `SUSPENDED` 회원은 정지 안내 조회, 로그아웃과 최소 인증 API만 이용할 수 있습니다.
- 정지 회원은 정지 해제 또는 기간 만료 전까지 계정 비활성화와 영구 탈퇴를 신청할 수 없습니다.
- 정지 회원의 30일 유예 영구탈퇴 허용은 현재 구현이 아닌 [2차 회원 탈퇴정책 개발 범위](withdrawal-policy.md#2차-회원-탈퇴정책-개발-범위)이며, 구현 전까지 현행 차단 정책을 유지합니다.
- 관리자 상태 변경과 `TB_EVTBOX` 이벤트는 같은 DB 트랜잭션으로 저장합니다.
- 사용자 서버는 5분마다 이벤트를 확인하고 처리 시점의 `TM_USERXM.USER_STAT`을 자체 Redis에 반영합니다.
- 이미 발급된 로그인 세션의 정지·해제 상태는 정상 운영 시 최대 5분 안에 반영됩니다.
- Redis 장애 시 `TB_EVTBOX` 전달 이벤트를 삭제하지 않아 다음 주기에 재시도하며 FCM 푸시 수신 여부에는 의존하지 않습니다.
- 관리자 현재 사용자 상세는 가장 최근 `TH_USSPND.SYNC_STAT`이 `PENDING`이면 `사용자 서버 반영 대기`, `COMPLETED`이면 `사용자 서버 반영 완료`로 표시합니다.
- `사용자 서버 반영 완료`는 사용자 서버가 Outbox 이벤트를 성공 처리했다는 의미입니다. 로그인 세션이 없으면 Redis 값을 새로 만들지 않고 이벤트 처리를 완료하며, 다음 로그인에서 DB 회원 상태를 적용합니다.
- 정지 안내 조회 시 활성 정지 이력이 없고 DB 회원 상태가 `SUSPENDED`가 아니면 DB 상태로 Redis 캐시를 즉시 보정합니다.
- 활성 정지 이력이 없더라도 최신 인증 상태가 여전히 `SUSPENDED`이면 전용 화면을 유지해 일반 화면과 정지 화면의 반복 이동을 차단합니다.

## 해제와 기간 만료

- 관리자 해제 시 정지 이력을 `RELEASED`로 변경하고 해제 관리자와 시각 및 내부 메모를 기록합니다.
- 기간 종료 시 정지 이력을 `EXPIRED`로 변경합니다.
- 회원 상태가 여전히 `SUSPENDED`일 때만 `PREV_STAT`으로 복구합니다.
- 영구 삭제 대기처럼 더 우선하는 상태가 적용되어 있으면 관리자 해제나 기간 만료가 그 상태를 덮어쓰지 않습니다.
- 영구 탈퇴를 취소할 때 유효한 정지가 남아 있으면 `SUSPENDED`로 복구합니다.

## 데이터 구조

- 정지 이력 테이블은 `TH_USSPND`이며 관리자 INSERT 직전에 `MAX(SPND_NUMB) + 1`로 번호를 발급합니다.
- 회원 원본이 영구 삭제된 뒤에도 제재 감사 이력을 보존할 수 있도록 `TM_USERXM` 외래키를 두지 않습니다.
- `UK_TH_USSPND_ACTV` 함수 기반 고유 인덱스로 회원별 활성 정지 한 건을 보장합니다.
- 공통코드는 `SPND_TYPE`, `SPND_RSON`, `SPND_STAT`, 회원 상태는 `USER_STAT.SUSPENDED`를 사용합니다.
- 서비스 간 상태 변경 전달은 `TB_EVTBOX`를 사용합니다. 사용자 서버 INSERT는 `AUTO_INCREMENT`, 관리자 서버 INSERT는 `MAX(EVNT_NUMB) + 1`로 번호를 발급합니다.
- `TB_EVTBOX`는 이벤트 번호·유형·회원번호·정지 이력 번호·등록일시만 저장하는 임시 전달함이며 사용자 서버 처리 성공 후 행을 삭제합니다.
- `TH_USSPND.SYNC_STAT`은 사용자 서버의 실제 반영 여부를 보존하며 상태 변경 시 `PENDING`, 사용자 서버 처리 성공 시 `COMPLETED`를 사용합니다.

## 한계

- 동일 Kakao 계정은 기존 `USER_IDXX`로 차단할 수 있습니다.
- 사용자가 다른 Kakao 계정을 새로 만들면 기존 정지 회원과 동일인인지 신뢰성 있게 판별할 수 없어 자동 차단하지 않습니다.
- 기기 지문이나 IP는 공유·변경 가능성과 개인정보 위험이 있어 계정 동일성의 강제 근거로 사용하지 않습니다.

## 구현 근거

- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/03-reset-user-data.sql`
- `scripts/db/mysql/routines.sql`
- `src/main/java/org/our/sadari/global/security/jwt/JwtFilter.java`
- `src/main/java/org/our/sadari/user/auth/service/AuthServiceImpl.java`
- `src/main/java/org/our/sadari/user/service/UserSuspensionServiceImpl.java`
- `src/main/java/org/our/sadari/user/service/UserWithdrawalServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/service/UserStatusEventServiceImpl.java`
- `src/main/frontend/src/pages/Settings/SuspensionPage.tsx`
- `sadari-admin` 저장소 `src/main/java/org/sadari/admin/sadariadmin/currentuser/service/impl/CurrentUserServiceImpl.java`
- `sadari-admin` 저장소 `src/main/java/org/sadari/admin/sadariadmin/currentuser/mapper/CurrentUserMapper.xml`
- `sadari-admin` 저장소 `src/main/frontend/src/pages/currentUser/CurrentUserDetailPage.tsx`
