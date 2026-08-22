package org.our.sadari.report.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportAlimDto;
import org.our.sadari.report.dto.ReportDto;

/**
 * fileName       : ReportService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 독후감과 독서 목표 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-08-01        SeungHyeon.Kang,Hanwon.Jang    최근 독후감·공개 계약 추가
 * 2026-08-04        SeungHyeon.Kang       독서 요약 공개 범위 계약 추가
 * 2026-08-14        SeungHyeon.Kang    공개 독후감 팔로우 작성자 우선 조회 계약 반영
 * 2026-08-15        SeungHyeon.Kang    공개 독후감 조회·정렬 계약
 * 2026-08-21        SeungHyeon.Kang    독후감별 알림 설정 계약 추가
 */
public interface ReportService {
    /**
     * 독후감과 필요한 도서 정보를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reportDto 등록할 독후감 및 도서 정보
     * @return 등록된 독후감 번호를 담은 처리 결과
     */
    ResultData setReport(Long userNumb, ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감 상세 정보와 연결된 도서 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 조회할 독후감 번호
     * @return 독후감 상세 조회 결과
     */
    ResultData getDetail(Long userNumb, Long reptNumb);

    /**
     * 로그인 사용자가 동일 ISBN으로 가장 최근에 작성한 독후감을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookIsbn 조회할 도서 ISBN
     * @return 동일 ISBN의 최근 독후감 조회 결과
     */
    ResultData getReportByIsbnDtl(Long userNumb, String bookIsbn);

    /**
     * ISBN 기준 활성 사용자의 공개 독후감을 요청한 정렬 기준으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookIsbn 조회할 도서 ISBN
     * @param sortType 공개 독후감 정렬 코드
     * @return 공개 독후감 목록 조회 결과
     */
    ResultData getPublicReportsByIsbn(Long userNumb, String bookIsbn, String sortType
                                    , String reptStat, int page);

    /**
     * ISBN 기준으로 도서의 평균 별점을 조회한다.
     * 평균 별점은 공개 여부와 관계없이 읽는 중 상태를 제외하고 계산한다.
     *
     * @author SeungHyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점 조회 결과
     */
    ResultData getPublicRatingAvgByIsbn(String bookIsbn);

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @return 독후감 목록 조회 결과
     */
    ResultData getBookList(Long userNumb, String bookKeyword, String sortType);

    /**
     * 로그인 사용자의 독후감을 검색어와 정렬 조건에 따라 페이지 단위로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param bookKeyword 책 제목 또는 작가명 검색어
     * @param sortType 목록 정렬 유형
     * @param page 조회할 페이지 번호
     * @return 현재 페이지 독후감과 다음 페이지 여부
     */
    ResultData getBookPage(Long userNumb, String bookKeyword, String sortType, int page);

    /**
     * 본인 또는 다른 사용자 화면에 표시할 주간, 월간, 연간 독서 요약과 목표 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @param pubcYsno 다른 사용자 조회에 적용할 독후감 공개 여부
     * @return 독서 요약 및 목표 달성 정보
     */
    ResultData getMonthlyReadingSummary(Long userNumb, String pubcYsno);

    /**
     * 주간, 월간, 연간 독서 목표를 저장한다.
     * 목표를 낮추는 경우에는 기간과 횟수 제한을 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param readingGoalDto 저장할 독서 목표 정보
     * @return 저장 후 갱신된 독서 요약 정보
     */
    ResultData setReadingGoal(Long userNumb, ReadingGoalDto readingGoalDto);

    // copyPreviousReadingGoal 호출로 이전 목표값을 새 목표에 반영한다
    ResultData copyPreviousReadingGoal(Long userNumb);

    /**
     * 기존 독후감 내용을 수정한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 번호를 담은 처리 결과
     */
    ResultData uptReport(Long userNumb, Long reptNumb, ReportDto reportDto);

    /**
     * 로그인 사용자가 작성한 독후감의 좋아요 또는 댓글 알림 사용 여부를 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param alimType 변경할 알림 유형
     * @param reportAlimDto 변경할 알림 사용 여부
     * @return 변경된 알림 사용 여부
     */
    ResultData uptReportAlim(Long userNumb, Long reptNumb, String alimType, ReportAlimDto reportAlimDto);

    /**
     * 독후감의 읽기 상태와 별점 및 공개 여부를 빠르게 수정한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 수정할 독후감 번호
     * @param reportDto 수정할 읽기 상태와 별점 및 공개 여부
     * @return 수정 처리 결과
     */
    ResultData uptReptStatusGrade(Long userNumb, Long reptNumb, ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param reptNumb 삭제할 독후감 번호
     * @return 삭제 처리 결과
     */
    ResultData delReport(Long userNumb, Long reptNumb);
}
