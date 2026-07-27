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
 * 사용자 화면 헤더 및 햄버거 메뉴 구성을 위한 메뉴 조회 API를 제공합니다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-menu")
@Tag(name = "사용자 메뉴", description = "사용자 화면의 헤더 및 햄버거 메뉴 조회 API")
public class UserMenuController {

    private final UserMenuService userMenuService;

    /**
     * 현재 URL에 해당하는 메뉴명과 노출 가능한 햄버거 메뉴 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    @GetMapping
    @Operation(summary = "사용자 메뉴 조회", description = "현재 URL 메뉴와 햄버거 노출 메뉴를 함께 조회한다.")
    public ResultData getUserMenu(@RequestParam String menuUrlx) {
        return userMenuService.getUserMenu(menuUrlx);
    }
}
