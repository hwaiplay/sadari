# 알림 및 푸시 정책

## 개요

- 기준일은 2026년 8월 21일입니다.
- 사용자 알림 생성·조회·삭제, 브라우저 푸시 발송과 계정 상태별 처리 기준에 적용합니다.

## 알림 생성

- 공통 발송 메서드는 수신자, 상황 코드, 템플릿 코드, 대상 번호, 치환 Map을 받습니다.
- `TB_ALTEMP`에서 상황 코드와 템플릿 코드가 일치하고 사용 중인 템플릿만 사용합니다.
- 템플릿의 `#{key}` 상용구를 전달받은 Map 값으로 치환합니다.
- 템플릿 링크와 대상 번호를 조합해 실제 이동 링크를 만듭니다.
- 탈퇴 또는 영구 삭제 대기 회원에게는 새 알림과 푸시를 생성하지 않습니다.
- 알림 DB 저장이 커밋된 뒤 FCM 푸시를 발송합니다.
- 푸시는 부가 기능이므로 푸시 실패가 이미 저장된 알림을 롤백하지 않습니다.
- 댓글 또는 대댓글 좋아요가 신규 등록되면 해당 댓글 작성자에게만 `LIKE`·`REPLY_LIKE` 알림과 푸시를 생성합니다.
- 댓글 좋아요 발신자 닉네임은 좋아요 대상 검증 시 활성 회원 원본에서 함께 조회하며 Redis 로그인 캐시 유무로 알림 생성을 생략하지 않습니다.
- 본인 댓글 좋아요, 중복 좋아요 등록 및 좋아요 취소에는 댓글 좋아요 알림을 생성하지 않습니다.
- 모임장이 활성 맞팔 회원을 초대하면 초대 대상자에게 `CLUB`·`INVITE_CLUB` 알림을 저장하고, 활성 푸시 구독이 있으면 커밋 후 푸시를 발송합니다.
- `INVITE_CLUB` 템플릿에는 `#{userName}`으로 모임장 닉네임을, `#{clubName}`으로 모임명을 전달하며 링크 대상 번호에는 모임 번호를 사용합니다.
- 독서 타이머 목표시간이 지나면 `REPORT` 상황의 `BOOK_TIMER_OVER` 템플릿에 `#{timerTime}`을 전달하고 `/timer`로 이동하는 알림을 저장합니다.

## 중복 방지

- 수신자, 상황, 템플릿, 제목, 내용, 링크가 모두 같은 알림을 비교합니다.
- 동일한 알림이 최근 1시간 이내에 있으면 새 알림 생성을 생략합니다.
- 반복적인 좋아요 또는 팔로우 조작으로 같은 알림이 누적되는 것을 방지합니다.
- 댓글 좋아요 취소 후 1시간 안에 같은 표시 내용과 링크로 다시 좋아요가 등록되면 새 알림 생성을 생략합니다.
- `BOOK_TIMER_OVER`는 세션마다 독립된 목표시간 이벤트이므로 댓글 등록 알림과 같이 1시간 중복 차단 대상에서 제외합니다.

## 알림센터

- 삭제되지 않은 알림을 한 번에 10개씩 조회합니다.
- 다음 페이지 존재 여부를 판단하기 위해 서버는 내부적으로 한 건을 추가 조회할 수 있습니다.
- 스크롤 시 다음 10개를 추가 조회합니다.
- 읽은 알림도 목록에 표시하지만 읽지 않은 알림보다 어둡게 표현합니다.
- 알림 항목을 눌러 링크로 이동하면 해당 알림을 읽음 처리하고 `READ_DATE`를 기록합니다.
- 푸시 알림을 눌러 링크로 이동한 경우에도 같은 알림을 읽음 처리합니다.
- 이미 읽은 알림의 읽음 요청은 성공으로 처리하는 멱등 방식입니다.
- 미읽음 배지는 읽지 않고 삭제되지 않은 알림 수를 표시합니다.

## 모두 지우기

- 모두 지우기는 현재 화면에 로드한 알림뿐 아니라 로그인 사용자의 삭제되지 않은 모든 알림에 적용합니다.
- `DELT_YSNO`를 `Y`로 변경해 알림센터 노출 대상에서 제외합니다.
- 읽음 여부는 모두 지우기에서 변경하지 않습니다.
- 삭제 애니메이션 이후 알림 없음 상태를 표시합니다.
- 삭제 상태 알림은 알림 삭제 스케줄러가 물리 삭제합니다.

## 푸시 구독

- 브라우저 알림 권한 요청은 사용자의 명시적인 푸시 알림 켜기 클릭 직후 실행합니다.
- 허용된 FCM 토큰과 브라우저 구독 정보를 `TB_PSHSUB`에 저장합니다.
- 푸시 알림 켜짐 상태는 `USEE_YSNO = 'Y'`입니다.
- 푸시 알림을 끄면 구독 행을 삭제하지 않고 `USEE_YSNO = 'N'`으로 변경합니다.
- 다시 켜면 유효한 구독을 활성 상태로 저장하거나 갱신합니다.
- 같은 브라우저 FCM token을 다른 계정에서 활성화하면 이전 계정의 같은 token은 비활성화하여 한 token이 여러 계정의 푸시를 동시에 받지 않게 합니다.
- 현재 디바이스 로그아웃은 브라우저에서 확인한 현재 FCM token만 비활성화하고, 전체 디바이스 로그아웃은 회원의 모든 token을 비활성화합니다.
- FCM이 `Device unregistered`를 반환하면 해당 토큰은 더 이상 사용할 수 없는 구독으로 처리해야 합니다.
- 푸시를 받지 못해도 알림센터의 DB 알림은 유지됩니다.

## 알림 유형 표시

- 관리자는 `알림 아이콘 관리` 화면에서 SVG 또는 PNG 아이콘을 코드별 한 행으로 등록합니다. 아이콘 코드는 사용 중인 `ALIM_SITU` 세부코드 중에서만 선택할 수 있으며 서버에서도 같은 조건을 검증합니다.
- `ALIM_SITU.DEFAULT`는 알림 상황별 아이콘을 특정할 수 없을 때 사용하는 기본 아이콘 코드입니다.
- 업로드 파일은 200KB 이하의 정사각형 이미지이며 한 변은 16px 이상 256px 이하여야 합니다.
- PNG는 서버에서 다시 인코딩해 메타데이터를 제거하고, SVG는 UTF-8 XML·루트 네임스페이스·크기·외부 참조 및 실행 요소를 검증한 뒤 원본을 저장합니다.
- 검증한 원본은 전용 `TM_ALICON.ICON_DATA`에 저장하며 일반 파일 테이블이나 배포 서버 파일 경로를 사용하지 않습니다.
- `TM_ALICON.ALIM_SITU`가 PK이며 아이콘 번호와 별도 관리명은 사용하지 않습니다. 화면 표시명은 `ALIM_SITU` 공통코드명을 사용합니다.
- 아이콘 이미지는 신규 버전을 만들지 않고 같은 `ALIM_SITU` 행을 직접 수정합니다. 따라서 같은 알림 상황의 과거 알림에도 수정된 이미지가 표시됩니다.
- 알림 템플릿과 발송 내역은 기존 `ALIM_SITU`로 아이콘을 결정하며 별도 아이콘 식별 컬럼을 저장하지 않습니다.
- 관리자 아이콘 목록은 `ALIM_SITU`의 모든 세부코드를 기준으로 조회하고 아이콘 등록 여부를 함께 표시합니다.
- 사용자 알림센터 목록 조회는 페이징한 `TB_ALIMXX.ALIM_SITU`로 `TM_ALICON`을 조인하여 MIME 유형과 바이너리를 함께 반환하고, 상황별 아이콘이 없으면 `DEFAULT` 행을 사용합니다. 별도 공개 아이콘 API는 사용하지 않습니다.
- `DEFAULT` 아이콘도 없거나 조인된 아이콘 표시가 실패하면 사용자 화면의 기존 기본 알림 아이콘으로 대체합니다. 브라우저 푸시 시스템 아이콘은 기존 고정 정적 아이콘을 유지합니다.

## 계정 수명주기

- 댓글 좋아요 알림은 기존 계정 수명주기 정책을 적용하며 `ACTIVE` 회원 간 신규 좋아요에만 생성합니다.
- `WITHDRAWN` 또는 `DELETE_PENDING` 수신자에게는 댓글 좋아요와 독서 모임 초대를 포함한 새 알림과 푸시를 생성하지 않습니다.
- 수신자가 비활성화 또는 영구 삭제 대기 상태로 전환되면 받은 알림을 삭제 상태로 변경하고 복귀 시 자동 복원하지 않습니다.
- 계정 복귀 시 유효한 독서 모임 초대가 다시 표시되더라도 기존 초대 알림을 복원하거나 재발송하지 않습니다.
- `WITHDRAWN` 또는 `DELETE_PENDING` 전환 시 모든 기기 푸시 구독을 비활성화하며, 재로그인이나 탈퇴 취소만으로 자동 활성화하지 않습니다. 사용자가 다시 명시적으로 푸시 알림을 켜야 합니다.
- 계정 상태 전환 전에 예약된 `BOOK_TIMER_OVER` 알림은 취소하며 복귀 또는 탈퇴 취소 뒤 자동 복원하거나 재발송하지 않습니다.
- 영구 삭제 시 `TB_PSHSUB`의 회원 푸시 구독 행을 물리 삭제합니다.
- 댓글 좋아요 발신자가 탈퇴하거나 대상 댓글이 삭제돼도 이미 발송된 알림은 수신자의 기록으로 유지합니다.
- 유지된 알림은 수신자의 모두 지우기와 알림 삭제 스케줄러 및 영구 탈퇴 정책에 따라 정리합니다.
- `TM_ALICON`은 회원 소유 데이터가 아닌 서비스 전역 운영 자산이므로 회원 비활성화, 영구 탈퇴 요청·취소 및 물리 삭제와 무관하게 영구 보존합니다.
- 아이콘 이미지에는 개인정보나 민감정보를 포함하지 않으며 인증된 사용자 알림 목록 조회에서만 알림 행과 조인해 제공합니다.

## 구현 근거

- `src/main/java/org/our/sadari/alim/service/AlimServiceImpl.java`
- `src/main/java/org/our/sadari/alim/mapper/AlimMapper.xml`
- `src/main/java/org/our/sadari/push/service/PushServiceImpl.java`
- `src/main/java/org/our/sadari/push/service/FirebaseMessagingProvider.java`
- `src/main/frontend/src/pages/Alim/AlimPage.tsx`
- `src/main/frontend/src/app/pwa/firebaseMessaging.ts`
- `src/main/java/org/our/sadari/reply/service/ReplyServiceImpl.java`
- `src/main/java/org/our/sadari/reply/mapper/ReplyMapper.xml`
- `src/main/java/org/our/sadari/readingClub/service/ReadingClubServiceImpl.java`
- `src/main/java/org/our/sadari/readingClub/mapper/ReadingClubMapper.xml`
- `src/main/java/org/our/sadari/global/common/constant/Constant.java`
- `src/main/java/org/our/sadari/timer/service/ReadingTimerServiceImpl.java`
- `TB_ALTEMP`의 `LIKE`·`REPLY_LIKE`, `CLUB`·`INVITE_CLUB` 템플릿
- `TB_ALTEMP`의 `REPORT`·`BOOK_TIMER_OVER` 템플릿
- `TM_ALICON.ALIM_SITU`, `TB_ALTEMP.ALIM_SITU`, `TB_ALIMXX.ALIM_SITU`
- `sadari-admin` 저장소 `alimicon` 패키지와 `pages/alim/AlimIconDetailPage.tsx`
