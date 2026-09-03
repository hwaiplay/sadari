package org.our.sadari.menu.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * description    : 사용자 메뉴 조회와 트리 구성을 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-10        SeungHyeon.Kang    사용자 메뉴 트리 조회 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMenuServiceImpl implements UserMenuService {

    /** 같은 상위 메뉴 안의 메뉴 정렬 기준 */
    private static final Comparator<UserMenuDto.UserMenuItemDto> MENU_COMPARATOR =
            Comparator.comparing(UserMenuDto.UserMenuItemDto::getSortOrdr,
                                  Comparator.nullsLast(Integer::compareTo))
                      .thenComparing(UserMenuDto.UserMenuItemDto::getMenuNumb);

    /** 사용자 메뉴 데이터 접근 객체 */
    private final UserMenuMapper userMenuMapper;

    /**
     * 현재 URL 메뉴와 사용자 햄버거 메뉴 트리를 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 브라우저 현재 경로
     * @return 현재 메뉴와 최대 3단계 메뉴 트리
     */
    @Override
    public ResultData getUserMenu(String menuUrlx) {
        // 빈 경로로 메뉴 일치 조회가 실행되지 않도록 요청값을 검증함
        if (StringUtil.isEmpty(menuUrlx)) {
            // "요청값이 올바르지 않습니다."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 현재 경로와 가장 구체적으로 일치하는 사용 중 메뉴를 조회함
        UserMenuDto.UserMenuItemDto currentMenu = userMenuMapper.getCurrentUserMenu(menuUrlx);
        // 노출 가능한 부모에서 이어지는 사용자 메뉴를 평면 목록으로 조회함
        List<UserMenuDto.UserMenuItemDto> visibleMenuList = userMenuMapper.getVisibleUserMenuList();
        // 평면 목록을 부모 메뉴의 하위 목록에 연결한 트리로 변환함
        List<UserMenuDto.UserMenuItemDto> menuTree = getMenuTree(visibleMenuList);

        // 현재 메뉴와 사용자 메뉴 트리를 담을 응답 객체를 생성함
        UserMenuDto.UserMenuResDto response = new UserMenuDto.UserMenuResDto();
        // 현재 URL과 일치하는 메뉴를 응답에 설정함
        response.setCurrentMenu(currentMenu);
        // 최대 3단계 사용자 메뉴 트리를 응답에 설정함
        response.setMenuList(menuTree);
        // 사용자 메뉴 조회 성공 응답을 반환함
        return ResultData.success(response);
    }

    /**
     * 기준 화면 아래의 노출 가능한 사용자 메뉴 트리를 조회함
     *
     * @author SeungHyeon.Kang
     * @param menuUrlx 하위 메뉴를 구성할 기준 화면 pathname
     * @return 기준 화면의 직계 하위 메뉴부터 시작하는 메뉴 트리
     */
    @Override
    public ResultData getUserMenuChildList(String menuUrlx) {
        // 빈 경로로 전체 메뉴가 조회되지 않도록 기준 화면 경로를 검증함
        if (StringUtil.isEmpty(menuUrlx)) {
            // "요청값이 올바르지 않습니다."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 트리 기준 화면과 그 아래의 노출 가능한 메뉴를 평면 목록으로 조회함
        List<UserMenuDto.UserMenuItemDto> pageMenuList = userMenuMapper.getUserMenuChildList(menuUrlx);
        // 기준 화면 메뉴를 루트로 하는 전체 트리로 변환함
        List<UserMenuDto.UserMenuItemDto> pageMenuTree = getMenuTree(pageMenuList);
        // 조회된 기준 화면이 없으면 설정 화면에 표시할 빈 메뉴 목록을 사용함
        List<UserMenuDto.UserMenuItemDto> childMenuTree = pageMenuTree.isEmpty()
                ? List.of() : pageMenuTree.get(0).getChildList();
        // 화면에서 렌더링할 하위 메뉴 트리 조회 성공 응답을 반환함
        return ResultData.success(childMenuTree);
    }

    /**
     * 부모 번호를 기준으로 평면 메뉴 목록을 트리로 변환함
     *
     * @author SeungHyeon.Kang
     * @param menuList 부모가 노출 중인 사용자 메뉴 평면 목록
     * @return 같은 상위 메뉴 안에서 정렬된 사용자 메뉴 트리
     */
    private List<UserMenuDto.UserMenuItemDto> getMenuTree(
            List<UserMenuDto.UserMenuItemDto> menuList) {
        // 조회 결과가 없으면 변경할 수 없는 빈 메뉴 목록을 반환함
        if (StringUtil.isEmpty(menuList)) {
            // 빈 사용자 메뉴 트리를 반환함
            return List.of();
        }

        // 메뉴 번호로 부모 객체를 찾기 위한 순서 보존 맵을 생성함
        Map<Long, UserMenuDto.UserMenuItemDto> menuMap = new LinkedHashMap<>();
        // 최상위 메뉴를 모을 목록을 생성함
        List<UserMenuDto.UserMenuItemDto> rootMenuList = new ArrayList<>();

        // 모든 메뉴의 하위 목록을 초기화하고 번호별 조회 맵에 저장함
        for (UserMenuDto.UserMenuItemDto menu : menuList) {
            // 이전 매핑 결과가 응답에 섞이지 않도록 빈 하위 목록을 설정함
            menu.setChildList(new ArrayList<>());
            // 메뉴 번호로 부모를 찾을 수 있도록 현재 메뉴를 저장함
            menuMap.put(menu.getMenuNumb(), menu);
        }

        // 각 메뉴를 최상위 목록 또는 부모 메뉴 하위 목록에 연결함
        for (UserMenuDto.UserMenuItemDto menu : menuList) {
            // 상위 메뉴 번호가 없으면 최상위 메뉴 목록에 추가함
            if (menu.getParnNumb() == null) {
                // 최상위 사용자 메뉴를 트리 루트 목록에 추가함
                rootMenuList.add(menu);
                continue;
            }

            // 현재 메뉴가 참조하는 상위 메뉴를 조회함
            UserMenuDto.UserMenuItemDto parentMenu = menuMap.get(menu.getParnNumb());
            // 노출 가능한 상위 메뉴가 있을 때만 현재 메뉴를 트리에 연결함
            if (parentMenu != null) {
                // 현재 메뉴를 상위 메뉴의 하위 목록에 추가함
                parentMenu.getChildList().add(menu);
            }
        }

        // 최상위 메뉴와 모든 하위 메뉴를 정렬함
        sortMenuTree(rootMenuList);
        // 완성된 사용자 메뉴 트리를 반환함
        return rootMenuList;
    }

    /**
     * 같은 상위 메뉴에 속한 메뉴를 정렬하고 하위 메뉴에도 재귀 적용함
     *
     * @author SeungHyeon.Kang
     * @param menuList 같은 상위 메뉴에 속한 메뉴 목록
     */
    private void sortMenuTree(List<UserMenuDto.UserMenuItemDto> menuList) {
        // 정렬 순서와 메뉴 번호를 기준으로 현재 단계 메뉴를 정렬함
        menuList.sort(MENU_COMPARATOR);
        // 모든 메뉴의 하위 메뉴 목록에도 같은 정렬 기준을 적용함
        for (UserMenuDto.UserMenuItemDto menu : menuList) {
            // 하위 메뉴가 있을 때만 다음 단계 정렬을 수행함
            if (!menu.getChildList().isEmpty()) {
                // 하위 메뉴 트리를 같은 기준으로 정렬함
                sortMenuTree(menu.getChildList());
            }
        }
    }
}
