package org.our.sadari.reply.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.reply.dto.ReplyDto;

/**
 * fileName       : ReplyService
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회, 등록, 수정 및 삭제 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang    최초 생성
 * 2026-07-29        HanWon.Jang    댓글 조회 로그인 사용자 조건 추가
 * 2026-08-03        HanWon.Jang    본인 댓글 수정 및 삭제 계약 추가
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
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글의 내용을 수정한다.
     *
     * @author HanWon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param reptNumb 수정할 댓글이 속한 독후감 번호
     * @param replNumb 수정할 댓글 번호
     * @param replyDto 변경할 댓글 내용
     * @return 수정된 댓글 번호를 포함한 처리 결과
     */
    ResultData uptReply(Long userNumb, Long reptNumb, Long replNumb, ReplyDto replyDto);


    /**
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글을 삭제 상태로 전환한다.
     *
     * @author HanWon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param reptNumb 삭제할 댓글이 속한 독후감 번호
     * @param replNumb 삭제할 댓글 번호
     * @return 삭제된 댓글 번호를 포함한 처리 결과
     */
    ResultData delReply(Long userNumb, Long reptNumb, Long replNumb);


    /**
     * 독후감에 대한 댓글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 댓글 목록을 조회하는 로그인 사용자 번호
     * @param reptNumb 댓글 목록을 조회할 독후감 번호
     * @return 독후감에 대한 댓글 조회 결과
     */
    ResultData getReplyList(Long userNumb, Long reptNumb);
}
