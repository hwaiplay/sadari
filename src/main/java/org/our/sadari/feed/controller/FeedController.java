package org.our.sadari.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.feed.service.FeedService;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : FeedController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
@Tag(name = "피드", description = "팔로잉 사용자의 공개 활동 피드 API")
public class FeedController {

    // 팔로잉 피드 조회 업무 처리 서비스
    private final FeedService feedService;

    /**
     * 로그인 사용자가 팔로우하는 활성 사용자의 공개 활동 피드를 페이지 단위로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 피드 페이지 번호
     * @return 팔로잉 피드 페이지 조회 결과
     */
    @GetMapping
    @Operation(summary = "팔로잉 피드 조회")
    public ResultData getFeedList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        // 인증 사용자와 요청 페이지를 서비스에 전달해 공개 범위가 적용된 피드를 조회한다
        return feedService.getFeedList(userNumb, page);
    }
}
