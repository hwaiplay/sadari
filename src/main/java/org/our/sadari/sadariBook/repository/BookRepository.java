package org.our.sadari.sadariBook.repository;

import java.util.Optional;
import org.our.sadari.sadariBook.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * fileName       : BookRepository
 * author         : hanWon.Jang
 * date           : 2026-04-04
 * description    : "책"에 대한 레포지토리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-04       hanWon.Jang     최초 생성
 */

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    Optional<BookEntity> findByBookIsbn(String id);
}