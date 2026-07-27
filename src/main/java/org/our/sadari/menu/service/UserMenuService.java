package org.our.sadari.menu.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * 사용자 헤더와 햄버거 메뉴에 필요한 메뉴 정보를 제공합니다.
 *
 * @author Seunghyeon.Kang
 */
public interface UserMenuService {

    /**
     * 현재 URL의 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    ResultData getUserMenu(String menuUrlx);
}
