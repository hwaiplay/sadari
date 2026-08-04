package org.our.sadari.alim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * fileName       : AlimController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 알림 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-08-04        OpenAI.Codex       외부 알림 발송 API 제거
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alim")
@Tag(name = "알림", description = "사용자 알림 목록과 읽음 및 삭제 상태 API")
public class AlimController {

    // Alim 업무 처리 서비스
    private final AlimService alimService;

    /**
     * 로그인 사용자의 알림 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    @GetMapping("/list")
    @Operation(summary = "내 알림 목록 조회", description = "로그인 사용자의 삭제되지 않은 알림 목록을 최신순으로 조회한다.")
    public ResultData getMyAlimList(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                  , @RequestParam(defaultValue = "1") int page) {
        // 로그인 사용자의 알림 목록을 조회 결과를 반환한다
        return alimService.getMyAlimList(loginUserNumb, page);
    }

    /**
     * 햄버거 메뉴의 알림 아이콘 오른쪽에 표시할 미읽음 알림 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    @GetMapping("/unread-count")
    @Operation(summary = "미읽음 알림 수 조회", description = "로그인 사용자의 읽지 않은 알림 수를 조회한다.")
    public ResultData getUnreadAlimCnt(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        // 햄버거 메뉴의 알림 아이콘 오른쪽에 표시할 미읽음 알림 수를 조회 결과를 반환한다
        return alimService.getUnreadAlimCnt(loginUserNumb);
    }

    /**
     * 사용자가 알림센터 항목 또는 브라우저 푸시 알림을 클릭한 경우 해당 알림 한 건을 읽음 처리한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param request 읽음 처리할 사용자별 알림 번호
     * @return 읽음 처리 후 남은 미읽음 알림 수
     */
    @PutMapping("/read-status")
    @Operation(summary = "알림 개별 읽음 처리", description = "사용자가 클릭한 알림 한 건의 읽음 여부와 읽은 일시를 갱신한다.")
    public ResultData uptAlimRead(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb
                                , @Valid @RequestBody AlimDto.AlimReadReqDto request) {
        // 사용자가 알림센터 항목 또는 브라우저 푸시 알림을 클릭한 경우 해당 알림 한 건을 읽음 처리 결과를 반환한다
        return alimService.uptAlimRead(loginUserNumb, request);
    }

    /**
     * 사용자가 모두 지우기 버튼을 누르면 아직 목록에 로드되지 않은 알림까지 모두 삭제 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @return 모두 지우기 처리 결과
     */
    @PostMapping("/delete-all")
    @Operation(summary = "알림 모두 지우기", description = "로그인 사용자의 삭제되지 않은 모든 알림을 삭제 상태로 변경한다.")
    public ResultData delAllAlim(@Parameter(hidden = true) @AuthenticationPrincipal Long loginUserNumb) {
        // 사용자가 모두 지우기 버튼을 누르면 아직 목록에 로드되지 않은 알림까지 모두 삭제 상태로 변경 결과를 반환한다
        return alimService.delAllAlim(loginUserNumb);
    }

}
