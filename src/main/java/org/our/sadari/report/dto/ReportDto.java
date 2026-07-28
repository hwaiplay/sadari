package org.our.sadari.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.our.sadari.book.dto.BookDto;

/**
 * fileName       : ReportDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 독후감과 독서 목표 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-07-28        Hanwon.Jang        댓글 필드 추가
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "독후감과 연결 도서 정보를 함께 전달하는 DTO")
public class ReportDto extends BookDto {

    @Schema(description = "작성자 사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "독후감 번호", example = "1")
    private Long reptNumb;

    @Schema(description = "독서 상태 코드", example = "DONE", allowableValues = {"READ", "DONE", "STOP"})
    @NotBlank
    private String reptStat;

    @Schema(description = "독서 상태명", example = "다 읽었어요")
    private String reptStatName;

    @Schema(description = "독서 시작일", example = "2026-07-01")
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reptStdt;

    @Schema(description = "독서 종료일", example = "2026-07-23")
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reptEndt;

    @Schema(description = "독후감 별점. 0점부터 등록 가능하다.", example = "4")
    private String reptGrde;

    @Schema(description = "책장 색상 코드", example = "BLUE")
    @NotBlank
    private String reptColr;

    @Schema(description = "책장 색상명 또는 색상값")
    private String reptColrName;

    @Schema(description = "공개 여부", example = "Y", allowableValues = {"Y", "N"})
    private String pubcYsno;

    @Schema(description = "공개 여부명", example = "공개")
    private String pubcYsnoName;

    @Schema(description = "독후감 본문", example = "인물의 선택이 끝까지 긴장감을 유지했다.")
    @Size(max = 4000)
    private String reptCntn;

    @Schema(description = "독후감 작성자 닉네임", example = "reader31")
    private String userNick;

    @Schema(description = "독후감 작성자 프로필 이미지 경로", example = "/uploads/profile/sample.jpg")
    private String porfPath;

    @Schema(description = "독후감이 받은 좋아요 수", example = "12")
    private Long likeCnt;

    @Schema(description = "로그인 사용자의 좋아요 여부", example = "Y", allowableValues = {"Y", "N"})
    private String likeYsno;

    @Schema(description = "조회 기준일의 독서 기록 존재 여부", example = "Y", allowableValues = {"Y", "N"})
    private String readingYn;

    @Schema(description = "책 제목 또는 작가명 검색어", example = "히가시노 게이고")
    private String bookKeyword;

    @Schema(description = "독후감 목록 정렬 유형", example = "END_DATE_DESC")
    private String sortType;

    @Schema(description = "독후감이 받은 댓글 수", example = "12")
    private Long replCnt;
}
