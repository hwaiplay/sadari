package org.our.sadari.social.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.event.LikeAlimEvent;
import org.our.sadari.alim.event.LikeAlimPublisher;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.feed.mapper.FeedMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.report.mapper.ReportMapper;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.mapper.SocialMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : SocialServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-22
 * description    : 사용자 검색과 팔로우 및 좋아요 업무 로직을 구현함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-22        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       프로필 통계 공개 범위 조건 추가
 * 2026-08-13        SeungHyeon.Kang    팔로우 버튼 상태 공통코드 조회 일원화
 * 2026-08-15        SeungHyeon.Kang    팔로우 목록 페이지 조회 추가
 * 2026-08-21        SeungHyeon.Kang    독후감 설정·알림 상황 통합
 * 2026-08-26        SeungHyeon.Kang        좋아요 목록·비동기 알림 처리
 * 2026-08-27        SeungHyeon.Kang    좋아요 알림 원본 유형과 공개 사진 반응 적용
 * 2026-08-28        HanWon.Jang        활성 사용자 관계순 검색 추가
 * 2026-09-03        HanWon.Jang        사용자 차단 검증과 목록 격리 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialServiceImpl implements SocialService {

    // 팔로우 목록 모달이 한 번에 조회할 사용자 수
    private static final int FOLLOW_PAGE_SIZE = 10;

    // Social 데이터 접근 객체
    private final SocialMapper socialMapper;
    // Report 데이터 접근 객체
    private final ReportMapper reportMapper;
    // Feed 데이터 접근 객체
    private final FeedMapper feedMapper;
    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // Alim 업무 처리 서비스
    private final AlimService alimService;
    // TokenRedis 업무 처리 서비스
    private final TokenRedisService tokenRedisService;
    // 좋아요 커밋 이후 알림 후처리 이벤트 발행기
    private final LikeAlimPublisher likeAlimPublisher;
    // 사용자 차단과 양방향 격리 업무 처리 서비스
    private final UserBlockService userBlockService;

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회함
     * 버튼명 판단은 소셜 Mapper가 팔로우 관계와 공통코드를 함께 조회하여 화면과 서버가 같은 기준을 사용하게 함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @Override
    public ResultData getFollowStatus(SocialDto.FollowDto req) {
        // validateFollowUsers 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateFollowUsers(req);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회 결과를 성공 응답으로 반환함
        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 저장함
     * 이미 팔로우 중인 경우에도 MERGE 쿼리를 사용하므로 중복 오류 없이 최신 버튼 상태만 반환함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData setFollow(SocialDto.FollowDto req) {
        // 유효한 사용자 번호가 있으면 차단 등록과 같은 순서로 사용자 쌍을 잠금
        if (!StringUtil.isEmpty(req) && !StringUtil.hasEmpty(req.getUserNumb(), req.getFlowNumb())
                && !req.getUserNumb().equals(req.getFlowNumb())) {
            // 차단 완료 뒤 팔로우 관계가 남는 경쟁 조건을 막음
            userBlockService.lockUsers(req.getUserNumb(), req.getFlowNumb());
        }

        // validateFollowUsers 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateFollowUsers(req);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 저장 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // Follow 업무 값을 socialMapper DTO에 설정함
        int insertCnt = socialMapper.setFollow(req);

        // 새 팔로우 관계가 실제로 저장된 경우에만 팔로우 알림을 발송함
        // 이미 팔로우 중이라 MERGE가 아무 것도 저장하지 않은 경우에는 같은 알림을 다시 만들 필요가 없음
        if (insertCnt > 0) {
            // sendFollowAlim 호출로 검증된 알림 또는 응답을 전송함
            sendFollowAlim(req);
        }

        // 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 저장 결과를 성공 응답으로 반환함
        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제함
     * 상대가 나를 팔로우하고 있는 반대 방향 관계는 삭제하지 않아 언팔로우 후에도 맞팔로우 유도 상태를 계산할 수 있음
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    @Override
    @Transactional
    public ResultData delFollow(SocialDto.FollowDto req) {
        // validateFollowUsers 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateFollowUsers(req);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // Follow 데이터를 DB에서 삭제함
        socialMapper.delFollow(req);
        // 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제 결과를 성공 응답으로 반환함
        return ResultData.success(createFollowStatus(socialMapper.getFollowStatusName(req)));
    }

    /**
     * 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소함
     * 현재 화면에서 지원하는 공개 독후감과 현재 프로필·배경사진만 허용하고,
     * 다른 TAGT_TYPE은 저장하지 않고 잘못된 요청으로 응답함
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 변경 후 좋아요 상세 정보
     */
    @Override
    @Transactional
    public ResultData setLike(SocialDto.LikeDto req) {
        // validateLikeTarget 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateLikeTarget(req);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분함
        if (socialMapper.dupLike(req) > 0) {
            // Like 데이터를 DB에서 삭제함
            socialMapper.delLike(req);
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환함
        else {
            // Like 업무 값을 socialMapper DTO에 설정함
            socialMapper.setLike(req);
            // 좋아요 커밋 이후에만 알림을 처리하도록 후처리 이벤트를 등록함
            sendLikeAlim(req);
        }

        // 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소 결과를 성공 응답으로 반환함
        return ResultData.success(socialMapper.getLikeDtl(req));
    }

    /**
     * 마이페이지 프로필 상단 통계 값을 조회함
     * 팔로우/팔로워/좋아요 집계는 social 도메인의 책임이고, 총 읽은 책도 같은 화면 통계 묶음으로 제공되어야 하므로
     * MyPageController가 여러 mapper를 직접 호출하지 않도록 social service에서 한 번에 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 총 읽은 책, 팔로우, 팔로워, 받은 좋아요 수
     */
    @Override
    public ResultData getMyPageProfileStats(Long userNumb) {
        // 본인 화면은 공개 여부 조건 없이 전체 독후감을 포함한 통계를 반환함
        return getProfileStatsResult(userNumb, userNumb, null);
    }

    /**
     * 사용자 프로필 통계 값을 조회함
     * 마이페이지와 다른 사람 프로필은 같은 SQL을 사용하고 공개 여부 조건으로 독후감 집계 범위만 구분함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 통계를 조회하는 로그인 사용자 번호
     * @param userNumb 조회할 사용자 번호
     * @return 총 읽은 책, 팔로우, 팔로워, 받은 좋아요 수
     */
    @Override
    public ResultData getProfileStats(Long loginUserNumb, Long userNumb) {
        // 공개 프로필 통계는 조회자와 대상 사이의 차단 관계를 먼저 검증함
        if (StringUtil.isEmpty(loginUserNumb) || userBlockService.isBlocked(loginUserNumb, userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 다른 사용자 화면은 공개 독후감만 포함한 통계를 반환함
        return getProfileStatsResult(loginUserNumb, userNumb, Constant.COMM_YES);
    }

    /**
     * 사용자 프로필 통계를 화면별 독후감 공개 범위로 조회함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 통계를 조회하는 로그인 사용자 번호
     * @param userNumb 조회할 사용자 번호
     * @param pubcYsno 독후감 공개 범위, null이면 전체 범위
     * @return 화면 범위에 맞춘 사용자 프로필 통계
     */
    private ResultData getProfileStatsResult(Long loginUserNumb, Long userNumb, String pubcYsno) {
        // validateTargetUser 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateTargetUser(userNumb);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 사용자 프로필 통계 값을 조회 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // 프로필 활동 통계 결과를 담을 객체를 생성함
        SocialDto.ProfileStatsDto req = new SocialDto.ProfileStatsDto();
        // 목록과 집계에서 같은 차단 범위를 적용할 조회자 번호를 설정함
        req.setLoginUserNumb(loginUserNumb);
        // UserNumb 업무 값을 req DTO에 설정함
        req.setUserNumb(userNumb);
        // 다른 사용자 프로필에 적용할 독후감 공개 범위를 설정함
        req.setPubcYsno(pubcYsno);
        // 사용자 프로필 통계 값을 조회 결과를 성공 응답으로 반환함
        return ResultData.success(socialMapper.getProfileStats(req));
    }

    /**
     * 닉네임 검색어를 포함하는 활성 사용자를 현재 로그인 사용자와의 관계순으로 조회함
     * 비활성화, 영구 탈퇴 대기와 이용정지 사용자는 관계가 남아 있어도 검색 결과에서 제외함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param keyword 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 친구와 한쪽 팔로우 관계 및 무관계 순으로 정렬한 활성 사용자 페이지
     */
    @Override
    public ResultData getUserSearchList(Long loginUserNumb, String keyword, int page) {
        // 인증 사용자 번호가 없으면 관계 정렬 기준을 만들 수 없어 검색을 거부함
        if (StringUtil.isEmpty(loginUserNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 닉네임 저장 최대 길이에 맞춰 검색어 양끝 공백과 길이를 정규화함
        String normalizedKeyword = StringUtil.normalizePlainText(keyword, Constant.USER_NICK_MAX_LENGTH);

        // 정규화 뒤 검색어가 없으면 전체 활성 사용자가 노출되지 않도록 요청을 거부함
        if (StringUtil.isEmpty(normalizedKeyword)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 활성 사용자 검색과 관계 정렬에 사용할 페이지 조건 객체를 생성함
        SocialDto.UserSearchReqDto req = createUserSearchReq(loginUserNumb, normalizedKeyword, page);
        // 활성 상태와 닉네임 및 로그인 사용자 관계를 한 조회에서 적용함
        List<SocialDto.FollowUserDto> searchedList = socialMapper.getUserSearchList(req);
        // 팔로우 목록과 같은 사용자 행 및 페이지 응답 구조를 반환함
        return getFollowPage(searchedList, Math.max(page, 1));
    }

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 조회함
     * 목록 행마다 로그인 사용자 기준 팔로우 상태를 같이 내려 모달에서 추가 상태 조회를 반복하지 않게 함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로잉 목록
     */
    @Override
    public ResultData getFollowingList(Long loginUserNumb, Long userNumb, int page) {
        // validateFollowListReq 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateFollowListReq(loginUserNumb, userNumb);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 특정 사용자가 팔로우하는 사용자 목록을 조회 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // createFollowListReq 호출로 후속 처리에 필요한 객체를 생성함
        SocialDto.FollowListReqDto req = createFollowListReq(loginUserNumb, userNumb, page);
        // 페이지 조건으로 특정 사용자가 팔로우하는 사용자 목록을 조회함
        List<SocialDto.FollowUserDto> searchedList = socialMapper.getFollowingList(req);
        // 팔로잉 목록의 현재 페이지와 다음 페이지 여부를 반환함
        return getFollowPage(searchedList, Math.max(page, 1));
    }

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 조회함
     * 팔로워 목록도 팔로잉 목록과 같은 응답 구조를 사용해 화면 모달을 공통으로 렌더링할 수 있게 함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로워 목록
     */
    @Override
    public ResultData getFollowerList(Long loginUserNumb, Long userNumb, int page) {
        // validateFollowListReq 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단함
        ResultData invalidResult = validateFollowListReq(loginUserNumb, userNumb);

        // invalidResult 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(invalidResult)) {
            // 특정 사용자를 팔로우하는 사용자 목록을 조회 과정에서 확인된 사용자 메시지
            return invalidResult;
        }

        // createFollowListReq 호출로 후속 처리에 필요한 객체를 생성함
        SocialDto.FollowListReqDto req = createFollowListReq(loginUserNumb, userNumb, page);
        // 페이지 조건으로 특정 사용자를 팔로우하는 사용자 목록을 조회함
        List<SocialDto.FollowUserDto> searchedList = socialMapper.getFollowerList(req);
        // 팔로워 목록의 현재 페이지와 다음 페이지 여부를 반환함
        return getFollowPage(searchedList, Math.max(page, 1));
    }

    /**
     * 접근 가능한 대상에 좋아요를 등록한 활성 사용자 목록을 조회함
     * 비활성 또는 삭제 대기 사용자의 좋아요 행은 보존하더라도 목록과 집계에서 제외함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param tagtType 좋아요 대상 유형
     * @param tagtNumb 좋아요 대상 번호
     * @param page 조회할 페이지 번호
     * @return 활성 좋아요 사용자 페이지
     */
    @Override
    public ResultData getLikeUserList(Long loginUserNumb, String tagtType, Long tagtNumb, int page) {
        // 인증 사용자 번호가 없으면 대상 정보 조회 전에 인증 실패로 응답함
        if (StringUtil.isEmpty(loginUserNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 대상 식별값이 없으면 사용자 목록 조회를 시작하지 않음
        if (StringUtil.hasEmpty(tagtType, tagtNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 허용 목록 비교를 위해 대상 유형의 공백과 대소문자를 정규화함
        String normalizedType = tagtType.trim().toUpperCase();
        // 현재 서비스가 좋아요를 제공하는 네 가지 대상 유형만 목록 조회를 허용함
        if (!isAllowedLikeListType(normalizedType) || tagtNumb <= 0) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 좋아요 목록 접근 검증과 페이지 조건을 같은 요청 객체에 설정함
        SocialDto.LikeUserReqDto req = createLikeUserReq(loginUserNumb, normalizedType, tagtNumb, page);
        // 비공개 독후감 등 현재 로그인 사용자가 조회할 수 없는 대상의 사용자 목록은 공개하지 않음
        if (socialMapper.getLikeTargetAccessCnt(req) <= 0) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 활성 사용자만 포함한 좋아요 목록을 페이지 조건으로 조회함
        List<SocialDto.FollowUserDto> searchedList = socialMapper.getLikeUserList(req);
        // 팔로우 목록과 동일한 페이지 응답 구조를 반환함
        return getFollowPage(searchedList, Math.max(page, 1));
    }

    /** 좋아요 사용자 목록에서 허용하는 대상 유형인지 확인함 */
    private boolean isAllowedLikeListType(String tagtType) {
        // 현재 화면에서 좋아요 수를 표시하는 대상 유형만 허용함
        return Constant.LIKE_TARGET_REPORT.equals(tagtType)
                || Constant.LIKE_TARGET_PROFILE_IMAGE.equals(tagtType)
                || Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(tagtType)
                || Constant.LIKE_TARGET_REPLY.equals(tagtType);
    }

    /** 좋아요 사용자 목록의 접근 검증과 페이지 조회 조건을 생성함 */
    private SocialDto.LikeUserReqDto createLikeUserReq(Long loginUserNumb, String tagtType
                                                     , Long tagtNumb, int page) {
        // 좋아요 사용자 목록 조회 조건을 담을 객체를 생성함
        SocialDto.LikeUserReqDto req = new SocialDto.LikeUserReqDto();
        // 인증 사용자와 대상 식별값을 접근 검증 조건으로 설정함
        req.setLoginUserNumb(loginUserNumb);
        req.setTagtType(tagtType);
        req.setTagtNumb(tagtNumb);
        // 요청 페이지를 첫 페이지 이상으로 보정함
        int normalizedPage = Math.max(page, 1);
        // 현재 페이지 시작 위치와 다음 페이지 판정용 조회 수를 설정함
        req.setPageOffset((normalizedPage - 1) * FOLLOW_PAGE_SIZE);
        req.setPageLimit(FOLLOW_PAGE_SIZE + 1);
        // 완성된 좋아요 사용자 목록 조회 조건을 반환함
        return req;
    }

    /**
     * 팔로우 기능에 필요한 사용자 번호를 검증함
     * 로그인 정보가 없으면 인증 실패, 상대가 없거나 자기 자신이면 잘못된 요청으로 응답함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateFollowUsers(SocialDto.FollowDto req) {
        // req 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getUserNumb())) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // req.getFlowNumb( 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(req.getFlowNumb()) || req.getUserNumb().equals(req.getFlowNumb())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // UserByNumb 데이터를 DB에서 조회함
        UserDto targetUser = userMapper.getUserByNumb(req.getFlowNumb());

        // targetUser 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(targetUser)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 어느 한쪽이라도 상대를 차단했으면 팔로우 상태와 변경을 모두 거절함
        if (userBlockService.isBlocked(req.getUserNumb(), req.getFlowNumb())) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 조회하거나 생성할 값이 없음을 반환함
        return null;
    }

    /**
     * 프로필 통계 또는 목록 조회 대상 사용자가 실제 존재하는지 검증함
     * 존재하지 않는 사용자 번호로 집계를 수행하면 빈 통계가 정상 데이터처럼 보일 수 있으므로 조회 전 차단함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회 대상 사용자 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateTargetUser(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // UserByNumb 데이터를 DB에서 조회함
        UserDto targetUser = userMapper.getUserByNumb(userNumb);

        // targetUser 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(targetUser)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 조회하거나 생성할 값이 없음을 반환함
        return null;
    }

    /**
     * 팔로우/팔로워 목록 조회에 필요한 로그인 사용자와 목록 주인 사용자를 검증함
     * 목록의 오른쪽 버튼명은 로그인 사용자 기준으로 계산되므로 로그인 사용자가 없으면 인증 실패로 응답함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateFollowListReq(Long loginUserNumb, Long userNumb) {
        // loginUserNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(loginUserNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 팔로우 목록 주인 사용자가 실제 존재하는지 먼저 검증함
        ResultData invalidResult = validateTargetUser(userNumb);
        // 대상 사용자가 없으면 차단 여부를 확인하지 않고 기존 실패 응답을 유지함
        if (!StringUtil.isEmpty(invalidResult)) {
            // 대상 사용자 검증에서 확정된 실패 응답을 반환함
            return invalidResult;
        }

        // 본인 목록이 아닌 경우 목록 주인과 조회자 사이의 차단 관계를 우선 적용함
        if (!loginUserNumb.equals(userNumb) && userBlockService.isBlocked(loginUserNumb, userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 팔로우 목록을 조회할 수 있는 상태임을 반환함
        return null;
    }

    /**
     * 팔로우/팔로워 목록 조회 DTO를 생성함
     * Controller와 Mapper가 같은 파라미터 구조를 공유하도록 Service에서 DTO 생성 지점을 고정함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로우 목록 조회 조건 DTO
     */
    private SocialDto.FollowListReqDto createFollowListReq(Long loginUserNumb, Long userNumb, int page) {
        // 팔로우 목록 조회 조건을 담을 객체를 생성함
        SocialDto.FollowListReqDto req = new SocialDto.FollowListReqDto();
        // LoginUserNumb 업무 값을 req DTO에 설정함
        req.setLoginUserNumb(loginUserNumb);
        // UserNumb 업무 값을 req DTO에 설정함
        req.setUserNumb(userNumb);
        // 요청 페이지를 첫 페이지 이상으로 보정함
        int normalizedPage = Math.max(page, 1);
        // 현재 팔로우 목록 페이지의 시작 위치를 설정함
        req.setPageOffset((normalizedPage - 1) * FOLLOW_PAGE_SIZE);
        // 다음 페이지 판정용 한 건을 추가한 조회 수를 설정함
        req.setPageLimit(FOLLOW_PAGE_SIZE + 1);
        // 팔로우/팔로워 목록 조회 DTO를 생성 결과를 반환함
        return req;
    }

    /**
     * 활성 사용자 닉네임 검색과 관계 정렬에 사용할 페이지 조건을 생성함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param keyword 정규화된 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 활성 사용자 검색 조건 DTO
     */
    private SocialDto.UserSearchReqDto createUserSearchReq(Long loginUserNumb, String keyword, int page) {
        // 활성 사용자 검색 조건을 담을 객체를 생성함
        SocialDto.UserSearchReqDto req = new SocialDto.UserSearchReqDto();
        // 관계 정렬 기준이 되는 로그인 사용자 번호를 설정함
        req.setLoginUserNumb(loginUserNumb);
        // 닉네임 포함 검색에 사용할 정규화된 검색어를 설정함
        req.setKeyword(keyword);
        // 요청 페이지를 첫 페이지 이상으로 보정함
        int normalizedPage = Math.max(page, 1);
        // 현재 검색 페이지의 시작 위치를 설정함
        req.setPageOffset((normalizedPage - 1) * FOLLOW_PAGE_SIZE);
        // 다음 페이지 판정용 한 건을 추가한 조회 수를 설정함
        req.setPageLimit(FOLLOW_PAGE_SIZE + 1);
        // 완성된 활성 사용자 검색 조건을 반환함
        return req;
    }

    /**
     * 팔로우 조회 결과를 현재 페이지 크기로 제한하고 다음 페이지 여부를 구성함
     *
     * @author SeungHyeon.Kang
     * @param searchedList 다음 페이지 판정용 한 건이 포함된 사용자 목록
     * @param page 현재 페이지 번호
     * @return 팔로우 사용자 페이지 응답
     */
    private ResultData getFollowPage(List<SocialDto.FollowUserDto> searchedList, int page) {
        // Mapper가 빈 값을 반환해도 페이지 응답을 유지하도록 빈 목록으로 보정함
        List<SocialDto.FollowUserDto> safeList = StringUtil.isEmpty(searchedList) ? List.of() : searchedList;
        // 제한 건수보다 한 건 더 조회되었는지 다음 페이지 여부로 판정함
        boolean hasNext = safeList.size() > FOLLOW_PAGE_SIZE;
        // 화면에는 현재 페이지 크기만 전달함
        List<SocialDto.FollowUserDto> visibleList = hasNext
                ? safeList.subList(0, FOLLOW_PAGE_SIZE)
                : safeList;
        // 현재 페이지 사용자 목록과 다음 페이지 여부를 반환함
        return ResultData.success(new PageDto<>(visibleList, page, hasNext));
    }

    /**
     * 좋아요 요청 대상을 검증함
     * TB_LIKEXX는 공용 테이블이므로 현재 도메인에서 허용한 공개 독후감과
     * 현재 프로필·배경사진으로 대상 유형을 제한함
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 실패 응답 또는 null
     */
    private ResultData validateLikeTarget(SocialDto.LikeDto req) {
        // req 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getUserNumb())) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 대상 유형 또는 대상 번호가 없으면 좋아요 대상을 확정할 수 없으므로 요청을 거부함
        if (StringUtil.isEmpty(req.getTagtType()) || StringUtil.isEmpty(req.getTagtNumb())) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // TagtType 업무 값을 req DTO에 설정함
        req.setTagtType(req.getTagtType().trim().toUpperCase());

        boolean isReport = Constant.LIKE_TARGET_REPORT.equals(req.getTagtType());
        boolean isProfile = Constant.LIKE_TARGET_PROFILE_IMAGE.equals(req.getTagtType());
        boolean isBackground = Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(req.getTagtType());

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분함
        if (!isReport && !isProfile && !isBackground) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 작성자와 알림 설정은 클라이언트 값을 신뢰하지 않고 현재 대상에서 조회함
        SocialDto.LikeDto likeTarget = isReport
                ? reportMapper.getReportLikeDtl(req)
                : feedMapper.getImageLikeTarget(req);

        // 본인 소유 또는 접근 가능한 현재 대상이 아니면 좋아요를 허용하지 않음
        if (StringUtil.isEmpty(likeTarget)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 서버에서 확인한 대상 소유자 번호를 알림 수신자로 설정함
        req.setTargetUserNumb(likeTarget.getTargetUserNumb());
        // 좋아요 대상 소유자와 양방향 차단 관계이면 개인 반응을 거절함
        if (userBlockService.isBlocked(req.getUserNumb(), likeTarget.getTargetUserNumb())) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 서버에서 확인한 대상별 좋아요 알림 여부를 후속 알림 처리 조건으로 설정함
        req.setLikeAlimYsno(likeTarget.getLikeAlimYsno());
        // 좋아요 대상 유형에 대응하는 알림 템플릿 코드를 설정함
        req.setAlimTempCode(resolveLikeAlimTemplate(req.getTagtType()));
        // 독후감과 사진 알림 모두 원본 콘텐츠를 직접 찾을 수 있도록 대상 번호를 설정함
        req.setAlimTagtNumb(req.getTagtNumb());

        // 조회하거나 생성할 값이 없음을 반환함
        return null;
    }

    /**
     * 독후감 또는 현재 사진 좋아요가 새로 등록된 경우 커밋 이후 알림 이벤트를 등록함
     * 좋아요 취소와 본인 소유 대상 및 수신 거부 상태에는 이벤트를 만들지 않음
     *
     * @author SeungHyeon.Kang
     * @param req 좋아요 요청 DTO
     */
    private void sendLikeAlim(SocialDto.LikeDto req) {
        // 독후감 작성자가 좋아요 알림을 껐으면 알림 저장과 푸시 예약을 모두 생략함
        if (!Constant.COMM_YES.equals(req.getLikeAlimYsno())) {
            // 독후감 좋아요 알림 처리 없이 호출부로 반환함
            return;
        }

        // 본인이 작성한 독후감에 본인이 좋아요를 누른 경우에는 자기 자신에게 알림을 만들 필요가 없어 중단함
        if (req.getTargetUserNumb().equals(req.getUserNumb())) {
            // 독후감 좋아요가 새로 등록된 경우 독후감 작성자에게 좋아요 알림을 발송 결과를 반환함
            return;
        }

        // 좋아요 응답 경로에서 Redis와 알림 DB 및 FCM 접근을 제거할 커밋 이후 이벤트를 생성함
        LikeAlimEvent event = new LikeAlimEvent(req.getUserNumb(), req.getTargetUserNumb(), req.getAlimTempCode()
                                              , req.getTagtType(), req.getAlimTagtNumb(), null, null);
        // 좋아요 트랜잭션이 커밋된 경우에만 비동기 알림 작업이 시작되도록 이벤트를 등록함
        likeAlimPublisher.setLikeAlim(event);
    }

    /** 좋아요 대상 유형에 대응하는 알림 템플릿 코드를 반환함 */
    private String resolveLikeAlimTemplate(String targetType) {
        if (Constant.LIKE_TARGET_PROFILE_IMAGE.equals(targetType)) {
            return Constant.ALIM_TEMP_CODE_LIKE_PROFILE_IMAGE;
        }
        if (Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(targetType)) {
            return Constant.ALIM_TEMP_CODE_LIKE_BACKGROUND_IMAGE;
        }
        return Constant.ALIM_TEMP_CODE_LIKE_REPORT;
    }

    /**
     * MySQL 함수에서 받은 버튼명을 프론트엔드 응답 DTO로 감쌈
     * ResultData.data의 필드명을 고정해 화면에서 응답 구조를 안정적으로 사용할 수 있게 함
     *
     * @author SeungHyeon.Kang
     * @param followStatName 화면에 표시할 팔로우 버튼명
     * @return 팔로우 상태 DTO
     */
    private SocialDto.FollowDto createFollowStatus(String followStatName) {
        // 팔로우 대상과 알림 정보를 담을 객체를 생성함
        SocialDto.FollowDto followDto = new SocialDto.FollowDto();
        // FollowStatName 업무 값을 followDto DTO에 설정함
        followDto.setFollowStatName(followStatName);
        // MySQL 함수에서 받은 버튼명을 프론트엔드 응답 DTO로 감싼 결과를 반환함
        return followDto;
    }

    /**
     * 팔로우를 받은 사용자에게 새 팔로워 알림을 발송함
     * 팔로우 INSERT가 실제로 발생한 경우에만 호출되며, sendAlim 공통 로직에서 1시간 내 동일 알림을 한 번 더 차단함
     *
     * @author SeungHyeon.Kang
     * @param req 팔로우를 수행한 사용자와 팔로우 대상 사용자 번호
     */
    private void sendFollowAlim(SocialDto.FollowDto req) {
        // 본인을 팔로우하는 요청은 검증에서 차단되지만, 알림 발송 직전에도 한 번 더 방어함
        if (req.getUserNumb().equals(req.getFlowNumb())) {
            // 팔로우를 받은 사용자에게 새 팔로워 알림을 발송 결과를 반환함
            return;
        }

        // getUserNick 업무 로직을 tokenRedisService에 위임함
        String sendUserNick = tokenRedisService.getUserNick(req.getUserNumb());

        // 로그인 Redis 정보가 없으면 사용자 테이블을 다시 조회하지 않고 부가 기능인 알림만 생략함
        if (StringUtil.isEmpty(sendUserNick)) {
            // 팔로우를 받은 사용자에게 새 팔로워 알림을 발송 결과를 반환함
            return;
        }

        Map<String, Object> replaceMap = new HashMap<>();
        // 후속 처리에 사용할 키와 값을 맵에 저장함
        replaceMap.put("userName", sendUserNick);

        // sendAlim 업무 로직을 alimService에 위임함
        alimService.sendUserAlim(
                req.getUserNumb()
                // getFlowNumb 조회로 후속 처리에 필요한 데이터를 가져옴
              , req.getFlowNumb()
              , Constant.ALIM_SITU_FOLLOW_CLUB
              , Constant.ALIM_TEMP_CODE_FOLLOW_USER
              , Constant.ALIM_TARGET_USER
              , req.getUserNumb()
              , null
              , replaceMap
        );
    }
}
