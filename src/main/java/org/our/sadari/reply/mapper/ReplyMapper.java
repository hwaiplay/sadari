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
 * 2026-07-28        Hanwon.Jang        최초 생성 및 댓글 조회·등록 정의
 * 2026-07-29        HanWon.Jang        댓글 알림 수신자 조회 메서드 정의
 * 2026-08-03        Hanwon.Jang        댓글 수정·삭제·좋아요 정의
 * 2026-08-04        HanWon.Jang        댓글 좋아요 알림 수신자 조회 메서드 정의
 * 2026-08-21        SeungHyeon.Kang    독후감 댓글 알림 설정 조회 추가
 * 2026-08-25        HanWon.Jang        범용 댓글 알림 조회 반영
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

    /**
     * 정상 이용 중인 로그인 사용자가 작성한 미삭제 댓글 내용을 수정한다.
     *
     * @author HanWon.Jang
     * @param replyDto 독후감, 댓글, 작성자 번호와 변경할 내용
     * @return 반영 건수
     */
    int uptReply(ReplyDto replyDto);

    /**
     * 정상 이용 중인 로그인 사용자가 작성한 미삭제 댓글을 삭제 상태로 전환한다.
     *
     * @author HanWon.Jang
     * @param replyDto 독후감, 댓글 및 작성자 번호
     * @return 반영 건수
     */
    int delReply(ReplyDto replyDto);

    /**
     * 정상 이용 회원이 접근할 수 있는 미삭제 댓글과 해당 댓글 작성자를 조회한다.
     *
     * @author HanWon.Jang
     * @param replyDto 로그인 사용자와 댓글 복합 식별값
     * @return 접근 가능한 댓글과 알림 수신자 정보
     */
    ReplyDto getReplyLikeTarget(ReplyDto replyDto);

    /**
     * 로그인 사용자의 댓글 좋아요를 중복 없이 등록한다.
     *
     * @author HanWon.Jang
     * @param replyDto 로그인 사용자와 댓글 복합 식별값
     * @return 반영 건수
     */
    int setReplyLike(ReplyDto replyDto);

    /**
     * 로그인 사용자의 댓글 좋아요를 취소한다.
     *
     * @author HanWon.Jang
     * @param replyDto 로그인 사용자와 댓글 복합 식별값
     * @return 반영 건수
     */
    int delReplyLike(ReplyDto replyDto);

    /**
     * 댓글 좋아요 수와 로그인 사용자의 좋아요 여부를 조회한다.
     *
     * @author HanWon.Jang
     * @param replyDto 로그인 사용자와 댓글 복합 식별값
     * @return 댓글 좋아요 상태
     */
    ReplyDto getReplyLikeDtl(ReplyDto replyDto);

    /**
     * 댓글이 등록된 대상의 소유자와 대상별 댓글 알림 정보를 조회한다.
     *
     * @author HanWon.Jang
     * @param replyDto 댓글 대상 유형과 대상 번호
     * @return 대상 소유자와 댓글 알림 정보
     */
    ReplyDto getReplyReportAlimDtl(ReplyDto replyDto);
}
