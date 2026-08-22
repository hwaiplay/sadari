package org.our.sadari.complaint.dto;

import lombok.Data;

/**
 * fileName       : ComplaintActionDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 누적 자동 조치 결과 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintActionDto {

    // 자동 조치 결과 번호
    private Long actnNumb;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 대상 버전 SHA-256 해시
    private String tagtHash;

    // 신고 대상 소유 사용자 번호
    private Long tagtUser;

    // 자동 조치 유형 세부코드
    private String actnType;

    // 자동 조치 결과 세부코드
    private String rsltCode;

    // 자동 조치 임계 신고 건수
    private Integer thrsCntt;

    // 자동 조치 판단 시 유효 누적 신고 건수
    private Integer cmplCntt;

    // 동일 대상 자동 조치 순번
    private Integer actnOrdr;

    // 자동 조치를 발생시킨 신고 번호
    private Long trigCmpl;

    // 자동 조치 결과 설명
    private String rsltCntn;
}
