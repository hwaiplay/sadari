package org.our.sadari.menu.dto;

import java.util.List;
import lombok.Data;

/**
 * 사용자 메뉴명 조회와 햄버거 노출 메뉴 목록 응답에 사용하는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
public class UserMenuDto {

    /**
     * TM_URMENU 한 건의 화면 표시 정보를 전달합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    public static class UserMenuItemDto {

        /** 1뎁스 메뉴 번호입니다. */
        private String menuNumb;

        /** 2뎁스 메뉴 번호입니다. */
        private String subxNumb;

        /** 헤더 또는 햄버거 메뉴에 표시할 메뉴명입니다. */
        private String menuName;

        /** 메뉴 클릭 시 이동할 프론트 URL입니다. */
        private String menuUrlx;

        /** 햄버거 메뉴 노출 정렬 순서입니다. */
        private Integer sortOrdr;
    }

    /**
     * 현재 URL의 메뉴와 햄버거 메뉴 목록을 한 번의 API 응답으로 전달합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    public static class UserMenuResDto {

        /** 현재 URL과 일치하는 메뉴이며 없으면 null입니다. */
        private UserMenuItemDto currentMenu;

        /** 노출 및 사용 여부가 모두 Y인 햄버거 메뉴 목록입니다. */
        private List<UserMenuItemDto> menuList;
    }
}
