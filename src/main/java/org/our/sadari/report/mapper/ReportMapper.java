package org.our.sadari.report.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.myPage.dto.ReadingGoalDto;
import org.our.sadari.myPage.dto.ReadingSummaryQueryDto;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : ReportMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 독후감과 독서 목표 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-08-01        SeungHyeon.Kang    ISBN 기준 최근 독후감 조회 추가
 * 2026-08-01        Hanwon.Jang        공개 목록과 빠른 수정 상태 정책 추가
 * 2026-08-04        SeungHyeon.Kang       독서 요약 공개 범위 조회 조건 문서화
 */
@Mapper
public interface ReportMapper {
    /**
     * 로그인 사용자의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 검색어, 정렬 조건을 담은 요청 DTO
     * @return 독후감 목록
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 본인 또는 다른 사용자 프로필의 기간별 독서량과 목표 달성 정보를 통합 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 선택적 공개 여부, 기간 및 목표 기준값
     * @return 기간별 독서량과 목표 달성 집계
     */
    ReadingSummaryQueryDto getReadingSummary(ReadingSummaryQueryDto req);

    /**
     * 본인 또는 다른 사용자 프로필에 표시할 현재 읽는 책과 올해 완료한 책을 한 번에 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 선택적 공개 여부, 현재 연도 기간 및 독서 상태
     * @return 독서 요약에 표시할 독후감 목록
     */
    List<ReportDto> getReadingSummaryList(ReadingSummaryQueryDto req);

    /**
     * 사용자, 목표 기간, 목표 유형에 해당하는 독서 목표를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 목표 조회 조건
     * @return 독서 목표 정보
     */
    ReadingGoalDto getReadingGoalDtl(ReadingGoalDto req);

    /**
     * 독서 목표를 신규 등록하거나 기존 목표를 갱신한다.
     *
     * @author SeungHyeon.Kang
     * @param req 저장할 목표 정보
     * @return 반영 건수
     */
    int setReadingGoal(ReadingGoalDto req);

    /**
     * 독후감 상세와 연결된 도서 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호와 독후감 번호
     * @return 독후감 상세 정보
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * 로그인 사용자가 동일 ISBN으로 가장 최근에 작성한 독후감을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 조회할 ISBN
     * @return 동일 ISBN의 최근 독후감 정보
     */
    ReportDto getReportByIsbnDtl(ReportDto req);

    /**
     * 좋아요를 허용할 수 있는 공개 독후감 대상인지 조회한다.
     * TB_LIKEXX 변경은 SocialMapper에서 처리하지만, 대상 검증 기준은 TM_REPORT이므로 ReportMapper에서 관리한다.
     *
     * @author SeungHyeon.Kang
     * @param req 독후감 번호, 요청 사용자 번호와 화면에서 전달한 작성자 번호
     * @return 좋아요 허용 대상 수
     */
    int getPublicReportLikeCnt(SocialDto.LikeDto req);

    /**
     * ISBN 기준 공개 독후감 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req ISBN과 로그인 사용자 번호
     * @return 공개 독후감 목록
     */
    List<ReportDto> getPublicReportList(ReportDto req);

    /**
     * ISBN 기준으로 연결된 완료 또는 중단 독후감의 평균 별점을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 평균 별점
     */
    BigDecimal getPublicRatingAvgByIsbn(String bookIsbn);

    /**
     * 신규 독후감을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 등록할 독후감 정보
     * @return 반영 건수
     */
    int setReport(ReportDto reportDto);

    /**
     * 기존 독후감을 수정한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 수정할 독후감 정보
     * @return 반영 건수
     */
    int uptReport(ReportDto reportDto);

    /**
     * 독후감의 읽기 상태와 별점 및 공개 여부를 빠르게 수정한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 사용자 번호, 독후감 번호, 읽기 상태, 별점, 공개 여부
     * @return 반영 건수
     */
    int uptReptStatusGrade(ReportDto reportDto);

    /**
     * 로그인 사용자의 독후감을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param reportDto 사용자 번호와 독후감 번호
     * @return 반영 건수
     */
    int delReport(ReportDto reportDto);
}
