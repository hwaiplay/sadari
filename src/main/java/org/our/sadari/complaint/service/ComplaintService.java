package org.our.sadari.complaint.service;

import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : ComplaintService
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 독후감과 댓글 신고 접수 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
public interface ComplaintService {

    /**
     * 활성 회원의 독후감 또는 댓글 신고를 검증하여 접수한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param request 신고 대상과 사유 입력값
     * @return 접수된 신고 번호 응답
     */
    ResultData setComplaint(Long userNumb, ComplaintCreateDto request);
}
