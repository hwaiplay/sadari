package org.our.sadari.timer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.timer.dto.ReadingTimerDto;
import org.our.sadari.timer.service.ReadingTimerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ReadingTimerController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 실행과 주간 출석 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-20        SeungHyeon.Kang    도서별 누적시간 페이지 조회 분리
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-timer")
@Tag(name = "독서 타이머", description = "독서 타이머 세션과 주간 출석 API")
public class ReadingTimerController {

    // 독서 타이머 업무 처리 서비스
    private final ReadingTimerService readingTimerService;

    /**
     * 현재 타이머와 이번 주 출석 현황을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 타이머 화면 요약 데이터
     */
    @GetMapping("/summary")
    @Operation(summary = "독서 타이머 요약 조회", description = "현재 세션, 일별 독서 시간과 주간 출석을 조회한다.")
    public ResultData getTimerSummary(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {

        // 로그인 사용자의 타이머 화면 요약 데이터를 반환함
        return readingTimerService.getTimerSummary(userNumb);
    }

    /**
     * 도서별 누적 독서 시간을 최근 완료 기록순으로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 현재 페이지 도서별 누적시간과 다음 페이지 여부
     */
    @GetMapping("/book-times")
    @Operation(summary = "도서별 누적 독서 시간 조회", description = "최근 완료 기록순으로 한 페이지에 20권씩 조회한다.")
    public ResultData getBookTimePage(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                    , @RequestParam(defaultValue = "1") int page) {

        // 로그인 사용자의 도서별 누적 독서 시간 페이지를 반환함
        return readingTimerService.getBookTimePage(userNumb, page);
    }

    /**
     * 새 독서 타이머 세션을 시작함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 선택한 도서 정보
     * @return 시작 후 타이머 화면 요약 데이터
     */
    @PostMapping("/sessions")
    @Operation(summary = "독서 타이머 시작", description = "선택한 읽는 중 도서와 연결하거나 도서 없이 새 세션을 시작한다.")
    public ResultData setTimer(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                              , @RequestBody(required = false) ReadingTimerDto.Request request) {

        // 중복 시작을 방지하며 새 독서 타이머를 시작한 결과를 반환함
        return readingTimerService.setTimer(userNumb, request);
    }

    /**
     * 독서 타이머 세션을 재개, 일시정지 또는 완료 처리함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tmrxNumb 변경할 세션 번호
     * @param request 변경할 타이머 상태
     * @return 변경 후 타이머 화면 요약 데이터
     */
    @PatchMapping("/sessions/{tmrxNumb}")
    @Operation(summary = "독서 타이머 상태 변경", description = "사용자 소유 세션을 재개, 일시정지 또는 완료 처리한다.")
    public ResultData uptTimer(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                              , @Parameter(description = "독서 타이머 세션 번호", example = "1") @PathVariable Long tmrxNumb
                              , @RequestBody ReadingTimerDto.Request request) {

        // 서버 시간으로 독서 시간을 확정하고 상태 변경 결과를 반환함
        return readingTimerService.uptTimer(userNumb, tmrxNumb, request);
    }
}
