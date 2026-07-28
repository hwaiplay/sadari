package org.our.sadari.reply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ReplyDto
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회 및 등록 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-28        Hanwon.Jang        댓글 컬럼 정의
 */
@Data
@Schema(description = "댓글 정보를 전달하는 DTO")
public class ReplyDto {

    @Schema(description = "댓글이 작성된 독후감 번호", example = "1")
    private Long reptNumb;

    @Schema(description = "독후감별 댓글 번호", example = "10")
    private Long replNumb;

    @Schema(description = "답글이 참조하는 부모 댓글 번호", example = "3")
    private Long uperNumb;

    @Schema(description = "댓글 작성자 사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "닉네임. 한글, 영문, 숫자를 사용할 수 있다.", example = "reader31")
    private String userNick;

    @Schema(description = "프로필 이미지 경로")
    private String porfPath;

    @Schema(description = "댓글 또는 답글 내용", example = "@reader31 저도 같은 생각이에요")
    private String replCntn;

    @Schema(description = "댓글 삭제 여부", example = "N", allowableValues = {"Y", "N"})
    private String deltYsno;

    @Schema(description = "댓글 등록 일시", example = "2026-07-28T15:30:00")
    private LocalDateTime regiDate;

    @Schema(description = "댓글 수정 일시", example = "2026-07-28T15:30:00")
    private LocalDateTime updtDate;
}
