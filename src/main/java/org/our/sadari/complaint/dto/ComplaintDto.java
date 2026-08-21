package org.our.sadari.complaint.dto;

import lombok.Data;

/**
 * fileName       : ComplaintDto
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 사용자 신고 접수 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintDto {

    // 신고 번호
    private Long cmplNumb;
    // 신고자 사용자 번호
    private Long userNumb;
    // 신고 대상 유형 세부코드
    private String tagtType;
    // 신고 대상 번호
    private Long tagtNumb;
    // 신고 사유 세부코드
    private String cmplRson;
    // 신고 상세 내용
    private String cmplCntn;
}
