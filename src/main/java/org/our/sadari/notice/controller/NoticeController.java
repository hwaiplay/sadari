package org.our.sadari.notice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.notice.service.NoticeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : NoticeController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자의 배포 공지사항 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    // 공지사항 조회 서비스
    private final NoticeService noticeService;

    @GetMapping
    public ResultData getNoticeList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @RequestParam(defaultValue = "1") int page) {
        return noticeService.getNoticeList(userNumb, page);
    }

    @GetMapping("/{notiNumb}")
    public ResultData getNoticeDtl(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @PathVariable Long notiNumb) {
        return noticeService.getNoticeDtl(userNumb, notiNumb);
    }
}
