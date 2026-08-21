package org.our.sadari.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.complaint.dto.ComplaintDto;

/**
 * fileName       : ComplaintMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 원문을 조회하고 신고 접수 이력을 저장한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·중복 및 대상 소유자 조회
 */
@Mapper
public interface ComplaintMapper {

    /**
     * 신고 접수 가능 여부를 판단할 현재 회원 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @return 현재 회원 상태 코드
     */
    String getUserStat(@Param("userNumb") Long userNumb);

    /**
     * 요청한 신고 대상 유형 또는 사유가 활성 공통코드인지 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param commCode 공통코드
     * @param comdCode 세부코드
     * @return 활성 코드 건수
     */
    int getActiveCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 동일 사용자가 같은 대상을 신고한 이력이 있는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @return 동일 대상 신고 건수
     */
    int dupComplaint(@Param("userNumb") Long userNumb, @Param("tagtType") String tagtType
                   , @Param("tagtNumb") Long tagtNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 프로필 내용과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 사용자 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 프로필 내용과 소유자
     */
    ComplaintDto getUserTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 공개 독후감 본문과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 독후감 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 독후감 본문과 소유자
     */
    ComplaintDto getReportTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 공개 독후감 댓글 본문과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 댓글 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 댓글 본문과 소유자
     */
    ComplaintDto getReplyTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 서버에서 확정한 대상 내용과 신고 사유를 접수 이력으로 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 신고 대상과 사유 및 내용 스냅샷
     * @param userNumb 신고자 사용자 번호
     * @return 저장된 신고 행 수
     */
    int setComplaint(@Param("complaint") ComplaintDto complaint, @Param("userNumb") Long userNumb);
}
