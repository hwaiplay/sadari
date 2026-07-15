package org.our.sadari.sadariBook.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.util.LocaleUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.sadariBook.dto.MonthlyReadingSummaryDto;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

/**
 * 책과 독후감 등록, 조회, 수정, 삭제 업무 로직을 처리하는 서비스 구현체입니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final ReportMapper reportMapper;
    private final CodeUtil codeUtil;

    /**
     * 로그인한 회원의 독후감 목록을 검색어와 정렬 조건에 맞춰 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookKeyword 책 제목 또는 작가 검색어
     * @param sortType 목록 정렬 코드
     * @return 독후감 목록
     */
    @Override
    public List<ReportDto> getBookList(Long userNumb, String bookKeyword, String sortType) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookKeyword(StringUtil.normalizePlainText(bookKeyword));
        reportDto.setSortType(normalizeListSortType(sortType));

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("Book report list lookup completed. userNumb={}, size={}", userNumb, list.size());
        return list;
    }

    /**
     * 이번 달과 올해의 완료 독서 권수, 비교 증감, 펼침 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @return 마이페이지 독서 요약 정보
     */
    @Override
    public MonthlyReadingSummaryDto getMonthlyReadingSummary(Long userNumb) {
        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate currentYearStart = today.withDayOfYear(1);
        LocalDate previousYearStart = currentYearStart.minusYears(1);

        // 이번 달은 오늘 기준 월의 1일부터 다음 달 1일 전까지를 집계 범위로 사용합니다.
        MonthlyReadingSummaryDto currentMonthReq = getDoneReportCntByPeriodReq(
                userNumb,
                currentMonthStart,
                currentMonthStart.plusMonths(1),
                today
        );
        // 지난 달 비교값은 지난 달 1일부터 이번 달 1일 전까지 같은 방식으로 계산합니다.
        MonthlyReadingSummaryDto previousMonthReq = getDoneReportCntByPeriodReq(
                userNumb,
                previousMonthStart,
                currentMonthStart,
                currentMonthStart.minusDays(1)
        );
        // 올해 집계는 1월 1일부터 다음 해 1월 1일 전까지의 완료 독서를 대상으로 합니다.
        MonthlyReadingSummaryDto currentYearReq = getDoneReportCntByPeriodReq(
                userNumb,
                currentYearStart,
                currentYearStart.plusYears(1),
                today
        );
        // 작년 비교값은 작년 1월 1일부터 올해 1월 1일 전까지의 완료 독서를 대상으로 합니다.
        MonthlyReadingSummaryDto previousYearReq = getDoneReportCntByPeriodReq(
                userNumb,
                previousYearStart,
                currentYearStart,
                currentYearStart.minusDays(1)
        );

        int currentMonthCount = reportMapper.getDoneReportCntByPeriod(currentMonthReq);
        int previousMonthCount = reportMapper.getDoneReportCntByPeriod(previousMonthReq);
        int currentYearCount = reportMapper.getDoneReportCntByPeriod(currentYearReq);
        int previousYearCount = reportMapper.getDoneReportCntByPeriod(previousYearReq);

        MonthlyReadingSummaryDto summary = new MonthlyReadingSummaryDto();
        summary.setMonthCode(today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH));
        summary.setCurrentMonthCount(currentMonthCount);
        summary.setPreviousMonthCount(previousMonthCount);
        summary.setCountDiff(currentMonthCount - previousMonthCount);
        summary.setYearCode(String.valueOf(today.getYear()));
        summary.setCurrentYearCount(currentYearCount);
        summary.setPreviousYearCount(previousYearCount);
        summary.setYearCountDiff(currentYearCount - previousYearCount);
        summary.setCurrentMonthReports(reportMapper.getDoneReportListByPeriod(currentMonthReq));
        summary.setCurrentYearReports(reportMapper.getDoneReportListByPeriod(currentYearReq));
        return summary;
    }

    /**
     * 완료 독서 권수와 목록 조회에 공통으로 사용하는 기간 조건 DTO를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param periodStart 조회 시작일
     * @param periodEndExclusive 조회 종료 경계일
     * @param targetDate 집계 기준일
     * @return 기간 조건 DTO
     */
    private MonthlyReadingSummaryDto getDoneReportCntByPeriodReq(
            Long userNumb,
            LocalDate periodStart,
            LocalDate periodEndExclusive,
            LocalDate targetDate
    ) {
        MonthlyReadingSummaryDto req = new MonthlyReadingSummaryDto();
        req.setUserNumb(userNumb);
        req.setPeriodStart(periodStart.toString());
        req.setPeriodEndExclusive(periodEndExclusive.toString());
        req.setTargetDate(targetDate.toString());
        return req;
    }


    /**
     * 목록 정렬 코드를 허용된 값으로 정규화합니다.
     *
     * @author Seunghyeon.Kang
     * @param sortType 요청 정렬 코드
     * @return 허용된 정렬 코드, 없거나 잘못된 값이면 종료일 내림차순
     */
    private String normalizeListSortType(String sortType) {
        // 화면에서 허용하는 시작일순과 별점순만 그대로 사용하고 나머지는 기본 정렬로 고정합니다.
        if (Constant.SORT_START_DATE_DESC.equals(sortType) || Constant.SORT_GRADE_DESC.equals(sortType)) {
            return sortType;
        }

        return Constant.SORT_END_DATE_DESC;
    }

    /**
     * 로그인한 회원의 독후감 상세 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 조회할 독후감 번호
     * @return 독후감 상세 정보
     */
    @Override
    public ReportDto getDetail(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        reportDto.setLocale(LocaleUtil.getLocale());

        return reportMapper.getReportDtl(reportDto);
    }

    /**
     * ISBN 기준 공개 독후감 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param bookIsbn 조회할 ISBN
     * @return 공개 독후감 목록
     */
    @Override
    public List<ReportDto> getPublicReportsByIsbn(Long userNumb, String bookIsbn) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookIsbn(StringUtil.normalizePlainText(bookIsbn));

        return reportMapper.getPublicReportList(reportDto);
    }

    /**
     * ISBN 기준 전체 독후감 평균 별점을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 평균 별점을 조회할 ISBN
     * @return 평균 별점
     */
    @Override
    public BigDecimal getPublicRatingAverageByIsbn(String bookIsbn) {
        return reportMapper.getPublicRatingAverageByIsbn(StringUtil.normalizePlainText(bookIsbn));
    }

    /**
     * 독후감 좋아요를 등록하거나 취소합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경 후 좋아요 상태
     */
    @Override
    @Transactional
    public ReportDto setReportLike(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        // 공개 독후감만 좋아요 대상이므로 비공개 또는 존재하지 않는 독후감은 요청 오류로 처리합니다.
        if (reportMapper.getPublicReportLikeTargetCnt(reportDto) == 0) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        // 이미 좋아요를 누른 상태면 취소하고, 아직 누르지 않은 상태면 등록하는 토글 방식입니다.
        if (reportMapper.dupReportLike(reportDto) > 0) {
            reportMapper.delReportLike(reportDto);
        } else {
            reportMapper.setReportLike(reportDto);
        }

        return reportMapper.getReportLikeDtl(reportDto);
    }

    /**
     * 독후감과 책 기본 정보를 함께 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportDto 저장할 독후감 정보
     * @return 저장된 독후감 정보
     */
    @Override
    @Transactional
    public ReportDto setReport(Long userNumb, ReportDto reportDto) {
        reportDto.setUserNumb(userNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, true);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
        validatePublicFlag(reportDto);
        validateReportContentBytes(reportDto);

        // 같은 ISBN 책이 없으면 책 정보를 먼저 등록하고, 이미 있으면 기존 책 번호를 연결합니다.
        if (reportMapper.dupBook(reportDto) == 0) {
            reportMapper.setBook(reportDto);
        } else {
            reportDto.setBookNumb(reportMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        reportMapper.setReport(reportDto);
        return reportDto;
    }

    /**
     * 로그인한 회원의 독후감을 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 수정할 독후감 번호
     * @param reportDto 수정할 독후감 정보
     * @return 수정된 독후감 정보
     */
    @Override
    public ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto) {
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, false);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
        validatePublicFlag(reportDto);
        validateReportContentBytes(reportDto);

        reportMapper.uptReport(reportDto);
        return reportDto;
    }

    /**
     * 로그인한 회원의 독후감을 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인한 회원 번호
     * @param reportNumb 삭제할 독후감 번호
     * @return 삭제 건수
     */
    @Override
    public int delReport(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.delReport(reportDto);
    }

    /**
     * 책장 색상 코드가 비어 있으면 공통코드의 첫 번째 색상으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 보정할 독후감 DTO
     */
    private void setDefaultReportColor(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getReportColr()) || reportDto.getReportColr().isBlank()) {
            reportDto.setReportColr(codeUtil.getFirstCode(Constant.CODE_BOOK_COLR));
        }
    }

    /**
     * 공개 여부가 비어 있으면 비공개 기본값으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 보정할 독후감 DTO
     */
    private void setDefaultPublicFlag(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getPubcYsno()) || reportDto.getPubcYsno().isBlank()) {
            reportDto.setPubcYsno(Constant.COMM_NO);
        }
    }

    /**
     * 독후감 저장 전 HTML entity 변환 없이 원문 기준 문자열을 정리합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 정리할 독후감 DTO
     * @param includeBookFields 책 정보 필드까지 정리할지 여부
     */
    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {
        reportDto.setReportStat(StringUtil.normalizePlainText(reportDto.getReportStat()));
        reportDto.setReportStdt(StringUtil.normalizePlainText(reportDto.getReportStdt()));
        reportDto.setReportEndt(StringUtil.normalizePlainText(reportDto.getReportEndt()));
        reportDto.setReportGrde(StringUtil.normalizePlainText(reportDto.getReportGrde()));
        reportDto.setReportColr(StringUtil.normalizePlainText(reportDto.getReportColr()));
        reportDto.setPubcYsno(StringUtil.normalizePlainText(reportDto.getPubcYsno()));
        reportDto.setReportCntn(StringUtil.normalizePlainText(reportDto.getReportCntn()));

        // 등록은 책 정보까지 함께 들어오고, 수정은 독후감 정보만 수정하므로 플래그로 처리 범위를 분리합니다.
        if (includeBookFields) {
            reportDto.setBookTitl(StringUtil.normalizePlainText(reportDto.getBookTitl()));
            reportDto.setBookAthr(StringUtil.normalizePlainText(reportDto.getBookAthr()));
            reportDto.setBookPubl(StringUtil.normalizePlainText(reportDto.getBookPubl()));
            reportDto.setBookIsbn(StringUtil.normalizePlainText(reportDto.getBookIsbn()));
            reportDto.setBookCvim(StringUtil.normalizePlainText(reportDto.getBookCvim()));
            reportDto.setBookDesc(StringUtil.normalizePlainText(reportDto.getBookDesc()));
        }
    }

    /**
     * 독후감 내용의 UTF-8 byte 길이가 DB 컬럼 한도를 넘지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportContentBytes(ReportDto reportDto) {
        if (XssUtil.utf8ByteLength(reportDto.getReportCntn()) > Constant.REPORT_CONTENT_MAX_BYTES) {
            throw new CustomException(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 독서 상태 코드가 공통코드에 등록된 값인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportStatus(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_READ_STAT, reportDto.getReportStat())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 책장 색상 코드가 공통코드에 등록된 값인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validateReportColor(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_BOOK_COLR, reportDto.getReportColr())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 공개 여부 값이 Y 또는 N인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param reportDto 검증할 독후감 DTO
     */
    private void validatePublicFlag(ReportDto reportDto) {
        if (!Constant.COMM_YES.equals(reportDto.getPubcYsno()) && !Constant.COMM_NO.equals(reportDto.getPubcYsno())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }
}
