package org.our.sadari.menu.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : UserMenuService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
public interface UserMenuService {

    /**
     * 현재 URL의 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    ResultData getUserMenu(String menuUrlx);
}
