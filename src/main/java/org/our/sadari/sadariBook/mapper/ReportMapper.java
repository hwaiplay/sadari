package org.our.sadari.sadariBook.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.sadariBook.dto.ReportDto;

/**
 * fileName       : ReportMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-05-03
 * description    : 도서와 독후감 저장, 조회, 수정, 삭제 SQL을 연결하는 Mapper
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03        SeungHyeon.Kang    최초 생성
 * 2026-07-13        SeungHyeon.Kang    공개 독후감 목록 조회 추가
 */
@Mapper
public interface ReportMapper {

    /**
     * 로그인 사용자의 독후감 목록을 조회한다.
     * @Author SeungHyeon.Kang
     * @param req 사용자 번호를 담은 DTO
     * @return 로그인 사용자의 독후감 목록
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 로그인 사용자의 특정 기간 완료 독서 권수를 조회한다.
     * 독서 상태가 DONE이고 독서 종료일이 집계 기간 안에 있으며, 아직 도래하지 않은 종료일은 제외한다.
     * @Author SeungHyeon.Kang
     * @param req 사용자 번호와 집계 기간 날짜 조건을 담은 DTO
     * @return 해당 기간 완료 독서 권수
     */
    int getDoneReportCntByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 로그인 사용자의 특정 기간 완료 독후감 목록을 조회한다.
     * 마이페이지 독서 요약을 펼쳤을 때 표시할 책 제목, 저자, 종료일, 독후감 번호를 함께 내려준다.
     * @Author SeungHyeon.Kang
     * @param req 사용자 번호와 집계 기간 날짜 조건을 담은 DTO
     * @return 해당 기간 완료 독후감 목록
     */
    List<ReportDto> getDoneReportListByPeriod(MonthlyReadingSummaryDto req);

    /**
     * 로그인 사용자의 독후감 상세 정보를 조회한다.
     * @Author SeungHyeon.Kang
     * @param req 사용자 번호와 독후감 번호를 담은 DTO
     * @return 독후감 상세 정보
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * 독후감에 연결된 도서 상세 정보를 조회한다.
     * @Author SeungHyeon.Kang
     * @param req 사용자 번호와 독후감 번호를 담은 DTO
     * @return 도서 상세 정보
     */
    BookDto getBookInfo(ReportDto req);

    /**
     * 도서 정보 화면에서 같은 도서의 공개 독후감 목록을 조회한다.
     * reportNumb가 있으면 해당 독후감의 도서를 기준으로 조회하고, bookIsbn이 있으면 ISBN을 기준으로 조회한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 reportNumb 또는 bookIsbn을 담은 DTO
     * @return 다른 사용자가 공개한 독후감 목록
     */
    List<ReportDto> getPublicReportList(ReportDto req);

    /**
     * 좋아요를 누를 수 있는 공개 독후감인지 확인한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 독후감 번호를 담은 DTO
     * @return 좋아요 가능 공개 독후감 건수
     */
    int getPublicReportLikeTargetCnt(ReportDto req);

    /**
     * 현재 사용자가 공개 독후감에 이미 좋아요를 눌렀는지 확인한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 독후감 번호를 담은 DTO
     * @return 기존 좋아요 건수
     */
    int dupReportLike(ReportDto req);

    /**
     * 공개 독후감 좋아요를 저장한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 독후감 번호를 담은 DTO
     * @return 저장 건수
     */
    int setReportLike(ReportDto req);

    /**
     * 공개 독후감 좋아요를 취소한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 독후감 번호를 담은 DTO
     * @return 삭제 건수
     */
    int delReportLike(ReportDto req);

    /**
     * 공개 독후감의 현재 좋아요 수와 내 좋아요 여부를 조회한다.
     * @Author SeungHyeon.Kang
     * @param req 현재 사용자 번호와 독후감 번호를 담은 DTO
     * @return 좋아요 상태 정보
     */
    ReportDto getReportLikeDtl(ReportDto req);

    /**
     * ISBN 기준으로 전체 독후감의 평균 별점을 조회한다.
     * @Author SeungHyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 전체 독후감 평균 별점
     */
    BigDecimal getPublicRatingAverageByIsbn(String bookIsbn);

    /**
     * ISBN 기준으로 이미 저장된 도서 여부를 확인한다.
     * @Author SeungHyeon.Kang
     * @param bookDto ISBN을 담은 DTO
     * @return 저장된 도서 건수
     */
    int dupBook(ReportDto bookDto);

    /**
     * ISBN 기준으로 저장된 도서 번호를 조회한다.
     * @Author SeungHyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 도서 번호
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 도서 정보를 저장한다.
     * @Author SeungHyeon.Kang
     * @param bookDto 저장할 도서 정보를 담은 DTO
     * @return 저장 건수
     */
    int setBook(ReportDto bookDto);

    /**
     * 독후감을 저장한다.
     * @Author SeungHyeon.Kang
     * @param reportDto 저장할 독후감 정보를 담은 DTO
     * @return 저장 건수
     */
    int setReport(ReportDto reportDto);

    /**
     * 독후감을 수정한다.
     * @Author SeungHyeon.Kang
     * @param reportDto 수정할 독후감 정보를 담은 DTO
     * @return 수정 건수
     */
    int uptReport(ReportDto reportDto);

    /**
     * 독후감을 삭제한다.
     * @Author SeungHyeon.Kang
     * @param reportDto 삭제할 독후감 번호와 사용자 번호를 담은 DTO
     * @return 삭제 건수
     */
    int delReport(ReportDto reportDto);
}
