# 관리자와 사용자 서비스 연동

## 문서 목적

- 목적: 사용자 서비스 `sadari`와 관리자 서비스 `sadari-admin`이 운영 데이터를 통해 연결되는 구조를 설명
- 적용 범위: 공통코드, 알림 템플릿, 사용자 메뉴, 스케줄러 제어·로그, 관리자 인증·권한
- 기준일: 2026-07-30
- 분석 대상: 현재 저장소 `sadari`와 형제 저장소 `../sadari-admin`

## 연동 방식

두 애플리케이션은 관리자 서비스가 사용자 서비스의 API를 직접 호출하는 구조가 아니다. 동일한 MySQL 운영 테이블을 제어면과 실행면이 공유하는 구조다.

```text
관리자 브라우저
    |
    v
sadari-admin
    |
    | 운영 설정 등록·수정
    v
MySQL 공통 운영 테이블
    ^
    | 설정 조회·업무 실행·로그 기록
    |
sadari 사용자 백엔드
    |
    v
사용자 브라우저 / PWA

sadari 사용자 스케줄러
    |
    | 실행·실패 로그 기록
    v
MySQL 스케줄러 로그 테이블
    |
    v
sadari-admin 운영 조회 화면
```

이 구조에서 `sadari-admin`은 운영자가 정책과 표시값을 관리하는 제어면이고, `sadari`는 해당 값을 실제 사용자 요청과 배치 작업에 적용하는 실행면이다.

## 연동 기능 지도

| 관리자 기능 | 공유 데이터 | 사용자 서비스 반영 지점 | 반영 방식 |
| --- | --- | --- | --- |
| 공통코드 관리 | `TM_CODEXM`, `TB_CODEXD` | 상태 검증, 화면 선택지, 비속어 사전, 색상, 닉네임, 스케줄러 | 사용자 API와 백엔드가 활성 세부코드를 조회 |
| 알림 템플릿 관리 | `TB_ALTEMP` | 좋아요, 팔로우, 목표기간 초과 알림 | 상황·템플릿 코드로 활성 템플릿 조회 후 치환 |
| 사용자 메뉴 관리 | `TM_URMENU` | Header 제목, 햄버거 메뉴 | 현재 URL과 노출·사용 여부를 조회 |
| 스케줄러 실행 제어 | `TB_CODEXD`의 `SCHD_CODE` | 목표기간 초과, 알림 삭제, 회원 영구 삭제 | 실행 직전 활성 코드 존재 여부 확인 |
| 스케줄러 로그 조회 | `TL_SCLOGX`, `TL_SCFAIL` | 사용자 백엔드가 기록한 실행 결과 | 관리자 목록·상세 화면에서 조회 |
| 관리자 메뉴·권한 | `TM_ADMENU`, `TM_AUTHXM`, `TB_AUTHMN`, `TM_ADMINX` | 운영 데이터 변경 권한 통제 | 메뉴별 조회·쓰기·삭제 권한 검사 |

## 공통코드 운영

### 관리자 제어

관리자는 공통코드 마스터와 세부코드를 등록·수정하고 세부코드의 사용 여부, 정렬 순서, 옵션값을 관리한다. 관리 API는 중복 코드와 필수값을 검증하고 등록·수정 관리자 정보를 남긴다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/controller/CodeManageController.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/service/CodeManageService.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/mapper/CodeMapper.xml`
- `../sadari-admin/src/main/frontend/src/pages/code/CodeListPage.tsx`
- `../sadari-admin/src/main/frontend/src/pages/code/CodeDetailPage.tsx`

### 사용자 서비스 적용

| 코드 그룹 | 사용자 기능 |
| --- | --- |
| `READ_STAT` | 독후감 독서 상태 선택과 백엔드 유효성 검증 |
| `BOOK_COLR` | 독서 달력 색상과 표지 대표색의 CIELAB 매칭 후보 |
| `FOLW_STAT` | 팔로우 관계별 버튼명 |
| `ALIM_SITU` | 알림 상황명과 아이콘 옵션 |
| `SCHD_CODE` | 스케줄러별 실행 중지·재개 |
| `BADX_WORD`, `EXCP_WORD` | 비속어 차단 사전과 정상 표현 예외 사전 |
| `NICK_SUBJ`, `NICK_PRED`, `NICK_ANML` | 신규 회원 자동 닉네임 조합 |
| `USER_STAT` | 활성, 계정 비활성화, 영구 삭제 대기 상태 |
| `WTHD_TYPE`, `WTHD_RSON` | 탈퇴 방식과 탈퇴 사유 검증 |

사용자 구현 근거:

- `src/main/java/org/our/sadari/global/common/code/util/CodeUtil.java`
- `src/main/java/org/our/sadari/report/service/ReportServiceImpl.java`
- `src/main/java/org/our/sadari/global/common/service/BadWordDetectionService.java`
- `src/main/java/org/our/sadari/user/service/NicknameGenerationServiceImpl.java`
- `src/main/frontend/src/features/Common/utils/codeUtil.ts`

`ALIM_SITU`의 `OPT1_CODE = '1'`과 `OPT1_NAME`은 알림센터 아이콘 표현에도 사용된다. 사용자 알림 조회 SQL이 옵션값을 함께 반환하고 `AlimPage`가 `HEART`, `FOLLOW` 같은 이름을 실제 아이콘과 색상 스타일로 매핑한다. 따라서 관리자는 코드명뿐 아니라 사용자 화면의 알림 시각 분류에 필요한 메타데이터도 관리한다.

관련 근거:

- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`
- `src/main/frontend/src/pages/Alim/AlimPage.tsx`

### 변경 전파 특성

- 일반 백엔드 코드 조회는 요청 시 DB에서 활성 코드를 읽는다.
- 사용자 프론트의 공통코드는 React Query에서 10분 동안 신선한 데이터로 취급한다.
- 비속어·예외 사전은 사용자 백엔드 메모리에 10분 동안 함께 캐시한다.
- 따라서 관리자가 공통코드를 수정해도 화면 선택지와 비속어 사전은 최대 10분 동안 이전 값을 사용할 수 있다.
- 현재 관리자 저장 후 사용자 캐시를 즉시 무효화하는 이벤트나 API는 구현되어 있지 않다.

## 알림 템플릿 운영

### 관리자가 등록하는 값

관리자 알림 템플릿 화면은 다음 값을 `TB_ALTEMP`에 등록·수정한다.

- 알림 상황 `ALIM_SITU`
- 템플릿 코드 `TEMP_CODE`
- 관리용 제목 `TEMP_TITL`
- 사용자에게 전달할 알림 제목 `ALIM_TITL`
- `#{key}` 형식의 치환 문구가 포함된 내용 `TEMP_CONT`
- 대상 번호와 결합할 이동 URL `LINK_URLX`
- 사용 여부 `USEE_YSNO`
- 등록·수정 관리자와 일시

`ALIM_SITU`와 `TEMP_CODE`를 복합 식별값으로 사용하며, 템플릿 코드는 영문 대문자와 밑줄만 허용한다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/controller/AlimTempController.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/service/impl/AlimTempServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/mapper/AlimTempMapper.xml`
- `../sadari-admin/src/main/frontend/src/pages/alim/AlimTempDetailPage.tsx`

### 사용자 서비스의 템플릿 소비

1. 좋아요, 팔로우 또는 스케줄러가 수신자, 상황 코드, 템플릿 코드, 대상 번호와 치환 Map을 `AlimService.sendAlim`에 전달한다.
2. 알림 서비스는 `TB_ALTEMP`에서 `USEE_YSNO = 'Y'`인 템플릿을 조회한다.
3. `TEMP_CONT`의 `#{key}`를 전달받은 Map 값으로 치환한다.
4. `LINK_URLX`와 대상 번호를 결합한다.
5. 동일한 최종 알림이 최근 1시간 안에 없을 때 `TB_ALIMXX`에 저장한다.
6. 업무 트랜잭션 커밋 후 FCM 푸시 발송을 시도한다.

현재 연결된 템플릿 코드는 다음과 같다.

| 발생 업무 | 상황 코드 | 템플릿 코드 | 주요 치환값 |
| --- | --- | --- | --- |
| 독후감 좋아요 | `LIKE` | `LIKE_REPORT` | 발신 사용자명 |
| 댓글·대댓글 좋아요 | `LIKE` | `REPLY_LIKE` | 발신 사용자명 |
| 사용자 팔로우 | `FOLLOW` | `FOLLOW_USER` | 발신 사용자명 |
| 독서 모임 초대 | `FOLLOW` | `INVITE_CLUB` | 모임장 닉네임, 모임명 |
| 목표 독서기간 초과 | `REPORT` | `REPORT_DATE_OVER` | 도서 제목 `bookTitl` |

사용자 구현 근거:

- `src/main/java/org/our/sadari/alim/service/AlimServiceImpl.java`
- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`
- `src/main/java/org/our/sadari/social/service/SocialServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/service/ReportDateOverServiceImpl.java`

### 운영상 의미

- 운영자는 문구와 이동 URL을 바꾸기 위해 사용자 서비스를 다시 빌드할 필요가 없다.
- 템플릿을 미사용으로 전환하면 이후 일반 알림 발송에서 활성 템플릿을 찾지 못한다.
- 목표기간 초과 대상 조회는 활성 `REPORT_DATE_OVER` 템플릿을 `INNER JOIN`하므로 템플릿이 없거나 미사용이면 대상 자체를 선택하지 않는다.
- 템플릿의 `#{key}`와 호출부 Map Key는 스키마로 강제되지 않으므로 오타가 있으면 미치환 문구가 남을 수 있다. 저장 전 치환 변수 검증은 향후 개선 대상이다.

## 사용자 메뉴 운영

### 관리자 제어

관리자는 `TM_URMENU`에서 단일 `MENU_NUMB`, 자기참조 `PARN_NUMB`, `MENU_LEVL`을 기준으로 최대 3단계 메뉴의 이름, URL, 계층별 정렬 순서, 노출 여부와 사용 여부를 관리한다. 관리자 서비스는 순환 참조, 4단계 이동과 하위 메뉴가 있는 메뉴 삭제를 차단하고, 사용자 API는 유효한 부모에서 이어지는 메뉴만 `childList` 트리로 제공한다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/usermenu/service/impl/UserMenuServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/usermenu/mapper/UserMenuMapper.xml`
- `../sadari-admin/src/main/frontend/src/pages/userMenu/UserMenuManagePage.tsx`

### 사용자 화면 반영

1. 사용자 Header는 경로가 바뀔 때 현재 `pathname`으로 메뉴 API를 호출한다.
2. URL이 정확히 일치하거나 `/`로 끝나는 가장 긴 상위 경로를 현재 메뉴로 선택한다.
3. 현재 메뉴가 있으면 Header에 `MENU_NAME`을 표시한다.
4. 일치하는 메뉴가 없으면 기존 로고를 표시한다.
5. 햄버거 메뉴에는 `SHOW_YSNO = 'Y'`이면서 `USEE_YSNO = 'Y'`인 메뉴만 정렬 순서대로 표시한다.

사용자 구현 근거:

- `src/main/java/org/our/sadari/menu/mapper/UserMenuMapper.xml`
- `src/main/java/org/our/sadari/menu/service/UserMenuServiceImpl.java`
- `src/main/frontend/src/components/Layout/Header/Header.tsx`
- `src/main/frontend/src/components/Layout/Header/HeaderMenuDrawer.tsx`

관리자 메뉴 설정은 사용자 화면의 탐색과 제목을 제어하지만 React Route나 백엔드 API 접근 권한을 생성하지 않는다. 메뉴를 숨겨도 직접 URL 접근을 차단하는 권한 기능은 별도로 필요하다.

## 스케줄러 제어와 관찰

### 실행 제어

사용자 백엔드의 `Scheduler`는 Cron 실행 직전에 `SCHD_CODE`의 활성 세부코드를 확인한다.

| 코드 | 업무 |
| --- | --- |
| `REPORT_DATE_OVER` | 목표 독서기간 초과 알림 |
| `ALIM_DELETE` | 삭제 상태 알림 물리 삭제 |
| `USER_HARD_DELETE` | 영구 삭제 대기 회원 물리 삭제 |

관리자가 공통코드의 사용 여부를 `N`으로 바꾸면 다음 Cron부터 업무 서비스 호출과 실행 로그 생성을 생략한다. 별도의 서버 재기동은 필요하지 않다.

### 실행 로그 관찰

사용자 스케줄러는 다음 데이터를 기록한다.

- `TL_SCLOGX`: 실행 번호, 스케줄러 코드, 메서드명, 실행 상태, 시작·종료 시각, 대상·성공·실패 건수, 실행 시간
- `TL_SCFAIL`: 실행 번호별 실패 순번, 실패 유형, 업무 결과 코드·메시지, 예외 유형·내용, 실패 시각

관리자 스케줄러 로그 화면은 목록에서 실행 결과를 조회하고 상세 화면에서 실행 요약과 실패 행을 함께 조회한다.

사용자 기록 근거:

- `src/main/java/org/our/sadari/global/scheduler/Scheduler.java`
- `src/main/java/org/our/sadari/global/scheduler/common/SchedulerLogSupport.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/SchedulerLogMapper.xml`

관리자 조회 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/schedulelog/mapper/ScheduleLogMapper.xml`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/schedulelog/service/impl/ScheduleLogServiceImpl.java`
- `../sadari-admin/src/main/frontend/src/pages/scheduleLog/ScheduleLogListPage.tsx`
- `../sadari-admin/src/main/frontend/src/pages/scheduleLog/ScheduleLogDetailPage.tsx`

현재 관리자 화면은 조회와 원인 확인을 제공한다. 수동 실행, 실패 건 재처리, 기간 검색, 집계 대시보드는 구현되어 있지 않다.

## 관리자 인증과 변경 권한

사용자 서비스의 Kakao OAuth·JWT 인증과 관리자 서비스 인증은 분리되어 있다.

1. 관리자가 `TM_ADMINX`의 계정으로 로그인한다.
2. 성공 시 임의 UUID 토큰과 관리자 번호, 권한 코드, 권한 레벨, 이름, 부서를 Redis Hash에 저장한다.
3. 토큰은 HttpOnly·SameSite Lax Cookie로 전달한다.
4. 요청마다 Redis 세션 TTL을 갱신한다.
5. `RedisAuthenticationFilter`가 Spring Security 인증 객체를 구성한다.
6. `MenuPermissionInterceptor`가 API 경로를 관리자 메뉴 URL에 매핑한다.
7. GET은 조회 권한, DELETE는 삭제 권한, 나머지 메서드는 쓰기 권한을 확인한다.

권한 데이터 구조:

- `TM_AUTHXM`: 관리자 권한 그룹
- `TM_ADMENU`: 관리자 화면 메뉴
- `TB_AUTHMN`: 권한 그룹별 메뉴 조회·쓰기·삭제 권한
- `TM_ADMINX.AUTH_CODE`: 관리자에게 부여한 권한 그룹

관리자 로그인은 `TM_ADMINX.AUTH_CODE`와 공통코드 그룹 `AUTH_CODE`를 연결해 권한 레벨을 조회한다. 관리자 계정의 권한 코드, 권한 그룹 마스터와 권한 레벨 공통코드가 함께 일치해야 정상 인증과 메뉴 권한 판단이 가능하다.

구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/admin/service/impl/AdminRedisAuthServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/config/RedisAuthenticationFilter.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/config/MenuPermissionInterceptor.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/authgroup/service/impl/AuthGroupServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/adminauth/service/impl/AdminAuthManageServiceImpl.java`

같은 권한 그룹의 메뉴 권한 변경은 인터셉터가 요청마다 DB를 조회하므로 다음 요청부터 반영된다. 특정 관리자의 `AUTH_CODE`를 다른 그룹으로 변경하면 기존 Redis 세션에는 이전 코드가 남으므로 재로그인 또는 세션 만료 전까지 이전 그룹 기준으로 동작할 수 있다.

## 포트폴리오 핵심 가치

이 연동은 관리자 화면을 별도 CRUD 프로젝트로 만든 사례가 아니다.

- 운영 설정을 공유 테이블에 기록하는 관리자 제어면
- 설정을 실제 업무 규칙으로 소비하는 사용자 실행면
- 사용자 배치가 남긴 로그를 관리자가 다시 관찰하는 운영 피드백
- 관리자별 메뉴 권한으로 운영 변경 범위를 제한하는 거버넌스

따라서 “관리자 페이지를 구현했다”보다 “운영자가 코드 배포 없이 사용자 메뉴, 공통코드, 알림 템플릿과 스케줄러 실행 여부를 조정하고 실행 결과를 추적하는 이중 애플리케이션 구조를 설계했다”라고 설명하는 것이 프로젝트의 실제 강점을 더 정확히 전달한다.

## 현재 한계와 개선 순서

1. 관리자 비밀번호는 현재 단순 SHA-256 비교이므로 BCrypt 또는 Argon2 기반 적응형 해시로 전환해야 한다.
2. 관리자 `SecurityConfig`의 기본 정책이 `permitAll`이고 등록된 관리 API는 인터셉터 매핑에 의존하므로 신규 API가 누락되지 않도록 기본 거부 정책과 자동화 테스트가 필요하다.
3. 관리자 권한 그룹 변경 시 기존 Redis 세션을 즉시 무효화해야 한다.
4. 공통코드와 비속어 사전 변경 후 사용자 캐시를 즉시 비우는 이벤트 또는 관리 API가 필요하다.
5. 알림 템플릿 저장 시 `#{key}` 목록과 실제 호출부 계약을 검증해야 한다.
6. `sadari-admin`에는 Docker·GitHub Actions 배포 구성이 없고 테스트도 Context Load 1건만 확인되므로 사용자 서비스와 독립된 CI/CD 및 권한 통합 테스트가 필요하다.
