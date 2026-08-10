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
 * 현재 URL의 메뉴명과 햄버거에 노출할 사용자 메뉴 목록을 함께 조회합니다.
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
