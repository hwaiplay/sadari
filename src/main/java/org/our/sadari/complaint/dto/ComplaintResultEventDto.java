package org.our.sadari.complaint.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ComplaintResultEventDto
 * author         : HanWon.Jang
 * date           : 2026-08-24
 * description    : 하나의 신고 조치로 생성되는 사용자 안내 이벤트를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-24        HanWon.Jang        최초 생성
 */
@Data
public class ComplaintResultEventDto {

    // 신고 조치 안내 이벤트 번호
    private Long evntNumb;
    // 자동 조치 결과 번호
    private Long actnNumb;
    // 조치 기준 신고 번호
    private Long trigCmpl;
    // 신고 대상 유형 세부코드
    private String tagtType;
    // 신고 대상 번호
    private Long tagtNumb;
    // 신고 대상 버전 해시
    private String tagtHash;
    // 피신고 사용자 번호
    private Long tagtUser;
    // 신고 대상 유형 표시명
    private String tagtName;
    // 서로 다른 신고 사유 수
    private Integer rsonCntt;
    // 피신고자 신고 유형 요약 코드
    private String rsonSumm;
    // 단일 신고 사유 세부코드
    private String rsonCode;
    // 피신고자 신고 유형 표시명
    private String rsonName;
    // 신고 조치 유형 코드
    private String actnType;
    // 신고자용 처리 결과 내용
    private String rptrCntn;
    // 피신고자용 조치 안내 내용
    private String tgtrCntn;
    // 신고 조치 완료 일시
    private LocalDateTime procDate;
}
