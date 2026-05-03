package org.our.sadari.sadariBook.service;

import java.util.List;

import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.dto.BookReportDto;
import org.our.sadari.sadariBook.entity.BookEntity;
import org.our.sadari.sadariBook.entity.BookReportEntity;
import org.our.sadari.sadariBook.mapper.ReportMapper;
import org.our.sadari.sadariBook.repository.BookReportRepository;
import org.our.sadari.sadariBook.repository.BookRepository;
import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.our.sadari.sadariUser.auth.repository.UserRepository;
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

    /**
     * 독후감 기록 로직
     */
    @Override
    public Long createReport(AddBookReportDto request) {

        // 유저 조회
        // 테스트 단계라 유저 넘버 직접 입력함
        UserEntity user = userRepository.findByUserIdxx("4798174319")
            .orElseThrow(() -> new RuntimeException("유저 없음"));

        // 책 먼저 저장
        BookEntity bookEntity = bookRepository.findByBookIsbn(request.getBookIsbn())
            .orElseGet(() -> {
                // 없으면 새로 저장
                BookEntity newBook = BookEntity.builder()
                    .bookIsbn(request.getBookIsbn()) // isbn
                    .bookTitl(request.getBookTitl()) // 책 제목
                    .bookAthr(request.getBookAthr()) // 저자
                    .bookPubl(request.getBookPubl()) // 출판사
                    .bookDesc(request.getBookDesc()) // 책 소개 내용
                    .bookCvim(request.getBookCvim()) // 책 표지 이미지
                    .build();

                return bookRepository.save(newBook);
            });

        // 독후감 저장
        BookReportEntity reportEntity = BookReportEntity.builder()
            .book(bookEntity)
            .user(user)
            .bookStat(request.getBookStat()) // 독서 상태
            .bookStdt(request.getBookStdt()) // 독서 시작일
            .bookEndt(request.getBookEndt()) // 독서 종료일
            .bookGrde((request.getBookGrde())) // 별점
            .bookCntn(request.getBookCntn()) // 독후감 내용
            .build();

        BookReportEntity saved = bookReportRepository.save(reportEntity);
       
        return saved.getBook().getBookNumb();
    }

    /**
     * 독후감 리스트 로직
     */
    @Override
    public List<BookReportDto> getBookList() {

        BookReportDto book = new BookReportDto();
        book.setUserNumb(Long.valueOf(1));

        // 리스트 조회
        List<BookReportDto> list = reportMapper.getReportList(book);
        log.info("독후감 리스트 조회 완료 {}", list);

        return list;
    }

    /**
     * 독후감 상세보기 로직
     */
    @Override
    public BookReportDto getDetail(Long reportNumb) {

        BookReportDto book = new BookReportDto();
        book.setReportNumb(reportNumb);
        book.setUserNumb(Long.valueOf(1));
        // 독후감 조회
        BookReportDto detail = reportMapper.getReportDtl(book);

        if (detail == null) {
            throw new RuntimeException("데이터 없음");
        }

        return detail;

    }

    /**
     * 독후감 수정
     */
    @Override
    @Transactional
    public Long setReport(Long reportNumb, BookReportDto request) {

        BookReportEntity entity = bookReportRepository.findById(reportNumb)
            .orElseThrow(() -> new IllegalArgumentException("독후감 없음"));

        entity.update(request.getReportStat(), request.getReportStdt(), request.getReportEndt(), request.getReportGrde(), request.getReportCntn());

        return entity.getReportNumb();
    }

}
