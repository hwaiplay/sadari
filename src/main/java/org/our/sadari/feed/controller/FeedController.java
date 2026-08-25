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
 * author         : Codex
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        Codex              최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feed")
@Tag(name = "피드", description = "팔로잉 사용자의 공개 활동 피드 API")
public class FeedController {

    private final FeedService feedService;

    /** 로그인 사용자의 팔로잉 피드를 조회한다. */
    @GetMapping
    @Operation(summary = "팔로잉 피드 조회")
    public ResultData getFeedList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return feedService.getFeedList(userNumb, page);
    }
}
