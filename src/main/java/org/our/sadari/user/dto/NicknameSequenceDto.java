package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : NicknameSequenceDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 신규 회원 닉네임 번호 발급 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "신규 회원 닉네임 번호 발급 DTO")
public class NicknameSequenceDto {

    @Schema(hidden = true)
    // 주어와 서술어 및 동물 명사로 구성한 닉네임 본문
    private String nickText;

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
