package org.our.sadari.sadariBook.service;

import java.math.BigDecimal;
import java.util.List;
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.sadariBook.dto.ReadingGoalDto;
import org.our.sadari.sadariBook.dto.ReportDto;

/**
 * 책과 독후감 업무 로직을 정의하는 서비스 인터페이스입니다.
 *
 * @author Seunghyeon.Kang
 */
public interface BookService {

    /**
     * 독후감을 신규 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportDto 등록할 독후감 정보
     * @return 등록된 독후감 정보
     */
    ReportDto setReport(Long userNumb, ReportDto reportDto);

    /**
     * 로그인한 회원의 독후감 상세 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 조회할 독후감 번호
     * @return 독후감 상세 정보
     */
    ReportDto getDetail(Long userNumb, Long reportNumb);

    /**
     * ISBN 기준 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookIsbn 조회할 ISBN
     * @return 공개 독후감 목록
     */
    List<ReportDto> getPublicReportsByIsbn(Long userNumb, String bookIsbn);

    /**
     * ISBN 기준 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 평균 별점을 조회할 ISBN
     * @return 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * 독후감 좋아요 상태를 토글합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경 후 좋아요 상태
     */
    ReportDto setReportLike(Long userNumb, Long reportNumb);

    /**
     * 로그인한 회원의 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookKeyword 책 제목 또는 작가 검색어
     * @param sortType 목록 정렬 코드
     * @return 독후감 목록
     */
    List<ReportDto> getBookList(Long userNumb, String bookKeyword, String sortType);

    /**
     * 마이페이지 월간/연간 독서 요약을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 독서 요약 정보
     */
    MonthlyReadingSummaryDto getMonthlyReadingSummary(Long userNumb);

    /**
     * 마이페이지 월간/연간 독서 목표를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param readingGoalDto 저장할 월간/연간 목표 권수
     * @return 저장 후 다시 조회한 독서 요약 정보
     */
    MonthlyReadingSummaryDto setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto);

    /**
     * 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 정보
     */
    ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto);

    /**
     * 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 건수
     */
    int delReport(Long userNumb, Long reportNumb);
}
