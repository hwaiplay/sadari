package org.our.sadari.sadariBook.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariBook.dto.BookReportDto;

import java.util.List;

/**
 * fileName       : ReportMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-05-03
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03        SeungHyeon.Kang       최초 생성
 */
@Mapper
public interface ReportMapper {

    /**
     * 독후감 리스트
     * @param req
     * @return
     */
    List<BookReportDto> getReportList(BookReportDto req);

    /**
     * 독후감 상세
     * @param req
     * @return
     */
    BookReportDto getReportDtl(BookReportDto req);
}
