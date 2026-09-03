package org.our.sadari.menu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.menu.service.UserMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : UserMenuController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    화면별 하위 사용자 메뉴 조회 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-menu")
@Tag(name = "사용자 메뉴", description = "사용자 화면의 헤더, 햄버거 및 화면별 하위 메뉴 조회 API")
public class UserMenuController {

    // UserMenu 업무 처리 서비스
    private final UserMenuService userMenuService;

    /**
     * 현재 URL에 해당하는 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    @GetMapping
    @Operation(summary = "사용자 메뉴 조회", description = "현재 URL 메뉴와 햄버거 노출 메뉴를 함께 조회한다.")
    public ResultData getUserMenu(@RequestParam String menuUrlx) {
        // 현재 URL에 해당하는 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회한 결과를 반환함
        return userMenuService.getUserMenu(menuUrlx);
    }

    /**
     * 기준 화면의 하위 사용자 메뉴 트리를 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 하위 메뉴를 구성할 기준 화면 pathname
     * @return 노출 가능한 직계 하위 메뉴부터 시작하는 최대 3단계 메뉴 트리
     */
    @GetMapping("/children")
    @Operation(summary = "화면 하위 사용자 메뉴 조회", description = "기준 화면 아래의 노출 가능한 사용자 메뉴 트리를 조회한다.")
    public ResultData getUserMenuChildList(@RequestParam String menuUrlx) {
        // 기준 화면의 메뉴 번호를 부모로 사용하는 노출 메뉴 트리 조회 결과를 반환함
        return userMenuService.getUserMenuChildList(menuUrlx);
    }
}
