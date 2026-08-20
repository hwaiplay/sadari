package org.our.sadari.reply.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.reply.dto.ReplyDto;

/**
 * fileName       : ReplyService
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회, 등록, 수정, 삭제 및 좋아요 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang    최초 생성
 * 2026-07-29        HanWon.Jang    댓글 조회 로그인 사용자 조건 추가
 * 2026-08-03        Hanwon.Jang    댓글 수정·삭제·좋아요 계약
 * 2026-08-04        HanWon.Jang    댓글 및 대댓글 좋아요 알림 계약 추가
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
     * 정상 이용 중인 로그인 사용자의 댓글 좋아요를 등록하고 신규 좋아요일 때 해당 댓글 작성자에게 알림을 생성한다.
     *
     * @author HanWon.Jang
     * @param userNumb 좋아요를 등록할 사용자 번호
     * @param reptNumb 좋아요 대상 댓글의 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 댓글 좋아요 상태와 좋아요 수
     */
    ResultData setReplyLike(Long userNumb, Long reptNumb, Long replNumb);


    /**
     * 정상 이용 중인 로그인 사용자의 댓글 좋아요를 취소한다.
     *
     * @author HanWon.Jang
     * @param userNumb 좋아요를 취소할 사용자 번호
     * @param reptNumb 좋아요 대상 댓글의 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 댓글 좋아요 상태와 좋아요 수
     */
    ResultData delReplyLike(Long userNumb, Long reptNumb, Long replNumb);


    /**
     * 독후감에 대한 댓글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 댓글 목록을 조회하는 로그인 사용자 번호
     * @param reptNumb 댓글 목록을 조회할 독후감 번호
     * @param page 조회할 부모 댓글 페이지 번호
     * @return 독후감에 대한 댓글 조회 결과
     */
    ResultData getReplyList(Long userNumb, Long reptNumb, int page);
}
