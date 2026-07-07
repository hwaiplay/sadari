package org.our.sadari.sadariBook.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariBook.dto.BookDto;
import org.our.sadari.sadariBook.dto.ReportDto;

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
 * 2026-05-03        Hanwon.Jang           독후감 저장 매퍼 추가
 */
@Mapper
public interface ReportMapper {

    /**
     * 독후감 리스트 조회
     * @Author SeungHyeon.Kang
     * @param req
     * @return
     */
    List<ReportDto> getReportList(ReportDto req);

    /**
     * 독후감 상세 조회
     * @Author SeungHyeon.Kang
     * @param req
     * @return
     */
    ReportDto getReportDtl(ReportDto req);

    /**
     * 도서 정보 상세 조회
     * @Author SeungHyeon.Kang
     * @param req
     * @return
     */
    BookDto getBookInfo(ReportDto req);

    /**
     * 책 중복 검사
     * @Author SeungHyeon.Kang
     * @param bookDto
     * @return
     */
    int dupBook(ReportDto bookDto);

    /**
     * 책 중복일 때 bookNumb 조회
     * @Author SeungHyeon.Kang
     * @param bookIsbn
     * @return
     */
    Long getBookNumbByIsbn(String bookIsbn);

    /**
     * 책 저장
     * @Author SeungHyeon.Kang
     * @param bookDto
     * @return
     */
    int setBook(ReportDto bookDto);
    
    /**
     * 독후감 저장
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
     */
    int setReport(ReportDto reportDto);
    
    /**
     * 독후감 수정
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
     */
    int uptReport(ReportDto reportDto);
    
    /** 
     * 독후감 삭제
     * @Author SeungHyeon.Kang
     * @param reportDto
     * @return
     */
    int delReport(ReportDto reportDto);
}
