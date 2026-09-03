package org.our.sadari.menu.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : UserMenuService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 업무 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    화면별 하위 사용자 메뉴 조회 계약 추가
 */
public interface UserMenuService {

    /**
     * 현재 URL의 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    ResultData getUserMenu(String menuUrlx);

    /**
     * 기준 화면 아래의 노출 가능한 사용자 메뉴 트리를 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 하위 메뉴를 구성할 기준 화면 pathname
     * @return 기준 화면의 직계 하위 메뉴부터 시작하는 메뉴 트리
     */
    ResultData getUserMenuChildList(String menuUrlx);
}
