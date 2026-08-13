package org.our.sadari.inquiry.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.inquiry.dto.InquiryCreateDto;
import org.our.sadari.inquiry.dto.InquiryDto;
import org.our.sadari.inquiry.mapper.InquiryMapper;
import org.springframework.context.support.StaticMessageSource;

/**
 * fileName       : InquiryServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 정지 회원의 이의제기 조회와 카테고리 제한을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    // 고객문의 데이터 접근 Mock
    @Mock
    private InquiryMapper inquiryMapper;
    // 고객문의 서비스 단위 테스트 대상
    private InquiryServiceImpl inquiryService;

    /** 각 테스트에 독립된 고객문의 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("common.alert.0009", Locale.KOREAN, "요청값이 올바르지 않아요.");
        messageSource.addMessage("common.alert.0009", Locale.getDefault(), "요청값이 올바르지 않아요.");
        new MessageUtils().setMessageSource(messageSource);
        inquiryService = new InquiryServiceImpl(inquiryMapper);
    }

    /** 정지 회원에게 현재 정지 이후 접수한 최신 이의제기 번호를 반환한다. */
    @Test
    void getSuspInquiryNumbReturnsLatest() {
        when(inquiryMapper.getUserStat(7L)).thenReturn("SUSPENDED");
        when(inquiryMapper.getSuspInquiryNumb(7L)).thenReturn(31L);

        ResultData result = inquiryService.getSuspInquiryNumb(7L);

        assertEquals(200, result.getCode());
        assertEquals(31L, result.getData());
    }

    /** 정지 회원이 이의제기 외 문의 유형을 보내면 저장하지 않는다. */
    @Test
    void setInquiryRejectsOtherCategoryForSuspended() {
        InquiryCreateDto inquiry = new InquiryCreateDto();
        inquiry.setInqrCatg("GENERAL");
        inquiry.setInqrTitl("문의 제목");
        inquiry.setInqrCntn("문의 내용");
        when(inquiryMapper.getUserStat(7L)).thenReturn("SUSPENDED");
        when(inquiryMapper.getInquiryCategoryCnt("GENERAL")).thenReturn(1);

        ResultData result = inquiryService.setInquiry(7L, inquiry);

        assertEquals(2009, result.getCode());
        verify(inquiryMapper, never()).setInquiry(any(InquiryDto.class), anyLong());
    }
}
