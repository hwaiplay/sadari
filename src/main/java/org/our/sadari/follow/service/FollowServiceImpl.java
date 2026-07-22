package org.our.sadari.follow.service;

import lombok.RequiredArgsConstructor;
import org.our.sadari.follow.dto.FollowStatusDto;
import org.our.sadari.follow.mapper.FollowMapper;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우 관계 조회, 등록, 삭제 업무를 처리하는 Service 구현체입니다.
 * TB_FOLLOW의 복합 PK와 Oracle 함수 FN_GET_FOLW_STAT을 기준으로 버튼 상태를 계산합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회합니다.
     * 버튼명 결정 기준은 DB 함수에 위임하여 SQL, 화면, 서비스가 서로 다른 판단식을 갖지 않도록 합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @Override
    public ResultData getFollowStatus(Long userNumb, Long flowNumb) {
        ResultData invalidResult = validateFollowUsers(userNumb, flowNumb);

        if (invalidResult != null) {
            return invalidResult;
        }

        return ResultData.success(createFollowStatus(followMapper.getFollowStatusName(userNumb, flowNumb)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 저장합니다.
     * 이미 팔로우 중인 경우에도 MERGE 쿼리를 사용하므로 중복 오류 없이 최신 버튼 상태만 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData setFollow(Long userNumb, Long flowNumb) {
        ResultData invalidResult = validateFollowUsers(userNumb, flowNumb);

        if (invalidResult != null) {
            return invalidResult;
        }

        followMapper.setFollow(userNumb, flowNumb);
        return ResultData.success(createFollowStatus(followMapper.getFollowStatusName(userNumb, flowNumb)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     * 상대가 나를 팔로우하고 있는 역방향 관계는 삭제하지 않아 언팔로우 후 맞팔로우 상태로 돌아갈 수 있게 합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData delFollow(Long userNumb, Long flowNumb) {
        ResultData invalidResult = validateFollowUsers(userNumb, flowNumb);

        if (invalidResult != null) {
            return invalidResult;
        }

        followMapper.delFollow(userNumb, flowNumb);
        return ResultData.success(createFollowStatus(followMapper.getFollowStatusName(userNumb, flowNumb)));
    }

    /**
     * 팔로우 기능에 필요한 사용자 번호를 검증합니다.
     * 로그인 정보가 없으면 인증 실패, 상대가 없거나 자기 자신이면 잘못된 요청으로 응답합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateFollowUsers(Long userNumb, Long flowNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (StringUtil.isEmpty(flowNumb) || userNumb.equals(flowNumb)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        UserDto targetUser = userMapper.getUserByNumb(flowNumb);

        if (StringUtil.isEmpty(targetUser)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return null;
    }

    /**
     * Oracle 함수에서 받은 버튼명을 프론트엔드 응답 DTO로 감쌉니다.
     * ResultData.data의 필드명을 고정해 화면에서 응답 구조를 안정적으로 사용할 수 있게 합니다.
     *
     * @author Seunghyeon.Kang
     * @param followStatName 화면에 표시할 팔로우 버튼명
     * @return 팔로우 상태 DTO
     */
    private FollowStatusDto createFollowStatus(String followStatName) {
        FollowStatusDto followStatusDto = new FollowStatusDto();
        followStatusDto.setFollowStatName(followStatName);
        return followStatusDto;
    }
}
