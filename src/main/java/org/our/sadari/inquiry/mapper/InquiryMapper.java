package org.our.sadari.inquiry.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.inquiry.dto.InquiryAnswerDto;
import org.our.sadari.inquiry.dto.InquiryDto;

/**
 * fileName       : InquiryMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의와 답변 데이터에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface InquiryMapper {

    String getUserStat(@Param("userNumb") Long userNumb);

    int getInquiryCategoryCnt(@Param("inqrCatg") String inqrCatg);

    Long getLatestSuspensionNumb(@Param("userNumb") Long userNumb);

    int getOpenSuspensionInquiryCnt(@Param("userNumb") Long userNumb, @Param("spndNumb") Long spndNumb);

    int setInquiry(@Param("inquiry") InquiryDto inquiry, @Param("userNumb") Long userNumb);

    List<InquiryDto> getInquiryList(@Param("userNumb") Long userNumb, @Param("startRow") int startRow
            , @Param("pageSize") int pageSize);

    InquiryDto getInquiryDtl(@Param("userNumb") Long userNumb, @Param("inqrNumb") Long inqrNumb);

    List<InquiryAnswerDto> getInquiryAnswerList(@Param("inqrNumb") Long inqrNumb);

    int uptInquiryAnswerRead(@Param("userNumb") Long userNumb, @Param("inqrNumb") Long inqrNumb);
}
