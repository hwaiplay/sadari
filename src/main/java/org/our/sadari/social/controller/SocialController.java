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
import org.our.sadari.social.service.SocialService;
import org.our.sadari.report.service.ReportService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 다른 사용자의 공개 프로필과 독서 활동 정보를 조회하는 Controller입니다.
 * 공개 독후감 목록에서 작성자 프로필로 이동할 때 사용하는 읽기 전용 API만 제공합니다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
@Tag(name = "소셜", description = "타인 프로필 조회와 팔로우/언팔로우 API")
public class SocialController {

    private final UserMapper userMapper;
    private final ReportService reportService;
    private final SocialService socialService;

    /**
     * 사용자 번호로 공개 프로필 정보를 조회합니다.
     * 마이페이지와 동일한 화면 구성을 사용할 수 있도록 프로필 사진, 배경 사진, 닉네임, 한줄 소개를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 공개 프로필 조회 결과
     */
    @GetMapping("/profile/{userNumb}")
    @Operation(summary = "타인 프로필 조회", description = "사용자 번호로 공개 프로필 정보를 조회한다.")
    public ResultData getSocialProfile(@Parameter(description = "조회할 사용자 번호", example = "31")
                                       @PathVariable Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        // 존재하지 않는 사용자 번호로 접근한 경우 화면에서 빈 프로필을 그리지 않도록 공통 실패 응답을 반환합니다.
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
     * 사용자 번호로 주간, 월간, 연간 독서 활동 요약을 조회합니다.
     * 목표 달성 횟수와 현재 기간의 완료 독후감 목록까지 마이페이지와 같은 기준으로 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 독서 활동 요약 조회 결과
     */
    @GetMapping("/profile/{userNumb}/reading-summary")
    @Operation(summary = "타인 독서 요약 조회", description = "사용자 번호로 공개 프로필의 독서 활동 요약을 조회한다.")
    public ResultData getSocialReadingSummary(@Parameter(description = "조회할 사용자 번호", example = "31")
                                              @PathVariable Long userNumb) {
        UserDto user = userMapper.getUserByNumb(userNumb);

        // 활동 요약 조회 전에 사용자 존재 여부를 먼저 확인해 의미 없는 집계를 방지합니다.
        if (StringUtil.isEmpty(user)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        return reportService.getMonthlyReadingSummary(userNumb);
    }

    /**
     * 로그인 사용자와 프로필 주인 사이의 팔로우 버튼명을 조회합니다.
     * 실제 버튼명 결정은 Oracle 함수 FN_GET_FOLW_STAT을 사용하는 SocialService에서 처리합니다.
     *
     * @author Seunghyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 프로필 주인 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    @GetMapping("/profile/{userNumb}/follow-status")
    @Operation(summary = "팔로우 버튼 상태 조회", description = "로그인 사용자와 상대 사용자 관계를 기준으로 팔로우, 맞팔로우, 팔로잉 버튼명을 조회한다.")
    public ResultData getFollowStatus(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb,
                                      @Parameter(description = "상대 사용자 번호", example = "31")
                                      @PathVariable Long userNumb) {
        return socialService.getFollowStatus(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우하도록 저장합니다.
     * 저장 후 화면이 즉시 최신 버튼명을 사용할 수 있도록 갱신된 팔로우 상태를 반환합니다.
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
        return socialService.setFollow(loginUserNumb, userNumb);
    }

    /**
     * 로그인 사용자가 프로필 주인을 팔로우 중인 관계를 삭제합니다.
     * 상대가 나를 팔로우한 역방향 관계는 유지되므로 삭제 후 맞팔로우 버튼명이 나올 수 있습니다.
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
        return socialService.delFollow(loginUserNumb, userNumb);
    }
}
