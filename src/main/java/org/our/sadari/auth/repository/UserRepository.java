package org.our.sadari.auth.repository;
import org.our.sadari.auth.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * fileName       : UserRepository
 * author         : hanWon.Jang
 * date           : 2026-03-18
 * description    : 카카오 로그인 시 db조회
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-18       hanWon.Jang     최초 생성
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUserIdxx(String provierId);
}