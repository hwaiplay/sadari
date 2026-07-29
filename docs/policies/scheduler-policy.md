# 스케줄러 운영 정책

## 공통 실행 조건

- 전역 설정 `scheduler.enabled`가 `true`일 때 스케줄러 컴포넌트를 활성화합니다.
- 개별 스케줄러는 `SCHD_CODE` 공통코드의 상세코드가 사용 상태일 때만 실행합니다.
- 상세코드가 없거나 사용 중지 상태이면 업무 호출과 실행 로그 생성을 모두 생략합니다.
- 한 번에 처리할 최대 대상 수는 `scheduler.max-size` 설정을 사용하며 기본 운영 기준은 100건입니다.

## 목표 독서기간 초과 알림

- 스케줄러 코드: `REPORT_DATE_OVER`
- 실행 시간: 매일 09:00부터 09:55까지 5분 간격, 추가로 10:00 정각
- 한 번 실행할 때 최대 100건을 처리합니다.
- 독서 상태가 진행 중인 독후감을 대상으로 합니다.
- 목표 종료일이 오늘, 어제 또는 이틀 전인 최근 범위를 후보로 조회합니다.
- 날짜 컬럼이 일 단위이므로 오늘을 포함한 최근 3개 날짜를 48시간 이내 정책 범위로 사용합니다.
- 같은 독후감 링크로 `REPORT_DATE_OVER` 알림이 이미 저장됐으면 다시 발송하지 않습니다.
- 알림 문구 치환을 위해 도서 제목을 함께 조회합니다.
- 템플릿 코드 `REPORT_DATE_OVER`를 사용합니다.

## 삭제 상태 알림 정리

- 스케줄러 코드: `ALIM_DELETE`
- 실행 주기: 10분마다
- `TB_ALIMXX.DELT_YSNO = 'Y'`인 알림을 물리 삭제합니다.
- 삭제 대상이 없으면 불필요한 실행 로그를 남기지 않습니다.

## 영구 탈퇴 회원 삭제

- 스케줄러 코드: `USER_HARD_DELETE`
- 실행 시간: 매일 03:00
- 영구 삭제 유예기간 30일이 지난 회원을 대상으로 합니다.
- 정책상 삭제 대상인 회원 연관 데이터와 `TM_USERXM` 회원 원본을 삭제합니다.
- 로그인 이력은 보존합니다.
- 한 번 실행할 때 `scheduler.max-size` 범위만 처리하고 다음 실행에서 나머지를 처리합니다.

## 실행 로그

- 실행 마스터 로그는 `TL_SCLOGX`에 저장합니다.
- 실패 상세만 `TL_SCFAIL`에 저장합니다.
- 실패 순번은 실행 번호별 `MAX(FAIL_NUMB) + 1`로 발급합니다.
- 실행 결과는 `RUNNING`, `NO_DATA`, `SUCCESS`, `PARTIAL`, `FAILURE`로 구분합니다.
- 업무 거절은 `REJECTED`, Java 예외는 `EXCEPTION` 실패 유형으로 기록합니다.
- 대상 수, 성공 수, 실패 수가 모두 0이면 실행 로그를 저장하지 않습니다.
- 개별 대상 실패는 전체 배치를 즉시 중단하지 않고 실패 상세에 기록한 뒤 가능한 나머지 대상을 계속 처리합니다.
- 로그 메시지는 최대 1,000자로 제한합니다.

## 구현 근거

- `global/scheduler/Scheduler.java`
- `global/scheduler/common/SchedulerLogSupport.java`
- `global/scheduler/service/ReportDateOverServiceImpl.java`
- `global/scheduler/service/AlimDeleteServiceImpl.java`
- `global/scheduler/service/UserHardDeleteServiceImpl.java`
- `global/scheduler/mapper/SchedulerLogMapper.xml`
