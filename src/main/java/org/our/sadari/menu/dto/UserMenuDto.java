package org.our.sadari.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;

/**
 * fileName       : UserMenuDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Schema(description = "현재 URL 메뉴와 햄버거 메뉴 응답 DTO")
public class UserMenuDto {

    /**
     * TM_URMENU 한 건의 화면 표시 정보를 전달한다.
     *
     * @author SeungHyeon.Kang
     */
    // 헤더와 햄버거 메뉴에 표시할 단일 메뉴 정보
    @Data
    @Schema(description = "사용자 화면 메뉴 항목 DTO")
    public static class UserMenuItemDto {

    @Schema(description = "1뎁스 메뉴 번호", example = "MENU_0001")
    private String menuNumb;
    @Schema(description = "상위 메뉴 번호", example = "MENU_0000")
    private String subxNumb;
    @Schema(description = "헤더 또는 햄버거 메뉴에 표시할 메뉴명", example = "알림")
    private String menuName;
    @Schema(description = "메뉴를 선택했을 때 이동할 프런트엔드 URL", example = "/alim")
    private String menuUrlx;
    @Schema(description = "햄버거 메뉴 노출 순서", example = "1")
    private Integer sortOrdr;
    }

    /**
     * 현재 URL의 메뉴와 햄버거 메뉴 목록을 한 번의 API 응답으로 전달한다.
     *
     * @author SeungHyeon.Kang
     */
    // 현재 URL 메뉴와 햄버거 메뉴 목록 응답
    @Data
    @Schema(description = "현재 URL 메뉴와 햄버거 메뉴 목록 DTO")
    public static class UserMenuResDto {

    @Schema(description = "현재 URL과 일치하는 메뉴 정보")
    private UserMenuItemDto currentMenu;
    @Schema(description = "노출 여부와 사용 여부가 모두 Y인 햄버거 메뉴 목록")
    private List<UserMenuItemDto> menuList;
    }
}
