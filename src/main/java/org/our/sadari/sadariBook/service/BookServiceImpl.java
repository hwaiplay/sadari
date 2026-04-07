package org.our.sadari.sadariBook.service;

import org.our.sadari.sadariBook.dto.BookReportDto;
import org.our.sadari.sadariBook.entity.BookEntity;
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
    private final UserRepository userRepository;

    // 독후감 기록 로직
    @Override
    public Long createReport(BookReportDto request) {

        // 유저 조회
        UserEntity user = userRepository.findByUserIdxx("4798174319")
        .orElseThrow(() -> new RuntimeException("유저 없음"));

        BookEntity entity = BookEntity.builder()
            .userNumb(user)
            .bookCvim(request.getCoverImage())
            .bookStat(request.getStatus())
            .bookStdt(request.getStartDate())
            .bookEndt(request.getEndDate())
            .bookGrde(request.getGrade())
            .bookCntn(request.getContent())
            .build();
            
        BookEntity saved = bookRepository.save(entity);
       
        return saved.getBookNumb();
    }
}
