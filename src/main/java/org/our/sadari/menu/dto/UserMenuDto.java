package org.our.sadari.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * fileName       : UserMenuDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 조회 응답 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    3단계 사용자 메뉴 트리 구조 적용
 */
@Schema(description = "현재 URL 메뉴와 햄버거 메뉴 응답 DTO")
public class UserMenuDto {

    /**
     * 사용자 화면에 표시할 단일 메뉴와 하위 메뉴 목록을 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @Schema(description = "사용자 화면 메뉴 항목 DTO")
    public static class UserMenuItemDto {

        /** 메뉴 번호 */
        @Schema(description = "메뉴 번호", example = "12")
        private Long menuNumb;

        /** 상위 메뉴 번호 */
        @Schema(description = "상위 메뉴 번호", example = "1")
        private Long parnNumb;

        /** 메뉴 단계 */
        @Schema(description = "메뉴 단계", example = "2")
        private Integer menuLevl;

        /** 메뉴 이름 */
        @Schema(description = "메뉴 이름", example = "내 모임")
        private String menuName;

        /** 메뉴 이동 URL */
        @Schema(description = "메뉴 이동 URL", example = "/reading-clubs/mine")
        private String menuUrlx;

        /** 정렬 순서 */
        @Schema(description = "같은 상위 메뉴 안의 정렬 순서", example = "1")
        private Integer sortOrdr;

        /** 하위 메뉴 목록 */
        @Schema(description = "정렬된 하위 메뉴 목록")
        private List<UserMenuItemDto> childList = new ArrayList<>();
    }

    /**
     * 현재 URL 메뉴와 사용자 메뉴 트리를 하나의 응답으로 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @Schema(description = "현재 URL 메뉴와 햄버거 메뉴 트리 DTO")
    public static class UserMenuResDto {

        /** 현재 URL 메뉴 */
        @Schema(description = "현재 URL과 가장 구체적으로 일치하는 메뉴")
        private UserMenuItemDto currentMenu;

        /** 사용자 메뉴 트리 */
        @Schema(description = "노출 여부와 사용 여부가 모두 Y인 최대 3단계 메뉴 트리")
        private List<UserMenuItemDto> menuList;
    }
}
