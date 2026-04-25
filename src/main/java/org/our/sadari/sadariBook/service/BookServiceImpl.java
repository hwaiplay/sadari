package org.our.sadari.sadariBook.service;

import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.entity.BookEntity;
import org.our.sadari.sadariBook.entity.BookReportEntity;
import org.our.sadari.sadariBook.repository.BookReportRepository;
import org.our.sadari.sadariBook.repository.BookRepository;
import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.our.sadari.sadariUser.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookReportRepository bookReportRepository;
    private final UserRepository userRepository;

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
        BookEntity bookEntity = bookRepository.findByBookIsbn(request.getIsbn())
            .orElseGet(() -> {
                // 없으면 새로 저장
                BookEntity newBook = BookEntity.builder()
                    .bookIsbn(request.getIsbn())
                    .bookTitl(request.getTitle())
                    .bookAthr(request.getAuthor())
                    .bookPubl(request.getPublisher())
                    .bookDesc(request.getDescription())
                    .bookCvim(request.getImage())
                    .build();

                return bookRepository.save(newBook);
            });

        // 독후감 저장
        BookReportEntity reportEntity = BookReportEntity.builder()
            .book(bookEntity)
            .user(user)
            .bookStat(request.getStatus())
            .bookStdt(request.getStartDate())
            .bookEndt(request.getEndDate())
            .bookGrde((request.getGrade()))
            .bookCntn(request.getContent())
            .build();

        BookReportEntity saved = bookReportRepository.save(reportEntity);
       
        return saved.getBookNumb();
    }

    /**
     * 독후감 상세보기 로직
     */
    @Override
    public AddBookReportDto getDetail(Long id) {

        // 독후감 조회
        AddBookReportDto detail = bookReportRepository.findDetail(id);

        if (detail == null) {
            throw new RuntimeException("데이터 없음");
        }

        return detail;

    }

    /**
     * 독후감 리스트 로직
     */
    // @Override
    // public List<BookReportDto> getBookList() {
        
    //     // userId 임시로 입력
    //     List<BookReportDto> entity = bookReportRepository.findAllByUserNumb("4798174319")
    //         .orElseThrow(() -> new RuntimeException("데이터 없음"));

    //     List<BookReportDto> list = new ArrayList<BookReportDto>();

    //     return list;
    // }
}
