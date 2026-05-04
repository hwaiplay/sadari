package org.our.sadari.sadariBook.service;

import java.util.List;

import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.ReportDto;
import org.our.sadari.sadariBook.entity.BookEntity;
import org.our.sadari.sadariBook.entity.BookReportEntity;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.our.sadari.sadariBook.repository.BookReportRepository;
import org.our.sadari.sadariBook.repository.BookRepository;
import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.our.sadari.sadariUser.auth.repository.UserRepository;
import org.our.sadari.sadariUser.user.dto.UserDto;
import org.our.sadari.sadariUser.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookReportRepository bookReportRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;

    /**
     * 독후감 기록 로직
     */
    @Override
    public ReportDto setReport(ReportDto reportDto) {
        
        BookDto bookDto = reportDto;
        int result = 0;

        // 책 중복 검사
        result = reportMapper.dupBook(bookDto);
        if(result == 0) {
            // 책 저장
            result = reportMapper.setBook(bookDto);
        }

        ReportDto resultDto = new ReportDto();
        
        // 독후감 저장
        if(result == 1) {
            resultDto = reportMapper.setReport(reportDto);
        }

        return resultDto;
    }

    /**
     * 독후감 리스트 로직
     */
    @Override
    public List<ReportDto> getBookList() {

        ReportDto book = new ReportDto();
        book.setUserNumb(Long.valueOf(1));

        // 리스트 조회
        List<ReportDto> list = reportMapper.getReportList(book);
        log.info("독후감 리스트 조회 완료 {}", list);

        return list;
    }

    /**
     * 독후감 상세보기 로직
     */
    @Override
    public ReportDto getDetail(Long reportNumb) {

        ReportDto book = new ReportDto();
        book.setReportNumb(reportNumb);
        book.setUserNumb(Long.valueOf(1));
        // 독후감 조회
        ReportDto detail = reportMapper.getReportDtl(book);

        if (detail == null) {
            throw new RuntimeException("데이터 없음");
        }

        return detail;

    }

    /**
     * 독후감 수정
     */
    // @Override
    // @Transactional
    // public ReportDto uptReport(Long reportNumb, ReportDto request) {

    //     BookReportEntity entity = bookReportRepository.findById(reportNumb)
    //         .orElseThrow(() -> new IllegalArgumentException("독후감 없음"));

    //     entity.update(request.getReportStat(), request.getReportStdt(), request.getReportEndt(), request.getReportGrde(), request.getReportCntn());

    //     return entity.getReportNumb();
    // }

}
