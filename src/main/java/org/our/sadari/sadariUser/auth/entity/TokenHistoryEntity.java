package org.our.sadari.sadariUser.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * fileName       : TokenHistoryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Entity
@Table(name = "TB_TOKHIS")
public class TokenHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokhis_seq_gen")
    @SequenceGenerator(
            name = "tokhis_seq_gen",
            sequenceName = "TOKHIS_SEQ",
            allocationSize = 1
    )
    private Long id;

    // 유저와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUMB", nullable = false)
    private UserEntity userNumb;

    // refreshToken
    @Column(name = "REFR_TOKN", nullable = false, unique = true, length = 500)
    private String refrTokn;

    // 만료 시간
    @Column(name = "EXPR_DATE", nullable = false)
    private LocalDateTime exprDate;

    // 생성 시간
    @Column(name = "CRET_DATE", nullable = false)
    private LocalDateTime cretDate;

    protected TokenHistoryEntity() {}

    public TokenHistoryEntity(UserEntity userNumb, String refrTokn, LocalDateTime exprDate) {
        this.userNumb = userNumb;
        this.refrTokn = refrTokn;
        this.exprDate = exprDate;
        this.cretDate = LocalDateTime.now();
    }

    public boolean isExpired() {
        return exprDate.isBefore(LocalDateTime.now());
    }
}
