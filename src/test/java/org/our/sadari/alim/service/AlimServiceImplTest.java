package org.our.sadari.alim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.alim.mapper.AlimMapper;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.push.service.PushService;

/**
 * 사용자 행동에 따라 알림 읽음 상태를 변경하는 서비스 정책을 검증한다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class AlimServiceImplTest {

    @Mock
    private AlimMapper alimMapper;

    @Mock
    private PushService pushService;

    private AlimServiceImpl alimService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 알림 서비스 구현체를 구성한다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        alimService = new AlimServiceImpl(alimMapper, pushService);
    }

    /**
     * 목록 조회가 미읽음 알림을 반환하되 읽음 UPDATE 없이 조회 결과만 구성하는지 검증한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getMyAlimListReturnsUnreadItemsWithoutUpdatingReadStatus() {
        AlimDto.AlimItemDto alimItem = new AlimDto.AlimItemDto();
        alimItem.setAlimNumb(1L);
        alimItem.setReadYsno("N");

        when(alimMapper.getMyAlimList(any(AlimDto.AlimListReqDto.class)))
                .thenReturn(List.of(alimItem));
        when(alimMapper.getUnreadAlimCnt(31L)).thenReturn(1);

        ResultData result = alimService.getMyAlimList(31L, 1);
        AlimDto.AlimListResDto data = (AlimDto.AlimListResDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(List.of(alimItem), data.getList());
        assertEquals(1, data.getUnreadCnt());
        assertFalse(data.isHasNext());
    }

    /**
     * 개별 알림 클릭 시 인증 사용자 번호를 요청 DTO에 설정하고 남은 미읽음 수를 반환하는지 검증한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void uptAlimReadUpdatesClickedItemAndReturnsUnreadCount() {
        AlimDto.AlimReadReqDto req = new AlimDto.AlimReadReqDto();
        req.setAlimNumb(3L);

        when(alimMapper.uptAlimRead(req)).thenReturn(1);
        when(alimMapper.getUnreadAlimCnt(31L)).thenReturn(2);

        ResultData result = alimService.uptAlimRead(31L, req);
        AlimDto.AlimUnreadCntDto data = (AlimDto.AlimUnreadCntDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(31L, req.getUserNumb());
        assertEquals(2, data.getUnreadCnt());
        verify(alimMapper).uptAlimRead(req);
    }

    /**
     * 모두 지우기 요청이 읽음 상태가 아닌 삭제 상태 일괄 갱신 매퍼를 호출하는지 검증한다.
     * 화면에 아직 조회되지 않은 알림도 같은 사용자 번호로 함께 삭제 처리되어야 한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void delAllAlimUpdatesDeleteStatusAndReturnsZeroUnreadCount() {
        ResultData result = alimService.delAllAlim(31L);
        AlimDto.AlimUnreadCntDto data = (AlimDto.AlimUnreadCntDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(0, data.getUnreadCnt());
        verify(alimMapper).delAllAlim(31L);
    }

    /**
     * 알림 INSERT에서 반환된 사용자별 알림 번호가 FCM payload 발송 단계까지 전달되는지 검증한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void sendAlimPassesInsertedAlimNumbToPushService() {
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        template.setAlimTitl("좋아요 알림");
        template.setTempCont("#{sender}님이 좋아요를 눌렀습니다.");
        template.setLinkUrlx("/book/detail/");

        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        when(alimMapper.dupSameAlimInHour(any(AlimDto.AlimItemDto.class))).thenReturn(0);
        doAnswer(invocation -> {
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            alim.setAlimNumb(7L);
            return 1;
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        ResultData result = alimService.sendAlim(
                31L
              , "LIKE"
              , "LIKE_REPORT"
              , 10L
              , Map.of("sender", "테스트")
        );

        assertEquals(200, result.getCode());
        verify(pushService).sendPush(
                31L
              , "좋아요 알림"
              , "테스트님이 좋아요를 눌렀습니다."
              , "/book/detail/10"
              , 7L
        );
    }
}
