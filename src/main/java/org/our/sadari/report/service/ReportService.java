package org.our.sadari.report.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;

/**
 * 독후감, 공개 독후감, 좋아요, 독서 목표 관련 업무 기능을 제공하는 Service 계약이다.
 * Controller는 이 인터페이스만 의존해 API 경로와 실제 업무 구현을 분리한다.
 *
 * @author Seunghyeon.Kang
 */
public interface ReportService {

    /**
     * 독후감과 필요한 도서 정보를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportDto 등록할 독후감 및 도서 정보
     * @return 등록된 독후감 번호를 담은 처리 결과
     */
    ResultData setReport(Long userNumb, ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감 상세 정보와 연결된 도서 정보를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 조회할 독후감 번호
     * @return 독후감 상세 조회 결과
     */
    ResultData getDetail(Long userNumb, Long reptNumb);

    /**
     * ISBN 기준으로 공개 독후감 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookIsbn 조회할 도서 ISBN
     * @return 공개 독후감 목록 조회 결과
     */
    ResultData getPublicReportsByIsbn(Long userNumb, String bookIsbn);

    /**
     * ISBN 기준으로 도서의 평균 별점을 조회한다.
     * 평균 별점은 공개 여부와 관계없이 전체 독후감을 기준으로 계산한다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점 조회 결과
     */
    ResultData getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건으로 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @return 독후감 목록 조회 결과
     */
    ResultData getBookList(Long userNumb, String bookKeyword, String sortType);

    /**
     * 마이페이지에 표시할 주간, 월간, 연간 독서 요약과 목표 정보를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 독서 요약 및 목표 달성 정보
     */
    ResultData getMonthlyReadingSummary(Long userNumb);

    /**
     * 주간, 월간, 연간 독서 목표를 저장한다.
     * 목표를 낮추는 경우에는 기간과 횟수 제한을 적용한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readingGoalDto 저장할 독서 목표 정보
     * @return 저장 후 갱신된 독서 요약 정보
     */
    ResultData setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto);

    ResultData copyPreviousReadingGoal(Long userNumb);

    /**
     * 기존 독후감 내용을 수정한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 번호를 담은 처리 결과
     */
    ResultData uptReport(Long userNumb, Long reptNumb, ReportDto reportDto);

    /**
     * 독후감의 읽기 상태와 별점만 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 읽기 상태와 별점
     * @return 수정 처리 결과
     */
    ResultData uptReptStatusGrade(Long userNumb, Long reptNumb, ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 삭제할 독후감 번호
     * @return 삭제 처리 결과
     */
    ResultData delReport(Long userNumb, Long reptNumb);
}
