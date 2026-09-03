package org.our.sadari.notice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.notice.dto.NoticeDto;
import org.our.sadari.notice.dto.NoticePageDto;
import org.our.sadari.notice.dto.UnreadNoticeDto;
import org.our.sadari.notice.mapper.NoticeMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : NoticeServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자 공지 접근과 다음 페이지 계산 정책을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang    사용자 공지사항 10개 단위 조회 검증 반영
 * 2026-08-19        SeungHyeon.Kang    홈 미읽음 공지 제목 조회 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    // 공지사항 데이터 접근 Mock
    @Mock
    private NoticeMapper noticeMapper;
    // 공지사항 서비스 단위 테스트 대상
    private NoticeServiceImpl noticeService;

    /** 각 테스트에 독립된 공지사항 서비스를 생성함 */
    @BeforeEach
    void setUp() {
        // 실패 응답에서 사용할 서버 공통 메시지 소스를 생성함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정함
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정함
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 공통 메시지 소스를 사용하도록 초기화함
        new MessageUtils().setMessageSource(messageSource);

        // 공지사항 서비스 단위 테스트 대상을 생성함
        noticeService = new NoticeServiceImpl(noticeMapper);
    }

    /** 비활성 사용자는 배포 공지 목록 SQL을 실행하지 못함 */
    @Test
    void getNoticeRejectsInactive() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(0);

        boolean active = noticeService.isActiveUser(7L);

        assertFalse(active);
        verify(noticeMapper, never()).getNoticeList(7L, "NOTICE", "Y", "N", 0, 11);
    }

    /** 활성 사용자의 목록은 11번째 행으로 다음 페이지 여부를 계산함 */
    @Test
    void getNoticeListHasNext() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        List<NoticeDto> rows = new ArrayList<>();
        for (int index = 0; index < 11; index++) {
            rows.add(new NoticeDto());
        }
        when(noticeMapper.getNoticeList(7L, "NOTICE", "Y", "N", 0, 11)).thenReturn(rows);

        ResultData result = noticeService.getNoticeList(7L, 1);
        NoticePageDto page = assertInstanceOf(NoticePageDto.class, result.getData());

        assertEquals(200, result.getCode());
        assertEquals(10, page.list().size());
        assertTrue(page.hasNext());
        assertFalse(page.list().isEmpty());
    }

    /** 비활성 사용자는 홈 미읽음 공지 제목 SQL을 실행하지 못함 */
    @Test
    void getUnreadRejectsInactive() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(0);

        boolean active = noticeService.isActiveUser(7L);

        assertFalse(active);
        verify(noticeMapper, never()).getUnreadNoticeList(7L, "NOTICE", "Y");
    }

    /** 활성 사용자의 홈에는 읽음 이력이 없는 배포 공지 제목만 전달함 */
    @Test
    void getUnreadNoticeList() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        UnreadNoticeDto notice = new UnreadNoticeDto();
        notice.setNotiNumb(11L);
        notice.setNotiTitl("서비스 점검 안내");
        List<UnreadNoticeDto> rows = List.of(notice);
        when(noticeMapper.getUnreadNoticeList(7L, "NOTICE", "Y")).thenReturn(rows);

        ResultData result = noticeService.getUnreadNoticeList(7L);

        assertEquals(200, result.getCode());
        assertEquals(rows, result.getData());
        verify(noticeMapper).getUnreadNoticeList(7L, "NOTICE", "Y");
    }

    /** 배포 공지 상세 GET은 기존 읽음 여부만 조회하고 이력을 저장하지 않음 */
    @Test
    void getNoticeDtlNoSideEffect() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        NoticeDto notice = new NoticeDto();
        notice.setNotiNumb(11L);
        notice.setReadYsno("N");
        when(noticeMapper.getNoticeDtl(11L, 7L, "NOTICE", "Y", "N")).thenReturn(notice);

        ResultData result = noticeService.getNoticeDtl(7L, 11L);

        assertEquals(200, result.getCode());
        assertEquals("N", notice.getReadYsno());
        verify(noticeMapper, never()).setNoticeView("NOTICE", 11L, 7L);
    }

    /** CSRF 보호 POST 서비스는 현재 배포 공지의 읽음 이력을 저장함 */
    @Test
    void setNoticeViewStoresRead() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        NoticeDto notice = new NoticeDto();
        notice.setNotiNumb(11L);
        when(noticeMapper.getNoticeDtl(11L, 7L, "NOTICE", "Y", "N")).thenReturn(notice);

        ResultData result = noticeService.setNoticeView(7L, 11L);

        assertEquals(200, result.getCode());
        verify(noticeMapper).setNoticeView("NOTICE", 11L, 7L);
    }

    /** 현재 배포되지 않은 공지에는 읽음 이력을 만들지 않음 */
    @Test
    void noticeViewRejectsMissing() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        when(noticeMapper.getNoticeDtl(11L, 7L, "NOTICE", "Y", "N")).thenReturn(null);

        ResultData result = noticeService.setNoticeView(7L, 11L);

        assertEquals(2004, result.getCode());
        verify(noticeMapper, never()).setNoticeView("NOTICE", 11L, 7L);
    }
}
