package org.our.sadari.book.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.book.dto.BookDto;

/**
 * 도서 마스터 테이블(TM_BOOK_INFO)을 단독으로 다루는 MyBatis Mapper입니다.
 * 독후감과 조인하지 않고 도서 자체의 중복 확인, 번호 조회, 신규 등록만 담당합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface BookMapper {

    /**
     * ISBN 기준으로 이미 등록된 도서가 있는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto ISBN을 포함한 도서 정보
     * @return 중복 도서 수
     */
    int dupBook(BookDto bookDto);

    /**
     * ISBN 기준으로 기존 도서 번호를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 도서 번호
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 신규 도서 정보를 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param bookDto 등록할 도서 정보
     * @return 반영 건수
     */
    int setBook(BookDto bookDto);

}
