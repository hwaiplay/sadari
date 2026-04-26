package org.our.sadari.sadariBook.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.our.sadari.sadariBook.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * fileName       : BookRepository
 * author         : hanWon.Jang
 * date           : 2026-04-04
 * description    : "책"에 대한 레포지토리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-04       hanWon.Jang     최초 생성
 * 2026-04-22       hanWon.Jang     책 표지 조회 쿼리 추가
 */

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    // 저장된 책 검색
    Optional<BookEntity> findByBookIsbn(String id);

    // 책 표지 이미지 조회
    @Query("SELECT b.bookCvim FROM BookEntity b WHERE bookNumb = :id")
    String findImageById(@Param("id") Long id);

    // 리스트에서 책 번호로 저장된 책 검색
    BookEntity findByBookNumb(Long bookNumb);
}