package org.our.sadari.sadariUser.auth.repository;

import jdk.jfr.Registered;
import org.our.sadari.sadariUser.auth.entity.TokenHistoryEntity;
import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

/**
 * fileName       : TokenHistoryRepository
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 토큰 관리 레포지토리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Registered
public interface TokenHistoryRepository extends JpaRepository<TokenHistoryEntity, Long> {

    // 유저 기준 삭제 (로그인 시 기존 토큰 제거)
    @Modifying
    void deleteByUserNumb(UserEntity userNumb);

    // 유저 기준 조회 (선택)
    Optional<TokenHistoryEntity> findByUserNumb(UserEntity userNumb);

    // refreshToken으로 조회
    Optional<TokenHistoryEntity> findByRefrTokn(String refrTokn);
}
