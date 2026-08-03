# 댓글 정책

## 현재 구현 상태

- `TB_REPLXX` 댓글 테이블과 댓글·답글 목록 조회 및 등록 API가 연결되어 있습니다.
- `PUT /api/reply/{reptNumb}/{replNumb}`는 로그인 사용자가 작성한 미삭제 댓글의 내용을 수정합니다.
- `DELETE /api/reply/{reptNumb}/{replNumb}`는 로그인 사용자가 작성한 미삭제 댓글을 논리 삭제합니다.
- 댓글 화면은 본인 댓글의 더보기 메뉴에 수정·삭제를 제공하고, 다른 사용자 댓글에는 신고·차단 메뉴를 유지합니다.

## 식별 방식

- 독후감 번호 `REPT_NUMB`와 댓글 번호 `REPL_NUMB`를 복합 기본키로 사용합니다.
- 댓글 번호는 독후감별 순번입니다.
- 독후감별 `MAX(REPL_NUMB) + 1`을 단순 조회 후 저장하면 동시 등록 충돌이 발생할 수 있으므로 등록 구현 시 독후감 단위 직렬화 또는 PK 충돌 재시도 정책이 필요합니다.
- 외래키는 사용하지 않으므로 독후감과 회원 삭제 시 서비스 또는 스케줄러가 댓글을 명시적으로 정리합니다.

## 대댓글

- `UPER_NUMB`에 부모 댓글 번호를 저장합니다.
- `UPER_NUMB`가 없으면 최상위 댓글입니다.
- `UPER_NUMB`가 있으면 대댓글입니다.
- 허용 깊이는 최상위 댓글과 그 댓글의 대댓글까지입니다.
- 대댓글에 다시 대댓글을 작성하는 3단계 구조는 허용하지 않습니다.
- 별도 레벨 컬럼 없이 부모 댓글 존재 여부와 부모의 `UPER_NUMB`를 조회해 깊이를 검증합니다.

## 삭제

- 사용자가 직접 삭제한 댓글은 `DELT_YSNO`를 `Y`로 변경하고 원문 행과 대댓글 연결 구조를 유지합니다.
- 삭제된 댓글은 원문 대신 “삭제된 댓글입니다.”를 표시하며 답글 작성, 더보기 및 좋아요 제어를 제공하지 않습니다.
- 일반 탈퇴 시 작성 댓글은 물리 삭제하지 않고 삭제된 댓글 상태로 변경합니다.
- 화면에는 원문 대신 삭제된 댓글임을 표시합니다.
- 영구 삭제 시 정책상 보존하지 않는 댓글 원문과 회원 연관 댓글 데이터를 정리합니다.
- 부모 댓글이 삭제돼도 기존 대댓글의 구조와 조회 가능성을 유지합니다.

## 수정 및 삭제 권한

- 댓글 수정·삭제 메뉴는 `myReplyYn`이 `Y`이고 `DELT_YSNO`가 `N`인 댓글에만 표시합니다.
- 서버는 독후감 번호, 댓글 번호, 인증 사용자 번호, 미삭제 상태를 모두 변경 조건으로 사용합니다.
- 서버는 `TM_USERXM.USER_STAT`이 `ACTIVE`인 작성자만 수정·삭제하도록 제한합니다.
- `WITHDRAWN` 및 `DELETE_PENDING` 계정은 메뉴를 제공하지 않으며 직접 API를 호출해도 변경 건수 없이 실패 처리합니다.
- 계정 비활성화로 삭제 상태가 된 댓글은 재로그인으로 계정이 `ACTIVE`로 복귀해도 자동 복원하지 않습니다.
- 댓글 수정·삭제는 알림과 팔로우·팔로워 관계를 변경하지 않습니다.
- 영구 삭제가 완료된 계정의 댓글 데이터는 복구하지 않습니다.

## 구현 근거

- `TB_REPLXX`
- `src/main/java/org/our/sadari/reply/controller/ReplyController.java`
- `src/main/java/org/our/sadari/reply/service/ReplyServiceImpl.java`
- `src/main/java/org/our/sadari/reply/mapper/ReplyMapper.xml`
- `src/main/java/org/our/sadari/user/mapper/UserMapper.xml`
- `src/main/java/org/our/sadari/global/scheduler/mapper/UserHardDeleteMapper.xml`
- `src/main/frontend/src/features/reply/ReplySheetView.tsx`
- `src/main/frontend/src/features/reply/hooks/useSetReplyForm.tsx`
- `src/main/frontend/src/features/reply/hooks/useDelReply.ts`
