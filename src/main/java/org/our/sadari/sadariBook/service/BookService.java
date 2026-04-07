package org.our.sadari.sadariBook.service;

import org.our.sadari.sadariBook.dto.BookReportDto;
import com.fasterxml.jackson.core.JsonParseException;

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
 */

public interface BookService {
    Long createReport(BookReportDto request);
}