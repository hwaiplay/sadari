package org.our.sadari.book.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.book.dto.BookDto;

/**
 * fileName       : BookMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 도서 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface BookMapper {

    /**
     * ISBN 기준으로 이미 등록된 도서가 있는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param bookDto ISBN을 포함한 도서 정보
     * @return 중복 도서 수
     */
    int dupBook(BookDto bookDto);

    /**
     * ISBN 기준으로 기존 도서 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param bookIsbn 조회할 도서 ISBN
     * @return 도서 번호
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 신규 도서 정보를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param bookDto 등록할 도서 정보
     * @return 반영 건수
     */
    int setBook(BookDto bookDto);

}
