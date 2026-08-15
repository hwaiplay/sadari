import { useQuery, type QueryFunctionContext } from "@tanstack/react-query";
import {
  getUserMenuApi,
  type UserMenuData,
} from "@/features/Menu/api/userMenuApi";

const USER_MENU_QUERY_KEY = "user-menu";

type UserMenuQueryKey = readonly [typeof USER_MENU_QUERY_KEY, string];

/**
 * Query Key에 포함된 현재 경로로 사용자 메뉴 정보를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param context 사용자 메뉴 Query Key를 포함한 조회 문맥
 * @return 현재 메뉴와 사용자 메뉴 목록
 * @throws 사용자 메뉴 API 호출 또는 공통 응답 검증에 실패한 경우
 */
async function fetchUserMenu(
  context: QueryFunctionContext<UserMenuQueryKey>,
): Promise<UserMenuData> {

  const [, menuUrlx] = context.queryKey;

  // TM_URMENU에 등록된 현재 경로의 메뉴명과 사용자 메뉴 트리를 함께 조회한다
  const response = await getUserMenuApi(menuUrlx);

  // 응답 데이터가 비어 있어도 메뉴 미등록 상태로 처리할 수 있는 구조를 반환한다
  return {
    currentMenu: response.data?.currentMenu ?? null,
    menuList: response.data?.menuList ?? [],
  };
}

/**
 * 현재 경로의 사용자 메뉴 정보를 공통 Query 캐시로 조회한다
 *
 * @author SeungHyeon.Kang
 * @param menuUrlx 브라우저의 현재 pathname
 * @param enabled 사용자 메뉴 API 호출 여부
 * @return 현재 메뉴와 사용자 메뉴 목록 조회 Query 객체
 */
export const useUserMenuQuery = (menuUrlx: string, enabled = true) => {

  const queryKey: UserMenuQueryKey = [USER_MENU_QUERY_KEY, menuUrlx];

  // 같은 경로를 조회하는 헤더와 로딩 화면이 서버 상태와 진행 상태를 공유하도록 반환한다
  return useQuery({
    queryKey,
    queryFn: fetchUserMenu,
    enabled,
  });
};
