package org.our.sadari.menu.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.menu.dto.UserMenuDto;
import org.our.sadari.menu.mapper.UserMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 메뉴 조회 결과를 헤더와 햄버거 메뉴에서 함께 사용할 응답으로 구성합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class UserMenuServiceImpl implements UserMenuService {

    private final UserMenuMapper userMenuMapper;

    /**
     * 현재 URL 메뉴와 노출 메뉴 목록을 조회 전용 트랜잭션에서 함께 반환합니다.
     * 현재 URL에 해당하는 데이터가 없는 것은 정상 분기이며 currentMenu를 null로 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getUserMenu(String menuUrlx) {
        if (StringUtil.isEmpty(menuUrlx)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        UserMenuDto.UserMenuItemDto currentMenu =
                userMenuMapper.getCurrentUserMenu(menuUrlx);
        List<UserMenuDto.UserMenuItemDto> menuList =
                userMenuMapper.getVisibleUserMenuList();

        // 비정상적으로 null 목록이 반환되어도 프론트가 별도 null 분기 없이 빈 메뉴를 렌더링하도록 보정한다.
        if (menuList == null) {
            menuList = Collections.emptyList();
        }

        UserMenuDto.UserMenuResDto response = new UserMenuDto.UserMenuResDto();
        response.setCurrentMenu(currentMenu);
        response.setMenuList(menuList);
        return ResultData.success(response);
    }
}
