package org.our.sadari.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.feed.service.FeedService;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : FeedController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 본인과 팔로잉 피드 조회 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 * 2026-08-27        SeungHyeon.Kang         알림 이동 대상 단건 조회 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
@Tag(name = "피드", description = "본인과 팔로잉 사용자의 공개 활동 피드 API")
public class FeedController {

    // 본인과 팔로잉 피드 조회 업무 처리 서비스
    private final FeedService feedService;

    /**
     * 로그인 사용자 본인과 팔로우하는 활성 사용자의 공개 활동 피드를 페이지 단위로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 피드 페이지 번호
     * @return 본인과 팔로잉 피드 페이지 조회 결과
     */
    @GetMapping
    @Operation(summary = "팔로잉 피드 조회")
    public ResultData getFeedList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        // 인증 사용자와 요청 페이지를 서비스에 전달해 공개 범위가 적용된 피드를 조회함
        return feedService.getFeedList(userNumb, page);
    }

    /**
     * 알림 링크가 지정한 현재 공개 피드 대상을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tagtType 조회할 피드 대상 유형
     * @param tagtNumb 조회할 피드 대상 번호
     * @return 알림 이동 대상 피드 항목 조회 결과
     */
    @GetMapping("/items/{tagtType}/{tagtNumb}")
    @Operation(summary = "알림 이동 대상 피드 조회")
    public ResultData getFeedDtl(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
            @PathVariable String tagtType,
            @PathVariable Long tagtNumb) {
        // 인증 사용자와 알림 링크의 대상 식별값을 서비스에 전달해 현재 공개 상태를 검증함
        return feedService.getFeedDtl(userNumb, tagtType, tagtNumb);
    }
}
