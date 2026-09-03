package org.our.sadari.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.notice.service.NoticeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : NoticeController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자의 배포 공지사항 조회 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-19        SeungHyeon.Kang    홈 미읽음 공지 제목 조회 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
@Tag(name = "공지사항", description = "활성 사용자의 배포 공지 목록과 미읽음 제목 및 상세 조회 API")
public class NoticeController {

    // 공지사항 조회 서비스
    private final NoticeService noticeService;

    /**
     * 현재 배포 중인 공지사항을 페이지 단위로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 배포 공지사항 목록과 다음 페이지 여부
     */
    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "활성 사용자가 현재 배포 중인 공지사항을 최근 배포 순서로 조회한다.")
    public ResultData getNoticeList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                  , @Parameter(description = "조회할 페이지 번호", example = "1")
                                    @RequestParam(defaultValue = "1") int page) {
        // 로그인 사용자와 페이지 번호에 해당하는 배포 공지 목록을 조회함
        return noticeService.getNoticeList(userNumb, page);
    }

    /**
     * 홈 화면에 표시할 로그인 사용자의 미읽음 공지 제목을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 현재 배포 중인 미읽음 공지 제목 목록
     */
    @GetMapping("/unread")
    @Operation(summary = "홈 미읽음 공지 조회", description = "활성 사용자의 읽음 이력이 없는 현재 배포 공지 번호와 제목을 조회한다.")
    public ResultData getUnreadNoticeList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 홈 제목 슬라이드에 사용할 로그인 사용자의 미읽음 공지 목록을 조회함
        return noticeService.getUnreadNoticeList(userNumb);
    }

    /**
     * 공지사항 주키에 해당하는 현재 배포 버전 상세를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param notiNumb 조회할 공지사항 주키
     * @return 현재 배포 중인 공지사항 상세
     */
    @GetMapping("/{notiNumb}")
    @Operation(summary = "공지사항 상세 조회", description = "활성 사용자가 현재 배포 공지 상세와 기존 읽음 여부를 조회한다.")
    public ResultData getNoticeDtl(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Parameter(description = "조회할 공지사항 주키", example = "1")
                                   @PathVariable Long notiNumb) {
        // 로그인 사용자가 선택한 현재 배포 공지 상세와 기존 읽음 여부를 조회함
        return noticeService.getNoticeDtl(userNumb, notiNumb);
    }

    /**
     * 공지사항 주키에 해당하는 현재 배포 버전의 읽음 이력을 저장함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param notiNumb 읽은 공지사항 주키
     * @return 읽음 이력 저장 결과
     */
    @PostMapping("/{notiNumb}/views")
    @Operation(summary = "공지사항 읽음 처리", description = "활성 사용자가 현재 배포 공지를 읽은 이력을 CSRF 보호 요청으로 저장한다.")
    public ResultData setNoticeView(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                  , @Parameter(description = "읽은 공지사항 주키", example = "1")
                                    @PathVariable Long notiNumb) {
        // 상태 변경 요청으로 분리한 현재 배포 공지 읽음 이력을 저장함
        return noticeService.setNoticeView(userNumb, notiNumb);
    }
}
