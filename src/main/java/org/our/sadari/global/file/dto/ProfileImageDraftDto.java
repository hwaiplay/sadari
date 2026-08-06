package org.our.sadari.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;

/**
 * fileName       : ProfileImageDraftDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-06
 * description    : 프로필 또는 배경 이미지 임시 저장 결과를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-06        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "프로필 이미지 임시 저장 DTO")
public class ProfileImageDraftDto {

    @Schema(description = "이미지 적용 대상", example = "PROFILE")
    private String imageType;

    @Schema(description = "로그인 사용자에게만 유효한 임시 이미지 식별값")
    private String draftToken;

    @Schema(description = "서버가 방향 보정과 축소를 완료한 비공개 미리보기 Data URL")
    private String previewDataUrl;

    @Schema(description = "임시 이미지 만료 시각")
    private Instant expiresAt;
}
