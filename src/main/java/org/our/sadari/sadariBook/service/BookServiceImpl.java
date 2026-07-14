package org.our.sadari.sadariBook.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final ReportMapper reportMapper;
    private final CodeUtil codeUtil;

    /**
     * 독후감 리스트 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @return
     */
    @Override
    public List<ReportDto> getBookList(Long userNumb, String bookKeyword, String sortType) {
        // 목록 조회 조건은 로그인 사용자 번호, 책 제목 검색어, 정렬 코드를 함께 사용한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookKeyword(XssUtil.escape(bookKeyword));
        reportDto.setSortType(normalizeListSortType(sortType));

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("Book report list lookup completed. userNumb={}, size={}", userNumb, list.size());
        return list;
    }

    /**
     * 독후감 목록 정렬값을 허용된 정렬 코드로 보정한다.
     * @Author SeungHyeon.Kang
     * @param sortType 화면에서 전달한 정렬 코드
     * @return Mapper에서 사용할 정렬 코드
     */
    private String normalizeListSortType(String sortType) {
        if (Constant.SORT_START_DATE_DESC.equals(sortType) || Constant.SORT_GRADE_DESC.equals(sortType)) {
            return sortType;
        }

        return Constant.SORT_END_DATE_DESC;
    }

    /**
     * 독후감 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    @Override
    public ReportDto getDetail(Long userNumb, Long reportNumb) {
        // 상세 조회는 로그인 사용자 번호와 독후감 번호를 함께 사용한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.getReportDtl(reportDto);
    }

    /**
     * 도서 정보 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    @Override
    public BookDto getBookInfo(Long userNumb, Long reportNumb) {
        // 책 정보도 독후감 소유자 조건을 함께 사용해 조회한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.getBookInfo(reportDto);
    }

    /**
     * 기준 독후감과 같은 도서의 공개 독후감 목록을 조회한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param reportNumb 기준 독후감 번호
     * @return 다른 사용자가 공개한 독후감 목록
     */
    @Override
    public List<ReportDto> getPublicReportsByReport(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.getPublicReportList(reportDto);
    }

    /**
     * ISBN이 같은 도서의 공개 독후감 목록을 조회한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param bookIsbn 도서 ISBN
     * @return 다른 사용자가 공개한 독후감 목록
     */
    @Override
    public List<ReportDto> getPublicReportsByIsbn(Long userNumb, String bookIsbn) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setBookIsbn(XssUtil.escape(bookIsbn));

        return reportMapper.getPublicReportList(reportDto);
    }

    /**
     * ISBN 기준으로 전체 독후감 평균 별점을 조회한다.
     * @Author SeungHyeon.Kang
     * @param bookIsbn 도서 ISBN
     * @return 전체 독후감 평균 별점
     */
    @Override
    public BigDecimal getPublicRatingAverageByIsbn(String bookIsbn) {
        return reportMapper.getPublicRatingAverageByIsbn(XssUtil.escape(bookIsbn));
    }

    /**
     * 공개 독후감 좋아요 상태를 토글한다.
     * @Author SeungHyeon.Kang
     * @param userNumb 현재 로그인 사용자 번호
     * @param reportNumb 좋아요 대상 독후감 번호
     * @return 변경 후 좋아요 수와 현재 사용자 좋아요 여부
     */
    @Override
    @Transactional
    public ReportDto setReportLike(Long userNumb, Long reportNumb) {
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        if (reportMapper.getPublicReportLikeTargetCnt(reportDto) == 0) {
            // 본인 글, 비공개 글, 존재하지 않는 글에는 좋아요를 저장하지 않는다.
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        if (reportMapper.dupReportLike(reportDto) > 0) {
            reportMapper.delReportLike(reportDto);
        } else {
            reportMapper.setReportLike(reportDto);
        }

        return reportMapper.getReportLikeDtl(reportDto);
    }

    /**
     * 독후감 등록
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportDto
     * @return
     */
    @Override
    @Transactional
    public ReportDto setReport(Long userNumb, ReportDto reportDto) {
        // 사용자 번호는 요청 본문이 아니라 인증 정보에서 받은 값으로 설정한다.
        reportDto.setUserNumb(userNumb);
        setDefaultReportColor(reportDto);
        setDefaultPublicFlag(reportDto);
        sanitizeReport(reportDto, true);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
        validatePublicFlag(reportDto);
        validateReportContentBytes(reportDto);

        if (reportMapper.dupBook(reportDto) == 0) {
            // ISBN 기준으로 등록된 책이 없으면 책 정보를 먼저 저장한 뒤 독후감을 연결한다.
            reportMapper.setBook(reportDto);
        } else {
            // 이미 등록된 책이면 중복 저장하지 않고 기존 책 번호만 독후감에 연결한다.
            reportDto.setBookNumb(reportMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        reportMapper.setReport(reportDto);
        return reportDto;
    }

    /**
     * 독후감 수정
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @param reportDto
     * @return
     */
    @Override
    public ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto) {
        // 수정 대상은 인증 사용자와 경로의 독후감 번호로 확정한다.
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
     * 독후감 삭제
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    @Override
    public int delReport(Long userNumb, Long reportNumb) {
        // 삭제 대상은 인증 사용자와 경로의 독후감 번호로 확정한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.delReport(reportDto);
    }

    /**
     * 책장 기본 색상 설정
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
     */
    private void setDefaultReportColor(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getReportColr()) || reportDto.getReportColr().isBlank()) {
            // 프론트에서 색상이 누락된 예외 상황에도 DB 필수값을 채우기 위해 기본색을 사용한다.
            reportDto.setReportColr(codeUtil.getFirstCode(Constant.CODE_BOOK_COLR));
        }
    }

    /**
     * 독후감 입력값 XSS 필터링
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @param includeBookFields
     * @return
     */
    /**
     * 공개 여부 값이 비어 있으면 비공개를 기본값으로 설정한다.
     * @Author SeungHyeon.Kang
     * @param reportDto 공개 여부를 보정할 독후감 DTO
     * @return
     */
    private void setDefaultPublicFlag(ReportDto reportDto) {
        if (StringUtil.isEmpty(reportDto.getPubcYsno()) || reportDto.getPubcYsno().isBlank()) {
            // 공개 여부가 누락되면 의도치 않은 노출을 막기 위해 비공개로 저장한다.
            reportDto.setPubcYsno(Constant.COMM_NO);
        }
    }

    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {
        reportDto.setReportStat(XssUtil.escape(reportDto.getReportStat()));
        reportDto.setReportStdt(XssUtil.escape(reportDto.getReportStdt()));
        reportDto.setReportEndt(XssUtil.escape(reportDto.getReportEndt()));
        reportDto.setReportGrde(XssUtil.escape(reportDto.getReportGrde()));
        reportDto.setReportColr(XssUtil.escape(reportDto.getReportColr()));
        reportDto.setPubcYsno(XssUtil.escape(reportDto.getPubcYsno()));
        reportDto.setReportCntn(XssUtil.escape(reportDto.getReportCntn()));

        if (includeBookFields) {
            // 등록 요청은 책 정보도 함께 저장하므로 책 관련 필드까지 필터링한다.
            reportDto.setBookTitl(XssUtil.escape(reportDto.getBookTitl()));
            reportDto.setBookAthr(XssUtil.escape(reportDto.getBookAthr()));
            reportDto.setBookPubl(XssUtil.escape(reportDto.getBookPubl()));
            reportDto.setBookIsbn(XssUtil.escape(reportDto.getBookIsbn()));
            reportDto.setBookCvim(XssUtil.escape(reportDto.getBookCvim()));
            reportDto.setBookDesc(XssUtil.escape(reportDto.getBookDesc()));
        }
    }

    /**
     * 독후감 내용 DB 바이트 제한 검증
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
     */
    private void validateReportContentBytes(ReportDto reportDto) {
        if (XssUtil.utf8ByteLength(reportDto.getReportCntn()) > Constant.REPORT_CONTENT_MAX_BYTES) {
            // XSS escape 이후 실제 DB에 저장될 문자열이 VARCHAR2(4000 BYTE)를 넘으면 ORA-01461이 발생하므로 저장 전 차단한다.
            throw new CustomException(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, HttpStatus.BAD_REQUEST); // 독후감 내용 바이트 초과 응답이다.
        }
    }

    private void validateReportStatus(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_READ_STAT, reportDto.getReportStat())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateReportColor(ReportDto reportDto) {
        if (!codeUtil.existsCode(Constant.CODE_BOOK_COLR, reportDto.getReportColr())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePublicFlag(ReportDto reportDto) {
        if (!Constant.COMM_YES.equals(reportDto.getPubcYsno()) && !Constant.COMM_NO.equals(reportDto.getPubcYsno())) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }
}
