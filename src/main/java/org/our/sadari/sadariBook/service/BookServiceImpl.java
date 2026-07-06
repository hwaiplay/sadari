package org.our.sadari.sadariBook.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final ReportMapper reportMapper;

    @Override
    @Transactional
    public ReportDto setReport(Long userNumb, ReportDto reportDto) {
        // 사용자 번호는 요청 본문이 아니라 인증 정보에서 받은 값으로 설정한다.
        reportDto.setUserNumb(userNumb);

        // 책이 없으면 새로 저장하고, 이미 있으면 기존 책 번호를 독후감에 연결한다.
        if (reportMapper.dupBook(reportDto) == 0) {
            reportMapper.setBook(reportDto);
        } else {
            reportDto.setBookNumb(reportMapper.getBookNumbByIsbn(reportDto.getBookIsbn()));
        }

        reportMapper.setReport(reportDto);
        return reportDto;
    }

    @Override
    public List<ReportDto> getBookList(Long userNumb) {
        // 목록 조회 조건은 로그인 사용자 번호만 사용한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);

        List<ReportDto> list = reportMapper.getReportList(reportDto);
        log.info("Book report list lookup completed. userNumb={}, size={}", userNumb, list.size());
        return list;
    }

    @Override
    public ReportDto getDetail(Long userNumb, Long reportNumb) {
        // 상세 조회는 로그인 사용자 번호와 독후감 번호를 함께 사용한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.getReportDtl(reportDto);
    }

    @Override
    public ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto) {
        // 수정 대상은 인증 사용자와 경로의 독후감 번호로 확정한다.
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        reportMapper.uptReport(reportDto);
        return reportDto;
    }

    @Override
    public int delReport(Long userNumb, Long reportNumb) {
        // 삭제 대상은 인증 사용자와 경로의 독후감 번호로 확정한다.
        ReportDto reportDto = new ReportDto();
        reportDto.setUserNumb(userNumb);
        reportDto.setReportNumb(reportNumb);

        return reportMapper.delReport(reportDto);
    }
}
