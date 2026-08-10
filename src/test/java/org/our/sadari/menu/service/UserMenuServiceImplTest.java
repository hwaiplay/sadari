package org.our.sadari.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.menu.dto.UserMenuDto;
import org.our.sadari.menu.mapper.UserMenuMapper;

/**
 * fileName       : UserMenuServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    3단계 메뉴 트리 구성 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class UserMenuServiceImplTest {

    // UserMenu 데이터 접근 객체
    @Mock
    private UserMenuMapper userMenuMapper;

    // 사용자 메뉴 서비스 단위 테스트 대상
    private UserMenuServiceImpl userMenuService;

    /**
     * 각 테스트에서 독립적인 사용자 메뉴 서비스를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 사용자 메뉴 서비스 단위 테스트 대상을 담을 객체를 생성한다
        userMenuService = new UserMenuServiceImpl(userMenuMapper);
    }

    /**
     * 현재 URL 메뉴와 노출 메뉴 목록이 하나의 응답으로 반환되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getUserMenuReturnsCurrentMenuAndVisibleList() {
        // 사용자 메뉴 목록 항목을 담을 객체를 생성한다
        UserMenuDto.UserMenuItemDto currentMenu = new UserMenuDto.UserMenuItemDto();
        // MenuName 업무 값을 currentMenu DTO에 설정한다
        currentMenu.setMenuName("독후감 상세보기");

        // 최상위 사용자 메뉴 목록 항목을 담을 객체를 생성한다
        UserMenuDto.UserMenuItemDto visibleMenu = getMenu(1L, null, 1, "독후감 달력", 1);
        // 2단계 사용자 메뉴 목록 항목을 담을 객체를 생성한다
        UserMenuDto.UserMenuItemDto secondMenu = getMenu(2L, 1L, 2, "독서 모임", 1);
        // 3단계 사용자 메뉴 목록 항목을 담을 객체를 생성한다
        UserMenuDto.UserMenuItemDto thirdMenu = getMenu(3L, 2L, 3, "내 모임", 1);

        // CurrentUserMenu 데이터를 DB에서 조회한다
        when(userMenuMapper.getCurrentUserMenu("/report/detail/1")).thenReturn(currentMenu);
        // VisibleUserMenuList 데이터를 DB에서 조회한다
        when(userMenuMapper.getVisibleUserMenuList()).thenReturn(List.of(thirdMenu, visibleMenu, secondMenu));

        // getUserMenu 업무 로직을 userMenuService에 위임한다
        ResultData result = userMenuService.getUserMenu("/report/detail/1");
        // 공통 응답에 포함된 업무 데이터를 조회한다
        UserMenuDto.UserMenuResDto data = (UserMenuDto.UserMenuResDto) result.getData();

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // getCurrentMenu 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(currentMenu, data.getCurrentMenu());
        // 필요한 값으로 불변 객체를 생성한다
        assertEquals(List.of(visibleMenu), data.getMenuList());
        // 2단계 메뉴가 최상위 메뉴의 하위 목록에 연결됐는지 검증한다
        assertEquals(List.of(secondMenu), visibleMenu.getChildList());
        // 3단계 메뉴가 2단계 메뉴의 하위 목록에 연결됐는지 검증한다
        assertEquals(List.of(thirdMenu), secondMenu.getChildList());
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(userMenuMapper).getCurrentUserMenu("/report/detail/1");
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(userMenuMapper).getVisibleUserMenuList();
    }

    /**
     * URL에 해당하는 메뉴가 없어도 실패시키지 않고 로고 표시를 위한 null 메뉴를 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getUserMenuReturnsNullCurrentMenuWhenUrlIsNotRegistered() {
        // CurrentUserMenu 데이터를 DB에서 조회한다
        when(userMenuMapper.getCurrentUserMenu(anyString())).thenReturn(null);
        // VisibleUserMenuList 데이터를 DB에서 조회한다
        when(userMenuMapper.getVisibleUserMenuList()).thenReturn(List.of());

        // getUserMenu 업무 로직을 userMenuService에 위임한다
        ResultData result = userMenuService.getUserMenu("/settings");
        // 공통 응답에 포함된 업무 데이터를 조회한다
        UserMenuDto.UserMenuResDto data = (UserMenuDto.UserMenuResDto) result.getData();

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // getCurrentMenu 조회로 후속 처리에 필요한 데이터를 가져온다
        assertNull(data.getCurrentMenu());
        // 필요한 값으로 불변 객체를 생성한다
        assertEquals(List.of(), data.getMenuList());
    }

    /**
     * 사용자 메뉴 테스트 항목을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param menuNumb 메뉴 번호
     * @param parnNumb 상위 메뉴 번호
     * @param menuLevl 메뉴 단계
     * @param menuName 메뉴 이름
     * @param sortOrdr 정렬 순서
     * @return 사용자 메뉴 테스트 항목
     */
    private UserMenuDto.UserMenuItemDto getMenu(Long menuNumb, Long parnNumb, Integer menuLevl,
                                                 String menuName, Integer sortOrdr) {
        // 사용자 메뉴 테스트 항목을 생성한다
        UserMenuDto.UserMenuItemDto menu = new UserMenuDto.UserMenuItemDto();
        // 테스트 메뉴 번호를 설정한다
        menu.setMenuNumb(menuNumb);
        // 테스트 상위 메뉴 번호를 설정한다
        menu.setParnNumb(parnNumb);
        // 테스트 메뉴 단계를 설정한다
        menu.setMenuLevl(menuLevl);
        // 테스트 메뉴 이름을 설정한다
        menu.setMenuName(menuName);
        // 테스트 정렬 순서를 설정한다
        menu.setSortOrdr(sortOrdr);
        // 생성한 사용자 메뉴 테스트 항목을 반환한다
        return menu;
    }
}
