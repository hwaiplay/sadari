# 회원 탈퇴 정책

## 공통 절차

1. 사용자가 탈퇴 유형과 탈퇴 사유를 선택합니다.
2. `OTHER` 사유를 선택하면 상세 사유를 필수로 입력합니다.
3. 상세 탈퇴 사유는 UTF-8 기준 최대 500바이트까지 입력할 수 있습니다.
4. 서버가 탈퇴 유형과 사유를 공통코드로 검증합니다.
5. 사용자가 최종 확인창에서 확인한 경우에만 재인증 요청을 생성합니다.
6. 탈퇴 요청은 예측 불가능한 OAuth `state`와 함께 Redis에 10분간 저장합니다.
7. Kakao 재인증 계정과 현재 로그인 계정이 일치해야 탈퇴를 진행합니다.
8. Kakao 연결 해제 성공 후 로컬 탈퇴 상태를 적용합니다.

확인창에서 취소하면 재인증 API를 호출하지 않으며 Kakao 화면으로 이동하지 않습니다.

## 서비스 탈퇴

- 탈퇴 유형: `SOFT`
- 회원 상태: `WITHDRAWN`
- 서비스 재로그인 시 기존 회원 계정을 복구할 수 있습니다.
- 회원 원본 정보와 로그인 이력은 유지합니다.
- 작성한 독후감은 모두 비공개로 전환합니다.
- 작성한 댓글은 삭제된 댓글 상태로 전환합니다.
- 수신 알림은 삭제 상태로 전환합니다.
- 브라우저 푸시 구독은 비활성화합니다.
- 팔로우 및 팔로워 관계는 유지합니다.
- 다른 사용자가 탈퇴 회원 프로필에 접근하면 제공 정보를 제한합니다.
- 탈퇴 과정에서 변경된 독후감 공개 상태, 알림, 푸시 구독은 계정 복구 시 자동 복원하지 않습니다.

## 영구 탈퇴

- 탈퇴 유형: `HARD`
- 회원 상태: `DELETE_PENDING`
- 운영 환경은 신청 직후 삭제하지 않고 기본 30일의 유예기간을 둡니다.
- 유예기간에는 영구 삭제 예정일을 사용자에게 표시합니다.
- 유예기간이 끝나기 전에는 영구 탈퇴를 취소할 수 있습니다.
- 취소하면 회원 상태를 `ACTIVE`로 변경하고 탈퇴일과 삭제 예정일을 제거합니다.
- 유예기간 동안에도 독후감 비공개, 댓글 삭제 상태, 알림 삭제 상태, 푸시 비활성화 정책을 적용합니다.
- 유예기간 종료 후 매일 03:00 영구 삭제 스케줄러가 대상 회원을 처리합니다.
- 영구 삭제 시 `TM_USERXM` 회원 원본을 삭제합니다.
- 로그인 이력은 보존하고, 정책상 보존 대상이 아닌 회원 연관 데이터는 삭제합니다.
- 탈퇴 이력에는 원본 OAuth 식별값 대신 SHA-256 해시를 저장합니다.
- 탈퇴 이력의 `USER_NUMB`는 탈퇴 당시 내부 회원번호를 감사 이력으로 보존합니다.
- `TH_USWTHD.USER_NUMB`는 회원 원본 삭제 후에도 값을 유지해야 하므로 `TM_USERXM`과 FK로 연결하지 않습니다.

## 로컬 영구 탈퇴 테스트

- `loc` 프로필은 `withdrawal.hard-delete-wait-days`를 `0`으로 설정해 영구 탈퇴 요청 시 삭제 예정일을 즉시 도래시킵니다.
- `withdrawal.hard-delete-test-enabled`가 `true`이면 로컬 전용 스케줄러가 10초마다 영구 삭제 대상을 처리합니다.
- 로컬 전용 스케줄러는 `loc` 프로필에서만 생성되며 운영 프로필에서는 활성화할 수 없습니다.
- 테스트 설정은 특정 회원만 구분하지 않으므로 연결된 DB에서 삭제 예정일이 지난 모든 회원을 대상으로 합니다.
- 공용 개발 DB에서 테스트할 때는 다른 사용자가 `DELETE_PENDING` 상태인지 확인한 뒤 실행해야 합니다.

## 외부 연동 실패

- Kakao 연결 해제가 실패하면 회원 상태를 변경하지 않습니다.
- 탈퇴 이력은 `UNLINK_PENDING` 상태로 기록하고 제한된 길이의 오류 내용을 저장합니다.
- 외부 연결 해제에 성공해야 서비스 탈퇴 또는 영구 삭제 대기 상태를 적용합니다.

## 탈퇴 상태값

| 상태 | 의미 |
| --- | --- |
| `COMPLETED` | 서비스 탈퇴 적용 완료 |
| `UNLINK_PENDING` | Kakao 연결 해제 실패로 재처리 필요 |
| `DELETE_PENDING` | 영구 삭제 유예기간 |
| `RESTORED` | 영구 탈퇴 신청 취소 |

## 구현 근거

- `user/service/UserWithdrawalServiceImpl.java`
- `user/mapper/UserWithdrawalMapper.xml`
- `global/scheduler/service/UserHardDeleteServiceImpl.java`
- `global/scheduler/LocalUserHardDeleteScheduler.java`
- `global/scheduler/mapper/UserHardDeleteMapper.xml`
- `pages/Settings/WithdrawalPage.tsx`
- `pages/Settings/WithdrawalPendingPage.tsx`
- `pages/Settings/WithdrawalResultPage.tsx`
