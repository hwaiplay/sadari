package org.our.sadari.report.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;

/**
 * 독후감, 도서, 좋아요, 독서 목표 관련 MyBatis SQL을 호출하는 Mapper이다.
 * SQL 상세 구현은 ReportMapper.xml에 두고, Java 계층에서는 이 인터페이스를 통해 타입을 보장한다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface ReportMapper {

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건으로 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 검색어, 정렬 조건을 담은 요청 DTO
     * @return 독후감 목록
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 지정 기간 안에 완료된 독후감 수를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 기간 조건을 담은 요청 DTO
     * @return 완료 독후감 수
     */
    int getDoneReportCntByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 지정 기간 안에 완료된 독후감 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 기간 조건을 담은 요청 DTO
     * @return 완료 독후감 목록
     */
    List<ReportDto> getDoneReportListByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 사용자, 목표 기간, 목표 유형에 해당하는 독서 목표를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 목표 조회 조건
     * @return 독서 목표 정보
     */
    ReadingGoalDto getReadingGoalDtl(ReadingGoalDto req);

    /**
     * 독서 목표를 신규 등록하거나 기존 목표를 갱신한다.
     *
     * @author Seunghyeon.Kang
     * @param req 저장할 목표 정보
     * @return 반영 건수
     */
    int setReadingGoal(ReadingGoalDto req);

    /**
     * 목표 유형별 전체 목표 달성 횟수를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 목표 유형
     * @return 목표 달성 횟수
     */
    int getReadingGoalAchvCnt(ReadingGoalDto req);

    /**
     * 독후감 상세와 연결된 도서 정보를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 독후감 번호
     * @return 독후감 상세 정보
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * ISBN 기준 공개 독후감 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req ISBN과 로그인 사용자 번호
     * @return 공개 독후감 목록
     */
    List<ReportDto> getPublicReportList(ReportDto req);

    /**
     * 좋아요 대상 독후감이 존재하며 좋아요 가능한 상태인지 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 좋아요 가능 대상 수
     */
    int getPublicReportLikeTargetCnt(ReportDto req);

    /**
     * 사용자가 해당 독후감에 이미 좋아요를 눌렀는지 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 중복 좋아요 수
     */
    int dupReportLike(ReportDto req);

    /**
     * 독후감 좋아요를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 반영 건수
     */
    int setReportLike(ReportDto req);

    /**
     * 독후감 좋아요를 취소한다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 반영 건수
     */
    int delReportLike(ReportDto req);

    /**
     * 좋아요 토글 후 화면에 표시할 좋아요 상태와 개수를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 좋아요 상세 정보
     */
    ReportDto getReportLikeDtl(ReportDto req);

    /**
     * ISBN 기준 도서 평균 별점을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * ISBN 기준으로 이미 등록된 도서인지 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto 도서 정보
     * @return 중복 도서 수
     */
    int dupBook(ReportDto bookDto);

    /**
     * ISBN 기준으로 도서 번호를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 도서 번호
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 신규 도서 정보를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto 등록할 도서 정보
     * @return 반영 건수
     */
    int setBook(ReportDto bookDto);

    /**
     * 신규 독후감을 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 등록할 독후감 정보
     * @return 반영 건수
     */
    int setReport(ReportDto reportDto);

    /**
     * 기존 독후감을 수정한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 수정할 독후감 정보
     * @return 반영 건수
     */
    int uptReport(ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 사용자 번호와 독후감 번호
     * @return 반영 건수
     */
    int delReport(ReportDto reportDto);
}
