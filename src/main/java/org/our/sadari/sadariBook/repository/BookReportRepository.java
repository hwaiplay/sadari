package org.our.sadari.sadariBook.repository;

import java.util.List;

import org.our.sadari.sadariBook.dto.AddBookReportDto;
import org.our.sadari.sadariBook.dto.BookReportDto;
import org.our.sadari.sadariBook.entity.BookReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * packageName    : org.our.sadari.sadariBook.repository
 * fileName       : BookReportRepository.java
 * author         : hanwon.Jang
 * date           : 2026-04-21
 * description    : "독후감"에 관한 레포지토리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-21       hanwon.Jang       최초 생성
 * 2026-04-25       hanwon.Jang       독후감 상세보기 로직
 */

public interface BookReportRepository extends JpaRepository<BookReportEntity, Long> {
    /**
    * 독후감 상세보기
    * @param bookNumb
    * @return 독후감 상세
    */
    @Query("""
        SELECT new org.our.sadari.sadariBook.dto.AddBookReportDto(
            b.bookTitl,
            b.bookAthr,
            b.bookPubl,
            b.bookIsbn,
            b.bookCvim,
            b.bookDesc,
            r.bookStat,
            r.bookStdt,
            r.bookEndt,
            r.bookGrde,
            r.bookCntn
        )
        FROM BookReportEntity r
        LEFT JOIN r.book b
        WHERE r.book.bookNumb = :bookNumb
    """)
    List<AddBookReportDto> findDetail(Long bookNumb); 

    /**
     * 독후감 리스트 조회
     * @param user
     * @return 독후감 리스트
     */
    @Query("""
        SELECT new org.our.sadari.sadariBook.dto.AddBookReportDto(
            b.bookTitl,
            r.book.bookNumb,
            r.user.userNumb
        )
        FROM BookReportEntity r
        LEFT JOIN r.book b
        WHERE r.user.userNumb = :user
    """)
    List<BookReportDto> findAllByUser(Long user);
}