package org.our.sadari.sadariBook.service;

import java.math.BigDecimal;
import java.util.List;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.ReportDto;

public interface BookService {

    /**
     * 독후감 등록
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportDto
     * @return
     */
    ReportDto setReport(Long userNumb, ReportDto reportDto);

    /**
     * 독후감 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    ReportDto getDetail(Long userNumb, Long reportNumb);

    /**
     * 도서 정보 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    BookDto getBookInfo(Long userNumb, Long reportNumb);

    /**
     * 기준 독후감과 같은 도서의 공개 독후감 목록을 조회한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param reportNumb 기준 독후감 번호
     * @return 다른 사용자가 공개한 독후감 목록
     */
    List<ReportDto> getPublicReportsByReport(Long userNumb, Long reportNumb);

    /**
     * ISBN이 같은 도서의 공개 독후감 목록을 조회한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param bookIsbn 도서 ISBN
     * @return 다른 사용자가 공개한 독후감 목록
     */
    List<ReportDto> getPublicReportsByIsbn(Long userNumb, String bookIsbn);

    /**
     * ISBN 기준으로 전체 독후감 평균 별점을 조회한다.
     * @Author SeungHyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 전체 독후감 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * 공개 독후감 좋아요 상태를 토글한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경 후 좋아요 수와 현재 사용자 좋아요 여부
     */
    ReportDto setReportLike(Long userNumb, Long reportNumb);

    /**
     * 독후감 리스트 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @return
     */
    List<ReportDto> getBookList(Long userNumb, String bookKeyword, String sortType);

    /**
     * 독후감 수정
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @param reportDto
     * @return
     */
    ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto);

    /**
     * 독후감 삭제
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    int delReport(Long userNumb, Long reportNumb);
}
