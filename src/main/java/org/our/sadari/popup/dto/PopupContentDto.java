package org.our.sadari.popup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : PopupContentDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠 조회 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "사용자 안내 팝업 콘텐츠 DTO")
public class PopupContentDto {

    @Schema(description = "팝업 사용 화면 구분 공통코드", example = "ACCOUNT")
    private String popuSitu;

    @Schema(description = "팝업 식별 코드", example = "WITHDRAWAL_POLICY")
    private String popuCode;

    @Schema(description = "첫 번째 영역의 JSON 문자열 목록", example = "[\"첫 번째 안내 문구\"]")
    private String contFirs;

    @Schema(description = "두 번째 영역의 JSON 문자열 목록", example = "[\"두 번째 안내 문구\"]")
    private String contSeco;

    @Schema(description = "세 번째 영역의 JSON 문자열 목록", example = "[\"세 번째 안내 문구\"]")
    private String contThir;

    @Schema(description = "네 번째 영역의 JSON 문자열 목록", example = "[\"네 번째 안내 문구\"]")
    private String contFour;
}
