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
 * fileName       : UserMenuServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
public class UserMenuServiceImpl implements UserMenuService {

    // UserMenu 데이터 접근 객체
    private final UserMenuMapper userMenuMapper;

    /**
     * 현재 URL 메뉴와 노출 메뉴 목록을 조회 전용 트랜잭션에서 함께 반환한다.
     * 현재 URL에 해당하는 데이터가 없는 것은 정상 분기이며 currentMenu를 null로 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저의 현재 pathname
     * @return 현재 메뉴와 햄버거 메뉴 목록
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getUserMenu(String menuUrlx) {
        // menuUrlx 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(menuUrlx)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        UserMenuDto.UserMenuItemDto currentMenu =
                // CurrentUserMenu 데이터를 DB에서 조회한다
                userMenuMapper.getCurrentUserMenu(menuUrlx);
        List<UserMenuDto.UserMenuItemDto> menuList =
                // VisibleUserMenuList 데이터를 DB에서 조회한다
                userMenuMapper.getVisibleUserMenuList();

        // 비정상적으로 null 목록이 반환되어도 프론트가 별도 null 분기 없이 빈 메뉴를 렌더링하도록 보정한다.
        if (StringUtil.isEmpty(menuList)) {
            // 조회 결과가 없을 때 사용할 빈 목록을 생성한다
            menuList = Collections.emptyList();
        }

        // 사용자 메뉴 조회 응답을 담을 객체를 생성한다
        UserMenuDto.UserMenuResDto response = new UserMenuDto.UserMenuResDto();
        // CurrentMenu 업무 값을 response DTO에 설정한다
        response.setCurrentMenu(currentMenu);
        // MenuList 업무 값을 response DTO에 설정한다
        response.setMenuList(menuList);
        // 현재 URL 메뉴와 노출 메뉴 목록을 조회 전용 트랜잭션에서 함께 반환한 결과를 성공 응답으로 반환한다
        return ResultData.success(response);
    }
}
