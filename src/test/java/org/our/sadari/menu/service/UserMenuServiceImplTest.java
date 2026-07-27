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
 * 현재 URL 메뉴와 햄버거 노출 메뉴를 함께 반환하는 사용자 메뉴 정책을 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class UserMenuServiceImplTest {

    @Mock
    private UserMenuMapper userMenuMapper;

    private UserMenuServiceImpl userMenuService;

    /**
     * 각 테스트에서 독립적인 사용자 메뉴 서비스를 구성합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        userMenuService = new UserMenuServiceImpl(userMenuMapper);
    }

    /**
     * 현재 URL 메뉴와 노출 메뉴 목록이 하나의 응답으로 반환되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getUserMenuReturnsCurrentMenuAndVisibleList() {
        UserMenuDto.UserMenuItemDto currentMenu = new UserMenuDto.UserMenuItemDto();
        currentMenu.setMenuName("독후감 상세보기");

        UserMenuDto.UserMenuItemDto visibleMenu = new UserMenuDto.UserMenuItemDto();
        visibleMenu.setMenuName("독후감 달력");

        when(userMenuMapper.getCurrentUserMenu("/book/detail/1")).thenReturn(currentMenu);
        when(userMenuMapper.getVisibleUserMenuList()).thenReturn(List.of(visibleMenu));

        ResultData result = userMenuService.getUserMenu("/book/detail/1");
        UserMenuDto.UserMenuResDto data = (UserMenuDto.UserMenuResDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(currentMenu, data.getCurrentMenu());
        assertEquals(List.of(visibleMenu), data.getMenuList());
        verify(userMenuMapper).getCurrentUserMenu("/book/detail/1");
        verify(userMenuMapper).getVisibleUserMenuList();
    }

    /**
     * URL에 해당하는 메뉴가 없어도 실패시키지 않고 로고 표시를 위한 null 메뉴를 반환하는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getUserMenuReturnsNullCurrentMenuWhenUrlIsNotRegistered() {
        when(userMenuMapper.getCurrentUserMenu(anyString())).thenReturn(null);
        when(userMenuMapper.getVisibleUserMenuList()).thenReturn(List.of());

        ResultData result = userMenuService.getUserMenu("/settings");
        UserMenuDto.UserMenuResDto data = (UserMenuDto.UserMenuResDto) result.getData();

        assertEquals(200, result.getCode());
        assertNull(data.getCurrentMenu());
        assertEquals(List.of(), data.getMenuList());
    }
}
