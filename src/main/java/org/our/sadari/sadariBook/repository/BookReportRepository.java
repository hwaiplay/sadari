package org.our.sadari.sadariBook.repository;

import org.our.sadari.sadariBook.entity.BookReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
 */

public interface BookReportRepository extends JpaRepository<BookReportEntity, Long> {

    // Page<BookReportEntity> findByUser(UserEntity user, Pageable pageable);

    // Optional<BookReportEntity> findById(Long id);
}