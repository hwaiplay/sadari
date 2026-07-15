package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 독후감 등록, 수정, 조회에 사용하는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDto extends BookDto {

    /** 로그인한 회원 번호입니다. 인증 정보에서 설정합니다. */
    private Long userNumb;

    /** 독후감 PK 번호입니다. 상세, 수정, 삭제 대상 식별에 사용합니다. */
    private Long reportNumb;

    /** 독서 상태 코드입니다. */
    @NotBlank
    private String reportStat;

    /** 독서 상태 코드명입니다. */
    private String reportStatName;

    /** 독서 시작일입니다. */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportStdt;

    /** 독서 종료일입니다. */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportEndt;

    /** 독후감 별점입니다. 1점부터 5점까지만 허용합니다. */
    @NotBlank
    @Pattern(regexp = "[1-5]")
    private String reportGrde;

    /** 책장 색상 코드입니다. */
    @NotBlank
    private String reportColr;

    /** 책장 색상 코드명입니다. */
    private String reportColrName;

    /** 독후감 공개 여부입니다. 공개는 Y, 비공개는 N으로 저장합니다. */
    private String pubcYsno;

    /** 공개 여부 코드명입니다. */
    private String pubcYsnoName;

    /** 독후감 내용입니다. */
    @NotBlank
    @Size(max = 4000)
    private String reportCntn;

    /** 공개 독후감 목록에 표시할 작성자 닉네임입니다. */
    private String userNick;

    /** 공개 독후감 목록에 표시할 작성자 프로필 이미지 경로입니다. */
    private String porfPath;

    /** 독후감이 받은 좋아요 수입니다. */
    private Long likeCnt;

    /** 현재 로그인한 회원이 좋아요를 눌렀는지 여부입니다. */
    private String likeYsno;

    /** 현재 읽는 중인 책인지 여부입니다. */
    private String readingYn;

    /** 메인 목록에서 책 제목과 작가 검색 조건으로 사용하는 키워드입니다. */
    private String bookKeyword;

    /** 메인 목록에서 종료일, 시작일, 별점 정렬 조건으로 사용하는 코드입니다. */
    private String sortType;
}
