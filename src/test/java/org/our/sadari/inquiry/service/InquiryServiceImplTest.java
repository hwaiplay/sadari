package org.our.sadari.inquiry.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.inquiry.dto.InquiryCreateDto;
import org.our.sadari.inquiry.dto.InquiryDto;
import org.our.sadari.inquiry.mapper.InquiryMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : InquiryServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 정지 회원의 이의제기 조회와 카테고리 제한을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    고객문의 제목과 본문 비속어 차단 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    // 고객문의 데이터 접근 Mock
    @Mock
    private InquiryMapper inquiryMapper;
    // 고객문의 비속어 검사 서비스 Mock
    @Mock
    private BadWordDetectionService badWordDetectionService;
    // 고객문의 서비스 단위 테스트 대상
    private InquiryServiceImpl inquiryService;

    /** 각 테스트에 독립된 고객문의 서비스를 생성함 */
    @BeforeEach
    void setUp() {
        // 실제 다국어 프로퍼티를 조회할 메시지 소스를 생성함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정함
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정함
        messageSource.setDefaultEncoding("UTF-8");
        // 실패 응답이 실제 공통 메시지 소스를 사용하도록 정적 조회 객체를 초기화함
        new MessageUtils().setMessageSource(messageSource);
        // 고객문의 서비스 단위 테스트 대상을 생성함
        inquiryService = new InquiryServiceImpl(inquiryMapper, badWordDetectionService);
    }

    /** 정지 회원에게 현재 정지 이후 접수한 최신 이의제기 번호를 반환함 */
    @Test
    void getSuspInquiryNumbReturnsLatest() {
        when(inquiryMapper.getUserStat(7L)).thenReturn("SUSPENDED");
        when(inquiryMapper.getSuspInquiryNumb(7L)).thenReturn(31L);

        ResultData result = inquiryService.getSuspInquiryNumb(7L);

        assertEquals(200, result.getCode());
        assertEquals(31L, result.getData());
    }

    /** 정지 회원이 이의제기 외 문의 유형을 보내면 저장하지 않음 */
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

    /** 고객문의 제목에 비속어가 포함되면 문의를 저장하지 않음 */
    @Test
    void setInquiryRejectsBadWordInTitle() {
        // 제목 비속어 검증 대상 고객문의 요청을 생성함
        InquiryCreateDto inquiry = new InquiryCreateDto();
        // 일반 문의 카테고리를 설정함
        inquiry.setInqrCatg("GENERAL");
        // 비속어가 포함된 문의 제목을 설정함
        inquiry.setInqrTitl("비속어가 포함된 제목");
        // 비속어가 없는 문의 본문을 설정함
        inquiry.setInqrCntn("정상 문의 내용");
        // 문의 등록이 가능한 활성 회원 상태를 설정함
        when(inquiryMapper.getUserStat(7L)).thenReturn("ACTIVE");
        // 제목에서 차단할 비속어가 탐지되는 조건을 설정함
        when(badWordDetectionService.findBadWord("비속어가 포함된 제목"))
                .thenReturn(Optional.of("비속어"));

        // 제목에 비속어가 포함된 고객문의 등록을 요청함
        ResultData result = inquiryService.setInquiry(7L, inquiry);

        // 공통 비속어 포함 실패 코드가 반환되는지 확인함
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 차단된 고객문의가 데이터베이스에 저장되지 않는지 확인함
        verify(inquiryMapper, never()).setInquiry(any(InquiryDto.class), anyLong());
    }

    /** 고객문의 본문에 비속어가 포함되면 문의를 저장하지 않음 */
    @Test
    void setInquiryRejectsBadWordInContent() {
        // 본문 비속어 검증 대상 고객문의 요청을 생성함
        InquiryCreateDto inquiry = new InquiryCreateDto();
        // 일반 문의 카테고리를 설정함
        inquiry.setInqrCatg("GENERAL");
        // 비속어가 없는 문의 제목을 설정함
        inquiry.setInqrTitl("정상 문의 제목");
        // 비속어가 포함된 문의 본문을 설정함
        inquiry.setInqrCntn("비속어가 포함된 문의 내용");
        // 문의 등록이 가능한 활성 회원 상태를 설정함
        when(inquiryMapper.getUserStat(7L)).thenReturn("ACTIVE");
        // 제목이 비속어 검사를 통과하는 조건을 설정함
        when(badWordDetectionService.findBadWord("정상 문의 제목"))
                .thenReturn(Optional.empty());
        // 본문에서 차단할 비속어가 탐지되는 조건을 설정함
        when(badWordDetectionService.findBadWord("비속어가 포함된 문의 내용"))
                .thenReturn(Optional.of("비속어"));

        // 본문에 비속어가 포함된 고객문의 등록을 요청함
        ResultData result = inquiryService.setInquiry(7L, inquiry);

        // 공통 비속어 포함 실패 코드가 반환되는지 확인함
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 차단된 고객문의가 데이터베이스에 저장되지 않는지 확인함
        verify(inquiryMapper, never()).setInquiry(any(InquiryDto.class), anyLong());
    }
}
