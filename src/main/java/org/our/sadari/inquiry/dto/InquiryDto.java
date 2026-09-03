package org.our.sadari.inquiry.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * fileName       : InquiryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의와 처리 상태 및 답변을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryDto {

    // 고객문의 번호
    private Long inqrNumb;
    // 고객문의 카테고리 코드
    private String inqrCatg;
    // 고객문의 카테고리명
    private String inqrCatgName;
    // 고객문의 제목
    private String inqrTitl;
    // 고객문의 내용
    private String inqrCntn;
    // 고객문의 처리 상태 코드
    private String inqrStat;
    // 고객문의 처리 상태명
    private String inqrStatName;
    // 연결된 이용정지 이력 번호
    private Long spndNumb;
    // 읽지 않은 답변 개수
    private Integer unreadCount;
    // 문의 등록 일시
    private LocalDateTime regiDate;
    // 최종 답변 일시
    private LocalDateTime answDate;
    // 관리자 답변 목록
    private List<InquiryAnswerDto> answers;
}
