package org.our.sadari.sadariBook.service;

import org.our.sadari.sadariBook.dto.BookReportDto;
import org.our.sadari.sadariBook.repository.BookRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    public BookReportDto createReport(BookReportDto request) throws JsonParseException {
        return request;
    }
}
