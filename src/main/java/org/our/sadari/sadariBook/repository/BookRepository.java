package org.our.sadari.sadariBook.repository;

import java.util.Optional;

import org.our.sadari.sadariBook.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * fileName       : BookRepository
 * author         : hanWon.Jang
 * date           : 2026-04-04
 * description    : 
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-04       hanWon.Jang     최초 생성
 */

public interface BookRepository extends JpaRepository<BookEntity, Long> {
}