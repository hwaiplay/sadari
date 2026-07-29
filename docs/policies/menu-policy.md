# 메뉴 및 화면 노출 정책

## URL별 헤더

- 사용자가 화면 URL에 접근하면 `TM_URMENU`에서 현재 URL과 일치하는 메뉴를 조회합니다.
- 정확히 일치하는 URL을 우선합니다.
- 메뉴 URL이 `/`로 끝나는 경로형 메뉴는 하위 URL의 접두사로 일치할 수 있습니다.
- 여러 메뉴가 일치하면 더 긴 URL을 우선해 가장 구체적인 메뉴를 선택합니다.
- 일치하는 메뉴가 있으면 헤더에 메뉴명을 표시합니다.
- 일치하는 메뉴가 없으면 기존 서비스 로고를 표시합니다.

## 햄버거 메뉴

- `SHOW_YSNO = 'Y'`이고 `USEE_YSNO = 'Y'`인 메뉴만 목록에 표시합니다.
- 메뉴 정렬은 데이터베이스의 노출 정렬 순서를 따릅니다.
- 메뉴명과 이동 URL은 하드코딩하지 않고 사용자 메뉴 API 응답을 사용합니다.
- 설정, 알림, 로그아웃처럼 별도 고정 기능으로 관리되는 항목은 사용자 메뉴 목록과 독립적으로 표시할 수 있습니다.

## 역할 구분

- URL에 따른 메뉴명과 메뉴 목록은 서버가 데이터베이스 기준으로 제공합니다.
- 실제 화면 이동과 헤더 렌더링은 프론트엔드 라우터와 헤더 컴포넌트가 담당합니다.
- 이 기능은 화면 표시 정책이며, 보안이 필요한 URL의 최종 접근 권한은 Spring Security에서 별도로 검증해야 합니다.
- Swagger와 API 문서는 관리자 역할만 접근할 수 있습니다.

## 구현 근거

- `menu/service/UserMenuServiceImpl.java`
- `menu/mapper/UserMenuMapper.xml`
- `components/Layout/Header/Header.tsx`
- `components/Layout/Header/HeaderMenuDrawer.tsx`
- `features/Menu/api/userMenuApi.ts`
