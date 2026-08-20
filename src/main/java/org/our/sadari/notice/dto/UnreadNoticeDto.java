package org.our.sadari.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : UnreadNoticeDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-19
 * description    : 홈에 표시할 미읽음 공지 번호와 카테고리명 및 제목을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-19        SeungHyeon.Kang    최초 생성
 * 2026-08-20        SeungHyeon.Kang    카테고리명 응답 추가
 */
@Data
@Schema(description = "홈 미읽음 공지 카테고리와 제목 응답 DTO")
public class UnreadNoticeDto {

    @Schema(description = "공지사항 주키", example = "1")
    private Long notiNumb;
    @Schema(description = "공지사항 카테고리명", example = "서비스")
    private String cateName;
    @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
    private String notiTitl;
}
