package org.our.sadari.menu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.menu.dto.UserMenuDto;

/**
 * fileName       : UserMenuMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface UserMenuMapper {

    /**
     * 현재 URL과 정확히 일치하거나 동적 상세 URL의 접두 경로와 일치하는 메뉴 한 건을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return URL과 가장 구체적으로 일치하는 메뉴, 없으면 null
     */
    UserMenuDto.UserMenuItemDto getCurrentUserMenu(@Param("menuUrlx") String menuUrlx);

    /**
     * 노출 여부와 사용 여부가 모두 Y인 햄버거 메뉴 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @return 정렬 순서에 따른 사용자 메뉴 목록
     */
    List<UserMenuDto.UserMenuItemDto> getVisibleUserMenuList();
}
