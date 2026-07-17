package org.our.sadari.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.our.sadari.book.dto.BookDto;

/**
 * 독후감 데이터와 독후감에 연결된 도서 데이터를 함께 전달하는 DTO이다.
 * BookDto를 상속해 독후감 등록 시 도서 정보와 독후감 정보를 하나의 요청 본문으로 받을 수 있게 구성한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDto extends BookDto {

    /** 로그인 사용자 번호이다. */
    private Long userNumb;

    /** 독후감 고유 번호이다. */
    private Long reportNumb;

    /** 독서 상태 코드이다. 예: 읽는 중, 완료 등 공통코드 값을 사용한다. */
    @NotBlank
    private String reportStat;

    /** 화면 표시용 독서 상태명이다. */
    private String reportStatName;

    /** 독서 시작일이다. yyyy-MM-dd 형식만 허용한다. */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportStdt;

    /** 독서 종료일이다. yyyy-MM-dd 형식만 허용한다. */
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportEndt;

    /** 독후감 별점이다. 읽고있어요 상태의 미선택 값은 0, 사용자가 선택하는 값은 1점부터 5점까지의 정수로 저장한다. */
    private String reportGrde;

    /** 책장 색상 코드이다. 공통코드 CODE_BOOK_COLR의 세부코드 값을 사용한다. */
    @NotBlank
    private String reportColr;

    /** 화면 표시용 책장 색상명 또는 색상값이다. */
    private String reportColrName;

    /** 공개 여부 코드이다. Y이면 공개, N이면 비공개이다. */
    private String pubcYsno;

    /** 화면 표시용 공개 여부명이다. */
    private String pubcYsnoName;

    /** 독후감 본문 내용이다. DB 저장 크기를 고려해 4000자 이하로 제한한다. */
    @NotBlank
    @Size(max = 4000)
    private String reportCntn;

    /** 공개 독후감 목록에 표시할 작성자 닉네임이다. */
    private String userNick;

    /** 공개 독후감 목록에 표시할 작성자 프로필 이미지 경로이다. */
    private String porfPath;

    /** 독후감이 받은 좋아요 수이다. */
    private Long likeCnt;

    /** 로그인 사용자가 해당 독후감에 좋아요를 눌렀는지 여부이다. */
    private String likeYsno;

    /** 마이페이지 독서 캘린더에서 해당 일자에 독서 기록이 있는지 표시하는 값이다. */
    private String readingYn;

    /** 독후감 목록에서 책 제목 또는 작가명 검색에 사용하는 검색어이다. */
    private String bookKeyword;

    /** 독후감 목록 정렬 유형이다. 종료일, 시작일, 별점 정렬에 사용한다. */
    private String sortType;
}
