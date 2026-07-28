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
 * fileName       : AlimServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 알림 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class AlimServiceImplTest {
    // Alim 데이터 접근 객체
    @Mock
    private AlimMapper alimMapper;

    // Push 업무 처리 서비스
    @Mock
    private PushService pushService;

    // 알림 서비스 단위 테스트 대상
    private AlimServiceImpl alimService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 알림 서비스 구현체를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 알림 서비스 단위 테스트 대상을 담을 객체를 생성한다
        alimService = new AlimServiceImpl(alimMapper, pushService);
    }

    /**
     * 목록 조회가 미읽음 알림을 반환하되 읽음 UPDATE 없이 조회 결과만 구성하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getMyAlimListReturnsUnreadItemsWithoutUpdatingReadStatus() {
        // 알림 목록 항목을 담을 객체를 생성한다
        AlimDto.AlimItemDto alimItem = new AlimDto.AlimItemDto();
        // AlimNumb 업무 값을 alimItem DTO에 설정한다
        alimItem.setAlimNumb(1L);
        // ReadYsno 업무 값을 alimItem DTO에 설정한다
        alimItem.setReadYsno("N");

        // MyAlimList 데이터를 DB에서 조회한다
        when(alimMapper.getMyAlimList(any(AlimDto.AlimListReqDto.class)))
                .thenReturn(List.of(alimItem));
        // UnreadAlimCnt 데이터를 DB에서 조회한다
        when(alimMapper.getUnreadAlimCnt(31L)).thenReturn(1);

        // getMyAlimList 업무 로직을 alimService에 위임한다
        ResultData result = alimService.getMyAlimList(31L, 1);
        // 공통 응답에 포함된 업무 데이터를 조회한다
        AlimDto.AlimListResDto data = (AlimDto.AlimListResDto) result.getData();

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // 필요한 값으로 불변 객체를 생성한다
        assertEquals(List.of(alimItem), data.getList());
        // getUnreadCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(1, data.getUnreadCnt());
        // 다음 알림 페이지 존재 여부를 확인한다
        assertFalse(data.isHasNext());
    }

    /**
     * 개별 알림 클릭 시 인증 사용자 번호를 요청 DTO에 설정하고 남은 미읽음 수를 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptAlimReadUpdatesClickedItemAndReturnsUnreadCount() {
        // 알림 읽음 처리 조건을 담을 객체를 생성한다
        AlimDto.AlimReadReqDto req = new AlimDto.AlimReadReqDto();
        // AlimNumb 업무 값을 req DTO에 설정한다
        req.setAlimNumb(3L);

        // AlimRead 데이터를 DB에서 수정한다
        when(alimMapper.uptAlimRead(req)).thenReturn(1);
        // UnreadAlimCnt 데이터를 DB에서 조회한다
        when(alimMapper.getUnreadAlimCnt(31L)).thenReturn(2);

        // uptAlimRead 업무 로직을 alimService에 위임한다
        ResultData result = alimService.uptAlimRead(31L, req);
        // 공통 응답에 포함된 업무 데이터를 조회한다
        AlimDto.AlimUnreadCntDto data = (AlimDto.AlimUnreadCntDto) result.getData();

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(31L, req.getUserNumb());
        // getUnreadCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(2, data.getUnreadCnt());
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimMapper).uptAlimRead(req);
    }

    /**
     * 모두 지우기 요청이 읽음 상태가 아닌 삭제 상태 일괄 갱신 매퍼를 호출하는지 검증한다.
     * 화면에 아직 조회되지 않은 알림도 같은 사용자 번호로 함께 삭제 처리되어야 한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delAllAlimUpdatesDeleteStatusAndReturnsZeroUnreadCount() {
        // delAllAlim 업무 로직을 alimService에 위임한다
        ResultData result = alimService.delAllAlim(31L);
        // 공통 응답에 포함된 업무 데이터를 조회한다
        AlimDto.AlimUnreadCntDto data = (AlimDto.AlimUnreadCntDto) result.getData();

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // getUnreadCnt 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(0, data.getUnreadCnt());
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(alimMapper).delAllAlim(31L);
    }

    /**
     * 알림 INSERT에서 반환된 사용자별 알림 번호가 FCM payload 발송 단계까지 전달되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendAlimPassesInsertedAlimNumbToPushService() {
        // 알림 발송에 사용할 템플릿 정보를 담을 객체를 생성한다
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        // AlimTitl 업무 값을 template DTO에 설정한다
        template.setAlimTitl("좋아요 알림");
        // TempCont 업무 값을 template DTO에 설정한다
        template.setTempCont("#{sender}님이 좋아요를 눌렀습니다.");
        // LinkUrlx 업무 값을 template DTO에 설정한다
        template.setLinkUrlx("/book/detail/");

        // AlimTemp 데이터를 DB에서 조회한다
        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        // 한 시간 안에 동일한 알림이 중복 등록되지 않았는지 검증한다
        when(alimMapper.dupSameAlimInHour(any(AlimDto.AlimItemDto.class))).thenReturn(0);
        doAnswer(invocation -> {
            // getArgument 조회로 후속 처리에 필요한 데이터를 가져온다
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            // AlimNumb 업무 값을 alim DTO에 설정한다
            alim.setAlimNumb(7L);
            // 테스트 콜백에서 준비한 처리 결과를 반환한다
            return 1;
        // 테스트 대상 의존 호출의 동작을 정의한다
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        // sendAlim 업무 로직을 alimService에 위임한다
        ResultData result = alimService.sendAlim(
                31L
              , "LIKE"
              , "LIKE_REPORT"
              , 10L
              , Map.of("sender", "테스트")
        );

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(pushService).sendPush(
                31L
              , "좋아요 알림"
              , "테스트님이 좋아요를 눌렀습니다."
              , "/book/detail/10"
              , 7L
        );
    }
}
