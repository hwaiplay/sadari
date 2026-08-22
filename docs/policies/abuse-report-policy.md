# 신고 접수 및 처리 정책

## 개요

- 기준일은 2026년 8월 22일입니다.
- 사용자, 독후감, 댓글 및 향후 독서 모임에 대한 공통 신고 데이터 구조에 적용합니다.
- 현재 구현 범위는 공통코드, `TH_CMPLNT` 테이블, 사용자 신고 접수 화면과 API 및 관리자 신고 조회·처리 화면입니다.
- 모든 신고 유형은 `TAGT_USER`로 연결된 피신고자에게 관리자 신고 상세에서 기존 회원 이용정지 기능을 동일하게 사용할 수 있습니다.
- 콘텐츠와 프로필 제재는 신고 접수만으로 자동 실행하지 않으며 관리자가 신고 상세에서 명시적으로 실행합니다.

## 신고 대상

신고 대상은 `CMPL_TAGT` 공통코드로 구분합니다.

| 코드 | 대상 | 대상 번호 |
| --- | --- | --- |
| `CMPL_USER` | 사용자 프로필과 계정 | `TM_USERXM.USER_NUMB` |
| `CMPL_BOOK_REPORT` | 공개 독후감 | `TM_REPORT.REPT_NUMB` |
| `CMPL_REPLY` | 독후감 댓글과 답글 | `TB_REPLXX.REPL_NUMB` |
| `CMPL_CLUB` | 향후 독서 모임 | `TM_CLUBXM.CLUB_NUMB` |

- `TH_CMPLNT`에는 `TAGT_TYPE`과 `TAGT_NUMB`를 함께 저장합니다.
- 여러 대상 테이블을 하나의 외래키로 참조할 수 없으므로 `TAGT_NUMB`에는 물리 외래키를 생성하지 않습니다.
- 신고 등록 서버는 `TAGT_TYPE`별 허용 테이블과 식별 컬럼을 고정 매핑하고 대상의 존재 여부와 신고 가능 상태를 검증합니다.
- 서버는 클라이언트가 화면 이동 상태로 보유한 본문을 저장하지 않고 대상 번호로 원본을 다시 조회하여 `TAGT_CNTN`에 접수 시점 스냅샷을 저장합니다.
- 서버는 같은 원본 조회 결과에서 대상 소유 사용자 번호를 확정해 `TAGT_USER`에 함께 저장하며, 클라이언트가 전달한 소유자 번호는 사용하지 않습니다.
- `CMPL_USER`는 신고 시점의 닉네임과 한줄소개, `CMPL_BOOK_REPORT`는 독후감 본문, `CMPL_REPLY`는 댓글 또는 답글 본문을 스냅샷으로 저장합니다.
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
- `CMPL_CNTN`은 최대 1,000자로 저장합니다.

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

## 관리자 신고 관리

- 신고 관리 메뉴는 관리자 사용자 관리의 하위 메뉴로 제공합니다.
- 신고 목록은 신고번호, 처리 상태, 대상 유형과 번호, 신고 사유, 신고자 및 접수일 범위로 검색합니다.
- 신고 상세는 별도 사용자 상세 화면으로 이동하지 않고 `TAGT_TYPE`과 `TAGT_NUMB`를 기준으로 처리합니다.
- 신고 상세는 별도의 신고 대상 영역을 만들지 않고 신고 정보 표 안에 `TAGT_TYPE`, `TAGT_NUMB`, `TAGT_USER`, 접수 시 저장된 `TAGT_CNTN` 원문 스냅샷과 피신고자의 현재 정보를 함께 표시합니다.
- `TAGT_USER`로 연결된 피신고자의 프로필·배경 이미지는 `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 상태에서 원본 파일이 남아 있을 때 관리자 이미지 보기 기능으로 제공하며 신고 이력에 이미지 사본을 저장하지 않습니다.
- `현 사용자 관리 > 현 사용자 상세`는 `TAGT_USER`로 연결된 사용자 본인·독후감·댓글이 받은 신고의 누적 횟수와 전체 이력을 함께 제공하며, 각 이력에는 대상 유형·대상 내용 스냅샷·신고자·사유·상세 내용·처리 상태·접수일·처리일을 표시합니다.
- 동일한 `TAGT_TYPE`과 `TAGT_NUMB`의 최근 다른 신고 10건과 전체 건수를 관리자 판단 정보로 제공합니다.
- 모든 신고 유형에서 `TAGT_USER`를 회원번호로 사용하여 기존 사용자 관리와 같은 이용정지 등록·해제·이력을 제공합니다. `TAGT_NUMB`는 콘텐츠 신고에서 회원번호로 해석하지 않습니다.
- 이용정지 API는 클라이언트에서 회원번호를 받지 않고 신고번호로 저장된 `TAGT_USER`를 서버에서 확정합니다.
- 관리자 삭제 권한이 있으면 `신고 관리 > 신고 상세`와 `현 사용자 관리 > 현 사용자 상세`에서 피신고자의 프로필 사진과 배경사진 참조를 제거해 기본 이미지 상태로 변경하고 자기소개를 `NULL` 처리할 수 있습니다. 참조되지 않는 이미지 메타정보와 내부 저장소 파일은 DB 커밋 뒤 삭제합니다.
- 관리자 삭제 권한이 있으면 `CMPL_BOOK_REPORT`의 독후감과 연결 댓글·답글·좋아요를 완전 삭제하고, `CMPL_REPLY`는 원문 행을 보존하면서 `DELT_YSNO='Y'`로 변경하며, `CMPL_CLUB`은 `CLUB_CNTN`을 `NULL` 처리합니다.
- 신고 정보 표는 접수 당시 `TAGT_CNTN`과 현재 원본 존재 여부를 구분해 표시하며, 원본을 조치한 뒤에도 스냅샷은 변경하거나 삭제하지 않습니다.
- `TH_CMPLNT` 신고 이력 자체는 관리자 조치로 삭제하지 않습니다. 메뉴 삭제 권한은 피신고자 정보와 신고 대상 원본 조치 API에만 사용합니다.
- 관리자 조치 알림은 알림 템플릿이 확정된 뒤 별도 연동하며 현재는 알림과 푸시를 생성하지 않습니다.

## 중복과 접근 범위

- 동일 사용자는 처리 상태와 관계없이 같은 `TAGT_TYPE`과 `TAGT_NUMB`의 대상을 한 번만 신고할 수 있습니다.
- 신고 API는 기존 신고를 먼저 조회해 재신고를 안내하고, `UK_TH_CMPLNT_USER_TAGT` 고유 제약으로 동시에 도착한 중복 요청도 차단합니다.
- 중복 신고 요청에는 `COMPLAINT_DUPLICATED` 응답을 반환하며 사용자 화면은 `이미 신고한 대상이에요.` 알림과 함께 동일 대상 재신고가 불가능함을 안내합니다.
- 일반 사용자는 본인이 등록한 신고를 포함한 신고 심사 내역을 조회하지 않습니다.
- 신고 데이터와 관리자 처리 내용은 관리자만 조회할 수 있습니다.
- `ACTIVE` 계정만 신고할 수 있으며 자기 자신과 본인 소유 콘텐츠의 신고는 서버에서 거절합니다.

## 계정 수명주기와 데이터 보존

- `WITHDRAWN` 상태에서는 기존 신고를 유지하고 관리자만 조회할 수 있으며 신규 신고를 허용하지 않습니다. 같은 계정이 `ACTIVE`로 복귀하면 보존된 신고 이력을 기준으로 동일 대상 재신고를 계속 차단합니다.
- `DELETE_PENDING` 유예기간에는 기존 신고를 유지하고 영구 탈퇴 취소 시에도 별도 복원 없이 같은 신고 이력과 중복 신고 제한을 유지합니다.
- 신고자의 계정이 물리 삭제되면 `TH_CMPLNT.USER_NUMB`를 `NULL`로 변경하여 신고자를 익명화하고 신고 기록은 운영 이력으로 유지합니다. 이후 생성된 새 계정은 익명화된 과거 신고와 연결하지 않으므로 신고 가능한 대상이면 새 계정 기준으로 신고할 수 있습니다.
- 신고 대상 사용자 또는 콘텐츠가 삭제되어도 `TAGT_TYPE`과 `TAGT_NUMB`는 유지합니다.
- 신고 대상자의 `WITHDRAWN` 또는 `DELETE_PENDING` 상태에서도 `TAGT_USER` 연결을 유지하여 현 사용자 상세의 누적 횟수와 전체 이력을 계속 제공합니다. 계정이 복귀하거나 영구 탈퇴를 취소해도 같은 이력을 별도 복원 없이 그대로 제공합니다.
- `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING` 피신고자에게 동일한 관리자 프로필·콘텐츠 조치를 허용합니다. 비활성화 복귀 또는 영구 탈퇴 취소 뒤에도 삭제한 이미지, 자기소개, 독후감, 댓글 표시 상태와 모임 소개를 자동 복원하지 않습니다.
- 신고 대상자 계정이 물리 삭제되면 `TAGT_USER`를 `NULL`로 변경합니다. 이때 현재 사용자 상세는 존재하지 않으며, 대상 내용 스냅샷과 신고 이력은 신고 관리에서만 익명 운영 이력으로 유지합니다.
- 신고 대상자 계정이 물리 삭제되면 피신고자의 현재 정보와 프로필·배경 이미지를 신고 상세에 표시하지 않으며, `TAGT_CNTN` 스냅샷만 운영 판단 근거로 유지합니다.
- 신고 접수 시 `TAGT_CNTN`에 저장한 대상 원문 또는 프로필 스냅샷은 관리자 신고 판단 근거로만 제공하며 일반 사용자에게 공개하지 않습니다.
- `TAGT_CNTN`은 신고 대상 원본의 수정·삭제, 신고자 또는 대상자의 `WITHDRAWN`, `DELETE_PENDING` 및 물리 삭제 뒤에도 신고 이력과 함께 유지합니다.
- 계정 비활성화 복귀 또는 영구 탈퇴 취소 시 `TAGT_CNTN`을 현재 원본으로 재동기화하거나 복원하지 않고 접수 시점 값으로 유지합니다.
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
| 5 | `TAGT_USER` | `bigint` | Y | `NULL` | `TM_USERXM.USER_NUMB` FK, 대상자 삭제 시 `NULL` | 신고 대상 소유 사용자 번호 |
| 6 | `TAGT_CNTN` | `text` | Y | `NULL` | 신규 신고는 서버가 원본 조회 후 저장, 기존 신고는 `NULL` 가능 | 신고 대상 내용 스냅샷 |
| 7 | `CMPL_RSON` | `varchar(20)` | N | 없음 | `CMPL_RSON`의 전역 유일 세부코드 | 신고 사유 |
| 8 | `CMPL_CNTN` | `varchar(1000)` | Y | `NULL` | `CMPL_OTHER` 사유 선택 시 API 필수 검증 | 신고 상세 내용 |
| 9 | `CMPL_STAT` | `varchar(20)` | N | `CMPL_RECEIVED` | `CMPL_STAT`의 전역 유일 세부코드 | 신고 처리 상태 |
| 10 | `PROC_CNTN` | `varchar(1000)` | Y | `NULL` | 관리자 처리 시 입력 | 관리자 처리 내용 |
| 11 | `PROC_ADMN` | `bigint` | Y | `NULL` | `TM_ADMINX.ADMN_NUMB` FK | 처리 관리자 번호 |
| 12 | `PROC_DATE` | `datetime(6)` | Y | `NULL` | 관리자 처리 시 입력 | 처리 일시 |
| 13 | `REGI_DATE` | `datetime(6)` | N | `CURRENT_TIMESTAMP(6)` | 등록 후 변경하지 않음 | 등록 일시 |
| 14 | `UPDT_DATE` | `datetime(6)` | N | `CURRENT_TIMESTAMP(6)` | 행 수정 시 자동 갱신 | 수정 일시 |

### 인덱스와 제약조건

| 구분 | 이름 | 컬럼 또는 참조 | 목적 및 삭제 정책 |
| --- | --- | --- | --- |
| PK | `PRIMARY` | `CMPL_NUMB` | 신고 단건 식별 |
| 고유키 | `UK_TH_CMPLNT_USER_TAGT` | `USER_NUMB`, `TAGT_TYPE`, `TAGT_NUMB` | 동일 사용자의 동일 대상 재신고와 동시 중복 접수 차단, 익명화된 `NULL` 신고자는 새 계정과 분리 |
| 인덱스 | `IX_TH_CMPLNT_TAGT` | `TAGT_TYPE`, `TAGT_NUMB`, `CMPL_STAT`, `REGI_DATE` | 대상별 신고와 처리 상태 조회 |
| 인덱스 | `IX_TH_CMPLNT_TAGT_USER` | `TAGT_USER`, `REGI_DATE`, `CMPL_NUMB` | 현 사용자 상세의 받은 신고 이력 최신순 조회 |
| 인덱스 | `IX_TH_CMPLNT_USER` | `USER_NUMB`, `REGI_DATE` | 신고자별 접수 이력 조회 |
| 인덱스 | `IX_TH_CMPLNT_STAT` | `CMPL_STAT`, `REGI_DATE`, `CMPL_NUMB` | 관리자 처리 대기 목록 조회 |
| FK | `FK_TH_CMPLNT_USER` | `USER_NUMB` → `TM_USERXM.USER_NUMB` | 회원 물리 삭제 시 `ON DELETE SET NULL`로 신고자 익명화 |
| FK | `FK_TH_CMPLNT_TAGT_USER` | `TAGT_USER` → `TM_USERXM.USER_NUMB` | 신고 대상자 물리 삭제 시 `ON DELETE SET NULL`로 대상자 연결 제거 |
| FK | `FK_TH_CMPLNT_PROC` | `PROC_ADMN` → `TM_ADMINX.ADMN_NUMB` | 실제 관리자 계정만 처리자로 저장 |

`TB_CODEXD.COMD_CODE`는 관리자 등록 단계에서 전체 공통코드 그룹을 대상으로 중복을 검사하므로 `TH_CMPLNT`에는 공통코드 그룹을 저장하지 않고 세부코드만 저장합니다. `TAGT_TYPE`, `CMPL_RSON`, `CMPL_STAT`의 활성 코드 여부는 신고 등록과 처리 API에서 각각 검증해야 합니다.

`TAGT_NUMB`는 `TAGT_TYPE`에 따라 사용자, 독후감, 댓글 또는 모임 번호를 가리키므로 단일 물리 외래키를 만들지 않습니다. 신고 등록 API는 대상 유형별 원본 테이블을 고정 매핑하여 대상 존재 여부를 검증하고 조회한 원문을 `TAGT_CNTN`에 변경 불가능한 접수 시점 스냅샷으로 저장하며, 같은 조회에서 확정한 소유 사용자 번호를 `TAGT_USER`에 저장합니다.

## 구현 근거

- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/output/02-admin-insert.sql`
- `src/main/java/org/our/sadari/complaint`
- `src/main/frontend/src/pages/UserReport`
- `sadari-admin/src/main/java/org/sadari/admin/sadariadmin/currentuser`
- `sadari-admin/src/main/frontend/src/pages/currentUser/CurrentUserDetailPage.tsx`
- `TH_CMPLNT`
- `CMPL_TAGT`
- `CMPL_RSON`
- `CMPL_STAT`
