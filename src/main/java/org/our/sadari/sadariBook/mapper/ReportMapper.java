package org.our.sadari.sadariBook.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.sadariBook.dto.ReportDto;

/**
 * 책과 독후감 데이터 조회 및 변경을 담당하는 MyBatis Mapper입니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface ReportMapper {

    /**
     * 로그인한 회원의 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호, 검색어, 정렬 조건을 담은 요청 DTO
     * @return 독후감 목록
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 특정 기간에 완료한 독서 권수를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 기간 조건과 회원 번호를 담은 요청 DTO
     * @return 완료 독서 권수
     */
    int getDoneReportCntByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 특정 기간에 완료한 독서 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 기간 조건과 회원 번호를 담은 요청 DTO
     * @return 완료 독서 목록
     */
    List<ReportDto> getDoneReportListByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 로그인한 회원의 독후감 상세 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 독후감 상세 정보
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * ISBN 또는 독후감 번호 기준으로 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 조회 조건을 담은 요청 DTO
     * @return 공개 독후감 목록
     */
    List<ReportDto> getPublicReportList(ReportDto req);

    /**
     * 좋아요를 누를 수 있는 공개 독후감인지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 대상 건수
     */
    int getPublicReportLikeTargetCnt(ReportDto req);

    /**
     * 현재 회원이 해당 독후감에 이미 좋아요를 눌렀는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 기존 좋아요 건수
     */
    int dupReportLike(ReportDto req);

    /**
     * 독후감 좋아요를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 저장 건수
     */
    int setReportLike(ReportDto req);

    /**
     * 독후감 좋아요를 취소합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 삭제 건수
     */
    int delReportLike(ReportDto req);

    /**
     * 독후감 좋아요 수와 현재 회원의 좋아요 여부를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 회원 번호와 독후감 번호를 담은 요청 DTO
     * @return 좋아요 상세 정보
     */
    ReportDto getReportLikeDtl(ReportDto req);

    /**
     * ISBN 기준 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 평균 별점을 조회할 ISBN
     * @return 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * ISBN 기준 책 중복 여부를 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto ISBN을 담은 요청 DTO
     * @return 중복 책 건수
     */
    int dupBook(ReportDto bookDto);

    /**
     * ISBN 기준 책 번호를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 조회할 ISBN
     * @return 책 번호
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 책 기본 정보를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto 저장할 책 정보
     * @return 저장 건수
     */
    int setBook(ReportDto bookDto);

    /**
     * 독후감을 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 저장할 독후감 정보
     * @return 저장 건수
     */
    int setReport(ReportDto reportDto);

    /**
     * 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 수정할 독후감 정보
     * @return 수정 건수
     */
    int uptReport(ReportDto reportDto);

    /**
     * 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 삭제할 독후감 조건
     * @return 삭제 건수
     */
    int delReport(ReportDto reportDto);
}
