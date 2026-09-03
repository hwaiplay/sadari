import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

export type UserMenuItem = {
  menuNumb: number;
  parnNumb?: number | null;
  menuLevl: number;
  menuName?: string;
  menuUrlx?: string;
  sortOrdr?: number;
  childList: UserMenuItem[];
};

export type UserMenuData = {
  currentMenu?: UserMenuItem | null;
  menuList: UserMenuItem[];
};

/**
 * 현재 URL의 메뉴명과 햄버거에 노출할 사용자 메뉴 목록을 함께 조회함
 *
 * @author HanWon.Jang
 * @param menuUrlx 브라우저의 현재 pathname
 * @return 현재 메뉴와 노출 메뉴 목록
 */
export const getUserMenuApi = async (menuUrlx: string) => {

  const response = await api.get<{ data: UserMenuData }>("/user-menu", {
    params: { menuUrlx },
  });
  return assertResultDataSuccess(response.data);
};

/**
 * 기준 화면 아래의 노출 가능한 사용자 메뉴 트리를 조회함
 *
 * @author HanWon.Jang
 * @param menuUrlx 하위 메뉴를 구성할 기준 화면 pathname
 * @return 기준 화면의 직계 하위 메뉴부터 시작하는 메뉴 목록
 */
export const getUserMenuChildListApi = async (menuUrlx: string) => {

  const response = await api.get<{ data: UserMenuItem[] }>("/user-menu/children", {
    params: { menuUrlx },
  });
  return assertResultDataSuccess(response.data);
};
