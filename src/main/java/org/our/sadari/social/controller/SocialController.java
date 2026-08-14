package org.our.sadari.social.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.service.ReadingStatisticsService;
import org.our.sadari.report.service.ReportService;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.service.SocialService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : SocialController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 팔로우와 좋아요 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       공개 독후감만 소셜 요약과 통계에 포함
 * 2026-08-14        SeungHyeon.Kang    공개 허용 독서 통계 조회 추가
 * 2026-08-14        SeungHyeon.Kang    공개 독서 통계 연도 선택 조회 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Tag(name = "소셜", description = "공개 프로필, 팔로우, 좋아요 API")
public class SocialController {

    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // Report 업무 처리 서비스
    private final ReportService reportService;
    // Social 업무 처리 서비스
    private final SocialService socialService;
    // 다른 사용자 프로필에 공개 독서 통계를 제공할 서비스
    private final ReadingStatisticsService readingStatisticsService;

    /**
     * 사용자 번호로 공개 프로필 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 공개 프로필 조회 결과
     */
    @GetMapping("/profile/{userNumb}")
    @Operation(summary = "공개 프로필 조회", description = "사용자 번호로 공개 프로필 정보를 조회한다.")
    public ResultData getSocialProfile(@Parameter(description = "조회할 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // UserByNumb 데이터를 DB에서 조회한다
        UserDto user = userMapper.getUserByNumb(userNumb);

        // user 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(user)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        Map<String, String> profile = new HashMap<>();
        // 공개 프로필 화면이 탈퇴 회원 전용 표시를 선택할 수 있도록 회원 상태를 설정한다
        profile.put("userStat", user.getUserStat());

        // 탈퇴 회원은 프로필 원본 대신 탈퇴 상태만 공개한다
        if (!Constant.USER_STAT_ACTIVE.equals(user.getUserStat())) {
            // 탈퇴 회원의 대체 닉네임을 설정한다
            profile.put("userNick", "탈퇴한 사용자");
            // 제한된 공개 프로필 정보를 반환한다
            return ResultData.success(profile);
        }

        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("userNick", user.getUserNick());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("porfPath", user.getPorfPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("bgimPath", user.getBgimPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("intrCntn", user.getIntrCntn());
        // 사용자 번호로 공개 프로필 정보를 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(profile);
    }

    /**
     * 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 독서 활동 요약 조회 결과
     */
    @GetMapping("/profile/{userNumb}/reading-summary")
    @Operation(summary = "공개 독서 요약 조회", description = "사용자 번호로 공개 프로필의 독서 활동 요약을 조회한다.")
    public ResultData getSocialReadingSummary(@Parameter(description = "조회할 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // UserByNumb 데이터를 DB에서 조회한다
        UserDto user = userMapper.getUserByNumb(userNumb);

        // user 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(user)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 탈퇴 회원의 목표와 독서 활동 집계는 공개하지 않고 빈 요약을 반환한다
        if (!Constant.USER_STAT_ACTIVE.equals(user.getUserStat())) {
            // 개인정보가 제거된 빈 독서 요약을 반환한다
            return ResultData.success(new MonthlyReadingSummaryDto());
        }

        // 다른 사용자 화면에는 공개 독후감과 목표만 제공하도록 공개 여부를 서버에서 고정한다
        ResultData summaryResult = reportService.getMonthlyReadingSummary(userNumb, Constant.COMM_YES);

        // 다른 사람 프로필도 마이페이지와 같은 통계 영역을 사용하므로 독서 요약 응답에 social 통계를 합쳐 내려준다.
        // 독서 요약이 실패하면 통계를 추가하지 않고 후속 응답 데이터 결합을 중단한다
        if (summaryResult.getCode() != 200) {
            // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 반환한다
            return summaryResult;
        }

        // getProfileStats 업무 로직을 socialService에 위임한다
        ResultData statsResult = socialService.getProfileStats(userNumb);

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (statsResult.getCode() != 200) {
            // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 반환한다
            return statsResult;
        }

        // 공통 응답에 포함된 업무 데이터를 조회한다
        MonthlyReadingSummaryDto summary = (MonthlyReadingSummaryDto) summaryResult.getData();
        // 공통 응답에 포함된 업무 데이터를 조회한다
        SocialDto.ProfileStatsDto profileStats = (SocialDto.ProfileStatsDto) statsResult.getData();

        // profileStats 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(profileStats)) {
            // TotalReadBookCnt 업무 값을 summary DTO에 설정한다
            summary.setTotalReadBookCnt(profileStats.getTotalReadBookCnt());
            // FollowingCnt 업무 값을 summary DTO에 설정한다
            summary.setFollowingCnt(profileStats.getFollowingCnt());
            // FollowerCnt 업무 값을 summary DTO에 설정한다
            summary.setFollowerCnt(profileStats.getFollowerCnt());
            // ReceivedLikeCnt 업무 값을 summary DTO에 설정한다
            summary.setReceivedLikeCnt(profileStats.getReceivedLikeCnt());
        }

        // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(summary);
    }

    /**
     * 사용자 번호로 공개 허용된 독서 통계를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 공개 통계를 조회할 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 공개 허용 시 연도별 독서 통계, 비공개 또는 제한 계정이면 빈 데이터
     */
    @GetMapping("/profile/{userNumb}/reading-statistics")
    @Operation(summary = "공개 독서 통계 조회", description = "정상 이용 회원이 공개를 허용한 연도별 독서 통계를 다른 사용자 프로필에 제공한다.")
    public ResultData getPublicReadingStats(
            @Parameter(description = "공개 통계를 조회할 사용자 번호", example = "31") @PathVariable Long userNumb
          , @Parameter(description = "조회할 연도", example = "2026") @RequestParam(required = false) Integer readYear) {
        // 서버가 계정 상태와 공개 설정을 검증한 다른 사용자용 독서 통계를 조회한다
        return readingStatisticsService.getPublicReadingStats(userNumb, readYear);
    }

    /**
     * 로그인 사용자의 팔로잉 목록을 조회한다.
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/me/following")
    @Operation(summary = "내 팔로잉 목록 조회", description = "로그인 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        // 로그인 사용자의 팔로잉 목록을 조회한 결과를 반환한다
        return socialService.getFollowingList(loginUserNumb, loginUserNumb);
    }

    /**
     * 로그인 사용자의 팔로워 목록을 조회한다.
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/me/followers")
    @Operation(summary = "내 팔로워 목록 조회", description = "로그인 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        // 로그인 사용자의 팔로워 목록을 조회한 결과를 반환한다
        return socialService.getFollowerList(loginUserNumb, loginUserNumb);
    }

    /**
     * 특정 사용자의 팔로잉 목록을 조회한다.
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함된다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                     , @Parameter(description = "목록 주인 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 특정 사용자의 팔로잉 목록을 조회한 결과를 반환한다
        return socialService.getFollowingList(loginUserNumb, userNumb);
    }

    /**
     * 특정 사용자의 팔로워 목록을 조회한다.
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함된다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                    , @Parameter(description = "목록 주인 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 특정 사용자의 팔로워 목록을 조회한 결과를 반환한다
        return socialService.getFollowerList(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @GetMapping("/profile/{userNumb}/follow-status")
    @Operation(summary = "팔로우 버튼 상태 조회", description = "로그인 사용자와 상대 사용자 관계를 기준으로 팔로우 버튼명을 조회한다.")
    public ResultData getFollowStatus(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                    , @Parameter(description = "상대 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회 결과를 반환한다
        return socialService.getFollowStatus(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우하도록 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    @PostMapping("/profile/{userNumb}/follow")
    @Operation(summary = "팔로우 등록", description = "로그인 사용자가 상대 사용자를 팔로우한다.")
    public ResultData setFollow(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                              , @Parameter(description = "팔로우할 상대 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 탈퇴 회원은 관계를 유지하되 새로운 팔로우 조작 대상에서 제외한다
        if (!isActiveUser(userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 로그인 사용자가 프로필 주인을 팔로우하도록 저장 결과를 반환한다
        return socialService.setFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    @DeleteMapping("/profile/{userNumb}/follow")
    @Operation(summary = "언팔로우", description = "로그인 사용자가 상대 사용자에게 건 팔로우 관계를 삭제한다.")
    public ResultData delFollow(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                              , @Parameter(description = "언팔로우할 상대 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 탈퇴 회원은 기존 팔로우 관계를 유지하므로 언팔로우 조작을 허용하지 않는다
        if (!isActiveUser(userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제 결과를 반환한다
        return socialService.delFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소한다.
     * TB_LIKEXX가 공용 좋아요 테이블이므로 독후감 전용 reptNumb가 아니라 TAGT_TYPE, TAGT_NUMB를 요청값으로 받는다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param request 좋아요 대상 유형과 대상 번호
     * @return 변경 후 좋아요 상태와 좋아요 수
     */
    @PostMapping("/like")
    @Operation(
            summary = "좋아요 토글"
          , description = "대상 유형, 대상 번호와 화면이 조회한 작성자 번호를 검증한 뒤 좋아요를 등록하거나 취소한다."
    )
    public ResultData setLike(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                            , @RequestBody SocialDto.LikeDto request) {
        // request 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(request)) {
            // 좋아요 대상과 알림 정보를 담을 객체를 생성한다
            request = new SocialDto.LikeDto();
        }

        // UserNumb 업무 값을 request DTO에 설정한다
        request.setUserNumb(loginUserNumb);
        // 대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소 결과를 반환한다
        return socialService.setLike(request);
    }

    /**
     * 소셜 관계 조작 대상 회원이 정상 이용 상태인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 상대 회원 번호
     * @return 정상 이용 회원 여부
     */
    private boolean isActiveUser(Long userNumb) {
        // 회원 번호로 현재 사용자 상태를 조회한다
        UserDto user = userMapper.getUserByNumb(userNumb);
        // 존재하는 정상 이용 회원만 관계 조작 대상으로 인정한다
        return !StringUtil.isEmpty(user) && Constant.USER_STAT_ACTIVE.equals(user.getUserStat());
    }

    /**
     * 팔로우 API의 경로 변수와 인증 사용자 번호를 Mapper까지 전달할 DTO로 변환한다.
     * 원시 파라미터를 XML에 직접 넘기지 않도록 Controller 진입점에서 요청 구조를 고정한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 상대 사용자 번호
     * @return 팔로우 요청 DTO
     */
    private SocialDto.FollowDto createFollowDto(Long loginUserNumb, Long userNumb) {
        // 팔로우 대상과 알림 정보를 담을 객체를 생성한다
        SocialDto.FollowDto followDto = new SocialDto.FollowDto();
        // UserNumb 업무 값을 followDto DTO에 설정한다
        followDto.setUserNumb(loginUserNumb);
        // FlowNumb 업무 값을 followDto DTO에 설정한다
        followDto.setFlowNumb(userNumb);
        // 팔로우 API의 경로 변수와 인증 사용자 번호를 Mapper까지 전달할 DTO로 변환 결과를 반환한다
        return followDto;
    }
}
