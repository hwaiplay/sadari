package org.our.sadari.inquiry.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.inquiry.dto.InquiryCreateDto;

/**
 * fileName       : InquiryService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의 접수와 조회 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성 및 정지 문의 계약
 */
public interface InquiryService {

    /**
     * 현재 이용정지 이후 접수한 최신 이의제기 문의 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 인증 사용자 번호
     * @return 현재 이용정지에 연결된 최신 문의 번호 응답
     */
    ResultData getSuspInquiryNumb(Long userNumb);

    ResultData getInquiryList(Long userNumb, int page);

    ResultData getInquiryDtl(Long userNumb, Long inqrNumb);

    ResultData setInquiry(Long userNumb, InquiryCreateDto inquiryCreateDto);
}
