package org.our.sadari.sadariBook.entity;

import org.our.sadari.sadariUser.auth.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * packageName    : org.our.sadari.sadariBook.entity
 * fileName       : BookReportEntity.java
 * author         : hanwon.Jang
 * date           : 2026-04-09
 * description    : 독후감을 정의한 Entity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-09       hanwon.Jang       최초 생성
 */

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TM_BOOK_REPORT")
public class BookReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tm_bookxm_seq_gen")
    @SequenceGenerator(
        name = "tm_bookxm_seq_gen",
        sequenceName = "TM_BOOKXM_SEQ",
        allocationSize = 1
    )

    // 독후감 번호
    @Column(name = "REPORT_NUMB")
    private Long bookNumb;
    
    // 유저와 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NUMB", nullable = false)
    private UserEntity userNumb;

    // 책과 연결 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "BOOK_NUMB",              // 내가 사용할 FK 컬럼명
        referencedColumnName = "BOOK_NUMB" // BookEntity의 PK 컬럼명)
    )
    private BookEntity book;

    // 독서 상태
    @Column(name = "REPORT_STAT", nullable = false)
    private String bookStat;

    // 독서 시작일
    @Column(name = "REPORT_STDT", nullable = false)
    private String bookStdt;
    
    // 독서 종료일
    @Column(name = "REPORT_ENDT", nullable = false)
    private String bookEndt;
    
    // 평점
    @Column(name = "REPORT_GRDE", nullable = false)
    private String bookGrde;

    // 기록 내용
    @Column(name = "REPORT_CNTN", nullable = false)
    private String bookCntn;
}
