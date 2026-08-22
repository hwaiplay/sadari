package org.our.sadari.complaint.dto;

import lombok.Data;

/**
 * fileName       : ComplaintDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 접수 시 저장할 대상 스냅샷과 사유를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·자동 조치 대상 정보 추가
 */
@Data
public class ComplaintDto {

    // 신고 번호
    private Long cmplNumb;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 대상 소유 사용자 번호
    private Long tagtUser;

    // 신고 대상 내용 스냅샷
    private String tagtCntn;

    // 프로필 사진 신고 대상의 파일 번호
    private Long fileNumb;

    // 신고 사유 세부코드
    private String cmplRson;

    // 신고 상세 내용
    private String cmplCntn;
}
