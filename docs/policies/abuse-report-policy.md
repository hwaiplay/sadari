# 신고 접수 및 처리 정책

## 개요

- 기준일은 2026년 8월 22일입니다.
- 사용자, 독후감, 댓글 및 향후 독서 모임에 대한 공통 신고 데이터 구조에 적용합니다.
- 현재 구현 범위는 공통코드, `TH_CMPLNT` 테이블, 사용자 신고 접수 화면과 API 및 관리자 신고 조회·처리 화면입니다.
- 모든 신고 유형은 `TAGT_USER`로 연결된 피신고자에게 관리자 신고 상세에서 기존 회원 이용정지 기능을 동일하게 사용할 수 있습니다.
- 독후감, 댓글, 프로필 사진, 배경사진 및 한줄소개는 반려를 제외한 동일 대상 버전 신고가 로컬 1건 또는 운영 5건 누적될 때 서버가 자동 조치하며 결과를 `TH_CMACTN`에 기록합니다.
- 사용자 전체 신고, 모임 신고, 회원 이용정지 및 자동 조치 범위 밖의 운영 제재는 관리자가 신고 상세에서 명시적으로 실행합니다.

## 신고 대상

신고 대상은 `CMPL_TAGT` 공통코드로 구분합니다.

| 코드 | 대상 | 대상 번호 |
| --- | --- | --- |
| `CMPL_USER` | 사용자 계정 | `TM_USERXM.USER_NUMB` |
| `CMPL_BOOK_REPORT` | 공개 독후감 | `TM_REPORT.REPT_NUMB` |
| `CMPL_REPLY` | 독후감 댓글과 답글 | `TB_REPLXX.REPL_NUMB` |
| `CMPL_CLUB` | 향후 독서 모임 | `TM_CLUBXM.CLUB_NUMB` |
| `CMPL_PROF_IMAGE` | 사용자의 현재 프로필 사진 | `TM_USERXM.USER_NUMB` |
| `CMPL_BG_IMAGE` | 사용자의 현재 배경사진 | `TM_USERXM.USER_NUMB` |
| `CMPL_INTRO` | 사용자의 현재 한줄소개 | `TM_USERXM.USER_NUMB` |

- `TH_CMPLNT`에는 `TAGT_TYPE`과 `TAGT_NUMB`를 함께 저장합니다.
- 여러 대상 테이블을 하나의 외래키로 참조할 수 없으므로 `TAGT_NUMB`에는 물리 외래키를 생성하지 않습니다.
- 신고 등록 서버는 `TAGT_TYPE`별 허용 테이블과 식별 컬럼을 고정 매핑하고 대상의 존재 여부와 신고 가능 상태를 검증합니다.
- 서버는 클라이언트가 화면 이동 상태로 보유한 본문을 저장하지 않고 대상 번호로 원본을 다시 조회하여 `TAGT_CNTN`에 접수 시점 스냅샷을 저장합니다.
- 서버는 같은 원본 조회 결과에서 대상 소유 사용자 번호를 확정해 `TAGT_USER`에 함께 저장하며, 클라이언트가 전달한 소유자 번호는 사용하지 않습니다.
- `CMPL_USER`는 세부 신고 대상과 겹치지 않도록 신고 시점의 닉네임만 저장하고, `CMPL_BOOK_REPORT`는 독후감 본문, `CMPL_REPLY`는 댓글 또는 답글 본문을 스냅샷으로 저장합니다.
- 모든 신규 신고는 실제 원문 또는 실제 이미지 바이트에서 계산한 SHA-256 값을 `TAGT_HASH`에 저장합니다. 같은 대상 번호라도 내용이 수정되면 별도 버전으로 중복 제한과 자동 조치 누적을 계산합니다.
- `CMPL_PROF_IMAGE`와 `CMPL_BG_IMAGE`는 현재 이미지의 원본 파일명을 `TAGT_CNTN`에 표시용으로 저장하고 실제 이미지 원본은 관리자 전용 `TH_CMEVDC`에 보관하여 신고와 `EVDC_NUMB`로 연결합니다. `CMPL_INTRO`는 현재 한줄소개 원문을 저장합니다.
- `ACTIVE` 상태로 실제 소셜 프로필과 콘텐츠가 공개되는 대상만 새로 신고할 수 있습니다. `WITHDRAWN`, `DELETE_PENDING` 대상의 기존 신고와 증거는 유지하지만 숨겨진 계정·독후감·댓글·프로필 사진·배경사진·한줄소개에 대한 신규 신고는 허용하지 않습니다.
- 새로운 신고 대상은 `CMPL_TAGT` 상세코드와 서버의 대상 검증 매핑을 추가하며 테이블 구조는 변경하지 않습니다.
- `CMPL_CLUB`은 향후 독서 모임 신고에 사용할 예약 코드이며 관련 화면과 API가 출시되기 전에는 신고 요청값으로 허용하지 않습니다.

## 신고 사유

신고 사유는 `CMPL_RSON` 공통코드의 활성 상세코드만 허용합니다.

| 코드 | 표시명 | 적용 범위 |
| --- | --- | --- |
| `CMPL_SPAM` | 스팸 및 홍보 | 도배와 상업성 홍보 |
| `CMPL_ABUSE` | 욕설 및 괴롭힘 | 욕설, 비방 및 괴롭힘 |
| `CMPL_SEXUAL` | 음란 및 성적 콘텐츠 | 음란물과 부적절한 성적 표현 |
| `CMPL_PRIVACY` | 개인정보 노출 | 개인정보와 사생활 침해 |
| `CMPL_ILLEGAL` | 불법 및 권리 침해 | 불법 정보와 저작권 등 권리 침해 |
| `CMPL_OTHER` | 기타 | 정형 사유로 분류되지 않는 신고 |

- `CMPL_OTHER`를 선택하면 신고 화면과 API에서 `CMPL_CNTN`을 필수로 검증합니다.
- `CMPL_CNTN`은 화면과 백엔드에서 최대 500자로 제한하며, 저장 전 공통 비속어 사전으로 검증합니다.

## 처리 상태

신고 처리 상태는 `CMPL_STAT` 공통코드로 관리합니다.

| 코드 | 표시명 | 의미 |
| --- | --- | --- |
| `CMPL_RECEIVED` | 접수 | 관리자 검토 전 상태 |
| `CMPL_REVIEWING` | 검토 중 | 관리자가 신고 내용을 확인하는 상태 |
| `CMPL_ACTIONED` | 조치 완료 | 신고에 따른 운영 조치를 완료한 상태 |
| `CMPL_REJECTED` | 반려 | 운영 조치 대상이 아니어서 종결한 상태 |

- 신규 신고의 기본 상태는 `CMPL_RECEIVED`입니다.
- `CMPL_RECEIVED` 신고는 담당 관리자가 `CMPL_REVIEWING`으로 변경한 뒤에만 최종 처리할 수 있습니다.
- `CMPL_ACTIONED`와 `CMPL_REJECTED`에는 관리자 처리 내용을 필수로 저장하고 `PROC_DATE`를 최종 처리 일시로 기록합니다.
- 다른 일반 관리자는 검토 담당자의 신고를 최종 처리할 수 없으며 `SUPER` 관리자는 담당 신고를 인계받아 처리할 수 있습니다.
- `UPDT_DATE`는 여러 관리자가 같은 신고를 동시에 덮어쓰지 않도록 화면 조회 버전 검증에 사용합니다.
- 회원 이용정지와 신고 상태는 각각 명시적으로 처리합니다. 이용정지 적용 또는 해제가 신고를 자동으로 최종 처리하지 않으며 관리자가 처리 내용을 확인한 뒤 `CMPL_ACTIONED`로 변경합니다.
- 누적 자동 조치가 적용되면 같은 대상의 `CMPL_RECEIVED`와 `CMPL_REVIEWING` 신고를 `CMPL_ACTIONED`로 일괄 변경하고 자동 조치 결과 내용을 `PROC_CNTN`과 `PROC_DATE`에 기록합니다. 자동 조치에는 처리 관리자가 없으므로 `PROC_ADMN`은 `NULL`로 저장합니다.

## 관리자 신고 관리

- 신고 관리 메뉴는 관리자 사용자 관리의 하위 메뉴로 제공합니다.
- 신고 목록은 신고번호, 처리 상태, 대상 유형과 번호, 신고 사유, 신고자 및 접수일 범위로 검색합니다.
- 신고 상세는 별도 사용자 상세 화면으로 이동하지 않고 `TAGT_TYPE`과 `TAGT_NUMB`를 기준으로 처리합니다.
- 신고 상세는 별도의 신고 대상 영역을 만들지 않고 신고 정보 표 안에 `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_USER`, 접수 시 저장된 `TAGT_CNTN` 원문 스냅샷과 피신고자의 현재 정보를 함께 표시합니다.
- 사용자 신고 화면은 익명 처리 범위를 `피신고자에게 신고자 정보가 공개되지 않음`으로 안내합니다. 관리자는 운영상 신고자 회원번호와 닉네임만 확인하며, 신고 관리 상세 API와 화면에서 신고자의 프로필·배경 이미지를 조회하거나 표시하지 않습니다.
- `TAGT_USER`로 연결된 피신고자의 현재 프로필·배경 이미지는 `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 상태에서 원본 파일이 남아 있을 때 관리자 이미지 보기 기능으로 제공합니다. 프로필·배경사진 신고는 현재 이미지와 별도로 접수 시점의 실제 증거 원본도 관리자 전용으로 제공합니다.
- `현 사용자 관리 > 현 사용자 상세`는 `TAGT_USER`로 연결된 사용자 본인·독후감·댓글이 받은 신고의 누적 횟수와 전체 이력을 함께 제공하며, 각 이력에는 대상 유형·대상 내용 스냅샷·신고자·사유·상세 내용·처리 상태·접수일·처리일을 표시합니다.
- 동일한 `TAGT_TYPE`과 `TAGT_NUMB`의 최근 다른 신고 10건과 전체 건수를 관리자 판단 정보로 제공합니다.
- 모든 신고 유형에서 `TAGT_USER`를 회원번호로 사용하여 기존 사용자 관리와 같은 이용정지 등록·해제·이력을 제공합니다. `TAGT_NUMB`는 콘텐츠 신고에서 회원번호로 해석하지 않습니다.
- 이용정지 API는 클라이언트에서 회원번호를 받지 않고 신고번호로 저장된 `TAGT_USER`를 서버에서 확정합니다.
- 관리자 삭제 권한이 있으면 `신고 관리 > 신고 상세`와 `현 사용자 관리 > 현 사용자 상세`에서 피신고자의 프로필 사진과 배경사진 참조를 제거해 기본 이미지 상태로 변경하고 자기소개를 `NULL` 처리할 수 있습니다. 참조되지 않는 이미지 메타정보와 내부 저장소 파일은 DB 커밋 뒤 삭제합니다.
- 관리자 삭제 권한이 있으면 `CMPL_BOOK_REPORT`의 독후감과 연결 댓글·답글·좋아요를 완전 삭제하고, `CMPL_REPLY`는 원문 행을 보존하면서 `DELT_YSNO='Y'`로 변경하며, `CMPL_CLUB`은 `CLUB_CNTN`을 `NULL` 처리합니다.
- 신고 관리에서 독후감·댓글·모임 소개 원본을 조치하면 같은 `TAGT_TYPE`, `TAGT_NUMB`의 `CMPL_RECEIVED`·`CMPL_REVIEWING` 신고를 같은 트랜잭션에서 `CMPL_ACTIONED`로 일괄 종결합니다. 콘텐츠 번호 전체가 비노출되므로 과거 `TAGT_HASH` 버전도 함께 종결합니다.
- 신고 관리 또는 현 사용자 상세에서 프로필 사진·배경사진을 기본 이미지로 변경하거나 한줄소개를 `NULL` 처리하면 해당 사용자의 `CMPL_PROF_IMAGE`, `CMPL_BG_IMAGE` 또는 `CMPL_INTRO` 미처리 신고만 같은 트랜잭션에서 일괄 종결합니다. 이용정지는 다른 신고를 자동 종결하지 않습니다.
- 수동 원본 조치로 종결한 신고에는 `PROC_ADMN`, `PROC_CNTN`, `PROC_DATE`를 저장하며, 이미 `CMPL_ACTIONED` 또는 `CMPL_REJECTED`인 신고의 기존 최종 판단은 덮어쓰지 않습니다.
- 신고 정보 표는 접수 당시 `TAGT_CNTN`과 현재 원본 존재 여부를 구분해 표시하며, 원본을 조치한 뒤에도 스냅샷은 변경하거나 삭제하지 않습니다.
- `TH_CMPLNT` 신고 이력 자체는 관리자 조치로 삭제하지 않습니다. 메뉴 삭제 권한은 피신고자 정보와 신고 대상 원본 조치 API에만 사용합니다.
- 관리자 조치 알림은 알림 템플릿이 확정된 뒤 별도 연동하며 현재는 알림과 푸시를 생성하지 않습니다.

## 신고 누적 자동 조치

자동 조치는 환경별 `complaint.auto-action` 설정을 사용합니다. 로컬은 기능 검증을 위해 다섯 대상 모두 1건, 운영은 정책 기준인 5건으로 고정하며 운영 환경변수로 임계치를 변경하지 않습니다. 관리자 앱도 로컬 기본 설정은 1건, `prod` 프로필은 5건을 사용해 사용자 서버와 같은 값을 표시합니다.

| 신고 대상 유형 | 로컬 / 운영 임계치 | 자동 조치 유형 | 원본 처리 |
| --- | ---: | --- | --- |
| `CMPL_BOOK_REPORT` | 1건 / 5건 | `CMPL_HIDE_REPORT` | 독후감의 `PUBC_YSNO`를 `N`으로 변경하고 원본과 연결 데이터 보존 |
| `CMPL_REPLY` | 1건 / 5건 | `CMPL_DEL_REPLY` | 댓글 또는 답글의 `DELT_YSNO`를 `Y`로 변경 |
| `CMPL_PROF_IMAGE` | 1건 / 5건 | `CMPL_RESET_PROF` | `PROF_NUMB`를 `NULL`로 변경하고 미참조 파일을 커밋 후 정리 |
| `CMPL_BG_IMAGE` | 1건 / 5건 | `CMPL_RESET_BG` | `BGIM_NUMB`를 `NULL`로 변경하고 미참조 파일을 커밋 후 정리 |
| `CMPL_INTRO` | 1건 / 5건 | `CMPL_CLEAR_INTRO` | `INTR_CNTN`을 `NULL`로 변경 |

- 자동 조치 누적 건수에서는 현재 처리 상태가 `CMPL_REJECTED`인 신고만 제외합니다. `CMPL_RECEIVED`, `CMPL_REVIEWING`, `CMPL_ACTIONED` 신고는 동일한 `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH`일 때만 누적 기준에 포함합니다.
- 각 대상 버전의 누적 건수가 현재 환경의 임계치에 도달하면 자동 조치를 실행하고 `ACTN_ORDR`를 저장합니다. 조치 뒤 원본은 비공개·삭제·초기화되어 같은 버전의 추가 신고와 다음 임계치 계산을 중지합니다. 콘텐츠 또는 프로필 정보가 새로 등록·수정되면 새 `TAGT_HASH`의 1건부터 별도로 누적하고 이전 버전 신고를 섞지 않습니다.
- 독후감 자동 조치는 비공개 전환만 수행합니다. 연결 댓글·답글·좋아요와 독후감 원본의 완전 삭제는 관리자 삭제 권한으로만 실행합니다.
- `CMPL_USER`는 사용자 계정 전반을 다루므로 프로필 사진·배경사진·한줄소개 자동 조치 건수에 합산하지 않습니다.
- 신고 대상 원본은 신고 저장 전에 잠금 조회하고 누적 건수도 현재 커밋 결과를 읽는 잠금 조회로 계산합니다. 서로 다른 사용자의 신고가 동시에 도착해도 대상별 저장과 누적 건수 계산을 직렬화하여 임계 신고를 누락하거나 같은 조치 순번을 중복 실행하지 않습니다.
- 원본 조치, `TH_CMACTN` 결과 저장 및 관련 신고 상태 변경은 신고 저장 트랜잭션에 포함합니다. 어느 단계든 실패하면 신규 신고를 포함한 전체 변경을 롤백하여 원본만 삭제되거나 결과 이력만 남는 부분 성공을 허용하지 않습니다.
- 자동 조치 성공 결과는 `CMPL_RSLT`의 `CMPL_APPLIED`로 기록합니다. 롤백된 실패는 결과 이력으로 확정하지 않으며 서버 오류 처리와 운영 로그로 확인합니다.
- 자동 조치 알림과 푸시는 템플릿 확정 뒤 별도 연동하며 현재는 생성하지 않습니다.
- `관리자 > 신고 관리 > 신고 상세`는 해당 대상 유형의 자동 조치 적용 여부, 예정 조치, 반려 제외 유효 신고 누적 건수와 현재 환경 임계치를 표시합니다. 현재 원본 해시가 신고 당시 버전과 같고 실제 노출 중인 `PENDING` 상태에만 다음 실행 누적 건수와 남은 건수를 표시합니다.
- 현재 원본이 없거나 버전이 바뀐 경우에는 `자동조치 완료`, `관리자 수동조치 완료`, `신고 당시 대상 버전 비노출`, `신고 대상 원본 없음` 중 판정된 상태를 표시하고 “다음 자동조치까지 N건”을 표시하지 않습니다.
- 자동 조치가 실제 실행되면 같은 영역에 조치 순번, 조치·결과 공통코드명, 실행 당시 누적·임계 건수, 자동 조치를 발생시킨 신고번호, 결과 상세와 실행일시를 `TH_CMACTN` 기준 최신 순으로 표시합니다.
- `CMPL_USER`와 `CMPL_CLUB`은 자동 조치 미적용 대상으로 명시하고 관리자 검토와 수동 조치 대상임을 안내합니다.

## 중복과 접근 범위

- 동일 사용자는 처리 상태와 관계없이 같은 `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH`의 대상 버전을 한 번만 신고할 수 있습니다. 대상 내용이 실제로 변경되어 해시가 달라지면 새 버전을 신고할 수 있습니다.
- 신고 API는 기존 신고를 먼저 조회해 재신고를 안내하고, `UK_TH_CMPLNT_USER_TAGT` 고유 제약으로 동시에 도착한 중복 요청도 차단합니다.
- 중복 신고 요청에는 `COMPLAINT_DUPLICATED` 응답을 반환하며 사용자 화면은 `이미 신고한 대상이에요.` 알림과 함께 동일 대상 재신고가 불가능함을 안내합니다.
- 일반 사용자는 본인이 등록한 신고를 포함한 신고 심사 내역을 조회하지 않습니다.
- 신고 데이터와 관리자 처리 내용은 관리자만 조회할 수 있습니다.
- `ACTIVE` 계정만 신고할 수 있으며 자기 자신과 본인 소유 콘텐츠의 신고는 서버에서 거절합니다.

## 계정 수명주기와 데이터 보존

- `WITHDRAWN` 상태에서는 기존 신고를 유지하고 관리자만 조회할 수 있으며 신고 접수와 피신고 대상 노출을 모두 중지합니다. 같은 계정이 `ACTIVE`로 복귀하면 보존된 신고 이력을 기준으로 동일 대상 버전 재신고를 계속 차단합니다.
- `DELETE_PENDING` 유예기간에는 기존 신고를 유지하고 영구 탈퇴 취소 시에도 별도 복원 없이 같은 신고 이력과 중복 신고 제한을 유지합니다.
- 신고자의 계정이 물리 삭제되면 `TH_CMPLNT.USER_NUMB`를 `NULL`로 변경하여 신고자를 익명화하고 신고 기록은 운영 이력으로 유지합니다. 이후 생성된 새 계정은 익명화된 과거 신고와 연결하지 않으므로 신고 가능한 대상이면 새 계정 기준으로 신고할 수 있습니다.
- 신고 대상 사용자 또는 콘텐츠가 삭제되어도 `TAGT_TYPE`과 `TAGT_NUMB`는 유지합니다.
- 신고 대상자의 `WITHDRAWN` 또는 `DELETE_PENDING` 상태에서도 `TAGT_USER` 연결을 유지하여 현 사용자 상세의 누적 횟수와 전체 이력을 계속 제공합니다. 계정이 복귀하거나 영구 탈퇴를 취소해도 같은 이력을 별도 복원 없이 그대로 제공합니다.
- `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 피신고자에게 동일한 관리자 프로필·콘텐츠 조치를 허용합니다. 비활성화 복귀 또는 영구 탈퇴 취소 뒤에도 삭제한 이미지, 자기소개, 독후감, 댓글 표시 상태와 모임 소개를 자동 복원하지 않습니다.
- 위 세 계정 상태에서 관리자 수동 원본 조치로 일괄 종결된 신고는 계정 복귀 또는 영구 탈퇴 취소 뒤 다시 열거나 처리 전 상태로 복원하지 않습니다.
- 누적 자동 조치도 `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 대상의 현재 원본에 같은 삭제·초기화 기준을 적용하며, 계정 비활성화 복귀 또는 영구 탈퇴 취소 시 자동 복원하지 않습니다.
- 신고 대상자 계정의 물리 삭제 트랜잭션은 `TAGT_USER`가 익명화되기 전에 해당 사용자의 `CMPL_RECEIVED`·`CMPL_REVIEWING` 신고를 시스템 처리 내용과 함께 `CMPL_ACTIONED`로 종결합니다. 이후 `TAGT_USER`와 `TH_CMEVDC.TAGT_USER`를 `NULL`로 변경하며, 대상 내용 스냅샷·해시·종결 신고 이력과 보존기간 안의 이미지 증거는 신고 관리에서만 익명 운영 이력으로 유지합니다.
- 신고 대상자 계정이 물리 삭제되면 `TH_CMACTN.TAGT_USER`도 `NULL`로 익명화합니다. 물리 삭제된 대상은 신규 신고와 자동 조치를 실행하지 않으며 기존 `TAGT_TYPE`, `TAGT_NUMB`, 조치 결과와 신고 시점 스냅샷만 운영 이력으로 유지합니다.
- 관리자 신고 상세의 자동 조치 영역은 `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 상태에서 같은 진행 정보와 이력을 제공하며, 대상자 물리 삭제 뒤에도 익명화된 기존 조치 이력을 대상 유형과 번호 기준으로 표시합니다.
- 신고 대상자 계정이 물리 삭제되면 피신고자의 현재 정보와 프로필·배경 이미지를 신고 상세에 표시하지 않으며, `TAGT_CNTN` 스냅샷만 운영 판단 근거로 유지합니다.
- 신고 접수 시 `TAGT_CNTN`에 저장한 대상 원문 또는 프로필 스냅샷은 관리자 신고 판단 근거로만 제공하며 일반 사용자에게 공개하지 않습니다.
- `TAGT_CNTN`은 신고 대상 원본의 수정·삭제, 신고자 또는 대상자의 `WITHDRAWN`, `DELETE_PENDING` 및 물리 삭제 뒤에도 신고 이력과 함께 유지합니다.
- 계정 비활성화 복귀 또는 영구 탈퇴 취소 시 `TAGT_CNTN`을 현재 원본으로 재동기화하거나 복원하지 않고 접수 시점 값으로 유지합니다.
- 프로필·배경 이미지 증거는 미처리 신고가 하나라도 연결되어 있으면 계속 보존합니다. 연결 신고가 모두 최종 처리된 뒤 마지막 `PROC_DATE`부터 180일이 지나면 `TH_CMEVDC.EVDC_DATA`를 포함한 증거 행을 물리 삭제하고 `TH_CMPLNT.EVDC_NUMB`는 `NULL`로 변경합니다.
- 신고자 또는 대상자가 `WITHDRAWN`, `DELETE_PENDING`으로 전환되어도 위 보존기간을 단축하지 않습니다. 영구 탈퇴 물리 삭제 시 회원번호만 익명화하고 증거는 만료일까지 유지합니다.
- 계정 복귀 또는 영구 탈퇴 취소는 이미지 증거의 관리자 공개 범위·만료일·삭제 상태를 변경하거나 이미 삭제된 증거를 복원하지 않습니다. 텍스트 스냅샷, 대상 해시와 자동 조치 이력은 기존 운영 이력으로 계속 유지합니다.
- 신고 데이터 처리는 알림, 푸시 구독, 팔로우, 좋아요 및 기타 소셜 관계의 삭제·보존·복원 범위를 변경하지 않습니다.

상세 계정 처리 기준은 [계정 비활성화 및 영구 탈퇴 정책](withdrawal-policy.md)을 따릅니다.

## 테이블 구조

`TH_CMPLNT`는 신고 접수와 관리자 처리 결과를 보존하는 이력 테이블입니다.

### 컬럼 명세

| 순서 | 컬럼 | 데이터 타입 | NULL | 기본값 | 속성 및 참조 | 설명 |
| ---: | --- | --- | :---: | --- | --- | --- |
| 1 | `CMPL_NUMB` | `bigint` | N | 없음 | PK, `AUTO_INCREMENT` | 신고 번호 |
| 2 | `USER_NUMB` | `bigint` | Y | `NULL` | `TM_USERXM.USER_NUMB` FK, 회원 삭제 시 `NULL` | 신고자 사용자 번호 |
| 3 | `TAGT_TYPE` | `varchar(20)` | N | 없음 | `CMPL_TAGT`의 전역 유일 세부코드 | 신고 대상 유형 |
| 4 | `TAGT_NUMB` | `bigint` | N | 없음 | 대상 유형별 업무 번호, 물리 FK 없음 | 신고 대상 번호 |
| 5 | `TAGT_HASH` | `char(64)` | N | 없음 | 실제 원문 또는 이미지 바이트 SHA-256 | 신고 대상 버전 해시 |
| 6 | `TAGT_USER` | `bigint` | Y | `NULL` | `TM_USERXM.USER_NUMB` FK, 대상자 삭제 시 `NULL` | 신고 대상 소유 사용자 번호 |
| 7 | `TAGT_CNTN` | `text` | Y | `NULL` | 신규 신고는 서버가 원본 조회 후 저장, 기존 신고는 `NULL` 가능 | 신고 대상 내용 스냅샷 |
| 8 | `EVDC_NUMB` | `bigint` | Y | `NULL` | `TH_CMEVDC.EVDC_NUMB` FK, 증거 만료 삭제 시 `NULL` | 비공개 이미지 증거 번호 |
| 9 | `CMPL_RSON` | `varchar(20)` | N | 없음 | `CMPL_RSON`의 전역 유일 세부코드 | 신고 사유 |
| 10 | `CMPL_CNTN` | `varchar(1000)` | Y | `NULL` | API 최대 500자·`CMPL_OTHER` 선택 시 필수·비속어 검증 | 신고 상세 내용 |
| 11 | `CMPL_STAT` | `varchar(20)` | N | `CMPL_RECEIVED` | `CMPL_STAT`의 전역 유일 세부코드 | 신고 처리 상태 |
| 12 | `PROC_CNTN` | `varchar(1000)` | Y | `NULL` | 관리자 처리 시 입력 | 관리자 처리 내용 |
| 13 | `PROC_ADMN` | `bigint` | Y | `NULL` | `TM_ADMINX.ADMN_NUMB` FK | 처리 관리자 번호 |
| 14 | `PROC_DATE` | `datetime(6)` | Y | `NULL` | 관리자 처리 시 입력 | 처리 일시 |
| 15 | `REGI_DATE` | `datetime(6)` | N | `CURRENT_TIMESTAMP(6)` | 등록 후 변경하지 않음 | 등록 일시 |
| 16 | `UPDT_DATE` | `datetime(6)` | N | `CURRENT_TIMESTAMP(6)` | 행 수정 시 자동 갱신 | 수정 일시 |

### 인덱스와 제약조건

| 구분 | 이름 | 컬럼 또는 참조 | 목적 및 삭제 정책 |
| --- | --- | --- | --- |
| PK | `PRIMARY` | `CMPL_NUMB` | 신고 단건 식별 |
| 고유키 | `UK_TH_CMPLNT_USER_TAGT` | `USER_NUMB`, `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH` | 동일 사용자의 동일 대상 버전 재신고와 동시 중복 접수 차단 |
| 인덱스 | `IX_TH_CMPLNT_TAGT` | `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH`, `CMPL_STAT`, `REGI_DATE` | 대상 버전별 신고와 처리 상태 조회 |
| 인덱스 | `IX_TH_CMPLNT_TAGT_USER` | `TAGT_USER`, `REGI_DATE`, `CMPL_NUMB` | 현 사용자 상세의 받은 신고 이력 최신순 조회 |
| 인덱스 | `IX_TH_CMPLNT_USER` | `USER_NUMB`, `REGI_DATE` | 신고자별 접수 이력 조회 |
| 인덱스 | `IX_TH_CMPLNT_STAT` | `CMPL_STAT`, `REGI_DATE`, `CMPL_NUMB` | 관리자 처리 대기 목록 조회 |
| FK | `FK_TH_CMPLNT_USER` | `USER_NUMB` → `TM_USERXM.USER_NUMB` | 회원 물리 삭제 시 `ON DELETE SET NULL`로 신고자 익명화 |
| FK | `FK_TH_CMPLNT_TAGT_USER` | `TAGT_USER` → `TM_USERXM.USER_NUMB` | 신고 대상자 물리 삭제 시 `ON DELETE SET NULL`로 대상자 연결 제거 |
| FK | `FK_TH_CMPLNT_EVDC` | `EVDC_NUMB` → `TH_CMEVDC.EVDC_NUMB` | 증거 만료 물리 삭제 시 `ON DELETE SET NULL`로 신고 이력 유지 |
| FK | `FK_TH_CMPLNT_PROC` | `PROC_ADMN` → `TM_ADMINX.ADMN_NUMB` | 실제 관리자 계정만 처리자로 저장 |

`TB_CODEXD.COMD_CODE`는 관리자 등록 단계에서 전체 공통코드 그룹을 대상으로 중복을 검사하므로 `TH_CMPLNT`에는 공통코드 그룹을 저장하지 않고 세부코드만 저장합니다. `TAGT_TYPE`, `CMPL_RSON`, `CMPL_STAT`의 활성 코드 여부는 신고 등록과 처리 API에서 각각 검증해야 합니다.

`TAGT_NUMB`는 `TAGT_TYPE`에 따라 사용자, 독후감, 댓글 또는 모임 번호를 가리키므로 단일 물리 외래키를 만들지 않습니다. 신고 등록 API는 대상 유형별 원본 테이블을 고정 매핑하여 대상 존재 여부를 검증하고 조회한 원문을 `TAGT_CNTN`에 변경 불가능한 접수 시점 스냅샷으로 저장하며, 같은 조회에서 확정한 소유 사용자 번호를 `TAGT_USER`에 저장합니다.

### 비공개 이미지 증거 테이블

`TH_CMEVDC`는 프로필·배경사진 신고 시점의 실제 이미지 원본을 관리자에게만 제공하는 임시 증거 테이블입니다. `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH` 고유키로 같은 이미지 버전의 바이트를 한 번만 저장하며, `EVDC_DATA`는 일반 파일 URL로 공개하지 않고 관리자 신고번호 기반 인증 API로만 조회합니다. 대상자 물리 삭제 시 `TAGT_USER`만 `NULL`로 익명화하고, 연결된 신고가 모두 최종 처리된 뒤 180일이 지나면 행 전체를 물리 삭제합니다.

### 자동 조치 결과 테이블

`TH_CMACTN`은 동일 대상의 신고 누적 자동 조치 결과를 조치 순번별로 보존하는 수정 불가능한 이력 테이블입니다.

| 순서 | 컬럼 | 데이터 타입 | NULL | 기본값 | 속성 및 참조 | 설명 |
| ---: | --- | --- | :---: | --- | --- | --- |
| 1 | `ACTN_NUMB` | `bigint` | N | 없음 | PK, `AUTO_INCREMENT` | 자동 조치 결과 번호 |
| 2 | `TAGT_TYPE` | `varchar(20)` | N | 없음 | `CMPL_TAGT` 세부코드 | 신고 대상 유형 |
| 3 | `TAGT_NUMB` | `bigint` | N | 없음 | 대상 유형별 업무 번호, 물리 FK 없음 | 신고 대상 번호 |
| 4 | `TAGT_HASH` | `char(64)` | N | 없음 | 대상 버전 SHA-256 | 신고 대상 버전 해시 |
| 5 | `TAGT_USER` | `bigint` | Y | `NULL` | `TM_USERXM.USER_NUMB` FK, 대상자 삭제 시 `NULL` | 신고 대상 소유 사용자 번호 |
| 6 | `ACTN_TYPE` | `varchar(20)` | N | 없음 | `CMPL_ACTN` 세부코드 | 자동 조치 유형 |
| 7 | `RSLT_CODE` | `varchar(20)` | N | 없음 | `CMPL_RSLT` 세부코드 | 자동 조치 결과 |
| 8 | `THRS_CNTT` | `int` | N | 없음 | 조치 시점 설정값 | 자동 조치 임계 신고 건수 |
| 9 | `CMPL_CNTT` | `int` | N | 없음 | 반려 제외 누적값 | 조치 시점 유효 누적 신고 건수 |
| 10 | `ACTN_ORDR` | `int` | N | 없음 | 같은 대상 버전의 환경별 임계치 조치 순번 | 동일 대상 버전 자동 조치 순번 |
| 11 | `TRIG_CMPL` | `bigint` | Y | `NULL` | `TH_CMPLNT.CMPL_NUMB` FK, 신고 삭제 시 `NULL` | 자동 조치 발생 신고 번호 |
| 12 | `RSLT_CNTN` | `varchar(1000)` | Y | `NULL` | 내부 처리 설명 | 자동 조치 결과 내용 |
| 13 | `REGI_DATE` | `datetime(6)` | N | `CURRENT_TIMESTAMP(6)` | 등록 후 변경하지 않음 | 등록 일시 |

| 구분 | 이름 | 컬럼 또는 참조 | 목적 및 삭제 정책 |
| --- | --- | --- | --- |
| PK | `PRIMARY` | `ACTN_NUMB` | 자동 조치 결과 단건 식별 |
| 고유키 | `UK_TH_CMACTN_TAGT_ORDR` | `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_HASH`, `ACTN_ORDR` | 같은 대상 버전의 동일 자동 조치 순번 중복 차단 |
| 인덱스 | `IX_TH_CMACTN_TAGT_USER` | `TAGT_USER`, `REGI_DATE`, `ACTN_NUMB` | 사용자별 자동 조치 이력 조회 |
| 인덱스 | `IX_TH_CMACTN_TRIG` | `TRIG_CMPL` | 조치를 발생시킨 신고 연결 |
| FK | `FK_TH_CMACTN_TAGT_USER` | `TAGT_USER` → `TM_USERXM.USER_NUMB` | 대상자 물리 삭제 시 `ON DELETE SET NULL`로 익명화 |
| FK | `FK_TH_CMACTN_TRIG` | `TRIG_CMPL` → `TH_CMPLNT.CMPL_NUMB` | 신고 이력 삭제 시 `ON DELETE SET NULL`로 조치 결과 보존 |

## 구현 근거

- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/output/02-admin-insert.sql`
- `src/main/java/org/our/sadari/complaint`
- `src/main/java/org/our/sadari/global/scheduler/service/UserHardDeleteServiceImpl.java`
- `src/main/java/org/our/sadari/global/scheduler/mapper/UserHardDeleteMapper.xml`
- `src/main/resources/application-loc.yml`
- `src/main/resources/application-prod.yml`
- `src/main/frontend/src/pages/UserReport`
- `sadari-admin/src/main/java/org/sadari/admin/sadariadmin/currentuser`
- `sadari-admin/src/main/java/org/sadari/admin/sadariadmin/complaint`
- `sadari-admin/src/main/frontend/src/pages/currentUser/CurrentUserDetailPage.tsx`
- `sadari-admin/src/main/frontend/src/pages/complaint/ComplaintDetailPage.tsx`
- `TH_CMPLNT`
- `TH_CMEVDC`
- `TH_CMACTN`
- `CMPL_TAGT`
- `CMPL_RSON`
- `CMPL_STAT`
- `CMPL_ACTN`
- `CMPL_RSLT`
