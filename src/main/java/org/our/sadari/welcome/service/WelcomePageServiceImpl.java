package org.our.sadari.welcome.service;

import static org.our.sadari.global.common.constant.Constant.COMM_YES;
import static org.our.sadari.global.common.constant.Constant.USER_STAT_ACTIVE;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.welcome.mapper.WelcomePageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : WelcomePageServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-28
 * description    : 활성 사용자에게 현재 배포 중인 웰컴페이지를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WelcomePageServiceImpl implements WelcomePageService {

    // 웰컴페이지 데이터 접근 객체
    private final WelcomePageMapper welcomePageMapper;

    /** 활성 사용자에게 현재 배포된 웰컴페이지를 제공한다. */
    @Override
    public ResultData getWelcomePageList(Long userNumb) {
        // 탈퇴 또는 정지 상태 계정에는 웰컴 콘텐츠를 제공하지 않는다.
        if (StringUtil.isEmpty(userNumb)
                || welcomePageMapper.getActiveUserCnt(userNumb, USER_STAT_ACTIVE) != 1) {
            // 접근할 수 없는 계정 상태를 공통 권한 오류로 반환한다.
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 배포된 관리자 페이지를 설정된 순서대로 반환한다.
        return ResultData.success(welcomePageMapper.getWelcomePageList(COMM_YES));
    }
}
