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
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
public interface InquiryService {

    ResultData getInquiryList(Long userNumb, int page);

    ResultData getInquiryDtl(Long userNumb, Long inqrNumb);

    ResultData setInquiry(Long userNumb, InquiryCreateDto inquiryCreateDto);
}
