# 사용자 안내 팝업 콘텐츠 정책

## 개요

- 목적: 사용자 화면의 정책·도움말 목록을 배포 없이 데이터베이스에서 변경할 수 있도록 관리합니다.
- 적용 범위: 계정 처리 정책 안내와 목표 내리기 도움말의 목록 문구입니다.
- 기준일: 2026-07-30
- 현재 상태: 사용자 조회 API와 화면 연동 및 관리자 등록·수정 화면과 쓰기 API가 구현되었습니다.

## 식별 체계

`CT_POPUPX`는 팝업 사용 화면 구분과 팝업 코드를 복합 기본키로 사용합니다.

| `POPU_SITU` | `POPU_CODE` | 적용 화면 |
| --- | --- | --- |
| `ACCOUNT` | `WITHDRAWAL_POLICY` | 회원 탈퇴 화면의 계정 처리 정책 안내 |
| `PROFILE` | `GOAL_DOWN` | 마이페이지의 목표 내리기 도움말 |

`POPU_SITU`는 공통코드 `POPU_SITU`의 사용 가능한 상세코드만 유효한 값으로 취급합니다.

## 콘텐츠 저장

| 컬럼 | 역할 | Null 정책 |
| --- | --- | --- |
| `MNGM_TITL` | 관리자 화면에서 팝업을 식별하는 제목 | 필수 |
| `CONT_FIRS` | 첫 번째 화면 영역의 문자열 JSON 배열 | 필수 |
| `CONT_SECO` | 두 번째 화면 영역의 문자열 JSON 배열 | 선택 |
| `CONT_THIR` | 세 번째 화면 영역의 문자열 JSON 배열 | 선택 |
| `CONT_FOUR` | 네 번째 화면 영역의 문자열 JSON 배열 | 선택 |

- 각 `CONT_*` 컬럼에는 HTML 태그가 아닌 JSON 문자열 배열을 저장합니다.
- MySQL 8.4에서도 기존 문자열 컬럼 계약을 유지하므로 초기 DML과 관리자 저장 기능 모두 검증된 JSON 문자열 배열만 사용합니다.
- 사용자 화면은 배열의 각 문자열을 React `<li>` 요소로 생성하며 원시 HTML을 렌더링하지 않습니다.
- 빈 배열, 문자열이 아닌 항목, 공백 문구, JSON 파싱 오류는 유효하지 않은 콘텐츠로 처리합니다.
- 같은 목록 안의 중복 문구는 사용자 화면에서 한 번만 표시합니다.

## 화면 매핑

### 계정 처리 정책 안내

- `CONT_FIRS`: 비활성화 영역의 전체 목록 문구
- `CONT_SECO`: 영구 탈퇴 영역의 전체 목록 문구
- 팝업 제목, 영역 제목, 배지 및 선택 카드의 한 줄 요약은 현재 화면 문구를 유지합니다.

### 목표 내리기 도움말

- `CONT_FIRS`: 주간·월간·연간 및 같은 값 저장 기준을 포함한 전체 목록 문구
- 팝업 제목과 목록 위의 요약 문구는 현재 메시지 프로퍼티를 유지합니다.

## 조회 및 실패 처리

- 인증된 사용자는 `GET /api/popup-content`에 `popuSitu`와 `popuCode`를 전달해 콘텐츠 한 건을 조회합니다.
- API는 `CT_POPUPX`의 사용자 노출 컬럼만 반환하며 `MNGM_TITL`, 등록자와 수정자 등의 관리 정보는 반환하지 않습니다.
- 콘텐츠 행이 없거나 API 호출이 실패하면 각 화면은 배포 시 포함된 기존 기본 문구를 표시합니다.
- 조회 중에도 기본 문구를 표시하여 팝업이 비거나 화면 구조가 바뀌지 않도록 합니다.
- 동일한 복합 식별값은 React Query 캐시를 재사용합니다.

## 계정 수명주기

- 팝업 콘텐츠는 사용자별 데이터가 아닌 전역 운영 콘텐츠이므로 계정 비활성화와 영구 탈퇴 시에도 보존합니다.
- 비활성화 계정은 같은 계정으로 재로그인하여 `ACTIVE` 상태로 복구된 뒤 최신 콘텐츠를 조회합니다.
- 영구 탈퇴 대기 사용자는 마이페이지에 접근하지 않으므로 목표 내리기 화면에서 조회 요청을 만들지 않습니다.
- 팝업 콘텐츠 API 자체에는 회원 상태별 추가 차단 로직을 두지 않으며 기존 인증 및 화면 접근 정책을 따릅니다.
- 영구 삭제 완료 사용자는 유효한 계정과 인증 토큰이 없으므로 인증된 조회 API를 호출할 수 없습니다.

## 구현 근거

- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/output/02-admin-insert.sql`
- `src/main/java/org/our/sadari/popup/controller/PopupContentController.java`
- `src/main/java/org/our/sadari/popup/service/PopupContentServiceImpl.java`
- `src/main/java/org/our/sadari/popup/mapper/PopupContentMapper.xml`
- `src/main/frontend/src/features/Popup/api/popupContentApi.ts`
- `src/main/frontend/src/features/Popup/hooks/usePopupContent.ts`
- `src/main/frontend/src/features/Popup/utils/popupContentUtil.ts`
- `src/main/frontend/src/pages/Settings/WithdrawalPage.tsx`
- `src/main/frontend/src/pages/My/ProfileEditPage.tsx`
