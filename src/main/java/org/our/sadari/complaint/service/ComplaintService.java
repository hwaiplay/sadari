package org.our.sadari.complaint.service;

import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : ComplaintService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 사용자 신고 접수 업무 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 * 2026-08-24        HanWon.Jang        신고 결과 확인 추가
 */
public interface ComplaintService {

    /**
     * 신고 대상 원문을 확인한 뒤 접수 시점 스냅샷과 신고 사유를 저장함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고하는 인증 사용자 번호
     * @param complaintCreateDto 신고 대상과 사유
     * @return 접수된 신고 번호
     */
    ResultData setComplaint(Long userNumb, ComplaintCreateDto complaintCreateDto);

    /**
     * 활성 사용자가 아직 확인하지 않은 신고 조치 결과 건수와 마지막 번호를 조회함
     *
     * @author HanWon.Jang
     * @param userNumb 인증 사용자 번호
     * @return 미확인 신고 조치 결과 요약
     */
    ResultData getPendingResultDtl(Long userNumb);

    /**
     * 활성 사용자가 팝업에서 확인한 시점까지의 신고 조치 결과를 확인 처리함
     *
     * @author HanWon.Jang
     * @param userNumb 인증 사용자 번호
     * @param resultNumb 조회 시점의 마지막 신고 조치 결과 번호
     * @return 확인 처리 결과
     */
    ResultData uptResultConfirm(Long userNumb, Long resultNumb);
}
