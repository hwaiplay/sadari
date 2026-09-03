package org.our.sadari.menu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.menu.dto.UserMenuDto;

/**
 * fileName       : UserMenuMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 데이터베이스 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    사용자 메뉴 계층 조회 추가
 */
@Mapper
public interface UserMenuMapper {

    /**
     * 현재 URL과 정확히 일치하거나 동적 상세 URL의 접두 경로와 일치하는 메뉴 한 건을 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return URL과 가장 구체적으로 일치하는 메뉴, 없으면 null
     */
    UserMenuDto.UserMenuItemDto getCurrentUserMenu(@Param("menuUrlx") String menuUrlx);

    /**
     * 노출 여부와 사용 여부가 모두 Y인 햄버거 메뉴 목록을 조회함
     *
     * @author SeungHyeon.Kang
     * @return 부모가 노출 중인 사용자 메뉴 평면 목록
     */
    List<UserMenuDto.UserMenuItemDto> getVisibleUserMenuList();

    /**
     * 기준 화면의 사용 중 메뉴를 부모로 하는 노출 가능한 하위 메뉴를 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 하위 메뉴를 구성할 기준 화면 pathname
     * @return 트리 구성 기준 화면과 그 아래의 노출 가능한 메뉴 평면 목록
     */
    List<UserMenuDto.UserMenuItemDto> getUserMenuChildList(@Param("menuUrlx") String menuUrlx);
}
