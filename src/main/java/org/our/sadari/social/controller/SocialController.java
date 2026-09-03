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
import org.our.sadari.social.service.UserBlockService;
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
 * description    : 사용자 검색과 공개 프로필 및 팔로우와 좋아요 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       공개 독후감만 소셜 요약과 통계에 포함
 * 2026-08-14        SeungHyeon.Kang    공개 독서 통계 조회 추가
 * 2026-08-15        SeungHyeon.Kang    접근 제한 회원 소셜 프로필 상태명 응답 추가
 * 2026-08-26        SeungHyeon.Kang        활성 좋아요 사용자 목록 조회 추가
 * 2026-08-27        SeungHyeon.Kang    공개 프로필 사진 반응 조회 추가
 * 2026-08-28        HanWon.Jang        피드 활성 사용자 검색 추가
 * 2026-09-03        HanWon.Jang        사용자 차단 API와 접근 검증 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Tag(name = "소셜", description = "사용자 검색, 공개 프로필, 팔로우와 좋아요 API")
public class SocialController {

    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // Report 업무 처리 서비스
    private final ReportService reportService;
    // Social 업무 처리 서비스
    private final SocialService socialService;
    // 사용자 차단과 양방향 격리 업무 처리 서비스
    private final UserBlockService userBlockService;
    // 다른 사용자 프로필에 공개 독서 통계를 제공할 서비스
    private final ReadingStatisticsService readingStatisticsService;

    /**
     * 사용자 번호로 공개 프로필 정보를 조회함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 사진 반응 상태를 계산할 로그인 사용자 번호
     * @param userNumb 조회할 사용자 번호
     * @return 공개 프로필 조회 결과
     */
    @GetMapping("/profile/{userNumb}")
    @Operation(summary = "공개 프로필 조회", description = "사용자 번호로 공개 프로필 정보를 조회한다.")
    public ResultData getSocialProfile(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                     , @Parameter(description = "조회할 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 로그인 사용자와 조회 대상이 없으면 사진 반응의 현재 사용자 상태를 계산할 수 없어 요청을 거부함
        if (StringUtil.hasEmpty(loginUserNumb, userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 어느 한쪽이라도 상대를 차단했으면 프로필 존재 여부를 구분하지 않는 공통 응답을 반환함
        if (userBlockService.isBlocked(loginUserNumb, userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // UserByNumb 데이터를 DB에서 조회함
        UserDto user = userMapper.getUserByNumb(userNumb);

        // user 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(user)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 공개 프로필과 사진 반응을 함께 담을 응답 객체를 생성함
        Map<String, Object> profile = new HashMap<>();
        // 공개 프로필 화면이 접근 제한 회원 안내를 선택할 수 있도록 회원 상태를 설정함
        profile.put("userStat", user.getUserStat());
        // 프론트엔드가 공통코드명을 임의로 하드코딩하지 않도록 회원 상태명을 설정함
        profile.put("userStatName", user.getUserStatName());

        // 접근 제한 회원은 프로필 원본 대신 회원 상태만 공개함
        if (!Constant.USER_STAT_ACTIVE.equals(user.getUserStat())) {
            // 탈퇴 회원의 대체 닉네임을 설정함
            profile.put("userNick", "탈퇴한 사용자");
            // 제한된 공개 프로필 정보를 반환함
            return ResultData.success(profile);
        }

        // 후속 처리에 사용할 키와 값을 맵에 저장함
        profile.put("userNick", user.getUserNick());
        // 후속 처리에 사용할 키와 값을 맵에 저장함
        profile.put("porfPath", user.getPorfPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장함
        profile.put("bgimPath", user.getBgimPath());
        // 일반 프로필 화면에 사용할 축소 배경사진 경로를 저장함
        profile.put("bgimDisplayPath", user.getBgimDisplayPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장함
        profile.put("intrCntn", user.getIntrCntn());
        // 현재 프로필 사진이 있으면 로그인 사용자 기준 좋아요와 댓글 집계를 저장함
        profile.put("profileImageReaction", getImageReaction(
                loginUserNumb, userNumb, Constant.LIKE_TARGET_PROFILE_IMAGE, user.getProfNumb()
        ));
        // 현재 배경사진이 있으면 로그인 사용자 기준 좋아요와 댓글 집계를 저장함
        profile.put("backgroundImageReaction", getImageReaction(
                loginUserNumb, userNumb, Constant.LIKE_TARGET_BACKGROUND_IMAGE, user.getBgimNumb()
        ));
        // 사용자 번호로 공개 프로필 정보를 조회 결과를 성공 응답으로 반환함
        return ResultData.success(profile);
    }

    /**
     * 다른 사용자 프로필에 표시할 현재 사진의 좋아요와 댓글 집계를 조회함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 사진 반응을 확인하는 로그인 사용자 번호
     * @param ownerUserNumb 사진 소유자 사용자 번호
     * @param tagtType 프로필 또는 배경사진 대상 유형
     * @param tagtNumb 현재 사진 파일 번호
     * @return 현재 사진 반응 집계 또는 사진이 없을 때 null
     */
    private UserDto.ImageReactionDto getImageReaction(Long loginUserNumb, Long ownerUserNumb
                                                     , String tagtType, Long tagtNumb) {
        // 현재 사진이 없으면 소셜 프로필에 반응 버튼을 표시하지 않음
        if (StringUtil.isEmpty(tagtNumb)) {
            // 사진 반응 대상이 없음을 반환함
            return null;
        }

        // 로그인 사용자와 사진 소유자를 분리한 반응 조회 객체를 생성함
        UserDto.ImageReactionDto request = new UserDto.ImageReactionDto();
        // 로그인 사용자의 현재 좋아요 여부를 계산할 사용자 번호를 설정함
        request.setUserNumb(loginUserNumb);
        // 현재 사진을 소유한 공개 프로필 사용자 번호를 설정함
        request.setOwnerUserNumb(ownerUserNumb);
        // 조회할 사진 유형을 설정함
        request.setTagtType(tagtType);
        // 교체되지 않은 현재 사진인지 검증할 파일 번호를 설정함
        request.setTagtNumb(tagtNumb);
        // 현재 사진에 연결된 좋아요와 댓글 집계를 반환함
        return userMapper.getImageReactionDtl(request);
    }

    /**
     * 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 독서 활동 요약 조회 결과
     */
    @GetMapping("/profile/{userNumb}/reading-summary")
    @Operation(summary = "공개 독서 요약 조회", description = "사용자 번호로 공개 프로필의 독서 활동 요약을 조회한다.")
    public ResultData getSocialReadingSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
          , @Parameter(description = "조회할 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 인증 사용자와 조회 대상 사이에 차단 관계가 있으면 공개 독서 요약도 제공하지 않음
        if (StringUtil.hasEmpty(loginUserNumb, userNumb) || userBlockService.isBlocked(loginUserNumb, userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // UserByNumb 데이터를 DB에서 조회함
        UserDto user = userMapper.getUserByNumb(userNumb);

        // user 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(user)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 탈퇴 회원의 목표와 독서 활동 집계는 공개하지 않고 빈 요약을 반환함
        if (!Constant.USER_STAT_ACTIVE.equals(user.getUserStat())) {
            // 개인정보가 제거된 빈 독서 요약을 반환함
            return ResultData.success(new MonthlyReadingSummaryDto());
        }

        // 다른 사용자 화면에는 공개 독후감과 목표만 제공하도록 공개 여부를 서버에서 고정함
        ResultData summaryResult = reportService.getMonthlyReadingSummary(userNumb, Constant.COMM_YES);

        // 다른 사람 프로필도 마이페이지와 같은 통계 영역을 사용하므로 독서 요약 응답에 social 통계를 합쳐 내려줌
        // 독서 요약이 실패하면 통계를 추가하지 않고 후속 응답 데이터 결합을 중단함
        if (summaryResult.getCode() != 200) {
            // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 반환함
            return summaryResult;
        }

        // getProfileStats 업무 로직을 socialService에 위임함
        ResultData statsResult = socialService.getProfileStats(loginUserNumb, userNumb);

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분함
        if (statsResult.getCode() != 200) {
            // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 반환함
            return statsResult;
        }

        // 공통 응답에 포함된 업무 데이터를 조회함
        MonthlyReadingSummaryDto summary = (MonthlyReadingSummaryDto) summaryResult.getData();
        // 공통 응답에 포함된 업무 데이터를 조회함
        SocialDto.ProfileStatsDto profileStats = (SocialDto.ProfileStatsDto) statsResult.getData();

        // profileStats 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (!StringUtil.isEmpty(profileStats)) {
            // TotalReadBookCnt 업무 값을 summary DTO에 설정함
            summary.setTotalReadBookCnt(profileStats.getTotalReadBookCnt());
            // FollowingCnt 업무 값을 summary DTO에 설정함
            summary.setFollowingCnt(profileStats.getFollowingCnt());
            // FollowerCnt 업무 값을 summary DTO에 설정함
            summary.setFollowerCnt(profileStats.getFollowerCnt());
            // ReceivedLikeCnt 업무 값을 summary DTO에 설정함
            summary.setReceivedLikeCnt(profileStats.getReceivedLikeCnt());
        }

        // 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회 결과를 성공 응답으로 반환함
        return ResultData.success(summary);
    }

    /**
     * 사용자 번호로 공개 허용된 독서 통계를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 공개 통계를 조회할 사용자 번호
     * @param readYear 조회할 연도, 없으면 현재 연도
     * @return 공개 허용 시 연도별 독서 통계, 비공개 또는 제한 계정이면 빈 데이터
     */
    @GetMapping("/profile/{userNumb}/reading-statistics")
    @Operation(summary = "공개 독서 통계 조회", description = "정상 이용 회원이 공개를 허용한 연도별 독서 통계를 다른 사용자 프로필에 제공한다.")
    public ResultData getPublicReadingStats(
            @Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
          , @Parameter(description = "공개 통계를 조회할 사용자 번호", example = "31") @PathVariable Long userNumb
          , @Parameter(description = "조회할 연도", example = "2026") @RequestParam(required = false) Integer readYear) {
        // 인증 사용자와 조회 대상 사이에 차단 관계가 있으면 공개 독서 통계도 제공하지 않음
        if (StringUtil.hasEmpty(loginUserNumb, userNumb) || userBlockService.isBlocked(loginUserNumb, userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 서버가 계정 상태와 공개 설정을 검증한 다른 사용자용 독서 통계를 조회함
        return readingStatisticsService.getPublicReadingStats(userNumb, readYear);
    }

    /**
     * 로그인 사용자가 다른 사용자를 차단하고 양방향 팔로우 관계를 삭제함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 차단 대상 사용자 번호
     * @return 차단 처리 결과
     */
    @PostMapping("/blocks/{userNumb}")
    @Operation(summary = "사용자 차단", description = "다른 사용자를 차단하고 두 사용자 사이의 팔로우 관계를 모두 삭제한다.")
    public ResultData setUserBlock(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                 , @Parameter(description = "차단 대상 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 인증 사용자와 대상 사용자 번호로 차단 관계를 등록함
        return userBlockService.setBlock(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자가 만든 한 방향의 사용자 차단을 해제함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 차단 해제 대상 사용자 번호
     * @return 차단 해제 처리 결과
     */
    @DeleteMapping("/blocks/{userNumb}")
    @Operation(summary = "사용자 차단 해제", description = "로그인 사용자가 직접 만든 차단 관계만 해제한다.")
    public ResultData delUserBlock(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                 , @Parameter(description = "차단 해제 대상 사용자 번호", example = "31") @PathVariable Long userNumb) {
        // 인증 사용자가 소유한 차단 방향만 삭제함
        return userBlockService.delBlock(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자가 직접 차단한 사용자 목록을 조회함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 차단 사용자 페이지
     */
    @GetMapping("/blocks")
    @Operation(summary = "차단 사용자 목록", description = "로그인 사용자가 직접 차단한 사용자만 최신 차단순으로 조회한다.")
    public ResultData getUserBlockList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                     , @Parameter(description = "조회할 페이지 번호", example = "1")
                                       @RequestParam(value = "page", defaultValue = "1") int page) {
        // 로그인 사용자가 관리할 수 있는 차단 사용자 목록을 조회함
        return userBlockService.getBlockList(loginUserNumb, page);
    }

    /**
     * 피드에서 닉네임 검색어와 로그인 사용자 관계를 기준으로 활성 사용자를 조회함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param keyword 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 관계 우선순위가 적용된 활성 사용자 페이지
     */
    @GetMapping("/users")
    @Operation(summary = "활성 사용자 검색", description = "닉네임이 검색어를 포함하는 활성 사용자를 로그인 사용자와의 관계순으로 조회한다.")
    public ResultData getUserSearchList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
          , @Parameter(description = "닉네임 검색어", example = "reader") @RequestParam String keyword
          , @Parameter(description = "조회할 페이지 번호", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page) {
        // 인증 사용자와 닉네임 검색어 및 페이지 조건을 소셜 검색 서비스에 전달함
        return socialService.getUserSearchList(loginUserNumb, keyword, page);
    }

    /**
     * 로그인 사용자의 팔로잉 목록을 조회함
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/me/following")
    @Operation(summary = "내 팔로잉 목록 조회", description = "로그인 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                       , @Parameter(description = "조회할 페이지 번호", example = "1")
                                         @RequestParam(value = "page", defaultValue = "1") int page) {
        // 로그인 사용자의 팔로잉 목록을 조회한 결과를 반환함
        return socialService.getFollowingList(loginUserNumb, loginUserNumb, page);
    }

    /**
     * 로그인 사용자의 팔로워 목록을 조회함
     * 마이페이지에서는 내 사용자 번호를 별도로 들고 있지 않으므로 인증 사용자 번호를 목록 주인으로 사용함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/me/followers")
    @Operation(summary = "내 팔로워 목록 조회", description = "로그인 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getMyFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                      , @Parameter(description = "조회할 페이지 번호", example = "1")
                                        @RequestParam(value = "page", defaultValue = "1") int page) {
        // 로그인 사용자의 팔로워 목록을 조회한 결과를 반환함
        return socialService.getFollowerList(loginUserNumb, loginUserNumb, page);
    }

    /**
     * 특정 사용자의 팔로잉 목록을 조회함
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함됨
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로잉 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/following")
    @Operation(summary = "팔로잉 목록 조회", description = "특정 사용자가 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowingList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                     , @Parameter(description = "목록 주인 사용자 번호", example = "31") @PathVariable Long userNumb
                                     , @Parameter(description = "조회할 페이지 번호", example = "1")
                                       @RequestParam(value = "page", defaultValue = "1") int page) {
        // 특정 사용자의 팔로잉 목록을 조회한 결과를 반환함
        return socialService.getFollowingList(loginUserNumb, userNumb, page);
    }

    /**
     * 특정 사용자의 팔로워 목록을 조회함
     * 목록의 각 사용자에는 현재 로그인 사용자 기준 팔로우 상태가 포함됨
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 팔로워 목록 조회 결과
     */
    @GetMapping("/profile/{userNumb}/followers")
    @Operation(summary = "팔로워 목록 조회", description = "특정 사용자를 팔로우하는 사용자 목록을 조회한다.")
    public ResultData getFollowerList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                    , @Parameter(description = "목록 주인 사용자 번호", example = "31") @PathVariable Long userNumb
                                    , @Parameter(description = "조회할 페이지 번호", example = "1")
                                      @RequestParam(value = "page", defaultValue = "1") int page) {
        // 특정 사용자의 팔로워 목록을 조회한 결과를 반환함
        return socialService.getFollowerList(loginUserNumb, userNumb, page);
    }

    /**
     * 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회함
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
        // 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회 결과를 반환함
        return socialService.getFollowStatus(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우하도록 저장함
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
        // 탈퇴 회원은 관계를 유지하되 새로운 팔로우 조작 대상에서 제외함
        if (!isActiveUser(userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 로그인 사용자가 프로필 주인을 팔로우하도록 저장 결과를 반환함
        return socialService.setFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제함
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
        // 탈퇴 회원은 기존 팔로우 관계를 유지하므로 언팔로우 조작을 허용하지 않음
        if (!isActiveUser(userNumb)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제 결과를 반환함
        return socialService.delFollow(createFollowDto(loginUserNumb, userNumb));
    }

    /**
     * 대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소함
     * TB_LIKEXX가 공용 좋아요 테이블이므로 독후감 전용 reptNumb가 아니라 TAGT_TYPE, TAGT_NUMB를 요청값으로 받음
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
        // request 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
        if (StringUtil.isEmpty(request)) {
            // 좋아요 대상과 알림 정보를 담을 객체를 생성함
            request = new SocialDto.LikeDto();
        }

        // UserNumb 업무 값을 request DTO에 설정함
        request.setUserNumb(loginUserNumb);
        // 대상 유형과 대상 번호를 기준으로 좋아요를 등록하거나 취소 결과를 반환함
        return socialService.setLike(request);
    }

    /**
     * 특정 대상에 좋아요를 등록한 활성 사용자 목록을 조회함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param tagtType 좋아요 대상 유형
     * @param tagtNumb 좋아요 대상 번호
     * @param page 조회할 페이지 번호
     * @return 활성 좋아요 사용자 목록 조회 결과
     */
    @GetMapping("/like-users")
    @Operation(summary = "좋아요 사용자 목록 조회", description = "접근 가능한 대상에 좋아요를 등록한 활성 사용자 목록을 조회한다.")
    public ResultData getLikeUserList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                    , @Parameter(description = "좋아요 대상 유형", example = "REPORT")
                                      @RequestParam(value = "tagtType") String tagtType
                                    , @Parameter(description = "좋아요 대상 번호", example = "1")
                                      @RequestParam(value = "tagtNumb") Long tagtNumb
                                    , @Parameter(description = "조회할 페이지 번호", example = "1")
                                      @RequestParam(value = "page", defaultValue = "1") int page) {
        // 접근 가능한 대상에 좋아요를 등록한 활성 사용자 목록을 반환함
        return socialService.getLikeUserList(loginUserNumb, tagtType, tagtNumb, page);
    }

    /**
     * 소셜 관계 조작 대상 회원이 정상 이용 상태인지 확인함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 상대 회원 번호
     * @return 정상 이용 회원 여부
     */
    private boolean isActiveUser(Long userNumb) {
        // 회원 번호로 현재 사용자 상태를 조회함
        UserDto user = userMapper.getUserByNumb(userNumb);
        // 존재하는 정상 이용 회원만 관계 조작 대상으로 인정함
        return !StringUtil.isEmpty(user) && Constant.USER_STAT_ACTIVE.equals(user.getUserStat());
    }

    /**
     * 팔로우 API의 경로 변수와 인증 사용자 번호를 Mapper까지 전달할 DTO로 변환함
     * 원시 파라미터를 XML에 직접 넘기지 않도록 Controller 진입점에서 요청 구조를 고정함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 상대 사용자 번호
     * @return 팔로우 요청 DTO
     */
    private SocialDto.FollowDto createFollowDto(Long loginUserNumb, Long userNumb) {
        // 팔로우 대상과 알림 정보를 담을 객체를 생성함
        SocialDto.FollowDto followDto = new SocialDto.FollowDto();
        // UserNumb 업무 값을 followDto DTO에 설정함
        followDto.setUserNumb(loginUserNumb);
        // FlowNumb 업무 값을 followDto DTO에 설정함
        followDto.setFlowNumb(userNumb);
        // 팔로우 API의 경로 변수와 인증 사용자 번호를 Mapper까지 전달할 DTO로 변환 결과를 반환함
        return followDto;
    }
}
