package org.our.sadari.menu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.menu.dto.UserMenuDto;

/**
 * TM_URMENU에서 현재 경로의 메뉴와 햄버거 노출 메뉴를 조회하는 Mapper입니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface UserMenuMapper {

    /**
     * 현재 URL과 정확히 일치하거나 동적 상세 URL의 접두 경로와 일치하는 메뉴 한 건을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return URL과 가장 구체적으로 일치하는 메뉴, 없으면 null
     */
    UserMenuDto.UserMenuItemDto getCurrentUserMenu(@Param("menuUrlx") String menuUrlx);

    /**
     * 노출 여부와 사용 여부가 모두 Y인 햄버거 메뉴 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @return 정렬 순서에 따른 사용자 메뉴 목록
     */
    List<UserMenuDto.UserMenuItemDto> getVisibleUserMenuList();
}
