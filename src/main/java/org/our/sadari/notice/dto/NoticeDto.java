package org.our.sadari.notice.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : NoticeDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 사용자에게 배포된 공지사항 제목과 HTML 본문을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Data
public class NoticeDto {

    // 공지사항 주키
    private Long notiNumb;
    // 배포 버전 번호
    private Integer versNumb;
    // 공지사항 카테고리 상세코드
    private String cateCode;
    // 공지사항 카테고리명
    private String cateName;
    // 공지사항 제목
    private String notiTitl;
    // 정제된 공지사항 HTML 본문
    private String notiCntn;
    // 상단 고정 여부
    private String topxYsno;
    // 현재 사용자의 읽음 여부
    private String readYsno;
    // 배포 일시
    private LocalDateTime dplyDate;
}
