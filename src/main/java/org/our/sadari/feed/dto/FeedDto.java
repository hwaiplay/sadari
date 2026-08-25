package org.our.sadari.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : FeedDto
 * author         : Codex
 * date           : 2026-08-25
 * description    : 팔로잉 피드 항목과 조회 조건을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        Codex              최초 생성
 */
@Data
@Schema(description = "팔로잉 피드 항목 DTO")
public class FeedDto {

    @Schema(description = "로그인 사용자 번호", hidden = true)
    private Long loginUserNumb;

    @Schema(description = "피드 대상 유형", allowableValues = {"REPORT", "PROFILE_IMAGE", "BACKGROUND_IMAGE"})
    private String tagtType;

    @Schema(description = "피드 대상 번호")
    private Long tagtNumb;

    @Schema(description = "피드 작성자 사용자 번호")
    private Long userNumb;

    @Schema(description = "피드 작성자 닉네임")
    private String userNick;

    @Schema(description = "피드 작성자 프로필 이미지 경로")
    private String porfPath;

    @Schema(description = "활동 발생 일시")
    private LocalDateTime activityDate;

    @Schema(description = "독후감 번호")
    private Long reptNumb;

    @Schema(description = "독서 상태명")
    private String reptStatName;

    @Schema(description = "독후감 평점")
    private String reptGrde;

    @Schema(description = "독후감 내용")
    private String reptCntn;

    @Schema(description = "도서 번호")
    private Long bookNumb;

    @Schema(description = "도서 제목")
    private String bookTitl;

    @Schema(description = "도서 저자")
    private String bookAthr;

    @Schema(description = "도서 표지 이미지 URL")
    private String bookCvim;

    @Schema(description = "프로필 또는 배경 이미지 경로")
    private String contentImagePath;

    @Schema(description = "좋아요 수")
    private Long likeCnt;

    @Schema(description = "로그인 사용자 좋아요 여부", allowableValues = {"Y", "N"})
    private String likeYsno;

    @Schema(description = "댓글 수")
    private Long replCnt;

    @Schema(description = "조회 시작 위치", hidden = true)
    private Integer pageOffset;

    @Schema(description = "다음 페이지 판정을 포함한 조회 건수", hidden = true)
    private Integer pageLimit;
}
