package org.our.sadari.sadariBook.service;

import java.util.List;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.ReportDto;

public interface BookService {

    /**
     * 독후감 등록
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportDto
     * @return
     */
    ReportDto setReport(Long userNumb, ReportDto reportDto);

    /**
     * 독후감 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    ReportDto getDetail(Long userNumb, Long reportNumb);

    /**
     * 도서 정보 상세 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    BookDto getBookInfo(Long userNumb, Long reportNumb);

    /**
     * 독후감 리스트 조회
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @return
     */
    List<ReportDto> getBookList(Long userNumb);

    /**
     * 독후감 수정
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @param reportDto
     * @return
     */
    ReportDto uptReport(Long userNumb, Long reportNumb, ReportDto reportDto);

    /**
     * 독후감 삭제
     * @Author SeungHyeon.Kang
     * @param userNumb
     * @param reportNumb
     * @return
     */
    int delReport(Long userNumb, Long reportNumb);
}
