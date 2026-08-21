package org.our.sadari.reply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ReplyDto
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회, 등록, 수정, 삭제 및 좋아요 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성 및 댓글 필드 정의
 * 2026-07-29        Hanwon.Jang        댓글 상태·작성 여부 필드 추가
 * 2026-08-03        Hanwon.Jang        댓글 수정·삭제·좋아요 필드 추가
 * 2026-08-04        HanWon.Jang        댓글 좋아요 알림 수신자 용도 확장
 * 2026-08-15        SeungHyeon.Kang    부모 댓글 페이지 조회 조건 추가
 * 2026-08-21        SeungHyeon.Kang    독후감 댓글 알림 설정 추가
 */
@Data
@Schema(description = "댓글 정보를 전달하는 DTO")
public class ReplyDto {

    @Schema(description = "댓글이 작성된 독후감 번호", example = "1")
    private Long reptNumb;

    @Schema(description = "전체 댓글에서 유일한 댓글 번호", example = "10")
    private Long replNumb;

    @Schema(description = "답글이 참조하는 부모 댓글 번호", example = "3")
    private Long uperNumb;

    @Schema(description = "부모 댓글 여부", example = "Y", allowableValues = {"Y", "N"})
    private String parentYn;

    @Schema(description = "댓글 작성자 사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "로그인 사용자가 작성한 댓글 여부", example = "Y", allowableValues = {"Y", "N"})
    private String myReplyYn;

    @Schema(description = "닉네임. 한글, 영문, 숫자 사용 가능", example = "reader31")
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

    @Schema(description = "댓글 수정 여부", example = "Y", allowableValues = {"Y", "N"})
    private String updtYsno;

    @Schema(description = "댓글 수정 여부에 따라 화면에 표시할 문구", example = "수정됨")
    private String updtYsnoNm;

    @Schema(description = "댓글 좋아요 수", example = "12")
    private Long likeCnt;

    @Schema(description = "로그인 사용자의 댓글 좋아요 여부", example = "Y", allowableValues = {"Y", "N"})
    private String likeYsno;

    @Schema(description = "댓글 좋아요 알림을 받을 댓글 작성자 사용자 번호", example = "31", hidden = true)
    private Long targetUserNumb;

    @Schema(description = "독후감 작성자의 댓글과 답글 알림 여부", example = "Y", allowableValues = {"Y", "N"}, hidden = true)
    private String replyAlimYsno;

    @Schema(description = "댓글 조회 시 계산된 동시 수정 충돌 검사용 원본 해시")
    private String editVersion;

    @Schema(description = "부모 댓글 조회 시작 위치", example = "0", hidden = true)
    private Integer pageOffset;

    @Schema(description = "다음 페이지 판정을 포함한 부모 댓글 조회 건수", example = "11", hidden = true)
    private Integer pageLimit;
}
