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
import org.our.sadari.notice.dto.NoticeDto;
import org.our.sadari.notice.dto.NoticePageDto;
import org.our.sadari.notice.mapper.NoticeMapper;

/**
 * fileName       : NoticeServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자 공지 접근과 다음 페이지 계산 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    // 공지사항 데이터 접근 Mock
    @Mock
    private NoticeMapper noticeMapper;
    // 공지사항 서비스 단위 테스트 대상
    private NoticeServiceImpl noticeService;

    /** 각 테스트에 독립된 공지사항 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        noticeService = new NoticeServiceImpl(noticeMapper);
    }

    /** 비활성 사용자는 배포 공지 목록 SQL을 실행하지 못한다. */
    @Test
    void getNoticeRejectsInactive() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(0);

        boolean active = noticeService.isActiveUser(7L);

        assertFalse(active);
        verify(noticeMapper, never()).getNoticeList(7L, "NOTICE", "Y", "N", 0, 21);
    }

    /** 활성 사용자의 목록은 21번째 행으로 다음 페이지 여부를 계산한다. */
    @Test
    void getNoticeListHasNext() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        List<NoticeDto> rows = new ArrayList<>();
        for (int index = 0; index < 21; index++) {
            rows.add(new NoticeDto());
        }
        when(noticeMapper.getNoticeList(7L, "NOTICE", "Y", "N", 0, 21)).thenReturn(rows);

        ResultData result = noticeService.getNoticeList(7L, 1);
        NoticePageDto page = assertInstanceOf(NoticePageDto.class, result.getData());

        assertEquals(200, result.getCode());
        assertEquals(20, page.list().size());
        assertTrue(page.hasNext());
        assertFalse(page.list().isEmpty());
    }

    /** 활성 사용자가 배포 공지 상세를 열면 읽음 이력을 저장한다. */
    @Test
    void getNoticeDtlStoresRead() {
        when(noticeMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        NoticeDto notice = new NoticeDto();
        notice.setNotiNumb(11L);
        when(noticeMapper.getNoticeDtl(11L, "Y")).thenReturn(notice);

        ResultData result = noticeService.getNoticeDtl(7L, 11L);

        assertEquals(200, result.getCode());
        assertEquals("Y", notice.getReadYsno());
        verify(noticeMapper).setNoticeView("NOTICE", 11L, 7L);
    }
}
