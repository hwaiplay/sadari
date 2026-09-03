package org.our.sadari.inquiry.dto;

import lombok.Data;

/**
 * fileName       : InquiryCreateDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의 등록값을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryCreateDto {

    // 고객문의 카테고리 코드
    private String inqrCatg;
    // 고객문의 제목
    private String inqrTitl;
    // 고객문의 내용
    private String inqrCntn;
}
