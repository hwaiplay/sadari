package org.our.sadari.social.service;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.report.mapper.ReportMapper;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.mapper.SocialMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우 관계와 좋아요처럼 사용자 간 반응 데이터를 처리하는 Service 구현체입니다.
 * Controller에서 만든 SocialDto를 그대로 Mapper까지 전달해 파라미터 이름과 SQL 컬럼 의미가 어긋나지 않게 합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final SocialMapper socialMapper;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회합니다.
     * 버튼명 판단은 Oracle 함수 FN_GET_FOLW_STAT에 위임하여 화면, 서비스, SQL이 서로 다른 기준을 갖지 않도록 합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @Override
    public ResultData getFollowStatus(SocialDto.FollowDto req) {
        ResultData invalidResult = validateFollowUsers(req);

        if (invalidResult != null) {
            return invalidResult;
        }

        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 저장합니다.
     * 이미 팔로우 중인 경우에도 MERGE 쿼리를 사용하므로 중복 오류 없이 최신 버튼 상태만 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData setFollow(SocialDto.FollowDto req) {
        ResultData invalidResult = validateFollowUsers(req);

        if (invalidResult != null) {
            return invalidResult;
        }

        socialMapper.setFollow(req);
        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     * 상대가 나를 팔로우하고 있는 반대 방향 관계는 삭제하지 않아 언팔로우 후에도 맞팔로우 유도 상태를 계산할 수 있습니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData delFollow(SocialDto.FollowDto req) {
        ResultData invalidResult = validateFollowUsers(req);

        if (invalidResult != null) {
            return invalidResult;
        }

        socialMapper.delFollow(req);
        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소합니다.
     * 현재 화면에서 지원하는 대상은 REPORT뿐이므로, 다른 TAGT_TYPE은 저장하지 않고 잘못된 요청으로 응답합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 변경 후 좋아요 상세 정보
     */
    @Override
    @Transactional
    public ResultData setLike(SocialDto.LikeDto req) {
        ResultData invalidResult = validateLikeTarget(req);

        if (invalidResult != null) {
            return invalidResult;
        }

        if (socialMapper.dupLike(req) > 0) {
            socialMapper.delLike(req);
        } else {
            socialMapper.setLike(req);
        }

        return ResultData.success(socialMapper.getLikeDtl(req));
    }

    /**
     * 팔로우 기능에 필요한 사용자 번호를 검증합니다.
     * 로그인 정보가 없으면 인증 실패, 상대가 없거나 자기 자신이면 잘못된 요청으로 응답합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateFollowUsers(SocialDto.FollowDto req) {
        if (StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getUserNumb())) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (StringUtil.isEmpty(req.getFlowNumb()) || req.getUserNumb().equals(req.getFlowNumb())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        UserDto targetUser = userMapper.getUserByNumb(req.getFlowNumb());

        if (StringUtil.isEmpty(targetUser)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return null;
    }

    /**
     * 좋아요 요청 대상을 검증합니다.
     * TB_LIKEXX는 공용 테이블이지만 현재 도메인에서 허용한 대상은 공개 독후감(REPORT)이므로 먼저 타입을 제한합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateLikeTarget(SocialDto.LikeDto req) {
        if (StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getUserNumb())) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (StringUtil.isEmpty(req.getTagtType()) || StringUtil.isEmpty(req.getTagtNumb())) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        req.setTagtType(req.getTagtType().trim().toUpperCase());

        if (!Constant.LIKE_TARGET_REPORT.equals(req.getTagtType())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        if (reportMapper.getPublicReportLikeTargetCnt(req) == 0) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
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
    private SocialDto.FollowDto createFollowStatus(String followStatName) {
        SocialDto.FollowDto followDto = new SocialDto.FollowDto();
        followDto.setFollowStatName(followStatName);
        return followDto;
    }
}
