# 알림과 스케줄러

## 문서 목적

- 목적: 실시간 알림과 정기 배치를 안정적으로 운영하기 위한 설계를 설명
- 적용 범위: 관리자 템플릿 관리, 중복 방지, FCM, PWA, 스케줄러 제어, 실행 로그 관찰
- 기준일: 2026-07-30

## 관리자에서 시작하는 알림 템플릿

알림 문구는 사용자 백엔드에 고정하지 않는다. 운영자는 별도 `sadari-admin`의 알림 템플릿 화면에서 다음 값을 등록·수정한다.

- 알림 상황
- 템플릿 코드
- 관리용 제목
- 사용자 알림 제목
- `#{key}` 치환 문구가 포함된 템플릿 내용
- 사용자 화면 이동 URL
- 사용 여부

관리자 서비스는 알림 상황 코드와 템플릿 코드 중복을 검사하고 알림 템플릿에 등록·수정 관리자와 일시를 함께 저장한다. 쓰기 권한이 있는 관리자에게만 저장 버튼을 노출하고, 백엔드 `MenuPermissionInterceptor`가 API 쓰기 권한을 다시 확인한다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/controller/AlimTempController.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/service/impl/AlimTempServiceImpl.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/alim/mapper/AlimTempMapper.xml`
- `../sadari-admin/src/main/frontend/src/pages/alim/AlimTempDetailPage.tsx`

## 재사용 가능한 템플릿 알림

알림 호출부는 문장을 직접 조합하지 않고 다음 값만 `AlimService.sendAlim`에 전달한다.

- 수신 사용자 번호
- 알림 상황 코드
- 템플릿 코드
- 이동 대상 유형·번호와 필요한 댓글 번호
- `#{key}` 상용구 치환 Map

서비스는 관리자가 알림 템플릿에 등록한 사용 중 템플릿을 조회하고 제목·내용을 치환해 이동 대상 정보와 함께 사용자 알림 발송 내역에 저장한다. 좋아요, 댓글, 팔로우, 독서모임, 독서 목표기간 초과와 타이머 등 서로 다른 도메인이 같은 발송 흐름을 재사용한다. 알림센터와 푸시는 알림번호 공통 경로를 사용하고 서버가 클릭 시점의 권한으로 최종 주소를 계산한다.

| 발생 기능 | 알림 상황 | 템플릿 코드 | 관리자 템플릿과 사용자 실행 연결 |
| --- | --- | --- | --- |
| 독후감 좋아요 | `LIKE` | 독후감 좋아요 | 관리자 문구의 사용자명 치환 후 독후감 링크 생성 |
| 댓글·대댓글 좋아요 | `LIKE` | 댓글 좋아요 | 관리자 문구의 사용자명 치환 후 해당 댓글이 속한 독후감 링크 생성 |
| 사용자 팔로우 | `FOLLOW` | 사용자 팔로우 | 관리자 문구의 사용자명 치환 후 프로필 링크 생성 |
| 독서 모임 초대 | `FOLLOW` | 독서모임 초대 | 관리자 문구의 모임장 닉네임과 모임명 치환 후 모임 링크 생성 |
| 목표기간 초과 | `독후감` | `REPORT_처리 기준일 초과 상태` | 관리자 문구의 `#{bookTitl}` 치환 후 독후감 링크 생성 |

구현 근거:

- `src/main/java/org/our/sadari/alim/service/AlimService.java`
- `src/main/java/org/our/sadari/alim/service/AlimServiceImpl.java`
- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`

### 템플릿 사용 여부의 영향

- 일반 알림은 `사용 여부 = 'Y'`인 템플릿만 조회한다.
- 목표기간 초과 대상 SQL은 활성 `REPORT_처리 기준일 초과 상태` 템플릿을 `INNER JOIN`한다.
- 관리자가 해당 템플릿을 미사용으로 바꾸면 목표기간 초과 대상 조회 자체가 0건이 된다.
- 템플릿은 사용자 백엔드에서 장기 캐시하지 않으므로 다음 알림 생성 시 DB 변경값을 사용한다.

현재 템플릿의 `#{key}` 목록을 관리자 저장 단계에서 검증하지는 않는다. 관리 문구와 호출부 Map Key가 다르면 치환되지 않은 문구가 남을 수 있으므로 계약 검증이 필요하다.

알림센터 아이콘도 관리자 공통코드와 연결된다. 알림 상황 코드의 `LIKE`는 독후감과 댓글 좋아요가 공유하고 `FOLLOW`는 팔로우 요청과 독서 모임이 공유한다. 사용자 알림 조회는 사용자 알림 발송 내역의 알림 상황 코드로 알림 아이콘 원본을 조인해 저장된 아이콘을 반환하며, 등록된 아이콘이 없으면 `DEFAULT` 아이콘을 사용한다.

관련 구현:

- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`
- `src/main/frontend/src/pages/Alim/AlimPage.tsx`

## 중복 알림 방지

같은 버튼을 반복 누르거나 요청이 재시도될 때 동일 알림이 쌓이지 않도록 최근 1시간의 알림을 확인한다.

비교 조건:

- 수신 사용자
- 알림 상황
- 템플릿 코드
- 최종 제목
- 최종 내용
- 이동 대상 유형·번호와 댓글 번호
- 삭제되지 않은 상태

같은 조건이 있으면 신규 행과 푸시를 만들지 않고 성공으로 처리한다.

## 트랜잭션 커밋 후 푸시

DB 저장 전에 FCM을 발송하면 사용자가 푸시를 눌렀을 때 아직 알림 또는 대상 데이터가 조회되지 않을 수 있다.

`AlimServiceImpl.schedulePushAfterCommit`은 Spring의 `TransactionSynchronizationManager`를 사용해 DB 커밋 이후 `PushService.sendPush`를 실행한다.

- DB 롤백 시 푸시를 보내지 않는다.
- FCM 발송 실패가 이미 커밋된 알림 저장을 롤백하지 않는다.
- 푸시 실패는 경고 로그로 남기고 알림센터 데이터는 유지한다.

이 설계는 DB와 외부 메시징 시스템의 원자적 트랜잭션이 불가능한 상황에서 데이터 정합성을 우선한 선택이다.

## PWA와 웹 푸시

프론트는 Service Worker를 등록하고 사용자의 직접 클릭 이후에만 브라우저 알림 권한을 요청한다. 권한이 허용되면 Firebase 토큰을 발급해 사용자 웹 푸시 구독에 저장한다.

주요 흐름:

1. 브라우저 PWA 지원 여부 확인
2. 사용자의 직접 동작에서 `Notification.requestPermission` 호출
3. Service Worker 준비 대기
4. VAPID 공개키로 FCM 토큰 발급
5. 백엔드 구독 API에 토큰 저장
6. 알림 끄기 시 구독의 사용 여부를 `N`으로 전환

구현 근거:

- `src/main/frontend/src/app/pwa/registerServiceWorker.ts`
- `src/main/frontend/src/app/pwa/firebaseMessaging.ts`
- `src/main/frontend/src/pages/Alim/AlimPage.tsx`
- `src/main/java/org/our/sadari/push/service/PushServiceImpl.java`
- `src/main/java/org/our/sadari/push/service/FirebaseMessagingProvider.java`

## 알림센터 상태 관리

- 20개 단위 페이지 조회
- 알림번호 공통 경로 또는 푸시 클릭 시 현재 권한 확인과 읽음 처리
- 모두 지우기 시 `삭제 여부 = 'Y'`
- 삭제되지 않은 알림만 목록에 노출
- 읽은 알림은 시각적으로 구분
- 읽지 않은 알림 수를 메뉴에 표시

정책 근거:

- `docs/policies/notification-push-policy.md`

## 스케줄러 실행 제어

`Scheduler`는 Cron 시간만으로 작업을 실행하지 않는다. 각 작업 시작 전에 스케줄러 코드 분류값이 사용 중인지 `CodeUtil.existsCode`로 확인한다.

스케줄러 코드도 `sadari-admin`의 공통코드 관리 화면에서 변경한다. 관리자가 특정 분류값의 사용 여부를 `N`으로 바꾸면 사용자 백엔드는 다음 Cron 실행에서 업무 서비스와 실행 로그 생성을 모두 건너뛴다. 서버 재기동이나 사용자 서비스 재배포는 필요하지 않다.

| 스케줄러 | 현재 주기 | 목적 |
| --- | --- | --- |
| `REPORT_처리 기준일 초과 상태` | 09:00~09:55 5분 간격, 10:00 추가 실행 | 목표 독서기간 초과 알림 |
| 알림 만료 정리 | 10분 간격 | 삭제 상태 알림 물리 삭제 |
| `장기 미접속 사용자 기준_DELETE` | 매일 03:00 | 유예기간이 지난 회원 영구 삭제 |

로컬 프로필에는 영구 탈퇴를 바로 검증하기 위한 별도 테스트 스케줄러가 있으며 설정 플래그로 제한한다.

구현 근거:

- `src/main/java/org/our/sadari/global/scheduler/Scheduler.java`
- `src/main/java/org/our/sadari/global/scheduler/LocalUserHardDeleteScheduler.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/service/CodeManageService.java`
- `../sadari-admin/src/main/frontend/src/pages/code/CodeDetailPage.tsx`

## 대량 대상 처리

목표기간 초과 알림은 대상이 많을 수 있으므로 설정한 `maxSize`만큼 제한해 한 실행에서 처리한다. 조회 조건은 목표기간이 지난 지 48시간 이내이며 동일 템플릿 알림이 아직 발송되지 않은 독후감으로 제한한다.

관련 구현:

- `src/main/java/org/our/sadari/global/scheduler/service/ReportDateOverServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/ReportDateOverMapper.xml`

## 스케줄러 실행·실패 로그

스케줄러는 스케줄러 실행 로그에 실행 단위 마스터 로그를 기록하고, 개별 실패는 스케줄러 실행 실패 상세에 기록한다.

- 대상 건수
- 성공 건수
- 실패 건수
- 실행 상태
- 메서드명
- 결과 메시지
- 실패 유형과 예외 내용

대상·성공·실패 건수가 모두 0이면 불필요한 실행 로그를 저장하지 않는다. 공통 `SchedulerLogSupport`가 로그 저장 실패를 원래 업무 실패와 분리해 스케줄러 로깅 장애가 핵심 배치를 중단하지 않도록 한다.

구현 근거:

- `src/main/java/org/our/sadari/global/scheduler/common/SchedulerLogSupport.java`
- `src/main/java/org/our/sadari/global/scheduler/service/SchedulerLogServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/SchedulerLogMapper.xml`

## 관리자 스케줄러 로그 화면

사용자 백엔드가 스케줄러 실행 로그, 스케줄러 실행 실패 상세에 기록한 결과를 관리자 서비스가 읽는다.

### 목록

- 실행 번호
- 스케줄러 코드와 공통코드명
- 실행 메서드
- 실행 상태
- 시작·종료 시각
- 대상·성공·실패 건수
- 실행 시간

### 상세

- 선택한 실행의 요약
- 실패 유형
- 업무 결과 코드와 메시지
- 예외 유형과 오류 내용
- 실패 시각

관리자 프론트는 실행 로그와 실패 목록을 병렬 조회해 상세 화면을 구성한다. 조회 권한이 없는 관리자는 프론트 메뉴에서 접근할 수 없고 백엔드 권한 인터셉터에서도 거부된다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/schedulelog/controller/ScheduleLogController.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/schedulelog/mapper/ScheduleLogMapper.xml`
- `../sadari-admin/src/main/frontend/src/pages/scheduleLog/ScheduleLogListPage.tsx`
- `../sadari-admin/src/main/frontend/src/pages/scheduleLog/ScheduleLogDetailPage.tsx`

이 흐름은 관리자가 설정하고 사용자 백엔드가 실행한 뒤 관리자가 결과를 다시 확인하는 운영 피드백을 완성한다.

## 향후 개선

- FCM의 `Device unregistered` 응답 시 해당 구독을 자동 비활성화하는 정책을 명확히 검증한다.
- 다중 서버 배포 시 동일 스케줄러 중복 실행을 막는 분산 잠금이 필요하다.
- 대량 푸시는 비동기 큐와 재시도·Dead Letter Queue 구조로 확장할 수 있다.
- 관리자 템플릿 저장 시 허용 치환 Key를 검증하고 미리보기를 제공한다.
- 관리자 화면에 스케줄러 수동 실행, 실패 건 재처리, 기간 검색과 상태 집계를 추가할 수 있다.
- 운영 코드 변경과 템플릿 수정 이력을 별도 감사 로그로 남길 수 있다.
