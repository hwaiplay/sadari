package org.our.sadari.sadariBook.service;

import java.util.List;

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
    // 한 책에 독후감 여러개일 수 있기 때문에 List 타입
    List<AddBookReportDto> getDetail(Long bookNumb);

    // 독후감 리스트 출력 (홈화면)
    List<HomeBookDto> getBookList(Long userNumb);
}