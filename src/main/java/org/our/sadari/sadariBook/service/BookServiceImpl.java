package org.our.sadari.sadariBook.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.our.sadari.global.common.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final ReportMapper reportMapper;

    /**
     * 독후감 기록 로직
     */
    @Override
    @Transactional
    public ReportDto setReport(ReportDto reportDto) {

        int bookCount = 0;

        reportDto.setUserNumb(Long.valueOf(1));

        // 책 중복 검사
        bookCount = reportMapper.dupBook(reportDto);

        // 책 저장 안 되어 있어야 책 저장
        if (bookCount == 0) {
            reportMapper.setBook(reportDto);
        }

        // 독후감 저장
        reportMapper.setReport(reportDto);

        return reportDto;
    }

    /**
     * 독후감 리스트 로직
     */
    @Override
    public List<ReportDto> getBookList(ReportDto reportDto) {

        // 리스트 조회
        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("독후감 리스트 조회 완료 {}", list);

        return list;
    }

    /**
     * 독후감 상세보기 로직
     */
    @Override
    public ReportDto getDetail(Long reportNumb) {

        ReportDto book = new ReportDto();

        // 독후감 번호 설정
        book.setReportNumb(reportNumb);

        // 유저 번호 설정 (임시)
        book.setUserNumb(Long.valueOf(1));

        // 독후감 조회
        ReportDto detail = reportMapper.getReportDtl(book);

        return detail;

    }

    /**
     * 독후감 수정
     */
    @Override
    public ReportDto uptReport(ReportDto reportDto) {

        // 독후감 수정
        reportMapper.uptReport(reportDto);

        return reportDto;
    }

    /**
     * 독후감 삭제
     */
    @Override
    public int delReport(ReportDto reportDto) {

        return reportMapper.delReport(reportDto);
    }
}
