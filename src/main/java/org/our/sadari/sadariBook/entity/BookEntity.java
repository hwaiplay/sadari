package org.our.sadari.sadariBook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/**
 * packageName    : 
 * fileName       : BookEntity.java
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : '독후감'을 정의한 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 */

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TM_BOOKXM")
@EntityListeners(AuditingEntityListener.class)
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tm_bookxm_seq_gen")
    @SequenceGenerator(
        name = "tm_bookxm_seq_gen",
        sequenceName = "TM_BOOKXM_SEQ",
        allocationSize = 1
    )

    // 독후감 번호
    @Column(name = "BOOK_NUMB")
    private Long bookNumb;
    
    // 유저와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUMB", nullable = false)
    private UserEntity userNumb;

    // 표지 이미지
    @Column(name = "BOOK_CVIM", nullable = false)
    private String bookCvim;

    // 독서 상태
    @Column(name = "BOOK_STAT", nullable = false)
    private String bookStat;

    // 독서 시작일
    @Column(name = "BOOK_STDT", nullable = false)
    private String bookStdt;
    
    // 독서 종료일
    @Column(name = "BOOK_ENDT", nullable = false)
    private String bookEndt;
}