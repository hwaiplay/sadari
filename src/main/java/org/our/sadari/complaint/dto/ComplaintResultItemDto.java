package org.our.sadari.complaint.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ComplaintResultItemDto
 * author         : HanWon.Jang
 * date           : 2026-08-24
 * description    : 신고자 또는 피신고자가 팝업에서 확인할 조치 결과 한 건을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-24        HanWon.Jang        최초 생성
 */
@Data
public class ComplaintResultItemDto {

    // 신고 조치 결과 확인 번호
    private Long rsltNumb;
    // 수신자 유형 코드
    private String rcvrType;
    // 신고자에게만 표시할 신고 번호
    private Long cmplNumb;
    // 신고 대상 유형 표시명
    private String tagtName;
    // 수신자 정책에 맞춘 신고 사유 표시명
    private String rsonName;
    // 수신자 정책에 맞춘 처리 결과 내용
    private String rsltCntn;
    // 신고자에게만 표시할 신고 접수 일시
    private LocalDateTime cmplDate;
    // 신고 조치 완료 일시
    private LocalDateTime procDate;
}
