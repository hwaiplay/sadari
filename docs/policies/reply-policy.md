# 댓글 정책

## 개요

- 기준일은 2026년 8월 21일입니다.
- 독후감 댓글과 답글의 등록·조회·변경·삭제·좋아요 및 관련 알림 처리에 적용합니다.

## 현재 구현 상태

- `TB_REPLXX` 댓글 테이블과 댓글·답글 목록 조회 및 등록 API가 연결되어 있습니다.
- `PUT /api/reply/{reptNumb}/{replNumb}`는 로그인 사용자가 작성한 미삭제 댓글의 내용을 수정합니다.
- `DELETE /api/reply/{reptNumb}/{replNumb}`는 로그인 사용자가 작성한 미삭제 댓글을 논리 삭제합니다.
- `PUT /api/reply/{reptNumb}/{replNumb}/likes`는 댓글 좋아요를 멱등하게 등록합니다.
- `DELETE /api/reply/{reptNumb}/{replNumb}/likes`는 댓글 좋아요를 멱등하게 취소합니다.
- 댓글 화면은 본인 댓글의 더보기 메뉴에 수정·삭제를 제공하고, 다른 사용자 댓글에는 신고·차단 메뉴를 유지합니다.
- 댓글 목록은 댓글별 좋아요 수와 현재 로그인 사용자의 좋아요 여부를 함께 조회합니다.

## 식별 방식

- 독후감 번호 `REPT_NUMB`와 댓글 번호 `REPL_NUMB`를 복합 기본키로 사용합니다.
- 댓글 번호는 `AUTO_INCREMENT`와 `UK_TB_REPLXX_NUMB`로 전체 댓글에서 유일하게 발급합니다.
- 독후감, 부모 댓글 및 회원 외래키가 있으므로 영구 삭제 프로시저는 참조 순서에 맞춰 댓글과 관련 좋아요를 명시적으로 정리합니다.

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
- 댓글 목록은 원문과 삭제 상태의 SHA-256 내용 해시를 `editVersion`으로 제공합니다.
- 댓글 수정 요청은 선택 시점의 `editVersion`을 포함하고, 서버는 현재 DB 내용 해시와 같을 때만 수정합니다.
- 다른 탭이나 기기에서 먼저 수정하거나 삭제해 해시가 달라지면 원문을 덮어쓰지 않고 HTTP 409로 응답합니다.

## 댓글 좋아요

- 댓글 좋아요는 공용 좋아요 테이블 `TB_LIKEXX`에 `TAGT_TYPE = REPLY`, `TAGT_NUMB = REPL_NUMB`로 저장합니다.
- 댓글 번호 `REPL_NUMB`는 전체 댓글에서 유일하므로 범용 좋아요 대상 번호로 사용하고, API에서는 `REPT_NUMB`와 함께 전달해 댓글 소속을 검증합니다.
- `ACTIVE` 사용자만 미삭제 댓글에 좋아요를 등록하거나 취소할 수 있습니다.
- 본인이 작성한 댓글에도 좋아요를 등록할 수 있습니다.
- `WITHDRAWN` 및 `DELETE_PENDING` 사용자는 댓글 화면과 API 모두에서 좋아요 접근을 제한합니다.
- 삭제된 댓글은 좋아요 버튼과 좋아요 수를 표시하지 않으며 좋아요 API 대상으로 허용하지 않습니다.
- 계정 비활성화 또는 영구 삭제 대기 전환 시 사용자가 댓글에 남긴 좋아요는 삭제하고, 계정이 복귀해도 자동 복원하지 않습니다.
- 영구 삭제 시 사용자가 남긴 댓글 좋아요와 물리 삭제되는 댓글을 대상으로 한 좋아요를 모두 삭제합니다.
- 댓글 또는 대댓글에 좋아요가 신규 등록되면 해당 `REPL_NUMB` 행의 작성자에게만 `LIKE` 상황의 `REPLY_LIKE` 템플릿 알림과 푸시를 생성합니다.
- 좋아요 등록자의 닉네임은 활성 회원 원본에서 대상 댓글과 함께 조회하여 Redis 로그인 캐시가 없더라도 알림 문구를 생성합니다.
- 본인이 작성한 댓글 또는 대댓글에 직접 등록한 좋아요는 자기 알림을 생성하지 않습니다.
- 이미 등록된 좋아요의 멱등 요청과 좋아요 취소는 알림을 새로 만들거나 기존 알림을 삭제하지 않습니다.
- 좋아요 취소 후 1시간 안에 같은 사용자가 같은 작성자의 같은 독후감 댓글에 다시 좋아요를 등록하면 공통 알림 중복 방지 정책에 따라 새 알림을 생략할 수 있습니다.
- 알림 클릭 링크는 좋아요 대상 댓글 또는 대댓글이 속한 독후감 상세 화면으로 연결합니다.
- 댓글 작성자가 `WITHDRAWN` 또는 `DELETE_PENDING` 상태이면 새 알림과 푸시를 생성하지 않습니다.
- 좋아요 등록자가 탈퇴하거나 대상 댓글이 삭제돼도 이미 발송된 알림은 수신자의 기록으로 유지하며 공통 알림 삭제 정책에 따라 정리합니다.

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
- `src/main/frontend/src/features/reply/hooks/useReplyLike.ts`
- `TB_LIKEXX`
- `TB_ALTEMP`의 `LIKE`·`REPLY_LIKE` 템플릿
