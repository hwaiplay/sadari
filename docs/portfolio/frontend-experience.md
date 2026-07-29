# 프론트엔드 경험

## 문서 목적

- 목적: Sadari 프론트엔드의 상태 관리, 오류 처리와 사용자 경험 설계를 설명
- 적용 범위: API 계층, 인증 복구, 서버 상태 캐시, 주요 상호작용, PWA
- 기준일: 2026-07-30

## API 성공 기준의 통일

HTTP 상태가 200이어도 업무 응답의 `ResultData.code`가 200이 아닐 수 있다. 저장·수정뿐 아니라 모든 API에서 업무 코드를 검사하지 않으면 실패 응답을 성공으로 표시하는 문제가 발생한다.

`assertResultDataSuccess`는 공통 성공 코드가 아니면 서버 메시지를 담은 `ResultDataError`를 발생시킨다. 각 도메인 API는 Axios 응답을 반환하기 전에 이 함수를 통과시킨다.

구현 근거:

- `src/main/frontend/src/app/api/resultData.ts`
- `src/main/frontend/src/features/Book/api/bookApi.ts`
- `src/main/frontend/src/features/Social/api/socialApi.ts`
- `src/main/frontend/src/features/Alim/api/alimApi.ts`

## 토큰 재발급 루프 방지

`src/main/frontend/src/app/api/axios.ts`는 Access Token 만료 응답을 받으면 Refresh Token으로 재발급한 뒤 원래 요청을 재시도한다.

안정성을 위해 다음 제어를 적용한다.

- 동시에 발생한 실패 요청이 하나의 `refreshRequest` Promise를 공유
- 원 요청에 재시도 여부 기록
- Refresh와 Logout API는 인터셉터 재발급 대상에서 제외
- 재발급 실패 시 인증 상태를 정리하고 로그인 경로로 이동

`useCheckAuth`도 한 화면 생명주기에서 Refresh 시도를 한 번으로 제한해 인증 확인과 로그인 화면이 반복되는 현상을 차단한다.

구현 근거:

- `src/main/frontend/src/app/api/axios.ts`
- `src/main/frontend/src/features/Auth/hooks/useCheckAuth.tsx`

## 서버 상태와 화면 상태의 분리

- TanStack Query: 사용자, 도서, 독후감, 공통코드 등 서버 상태 조회·캐시
- Zustand: 화면 간 공유가 필요한 클라이언트 상태
- React local state: 모달 단계, 입력값, 임시 편집 상태

공통코드는 정규화한 Query Key와 10분 `staleTime`을 사용한다. 조회 데이터와 편집 중인 화면 상태를 분리해 API 캐시 갱신과 사용자 입력이 서로 영향을 주지 않게 한다.

## 독후감 상세와 편집의 통합

독후감 상세 화면에서 상태, 공개 여부, 평점, 독서기간과 기록을 직접 수정할 수 있다.

- 4열 요약 영역을 클릭하면 단계형 편집 팝업 표시
- 이전·다음·확인으로 단계 이동
- 평점 0.5 단위 입력과 드래그
- 독서기간 Range Calendar
- 기록 클릭 시 TextArea 전환과 바이트 표시
- 실제 편집을 시작한 경우에만 삭제·취소·저장 노출
- 책 영역을 유지한 채 기록과 도서 정보 영역을 페이드 전환

등록 화면도 같은 정보 구조와 도서 정보 전환을 사용해 상세·등록·수정의 시각적 모델을 통일했다.

대표 구현:

- `src/main/frontend/src/pages/Book/Detail/DetailPage.tsx`
- `src/main/frontend/src/pages/Book/Set/SetReportPage.tsx`
- `src/main/frontend/src/features/Book/Update/useUpdateMutation.tsx`

## 마이페이지 체감 성능

프로필 정보와 무거운 독서 요약을 독립적으로 조회한다. 프로필을 먼저 표시하고 독서 활동 데이터는 준비된 뒤 페이드 인한다. 이후 SQL 통합으로 요약 자체도 최대 19회에서 2회 조회로 줄었다.

구현 근거:

- `src/main/frontend/src/pages/My/ProfileEditPage.tsx`
- `docs/performance/my-page-reading-summary-optimization.md`

## 데이터 기반 메뉴

현재 URL에 해당하는 사용자 메뉴를 조회해 Header 제목을 결정한다. 메뉴가 없으면 로고를 표시하고, `SHOW_YSNO`와 `USEE_YSNO`가 모두 활성인 메뉴만 햄버거 메뉴에 노출한다.

이 구조는 메뉴명을 화면 소스에 중복 작성하지 않고 DB 설정과 사용자 화면을 연결한다.

구현 근거:

- `src/main/java/org/our/sadari/menu/service/UserMenuServiceImpl.java`
- `src/main/frontend/src/features/Menu/api/userMenuApi.ts`
- `src/main/frontend/src/components/Layout/Header/Header.tsx`
- `src/main/frontend/src/components/Layout/Header/HeaderMenuDrawer.tsx`

## 이미지 실패 복구

프로필 이미지 URL이 없거나 로드에 실패해도 깨진 이미지 아이콘을 노출하지 않도록 `ProfileImage`가 기본 프로필 이미지로 대체한다. Navigation, Header 메뉴, 마이페이지와 소셜 프로필에서 같은 컴포넌트를 사용한다.

구현 근거:

- `src/main/frontend/src/features/User/components/ProfileImage.tsx`

## 메시지와 다국어 구조

화면 문구는 메시지 키로 조회하며 현재 Locale에 키가 없으면 한국어 메시지로 폴백하고, 그마저 없으면 키 자체를 반환한다.

구현 근거:

- `src/main/frontend/src/app/messages/message.ts`
- `src/main/frontend/src/app/messages/messages.properties`
- `src/main/frontend/src/app/messages/messages_en.properties`

## PWA

프로덕션과 localhost 환경에서 Service Worker를 등록하고, 웹 푸시 권한·FCM 토큰·알림 클릭 이동을 처리한다. 사용자의 직접 클릭 없이 브라우저 권한 창을 강제로 띄우지 않는 정책을 적용한다.

구현 근거:

- `src/main/frontend/src/app/pwa/registerServiceWorker.ts`
- `src/main/frontend/src/app/pwa/firebaseMessaging.ts`
- `src/main/resources/static/service-worker.js`

## 향후 개선

- 컴포넌트 단위 시각 회귀 테스트와 접근성 자동 검사를 CI에 추가한다.
- 단계형 독후감 편집 팝업의 키보드 탐색과 Screen Reader 동작을 검증한다.
- 전역 API 오류 표시 정책을 Toast, Modal, 인라인 오류로 세분화할 수 있다.
