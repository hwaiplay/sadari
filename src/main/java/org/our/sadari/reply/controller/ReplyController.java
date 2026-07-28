package org.our.sadari.reply.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.reply.dto.ReplyDto;
import org.our.sadari.reply.service.ReplyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ReplyController
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회 및 등록 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reply")
@Tag(name = "댓글", description = "독후감 댓글과 답글 조회 및 등록 API")
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
     * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param reptNumb 댓글 목록을 조회할 독후감 번호
     * @return 독후감 댓글과 답글 목록 조회 결과
     */
    @GetMapping("/{reptNumb}")
    @Operation(summary = "댓글 목록 조회", description = "독후감 번호에 연결된 댓글과 답글 목록을 조회한다.")
    public ResultData getReplyList(
            @Parameter(description = "댓글 목록을 조회할 독후감 번호", example = "1") @PathVariable Long reptNumb) {
        // 독후감 번호에 연결된 댓글과 답글 목록 조회 결과를 반환한다
        return replyService.getReplyList(reptNumb);
    }
}
