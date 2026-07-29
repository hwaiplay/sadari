# 알림과 스케줄러

## 문서 목적

- 목적: 실시간 알림과 정기 배치를 안정적으로 운영하기 위한 설계를 설명
- 적용 범위: 템플릿 알림, 중복 방지, FCM, PWA, 스케줄러, 실행 로그
- 기준일: 2026-07-30

## 재사용 가능한 템플릿 알림

알림 호출부는 문장을 직접 조합하지 않고 다음 값만 `AlimService.sendAlim`에 전달한다.

- 수신 사용자 번호
- 알림 상황 코드
- 템플릿 코드
- 링크에 결합할 대상 번호
- `#{key}` 상용구 치환 Map

서비스는 `TB_ALTEMP`에서 사용 중인 템플릿을 조회하고 제목·내용·링크를 조합해 `TB_ALIMXX`에 저장한다. 좋아요, 팔로우, 독서 목표기간 초과 등 서로 다른 도메인이 같은 발송 흐름을 재사용한다.

구현 근거:

- `src/main/java/org/our/sadari/alim/service/AlimService.java`
- `src/main/java/org/our/sadari/alim/service/AlimServiceImpl.java`
- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`

## 중복 알림 방지

같은 버튼을 반복 누르거나 요청이 재시도될 때 동일 알림이 쌓이지 않도록 최근 1시간의 알림을 확인한다.

비교 조건:

- 수신 사용자
- 알림 상황
- 템플릿 코드
- 최종 제목
- 최종 내용
- 최종 링크
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

프론트는 Service Worker를 등록하고 사용자의 직접 클릭 이후에만 브라우저 알림 권한을 요청한다. 권한이 허용되면 Firebase 토큰을 발급해 `TB_PSHSUB`에 저장한다.

주요 흐름:

1. 브라우저 PWA 지원 여부 확인
2. 사용자의 직접 동작에서 `Notification.requestPermission` 호출
3. Service Worker 준비 대기
4. VAPID 공개키로 FCM 토큰 발급
5. 백엔드 구독 API에 토큰 저장
6. 알림 끄기 시 구독의 `USEE_YSNO`를 `N`으로 전환

구현 근거:

- `src/main/frontend/src/app/pwa/registerServiceWorker.ts`
- `src/main/frontend/src/app/pwa/firebaseMessaging.ts`
- `src/main/frontend/src/pages/Alim/AlimPage.tsx`
- `src/main/java/org/our/sadari/push/service/PushServiceImpl.java`
- `src/main/java/org/our/sadari/push/service/FirebaseMessagingProvider.java`

## 알림센터 상태 관리

- 20개 단위 페이지 조회
- 알림 링크 클릭 또는 푸시 클릭 시 읽음 처리
- 모두 지우기 시 `DELT_YSNO = 'Y'`
- 삭제되지 않은 알림만 목록에 노출
- 읽은 알림은 시각적으로 구분
- 읽지 않은 알림 수를 메뉴에 표시

정책 근거:

- `docs/policies/notification-push-policy.md`

## 스케줄러 실행 제어

`Scheduler`는 Cron 시간만으로 작업을 실행하지 않는다. 각 작업 시작 전에 `SCHD_CODE` 상세코드가 사용 중인지 `CodeUtil.existsCode`로 확인한다.

| 스케줄러 | 현재 주기 | 목적 |
| --- | --- | --- |
| `REPORT_DATE_OVER` | 09:00~09:55 5분 간격, 10:00 추가 실행 | 목표 독서기간 초과 알림 |
| `ALIM_DELETE` | 10분 간격 | 삭제 상태 알림 물리 삭제 |
| `USER_HARD_DELETE` | 매일 03:00 | 유예기간이 지난 회원 영구 삭제 |

로컬 프로필에는 영구 탈퇴를 바로 검증하기 위한 별도 테스트 스케줄러가 있으며 설정 플래그로 제한한다.

구현 근거:

- `src/main/java/org/our/sadari/global/scheduler/Scheduler.java`
- `src/main/java/org/our/sadari/global/scheduler/LocalUserHardDeleteScheduler.java`

## 대량 대상 처리

목표기간 초과 알림은 대상이 많을 수 있으므로 설정한 `maxSize`만큼 제한해 한 실행에서 처리한다. 조회 조건은 목표기간이 지난 지 48시간 이내이며 동일 템플릿 알림이 아직 발송되지 않은 독후감으로 제한한다.

관련 구현:

- `src/main/java/org/our/sadari/global/scheduler/service/ReportDateOverServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/ReportDateOverMapper.xml`

## 스케줄러 실행·실패 로그

스케줄러는 `TL_SCLOGX`에 실행 단위 마스터 로그를 기록하고, 개별 실패는 `TL_SCFAIL`에 기록한다.

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

## 향후 개선

- FCM의 `Device unregistered` 응답 시 해당 구독을 자동 비활성화하는 정책을 명확히 검증한다.
- 다중 서버 배포 시 동일 스케줄러 중복 실행을 막는 분산 잠금이 필요하다.
- 대량 푸시는 비동기 큐와 재시도·Dead Letter Queue 구조로 확장할 수 있다.

