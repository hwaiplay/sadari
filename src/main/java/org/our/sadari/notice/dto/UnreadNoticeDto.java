package org.our.sadari.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : UnreadNoticeDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-19
 * description    : 홈 제목 슬라이드에 표시할 미읽음 공지 번호와 제목을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-19        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "홈 미읽음 공지 제목 응답 DTO")
public class UnreadNoticeDto {

    @Schema(description = "공지사항 주키", example = "1")
    private Long notiNumb;
    @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
    private String notiTitl;
}
