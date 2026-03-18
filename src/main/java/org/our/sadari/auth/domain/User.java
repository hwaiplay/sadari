package org.our.sadari.auth.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TM_USERXM")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tm_userxm_seq_gen")
    @SequenceGenerator(
        name = "tm_userxm_seq_gen",
        sequenceName = "TM_USERXM_SEQ",
        allocationSize = 1
    )
    @Column(name = "USER_NUMB")
    private Long userNumb;

    // @Column(name = "USER_EMIL", nullable = false, unique = true)
    // private String email;

    @Column(name = "USER_NICK", nullable = false, unique = true)
    private String nickname;

    // private String provider; // KAKAO

    // private String providerId; // 카카오 id
}