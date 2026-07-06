package org.our.sadari.sadariBook.service;

import java.util.List;
import org.our.sadari.sadariBook.dto.ReportDto;

public interface BookService {

    // 로그인 사용자 번호로 독후감을 저장한다.
    ReportDto setReport(Long userNumb, ReportDto reportDto);

    // 로그인 사용자 번호와 독후감 번호로 상세 내용을 조회한다.
    ReportDto getDetail(Long userNumb, Long reportNumb);

    // 로그인 사용자 번호로 독후감 목록을 조회한다.
    List<ReportDto> getBookList(Long userNumb);

    // 로그인 사용자 번호와 독후감 번호로 독후감을 수정한다.
    ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto);

    // 로그인 사용자 번호와 독후감 번호로 독후감을 삭제한다.
    int delReport(Long userNumb, Long reportNumb);
}
