package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDto extends BookDto {

    // 사용자 번호는 인증 정보에서 설정한다.
    private Long userNumb;
    // 독후감 번호는 상세, 수정, 삭제 대상 식별에 사용한다.
    private Long reportNumb;

    // 독서 상태는 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    private String reportStat;
    private String reportStatName;

    // 독서 시작일은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportStdt;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportEndt;
    @NotBlank
    @Pattern(regexp = "[1-5]")
    private String reportGrde;
    @NotBlank
    private String reportColr;
    private String reportColrName;
    // 독후감 공개 여부이며 공개는 Y, 비공개는 N으로 저장한다.
    private String pubcYsno;
    // 공개여부 코드를 화면 표시 명칭으로 변환한 값이다.
    private String pubcYsnoName;

    // 독후감 내용은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    @Size(max = 4000)
    private String reportCntn;

    // 공개 독후감 목록에서 작성자를 표시하기 위한 사용자 닉네임이다.
    private String userNick;
    // 공개 독후감 목록에서 작성자 프로필 이미지를 표시하기 위한 프로필 경로이다.
    private String porfPath;
    // 공개 독후감 목록에서 독후감이 받은 좋아요 수를 표시한다.
    private Long likeCnt;
    // 현재 로그인 사용자가 해당 공개 독후감에 좋아요를 눌렀는지 Y/N으로 표시한다.
    private String likeYsno;
    private String readingYn;
    // 목록 조회에서 책 제목 검색 조건으로 사용한다.
    private String bookKeyword;
    // 목록 조회에서 종료일, 시작일, 별점 정렬 조건으로 사용한다.
    private String sortType;
}
