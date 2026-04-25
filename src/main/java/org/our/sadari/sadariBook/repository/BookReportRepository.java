package org.our.sadari.sadariBook.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
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
    * @param 독후감 id
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
        JOIN r.book b
        WHERE r.id = :id
    """)
    AddBookReportDto findDetail(Long id); 

    /**
     * 독후감 리스트 조회
     * @param userIdxx
     * @return 독후감 리스트
     */
    List<BookReportDto> findAllByUserIdxx(String userIdxx);
}