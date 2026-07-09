package org.our.sadari.sadariBook.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.code.util.CodeUtil;
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
    public List<ReportDto> getBookList(Long userNumb) {
        // 목록 조회 조건은 로그인 사용자 번호만 사용한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("Book report list lookup completed. userNumb={}, size={}", userNumb, list.size());
        return list;
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
        sanitizeReport(reportDto, true);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
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
        sanitizeReport(reportDto, false);
        validateReportStatus(reportDto);
        validateReportColor(reportDto);
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
        if (reportDto.getReportColr() == null || reportDto.getReportColr().isBlank()) {
            // 프론트에서 색상이 누락된 예외 상황에도 DB 필수값을 채우기 위해 기본색을 사용한다.
            codeUtil.getCodeList("BOOK_COLR").stream()
                    .findFirst()
                    .ifPresent(code -> reportDto.setReportColr(code.getComdCode()));
        }
    }

    /**
     * 독후감 입력값 XSS 필터링
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @param includeBookFields
     * @return
     */
    private void sanitizeReport(ReportDto reportDto, boolean includeBookFields) {
        reportDto.setReportStat(XssUtil.escape(reportDto.getReportStat()));
        reportDto.setReportStdt(XssUtil.escape(reportDto.getReportStdt()));
        reportDto.setReportEndt(XssUtil.escape(reportDto.getReportEndt()));
        reportDto.setReportGrde(XssUtil.escape(reportDto.getReportGrde()));
        reportDto.setReportColr(XssUtil.escape(reportDto.getReportColr()));
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
        boolean isValidStatus = codeUtil.getCodeList("READ_STAT").stream()
                .anyMatch(code -> code.getComdCode().equals(reportDto.getReportStat()));

        if (!isValidStatus) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateReportColor(ReportDto reportDto) {
        boolean isValidColor = codeUtil.getCodeList("BOOK_COLR").stream()
                .anyMatch(code -> code.getComdCode().equalsIgnoreCase(reportDto.getReportColr()));

        if (!isValidColor) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
    }
}
