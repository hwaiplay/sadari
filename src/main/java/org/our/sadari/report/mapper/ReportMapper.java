package org.our.sadari.report.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.myPage.dto.MonthlyReadingSummaryDto;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.social.dto.SocialDto;

/**
 * 독후감, 독서 목표, 공개 독후감 조회를 담당하는 MyBatis Mapper 계약입니다.
 * 좋아요 등록/삭제처럼 TB_LIKEXX를 직접 변경하는 기능은 SocialMapper에서 관리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface ReportMapper {

    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 검색어, 정렬 조건을 담은 요청 DTO
     * @return 독후감 목록
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 지정한 기간 안에 완료된 독후감 수를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 기간 조건을 담은 요청 DTO
     * @return 완료 독후감 수
     */
    int getReportCntByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 지정한 기간 안에 완료된 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 기간 조건을 담은 요청 DTO
     * @return 완료 독후감 목록
     */
    List<ReportDto> getSummaryReportList(MonthlyReadingSummaryDto req);

    /**
     * 사용자, 목표 기간, 목표 유형에 해당하는 독서 목표를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 목표 조회 조건
     * @return 독서 목표 정보
     */
    ReadingGoalDto getReadingGoalDtl(ReadingGoalDto req);

    /**
     * 독서 목표를 신규 등록하거나 기존 목표를 갱신합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 저장할 목표 정보
     * @return 반영 건수
     */
    int setReadingGoal(ReadingGoalDto req);

    /**
     * 목표 유형별 전체 목표 달성 횟수를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 목표 유형
     * @return 목표 달성 횟수
     */
    int getReadingGoalAchvCnt(ReadingGoalDto req);

    /**
     * 독후감 상세와 연결된 도서 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 독후감 번호
     * @return 독후감 상세 정보
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * 좋아요를 허용할 수 있는 공개 독후감 대상인지 조회합니다.
     * TB_LIKEXX 변경은 SocialMapper에서 처리하지만, 대상 검증 기준은 TM_REPORT이므로 ReportMapper에서 관리합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 독후감 번호와 사용자 번호
     * @return 좋아요 허용 대상 수
     */
    int getPublicReportLikeTargetCnt(SocialDto.LikeDto req);

    /**
     * ISBN 기준 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req ISBN과 로그인 사용자 번호
     * @return 공개 독후감 목록
     */
    List<ReportDto> getPublicReportList(ReportDto req);

    /**
     * ISBN 기준으로 연결된 완료 독후감의 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * 신규 독후감을 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 등록할 독후감 정보
     * @return 반영 건수
     */
    int setReport(ReportDto reportDto);

    /**
     * 기존 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 수정할 독후감 정보
     * @return 반영 건수
     */
    int uptReport(ReportDto reportDto);

    /**
     * 독후감의 읽기 상태와 별점만 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 사용자 번호, 독후감 번호, 읽기 상태, 별점
     * @return 반영 건수
     */
    int uptReptStatusGrade(ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 사용자 번호와 독후감 번호
     * @return 반영 건수
     */
    int delReport(ReportDto reportDto);
}
