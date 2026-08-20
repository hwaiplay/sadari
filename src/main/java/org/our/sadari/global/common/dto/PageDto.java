package org.our.sadari.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * fileName       : PageDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-15
 * description    : 목록 API의 현재 페이지 항목과 다음 페이지 여부를 공통으로 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-15        SeungHyeon.Kang    최초 생성
 */
@Schema(description = "페이지 단위 목록 응답 DTO")
public record PageDto<T>(
        @Schema(description = "현재 페이지 목록") List<T> list
      , @Schema(description = "현재 페이지 번호", example = "1") int page
      , @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext
) {

}
