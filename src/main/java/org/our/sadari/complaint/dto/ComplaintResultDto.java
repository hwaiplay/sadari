package org.our.sadari.complaint.dto;

import java.util.List;
import lombok.Data;

/**
 * fileName       : ComplaintResultDto
 * author         : HanWon.Jang
 * date           : 2026-08-24
 * description    : 사용자가 팝업에서 확인할 신고 조치 결과 목록을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-24        HanWon.Jang        최초 생성
 * 2026-08-24        HanWon.Jang        수신자별 상세 결과 목록 추가
 */
@Data
public class ComplaintResultDto {

    // 사용자가 아직 확인하지 않은 신고 조치 결과 건수
    private Integer rsltCntt;

    // 조회 시점에 확인할 마지막 신고 조치 결과 번호
    private Long lastRsltNumb;

    // 현재 팝업에서 표시할 미확인 신고 조치 결과 목록
    private List<ComplaintResultItemDto> resultList;
}
