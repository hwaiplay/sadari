package org.our.sadari.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.complaint.dto.ComplaintDto;

/**
 * fileName       : ComplaintMapper
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 독후감과 댓글 신고 대상 및 접수 데이터에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface ComplaintMapper {

    /**
     * 신고자의 계정 상태를 검증하고 동시 중복 접수를 직렬화하도록 회원 행을 잠근다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @return 활성 신고자 사용자 번호 또는 null
     */
    Long getActiveUserNumbForUpdate(@Param("userNumb") Long userNumb);

    /**
     * 사용 가능한 신고 사유 세부코드인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param cmplRson 신고 사유 세부코드
     * @return 사용 가능한 코드 개수
     */
    int getComplaintReasonCnt(@Param("cmplRson") String cmplRson);

    /**
     * 공개 상태인 독후감의 작성자 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 독후감 번호
     * @return 독후감 작성자 번호 또는 null
     */
    Long getReportOwnerNumb(@Param("tagtNumb") Long tagtNumb);

    /**
     * 공개 독후감에 등록된 미삭제 댓글의 작성자 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 댓글 번호
     * @return 댓글 작성자 번호 또는 null
     */
    Long getReplyOwnerNumb(@Param("tagtNumb") Long tagtNumb);

    /**
     * 동일 사용자가 동일 대상을 신고한 이력이 있는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 중복 여부를 확인할 신고값
     * @return 기존 신고 개수
     */
    int getDupComplaintCnt(ComplaintDto complaint);

    /**
     * 검증된 독후감 또는 댓글 신고를 접수 상태로 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 저장할 신고 데이터
     * @return 저장된 행 개수
     */
    int setComplaint(ComplaintDto complaint);
}
