package org.our.sadari.complaint.service;

import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : ComplaintService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 사용자 신고 접수 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
public interface ComplaintService {

    /**
     * 신고 대상 원문을 확인한 뒤 접수 시점 스냅샷과 신고 사유를 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고하는 인증 사용자 번호
     * @param complaintCreateDto 신고 대상과 사유
     * @return 접수된 신고 번호
     */
    ResultData setComplaint(Long userNumb, ComplaintCreateDto complaintCreateDto);
}
