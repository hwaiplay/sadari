package org.our.sadari.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * fileName       : User
 * author         : hanWon.jang
 * date           : 2026-03-15
 * description    : '회원 정보'를 정의한 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-17        hanWon.jang       리팩터리 및 JWT 토큰 발급
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TM_USERXM")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tm_userxm_seq_gen")
    @SequenceGenerator(
        name = "tm_userxm_seq_gen",
        sequenceName = "TM_USERXM_SEQ",
        allocationSize = 1
    )
    
    //회원번호
    @Column(name = "USER_NUMB")
    private Long userNumb;
    
    //회원 닉네임
    @Column(name = "USER_NICK", nullable = false)
    private String nickname;
    
    //회원 로그인 소셜타입
    @Column(name = "USER_PROV")
    private String userProv;
    
    //소셜 ID 값
    @Column(name = "USER_IDXX", nullable = false, unique = true)
    private String userIdxx;

    //가입일
    @CreatedDate
    @Column(name = "JOIN_DATE", updatable = false)
    private LocalDateTime joinDate;

    //소셜 프로필 사진 경로
    @Column(name = "PROF_PATH", length = 500)
    private String porfPath;
}