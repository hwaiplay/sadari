package org.our.sadari.sadariBook.service;

import java.util.List;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.dto.BookReportDto;
import org.our.sadari.sadariBook.dto.HomeBookDto;

/**
 * packageName    : 
 * fileName       : BookService.java
 * author         : hanwon.Jang
 * date           : 2026-04-04
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-04       hanwon.Jang       최초 생성
 * 2026-04-08       hanwon.Jang       상세조회, 리스트 출력 추가
 */

public interface BookService {
    // 독후감 기록
    Long createReport(AddBookReportDto request);

    // 독후감 상세보기
    BookReportDto getDetail(Long reportNumb);

    // 독후감 리스트 출력 (홈화면)
    List<BookReportDto> getBookList();

    // 독후감 수정
    Long setReport(Long reportNumb, AddBookReportDto request);
}