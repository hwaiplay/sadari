package org.our.sadari.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * fileName       : AuthLogoutDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-11
 * description    : 현재 기기 또는 전체 기기 로그아웃 요청을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-11        SeungHyeon.Kang    최초 생성
 */
@Getter
@Setter
@Schema(description = "로그아웃 범위 요청 DTO")
public class AuthLogoutDto {

    @Pattern(regexp = "CURRENT|ALL")
    @Schema(description = "로그아웃 범위", allowableValues = {"CURRENT", "ALL"}, example = "CURRENT")
    private String scope;

    @Size(max = 750)
    @Schema(description = "현재 브라우저에서 비활성화할 FCM token")
    private String pushToken;
}
