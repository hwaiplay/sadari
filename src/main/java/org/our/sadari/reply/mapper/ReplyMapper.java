package org.our.sadari.reply.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.reply.dto.ReplyDto;

/**
 * fileName       : ReplyMapper
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-28        Hanwon.Jang        댓글 조회 및 등록 메서드 정의
 */
@Mapper
public interface ReplyMapper {

    /**
     * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param replyDto 조회할 독후감 번호를 포함한 댓글 조건
     * @return 독후감 댓글과 답글 목록
     */
    List<ReplyDto> getReplyList(ReplyDto replyDto);

    /**
     * 댓글 또는 답글을 TB_REPLXX에 등록한다.
     *
     * @author Hanwon.Jang
     * @param replyDto 등록할 댓글 또는 답글 정보
     * @return 반영 건수
     */
    int setReply(ReplyDto replyDto);
}
