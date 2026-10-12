package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : AuthStateDto
 * author         : SeungHyeon.Kang
 * date           : 2026-10-13
 * description    : 로그인 초기 화면에 필요한 인증 상태를 전달
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-13        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "로그인 초기 인증 상태")
public class AuthStateDto {

    @Schema(description = "회원 상태", example = "ACTIVE")
    private String userStat;

    @Schema(description = "최초 로그인 온보딩 완료 여부", example = "Y")
    private String onbdYsno;

    @Schema(description = "영문 사용 여부", example = "N")
    private String englishYsno;
}
