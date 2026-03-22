package org.our.sadari.auth.repository;

import jdk.jfr.Registered;
import org.our.sadari.auth.entity.TokenHistoryEntity;
import org.our.sadari.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * fileName       : TokenHistoryRepository
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Registered
public interface TokenHistoryRepository extends JpaRepository<TokenHistoryEntity, Long> {

    // 유저 기준 삭제 (로그인 시 기존 토큰 제거)
    void deleteByUserNumb(UserEntity userNumb);

    // 유저 기준 조회 (선택)
    Optional<TokenHistoryEntity> findByUserNumb(UserEntity userNumb);

    // refreshToken으로 조회
    Optional<TokenHistoryEntity> findByRefrTokn(String refrTokn);
}
