package org.our.sadari.alim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.push.service.PushService;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : AlimServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 알림 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 처리 검증
 * 2026-08-25        HanWon.Jang        템플릿 링크 우선 검증
 * 2026-08-27        SeungHyeon.Kang    알림번호 기반 라우팅과 사진 프로필 이동 검증
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
        // 실제 공통 실패 메시지를 사용할 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 테스트 메시지 프로퍼티 기준을 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // 실패 응답이 실제 메시지 소스를 조회하도록 정적 객체를 초기화한다
        new MessageUtils().setMessageSource(messageSource);
        // 알림 서비스 단위 테스트 대상을 담을 객체를 생성한다
        alimService = new AlimServiceImpl(alimMapper, pushService);
    }

    /**
     * 목록 조회가 미읽음 알림을 반환하되 읽음 UPDATE 없이 조회 결과만 구성하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getAlimListKeepsUnread() {
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
    void uptAlimReadReturnsCnt() {
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
    void delAllAlimReturnsZero() {
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
    void sendAlimUsesInsertedNumb() {
        // 알림 발송에 사용할 템플릿 정보를 담을 객체를 생성한다
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        // AlimTitl 업무 값을 template DTO에 설정한다
        template.setAlimTitl("좋아요 알림");
        // TempCont 업무 값을 template DTO에 설정한다
        template.setTempCont("#{sender}님이 좋아요를 눌렀습니다.");
        // 알림 수신자가 정상 이용 회원인 조건을 설정한다
        when(alimMapper.getActiveAlimUserCnt(31L)).thenReturn(1);
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
              , Constant.LIKE_TARGET_REPORT
              , 10L
              , null
              , Map.of("sender", "테스트")
        );

        // getCode 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(200, result.getCode());
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(pushService).sendPush(
                31L
              , "좋아요 알림"
              , "테스트님이 좋아요를 눌렀습니다."
              , "/notification-target/7"
              , 7L
        );
    }

    /**
     * 모임 알림도 저장된 알림번호를 공통 이동 경로로 전달하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendAlimLinksTarget() {
        // 모임 멤버 관리 화면으로 이동할 알림 템플릿을 구성한다
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        template.setAlimTitl("새로운 가입 신청");
        template.setTempCont("#{clubName}에 새로운 가입 신청이 있어요.");
        // 활성 모임장과 사용 가능한 알림 템플릿 및 신규 알림 저장 결과를 구성한다
        when(alimMapper.getActiveAlimUserCnt(31L)).thenReturn(1);
        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        doAnswer(invocation -> {
            // 저장된 알림 번호를 푸시 payload에 전달하도록 구성한다
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            alim.setAlimNumb(9L);
            return 1;
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        // 신규 가입 신청 알림을 모임장에게 발송한다
        ResultData result = alimService.sendAlim(
                31L
              , Constant.ALIM_SITU_FOLLOW_CLUB
              , Constant.ALIM_TEMP_CODE_CLUB_JOIN_REQUESTED
              , Constant.ALIM_TARGET_READING_CLUB
              , 10L
              , null
              , Map.of("clubName", "책벌레 모임")
        );

        // 문구와 알림번호 기반 공통 이동 경로가 포함된 푸시 발송을 검증한다
        assertEquals(200, result.getCode());
        verify(pushService).sendPush(
                31L
              , "새로운 가입 신청"
              , "책벌레 모임에 새로운 가입 신청이 있어요."
              , "/notification-target/9"
              , 9L
        );
        // 같은 모임에서 연속 신청이 들어와도 가입 신청 알림은 중복 차단 조회를 하지 않는지 검증한다
        verify(alimMapper, never()).dupSameAlimInHour(any(AlimDto.AlimItemDto.class));
    }

    /**
     * 상세 번호가 없는 타이머 알림도 알림번호 기반 공통 이동 경로를 사용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendTimerAlimUsesTarget() {
        // 타이머 종료 알림 템플릿 정보를 생성한다
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        // 타이머 종료 알림 제목을 설정한다
        template.setAlimTitl("독서 타이머 알림");
        // 타이머 종료 알림 내용을 설정한다
        template.setTempCont("독서 타이머가 종료되었습니다.");
        // 알림 수신자가 정상 이용 회원인 조건을 설정한다
        when(alimMapper.getActiveAlimUserCnt(31L)).thenReturn(1);
        // 타이머 종료 템플릿 조회 결과를 설정한다
        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        // 알림 저장 뒤 푸시 payload에 사용할 알림 번호를 설정한다
        doAnswer(invocation -> {
            // 저장할 타이머 종료 알림을 조회한다
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            // 푸시 payload에 사용할 알림 번호를 설정한다
            alim.setAlimNumb(8L);
            // 알림 한 건 저장 결과를 반환한다
            return 1;
        // 테스트 대상 의존 호출의 동작을 정의한다
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        // 별도 대상 번호가 없는 타이머 종료 알림을 발송한다
        ResultData result = alimService.sendAlim(
                31L, Constant.ALIM_SITU_TIMER, Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER
              , Constant.ALIM_TARGET_TIMER, null, null, Map.of());

        // 타이머 종료 알림 발송이 성공했는지 확인한다
        assertEquals(200, result.getCode());
        // 푸시 알림이 저장된 알림번호 기반 공통 이동 경로를 사용하는지 확인한다
        verify(pushService).sendPush(
                31L
              , "독서 타이머 알림"
              , "독서 타이머가 종료되었습니다."
              , "/notification-target/8"
              , 8L
        );
    }

    /**
     * 사진 댓글 좋아요 알림도 알림번호 기반 공통 이동 경로를 사용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void sendImageReplyLikeTarget() {
        // 사진 댓글 좋아요 알림 템플릿 정보를 생성한다
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        // 알림 제목을 설정한다
        template.setAlimTitl("댓글 알림");
        // 알림 내용을 설정한다
        template.setTempCont("사진 댓글에 새로운 반응이 있습니다.");
        // 알림 수신자가 정상 이용 회원인 조건을 설정한다
        when(alimMapper.getActiveAlimUserCnt(31L)).thenReturn(1);
        // 대상별 댓글 좋아요 템플릿 조회 결과를 설정한다
        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        // 동일 알림 중복 검사에서 신규 알림으로 판정하는 조건을 설정한다
        when(alimMapper.dupSameAlimInHour(any(AlimDto.AlimItemDto.class))).thenReturn(0);
        // 알림 저장 뒤 푸시 payload에 사용할 알림 번호를 설정한다
        doAnswer(invocation -> {
            // 저장할 댓글 좋아요 알림을 조회한다
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            // 푸시 payload에 사용할 알림 번호를 설정한다
            alim.setAlimNumb(9L);
            // 알림 한 건 저장 결과를 반환한다
            return 1;
        // 테스트 대상 의존 호출의 동작을 정의한다
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        // 프로필 사진을 대상으로 하는 댓글 좋아요 알림을 발송한다
        ResultData result = alimService.sendAlim(
                31L, "LIKE", Constant.ALIM_TEMP_CODE_REPLY_LIKE
              , Constant.LIKE_TARGET_PROFILE_IMAGE, 157L, null, Map.of());

        // 대상별 댓글 좋아요 알림 발송이 성공했는지 확인한다
        assertEquals(200, result.getCode());
        // 푸시 링크가 저장된 알림번호 기반 공통 이동 경로인지 확인한다
        verify(pushService).sendPush(
                31L,
                "댓글 알림",
                "사진 댓글에 새로운 반응이 있습니다.",
                "/notification-target/9",
                9L
        );
    }

    /** 대댓글 알림도 알림번호 기반 공통 이동 경로를 사용하는지 검증한다. */
    @Test
    void sendReplyUsesTargetRoute() {
        AlimDto.AlimTempDto template = new AlimDto.AlimTempDto();
        template.setAlimTitl("댓글 알림");
        template.setTempCont("답글이 등록됐습니다.");
        when(alimMapper.getActiveAlimUserCnt(31L)).thenReturn(1);
        when(alimMapper.getAlimTemp(any(AlimDto.AlimTempDto.class))).thenReturn(template);
        doAnswer(invocation -> {
            AlimDto.AlimItemDto alim = invocation.getArgument(0);
            alim.setAlimNumb(10L);
            return 1;
        }).when(alimMapper).setAlim(any(AlimDto.AlimItemDto.class));

        ResultData result = alimService.sendAlim(
                31L, "REPLY", Constant.ALIM_TEMP_CODE_REPLY_TO_COMMENT
              , Constant.LIKE_TARGET_REPORT, 157L, 8L, Map.of());

        assertEquals(200, result.getCode());
        verify(pushService).sendPush(
                31L,
                "댓글 알림",
                "답글이 등록됐습니다.",
                "/notification-target/10",
                10L
        );
    }

    /** 본인 독후감 댓글 알림은 현재 공개 여부와 관계없이 본인 상세 화면으로 이동하는지 검증한다. */
    @Test
    void getOwnerReportTarget() {
        AlimDto.AlimTargetDto target = createReportTarget(31L, Constant.COMM_NO, Constant.COMM_NO);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals("/report/detail/157?showReplies=Y&replNumb=8", data.getLinkUrlx());
    }

    /** 현재 팔로우 중인 사용자의 공개 독후감 알림은 피드 항목으로 이동하는지 검증한다. */
    @Test
    void getFollowerReportTarget() {
        AlimDto.AlimTargetDto target = createReportTarget(32L, Constant.COMM_YES, Constant.COMM_YES);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals("/feed?tagtType=REPORT&tagtNumb=157&replNumb=8", data.getLinkUrlx());
    }

    /** 현재 비팔로워인 사용자의 공개 독후감 알림은 공개 독후감 대상 화면으로 이동하는지 검증한다. */
    @Test
    void getPublicReportTarget() {
        AlimDto.AlimTargetDto target = createReportTarget(32L, Constant.COMM_YES, Constant.COMM_NO);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals("/report/public-reports/target/157?replNumb=8", data.getLinkUrlx());
    }

    /** 사진 알림 생성 후 팔로우를 끊어도 현재 사진이면 소유자의 공개 프로필로 이동하는지 검증한다. */
    @Test
    void getUnfollowedImageTarget() {
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTagtType(Constant.LIKE_TARGET_PROFILE_IMAGE);
        target.setTagtNumb(157L);
        target.setTargetUserNumb(32L);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        target.setFollowYsno(Constant.COMM_NO);
        target.setReplNumb(8L);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(
                "/social/profile/32?tagtType=PROFILE_IMAGE&tagtNumb=157&replNumb=8",
                data.getLinkUrlx()
        );
    }

    /** 본인 현재 배경사진 알림은 마이페이지의 해당 사진으로 이동하는지 검증한다. */
    @Test
    void getOwnerImageTarget() {
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTagtType(Constant.LIKE_TARGET_BACKGROUND_IMAGE);
        target.setTagtNumb(159L);
        target.setTargetUserNumb(31L);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        assertEquals(200, result.getCode());
        assertEquals(
                "/mypage/profile?tagtType=BACKGROUND_IMAGE&tagtNumb=159",
                data.getLinkUrlx()
        );
    }

    /** 팔로우 알림은 현재 활성 상태인 대상 사용자의 프로필로 이동하는지 검증한다. */
    @Test
    void getActiveUserTarget() {
        // 활성 사용자 프로필을 가리키는 알림 대상 정보를 생성한다
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTagtType(Constant.ALIM_TARGET_USER);
        target.setTagtNumb(32L);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        // 팔로우 알림의 현재 이동 대상을 조회한다
        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        // 활성 사용자 프로필 경로가 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals("/social/profile/32", data.getLinkUrlx());
    }

    /** 타이머 종료 알림은 별도 대상 번호 없이 타이머 화면으로 이동하는지 검증한다. */
    @Test
    void getTimerTarget() {
        // 타이머 종료 알림 대상 정보를 생성한다
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTempCode(Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER);
        target.setTagtType(Constant.ALIM_TARGET_TIMER);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        // 타이머 종료 알림의 현재 이동 대상을 조회한다
        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        // 타이머 화면 경로가 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals("/timer", data.getLinkUrlx());
    }

    /** 모임 가입 승인 알림은 현재 모임 관계를 확인할 수 있는 내 모임 화면으로 이동하는지 검증한다. */
    @Test
    void getClubMembershipTarget() {
        // 모임 가입 승인 알림 대상 정보를 생성한다
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTempCode(Constant.ALIM_TEMP_CODE_CLUB_JOIN_APPROVED);
        target.setTagtType(Constant.ALIM_TARGET_READING_CLUB);
        target.setTagtNumb(10L);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        // 모임 가입 승인 알림의 현재 이동 대상을 조회한다
        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        // 내 모임 화면 경로가 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals("/reading-clubs/mine", data.getLinkUrlx());
    }

    /** 모임 가입 신청 알림은 현재 활성 모임장에게만 멤버 관리 화면을 제공하는지 검증한다. */
    @Test
    void getClubManageForOwner() {
        // 현재 모임장이 수신자인 가입 신청 알림 대상 정보를 생성한다
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTempCode(Constant.ALIM_TEMP_CODE_CLUB_JOIN_REQUESTED);
        target.setTagtType(Constant.ALIM_TARGET_READING_CLUB);
        target.setTagtNumb(10L);
        target.setTargetUserNumb(31L);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        // 모임 가입 신청 알림의 현재 이동 대상을 조회한다
        ResultData result = alimService.getAlimTarget(31L, 7L);
        AlimDto.AlimTargetDto data = (AlimDto.AlimTargetDto) result.getData();

        // 현재 모임장에게 멤버 관리 화면 경로가 반환되는지 확인한다
        assertEquals(200, result.getCode());
        assertEquals("/reading-clubs/manage/members/10", data.getLinkUrlx());
    }

    /** 모임장이 변경된 뒤에는 과거 가입 신청 알림으로 멤버 관리 화면을 열 수 없는지 검증한다. */
    @Test
    void rejectFormerClubOwner() {
        // 알림 수신자와 현재 모임장이 다른 가입 신청 알림 대상 정보를 생성한다
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTempCode(Constant.ALIM_TEMP_CODE_CLUB_JOIN_REQUESTED);
        target.setTagtType(Constant.ALIM_TARGET_READING_CLUB);
        target.setTagtNumb(10L);
        target.setTargetUserNumb(32L);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(target);

        // 과거 모임장이 가입 신청 알림의 이동 대상을 조회한다
        ResultData result = alimService.getAlimTarget(31L, 7L);

        // 현재 권한이 없는 멤버 관리 화면 접근이 거부되는지 확인한다
        assertEquals(2020, result.getCode());
    }

    /** 인증 사용자가 소유하지 않은 알림번호는 대상 정보 없이 접근 거부되는지 검증한다. */
    @Test
    void getForeignAlimTarget() {
        when(alimMapper.getAlimTargetDtl(any(AlimDto.AlimTargetDto.class))).thenReturn(null);

        ResultData result = alimService.getAlimTarget(31L, 7L);

        assertEquals(2020, result.getCode());
    }

    /**
     * 독후감 알림 이동 테스트에서 공통으로 사용할 현재 콘텐츠 상태를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param targetUserNumb 독후감 작성자 번호
     * @param pubcYsno 현재 독후감 공개 여부
     * @param followYsno 현재 알림 수신자의 작성자 팔로우 여부
     * @return 댓글 위치가 포함된 독후감 알림 대상 상태
     */
    private AlimDto.AlimTargetDto createReportTarget(Long targetUserNumb, String pubcYsno, String followYsno) {
        AlimDto.AlimTargetDto target = new AlimDto.AlimTargetDto();
        target.setTagtType(Constant.LIKE_TARGET_REPORT);
        target.setTagtNumb(157L);
        target.setReplNumb(8L);
        target.setTargetUserNumb(targetUserNumb);
        target.setTargetUserStat(Constant.USER_STAT_ACTIVE);
        target.setPubcYsno(pubcYsno);
        target.setReptStat("DONE");
        target.setFollowYsno(followYsno);
        // 현재 독후감과 관계 상태가 설정된 알림 대상 객체를 반환한다
        return target;
    }
}
