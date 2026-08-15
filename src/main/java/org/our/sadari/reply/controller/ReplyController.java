package org.our.sadari.reply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.reply.dto.ReplyDto;
import org.our.sadari.reply.service.ReplyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ReplyController
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회, 등록, 수정, 삭제 및 좋아요 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-29        HanWon.Jang        댓글 조회 시 로그인 사용자 번호 전달
 * 2026-08-03        HanWon.Jang        본인 댓글 수정 및 삭제 API 추가
 * 2026-08-03        HanWon.Jang        댓글 좋아요 등록 및 취소 API 추가
 * 2026-08-11        SeungHyeon.Kang    다중 탭 댓글 수정 충돌 409 응답 추가
 * 2026-08-15        SeungHyeon.Kang    부모 댓글 페이지 조회 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reply")
@Tag(name = "댓글", description = "독후감 댓글과 답글 조회, 등록, 수정 및 삭제 API")
public class ReplyController {

    // Reply 업무 처리 서비스
    private final ReplyService replyService;

    /**
     * 로그인 사용자가 작성한 댓글 또는 답글을 등록한다.
     *
     * @author Hanwon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param request 등록할 댓글 또는 답글 정보
     * @return 등록된 댓글 번호를 포함한 처리 결과
     */
    @PostMapping
    @Operation(summary = "댓글 등록", description = "로그인 사용자가 독후감에 댓글 또는 답글을 등록한다.")
    public ResultData setReply(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                             , @Valid @RequestBody ReplyDto request) {
        // 로그인 사용자 번호와 검증할 댓글 정보를 서비스에 전달한 등록 결과를 반환한다
        return replyService.setReply(userNumb, request);
    }

    /**
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글의 내용을 수정한다.
     *
     * @author HanWon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reptNumb 수정할 댓글이 속한 독후감 번호
     * @param replNumb 수정할 댓글 번호
     * @param request 변경할 댓글 내용
     * @param response 수정 충돌 HTTP 상태를 기록할 응답 객체
     * @return 수정된 댓글 번호를 포함한 처리 결과
     */
    @PutMapping("/{reptNumb}/{replNumb}")
    @Operation(summary = "댓글 수정", description = "로그인 사용자가 직접 작성한 미삭제 댓글 또는 답글을 수정한다.")
    public ResultData uptReply(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                             , @Parameter(description = "독후감 번호", example = "1") @PathVariable Long reptNumb
                             , @Parameter(description = "수정할 댓글 번호", example = "10") @PathVariable Long replNumb
                             , @Valid @RequestBody ReplyDto request
                             , @Parameter(hidden = true) HttpServletResponse response) {
        // 원본 버전을 포함한 댓글 수정 결과를 조회한다
        ResultData result = replyService.uptReply(userNumb, reptNumb, replNumb, request);
        // 다른 탭이나 기기의 선행 수정이 확인되면 표준 충돌 상태로 응답한다
        if (result.getCode() == ResultEnum.COMMON_EDIT_CONFLICT.getCode()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        // 경로에서 확정한 댓글 식별값과 변경 내용을 서비스에 전달한 수정 결과를 반환한다
        return result;
    }

    /**
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글을 삭제 상태로 전환한다.
     *
     * @author HanWon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reptNumb 삭제할 댓글이 속한 독후감 번호
     * @param replNumb 삭제할 댓글 번호
     * @return 삭제된 댓글 번호를 포함한 처리 결과
     */
    @DeleteMapping("/{reptNumb}/{replNumb}")
    @Operation(summary = "댓글 삭제", description = "로그인 사용자가 직접 작성한 미삭제 댓글 또는 답글을 삭제 상태로 전환한다.")
    public ResultData delReply(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                             , @Parameter(description = "독후감 번호", example = "1") @PathVariable Long reptNumb
                             , @Parameter(description = "삭제할 댓글 번호", example = "10") @PathVariable Long replNumb) {
        // 경로에서 확정한 댓글 식별값을 서비스에 전달한 삭제 결과를 반환한다
        return replyService.delReply(userNumb, reptNumb, replNumb);
    }

    /**
     * 로그인 사용자의 미삭제 댓글 좋아요를 등록한다.
     *
     * @author HanWon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reptNumb 좋아요 대상 댓글이 속한 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 좋아요 상태와 좋아요 수
     */
    @PutMapping("/{reptNumb}/{replNumb}/likes")
    @Operation(summary = "댓글 좋아요 등록", description = "정상 이용 중인 로그인 사용자가 미삭제 댓글에 좋아요를 등록한다.")
    public ResultData setReplyLike(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Parameter(description = "독후감 번호", example = "1") @PathVariable Long reptNumb
                                 , @Parameter(description = "좋아요 대상 댓글 번호", example = "10") @PathVariable Long replNumb) {
        // 인증 사용자와 댓글 복합 식별값을 서비스에 전달한 좋아요 등록 결과를 반환한다
        return replyService.setReplyLike(userNumb, reptNumb, replNumb);
    }

    /**
     * 로그인 사용자의 미삭제 댓글 좋아요를 취소한다.
     *
     * @author HanWon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reptNumb 좋아요 대상 댓글이 속한 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 좋아요 상태와 좋아요 수
     */
    @DeleteMapping("/{reptNumb}/{replNumb}/likes")
    @Operation(summary = "댓글 좋아요 취소", description = "정상 이용 중인 로그인 사용자가 미삭제 댓글의 좋아요를 취소한다.")
    public ResultData delReplyLike(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Parameter(description = "독후감 번호", example = "1") @PathVariable Long reptNumb
                                 , @Parameter(description = "좋아요 대상 댓글 번호", example = "10") @PathVariable Long replNumb) {
        // 인증 사용자와 댓글 복합 식별값을 서비스에 전달한 좋아요 취소 결과를 반환한다
        return replyService.delReplyLike(userNumb, reptNumb, replNumb);
    }

    /**
     * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb Spring Security에서 주입한 로그인 사용자 번호
     * @param reptNumb 댓글 목록을 조회할 독후감 번호
     * @param page 조회할 부모 댓글 페이지 번호
     * @return 독후감 댓글과 답글 목록 조회 결과
     */
    @GetMapping("/{reptNumb}")
    @Operation(summary = "댓글 목록 조회", description = "독후감 번호에 연결된 댓글과 답글 목록을 조회한다.")
    public ResultData getReplyList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Parameter(description = "댓글 목록을 조회할 독후감 번호", example = "1")
                                   @PathVariable Long reptNumb
                                 , @Parameter(description = "조회할 부모 댓글 페이지 번호", example = "1")
                                   @RequestParam(value = "page", defaultValue = "1") int page) {
        // 로그인 사용자 번호를 포함한 댓글과 답글 목록 조회 결과를 반환한다
        return replyService.getReplyList(userNumb, reptNumb, page);
    }
}
