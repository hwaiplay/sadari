package org.our.sadari.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : UserBlockDto
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 요청과 차단 사용자 목록 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@Data
@Schema(description = "사용자 차단 요청 및 목록 DTO")
public class UserBlockDto {

    @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
    private Long userNumb;

    @Schema(description = "차단 대상 사용자 번호", example = "32")
    private Long blocNumb;

    @Schema(description = "차단 대상 닉네임", example = "reader32")
    private String userNick;

    @Schema(description = "차단 대상 계정 상태 표시명", example = "정상")
    private String userStatName;

    @Schema(description = "차단 등록 일시", example = "2026-09-03T12:00:00")
    private LocalDateTime regiDate;

    @Schema(description = "차단 목록 조회 시작 위치", example = "0", hidden = true)
    private Integer pageOffset;

    @Schema(description = "다음 페이지 판정을 포함한 조회 건수", example = "11", hidden = true)
    private Integer pageLimit;
}
