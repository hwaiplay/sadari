package org.our.sadari.inquiry.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : InquiryAnswerDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 고객문의에 등록된 관리자 답변을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryAnswerDto {

    // 고객문의 답변 번호
    private Long answNumb;
    // 관리자 답변 내용
    private String answCntn;
    // 사용자 읽음 여부
    private String readYsno;
    // 답변 등록 일시
    private LocalDateTime regiDate;
}
