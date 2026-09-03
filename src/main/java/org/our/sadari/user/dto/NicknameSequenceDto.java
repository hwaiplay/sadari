package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : NicknameSequenceDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 신규 회원 닉네임 번호 발급 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    닉네임 문구 키를 세부코드 조합 키로 변경
 */
@Data
@Schema(description = "신규 회원 닉네임 번호 발급 DTO")
public class NicknameSequenceDto {

    @Schema(hidden = true)
    // 닉네임 주어 세부코드
    private String subjCode;

    @Schema(hidden = true)
    // 닉네임 서술어 세부코드
    private String predCode;

    @Schema(hidden = true)
    // 닉네임 동물 명사 세부코드
    private String anmlCode;

    @Schema(hidden = true)
    // YYMM 형식의 닉네임 발급 연월
    private String issuYeam;

    @Schema(hidden = true)
    // 닉네임 조합과 발급 연월별 마지막 발급 번호
    private Integer lastNumb;

    @Schema(hidden = true)
    // 닉네임 조합 최초 발급일시
    private LocalDateTime regiDate;

    @Schema(hidden = true)
    // 닉네임 조합 최근 발급일시
    private LocalDateTime updtDate;
}
