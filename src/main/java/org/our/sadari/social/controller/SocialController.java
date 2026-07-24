package org.our.sadari.social.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 다른 사용자 프로필, 팔로우, 좋아요처럼 사용자 간 social 동작을 제공하는 Controller이다.
 * 팔로우와 좋아요 요청은 SocialDto 중첩 DTO로 묶어 Service와 Mapper까지 같은 요청 구조를 사용한다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Tag(name = "소셜", description = "공개 프로필, 팔로우, 좋아요 API")
public class SocialController {

    private final UserMapper userMapper;
    private final ReportService reportService;
    private final SocialService socialService;

    /**
     * 사용자 번호로 공개 프로필 정보를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 공개 프로필 조회 결과
     */
    @GetMapping("/profile/{userNumb}")
    @Operation(summary = "공개 프로필 조회", description = "사용자 번호로 공개 프로필 정보를 조회한다.")
    public ResultData getSocialProfile(@Parameter(description = "조회할 사용자 번호", example = "31")
                                       @PathVariable Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        if (StringUtil.isEmpty(user)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("userNick", user.getUserNick());
        profile.put("porfPath", user.getPorfPath());
        profile.put("bgimPath", user.getBgimPath());
        profile.put("intrCntn", user.getIntrCntn());

        return ResultData.success(profile);
    }

    /**
     * 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 독서 활동 요약 조회 결과
     */
    @GetMapping("/profile/{userNumb}/reading-summary")
    @Operation(summary = "공개 독서 요약 조회", description = "사용자 번호로 공개 프로필의 독서 활동 요약을 조회한다.")
    public ResultData getSocialReadingSummary(@Parameter(description = "조회할 사용자 번호", example = "31")
                                              @PathVariable Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        if (StringUtil.isEmpty(user)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        ResultData summaryResult = reportService.getMonthlyReadingSummary(userNumb);

        // 다른 사람 프로필도 마이페이지와 같은 통계 영역을 사용하므로 독서 요약 응답에 social 통계를 합쳐 내려준다.
        // 독서 요약이 실패하면 통계를 추가하지 않고 원래 실패 응답을 그대로 반환한다.
        if (summaryResult.getCode() != 200) {
            return summaryResult;
        }

        ResultData statsResult = socialService.getProfileStats(userNumb);

        if (statsResult.getCode() != 200) {
            return statsResult;
        }

        MonthlyReadingSummaryDto summary = (MonthlyReadingSummaryDto) summaryResult.getData();
        SocialDto.ProfileStatsDto profileStats = (SocialDto.ProfileStatsDto) statsResult.getData();

        if (!StringUtil.isEmpty(profileStats)) {
            summary.setTotalReadBookCnt(profileStats.getTotalReadBookCnt());
            summary.setFollowingCnt(profileStats.getFollowingCnt());
            summary.setFollowerCnt(profileStats.getFollowerCnt());
            summary.setReceivedLikeCnt(profileStats.getReceivedLikeCnt());
        }

        return ResultData.success(summary);
    }

    /**
     * 로그인 사용자의 팔로잉 목록을 조회합니다.
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/me/following")
    @Operation(summary = "내 팔로잉 목록 조회", description = "로그인 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        return socialService.getFollowingList(loginUserNumb, loginUserNumb);
    }

    /**
     * 로그인 사용자의 팔로워 목록을 조회합니다.
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용합니다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/me/followers")
    @Operation(summary = "내 팔로워 목록 조회", description = "로그인 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        return socialService.getFollowerList(loginUserNumb, loginUserNumb);
    }

    /**
     * 특정 사용자의 팔로잉 목록을 조회합니다.
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함됩니다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                       @Parameter(description = "목록 주인 사용자 번호", example = "31")
                                       @PathVariable Long userNumb) {
        return socialService.getFollowingList(loginUserNumb, userNumb);
    }

    /**
     * 특정 사용자의 팔로워 목록을 조회합니다.
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함됩니다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                      @Parameter(description = "목록 주인 사용자 번호", example = "31")
                                      @PathVariable Long userNumb) {
        return socialService.getFollowerList(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @GetMapping("/profile/{userNumb}/follow-status")
    @Operation(summary = "팔로우 버튼 상태 조회", description = "로그인 사용자와 상대 사용자 관계를 기준으로 팔로우 버튼명을 조회한다.")
    public ResultData getFollowStatus(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                      @Parameter(description = "상대 사용자 번호", example = "31")
                                      @PathVariable Long userNumb) {
        return socialService.getFollowStatus(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우하도록 저장한다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    @PostMapping("/profile/{userNumb}/follow")
    @Operation(summary = "팔로우 등록", description = "로그인 사용자가 상대 사용자를 팔로우한다.")
    public ResultData setFollow(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                @Parameter(description = "팔로우할 상대 사용자 번호", example = "31")
                                @PathVariable Long userNumb) {
        return socialService.setFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    @DeleteMapping("/profile/{userNumb}/follow")
    @Operation(summary = "언팔로우", description = "로그인 사용자가 상대 사용자에게 건 팔로우 관계를 삭제한다.")
    public ResultData delFollow(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                @Parameter(description = "언팔로우할 상대 사용자 번호", example = "31")
                                @PathVariable Long userNumb) {
        return socialService.delFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소한다.
     * TB_LIKEXX가 공용 좋아요 테이블이므로 독후감 전용 reptNumb가 아니라 TAGT_TYPE, TAGT_NUMB를 요청값으로 받는다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param request 좋아요 대상 유형과 대상 번호
     * @return 변경 후 좋아요 상태와 좋아요 수
     */
    @PostMapping("/like")
    @Operation(summary = "좋아요 토글", description = "대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소한다.")
    public ResultData setLike(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                              @RequestBody SocialDto.LikeDto request) {
        if (request == null) {
            request = new SocialDto.LikeDto();
        }

        request.setUserNumb(loginUserNumb);
        return socialService.setLike(request);
    }

    /**
     * 팔로우 API의 경로 변수와 인증 사용자 번호를 Mapper까지 전달할 DTO로 변환한다.
     * 원시 파라미터를 XML에 직접 넘기지 않도록 Controller 진입점에서 요청 구조를 고정한다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 상대 사용자 번호
     * @return 팔로우 요청 DTO
     */
    private SocialDto.FollowDto createFollowDto(Long loginUserNumb, Long userNumb) {
        SocialDto.FollowDto followDto = new SocialDto.FollowDto();
        followDto.setUserNumb(loginUserNumb);
        followDto.setFlowNumb(userNumb);
        return followDto;
    }
}
