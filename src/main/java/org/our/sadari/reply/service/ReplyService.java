package org.our.sadari.reply.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.reply.dto.ReplyDto;

/**
 * fileName       : ReplyService
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang    최초 생성
 */
public interface ReplyService {


    /**
     * 댓글을 등록한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param replyDto 댓글 정보
     * @return 등록된 댓글 처리 결과
     */
    ResultData setReply(Long userNumb, ReplyDto replyDto);


    /**
     * 독후감에 대한 댓글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param reptNumb
     * @return 독후감에 대한 댓글 조회 결과
     */
    ResultData getReplyList(Long reptNumb);
}
