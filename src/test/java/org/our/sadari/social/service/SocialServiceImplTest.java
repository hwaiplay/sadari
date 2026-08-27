package org.our.sadari.social.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.event.LikeAlimEvent;
import org.our.sadari.alim.event.LikeAlimPublisher;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.feed.mapper.FeedMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.report.mapper.ReportMapper;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.mapper.SocialMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : SocialServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-04
 * description    : 프로필 통계 조회의 독후감 공개 범위 전달 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-04        SeungHyeon.Kang       최초 생성
 * 2026-08-21        SeungHyeon.Kang    독후감별 좋아요 알림 설정 검증 추가
 * 2026-08-25        HanWon.Jang        사진 좋아요 링크 기준 검증
 * 2026-08-26        HanWon.Jang        좋아요 목록·비동기 알림 검증
 */
@ExtendWith(MockitoExtension.class)
class SocialServiceImplTest {

    // Social 데이터 접근 객체
    @Mock
    private SocialMapper socialMapper;

    // Report 데이터 접근 객체
    @Mock
    private ReportMapper reportMapper;

    // Feed 데이터 접근 객체
    @Mock
    private FeedMapper feedMapper;

    // User 데이터 접근 객체
    @Mock
    private UserMapper userMapper;

    // 알림 업무 처리 서비스
    @Mock
    private AlimService alimService;

    // Token Redis 업무 처리 서비스
    @Mock
    private TokenRedisService tokenRedisService;

    // 좋아요 커밋 이후 알림 이벤트 발행기
    @Mock
    private LikeAlimPublisher likeAlimPublisher;

    // 프로필 통계 서비스 단위 테스트 대상
    private SocialServiceImpl socialService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 소셜 서비스 구현체를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 실패 응답도 실제 공통 메시지를 사용할 수 있도록 테스트 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 조회 기준으로 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 프로퍼티 파일 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 초기화된 메시지 소스를 사용하도록 연결한다
        new MessageUtils().setMessageSource(messageSource);
        // 소셜 서비스 단위 테스트 대상을 생성한다
        socialService = new SocialServiceImpl(
                socialMapper, reportMapper, feedMapper, userMapper, alimService, tokenRedisService, likeAlimPublisher);
        // 프로필 통계 조회 대상 사용자가 존재하도록 설정한다
        lenient().when(userMapper.getUserByNumb(31L)).thenReturn(new UserDto());
        // 프로필 통계 SQL이 빈 기본 통계를 반환하도록 설정한다
        lenient().when(socialMapper.getProfileStats(any(SocialDto.ProfileStatsDto.class)))
                .thenReturn(new SocialDto.ProfileStatsDto());
    }

    /**
     * 독후감 작성자가 좋아요 알림을 끈 경우 좋아요는 저장하고 알림은 생성하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setLikeSkipsDisabledAlim() {
        // 독후감 좋아요 요청을 생성한다
        SocialDto.LikeDto request = new SocialDto.LikeDto();
        // 좋아요를 등록할 로그인 사용자 번호를 설정한다
        request.setUserNumb(44L);
        // 좋아요 대상 유형을 독후감으로 설정한다
        request.setTagtType(Constant.LIKE_TARGET_REPORT);
        // 좋아요 대상 독후감 번호를 설정한다
        request.setTagtNumb(157L);

        // 서버에서 확인한 독후감 작성자와 꺼진 좋아요 알림 설정을 생성한다
        SocialDto.LikeDto likeTarget = new SocialDto.LikeDto();
        // 알림 수신자 독후감 작성자 번호를 설정한다
        likeTarget.setTargetUserNumb(31L);
        // 독후감 좋아요 알림을 끈 상태로 설정한다
        likeTarget.setLikeAlimYsno(Constant.COMM_NO);
        // 접근 가능한 독후감과 알림 설정 조회 결과를 구성한다
        when(reportMapper.getReportLikeDtl(request)).thenReturn(likeTarget);
        // 기존 좋아요가 없는 신규 등록 조건을 구성한다
        when(socialMapper.dupLike(request)).thenReturn(0);
        // 변경 후 좋아요 상세 조회 결과를 구성한다
        when(socialMapper.getLikeDtl(request)).thenReturn(request);

        // 좋아요 알림을 끈 독후감에 좋아요를 등록한다
        ResultData result = socialService.setLike(request);

        // 좋아요 등록 자체는 성공하는지 확인한다
        assertEquals(200, result.getCode());
        // 좋아요 행 등록은 수행되는지 확인한다
        verify(socialMapper).setLike(request);
        // 알림 저장 서비스는 호출되지 않는지 확인한다
        verify(alimService, never()).sendAlim(any(), any(), any(), any(), any());
        // 알림이 꺼진 경우 발신자 닉네임도 조회하지 않는지 확인한다
        verify(tokenRedisService, never()).getUserNick(eq(44L));
        // 알림이 꺼진 경우 커밋 이후 알림 이벤트도 등록하지 않는지 확인한다
        verify(likeAlimPublisher, never()).setLikeAlim(any(LikeAlimEvent.class));
    }

    /**
     * 프로필 사진 좋아요 알림이 대상별 피드 링크에 해당 사진 번호를 전달하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setImageLikeUsesLink() {
        // 프로필 사진 좋아요 요청을 생성한다
        SocialDto.LikeDto request = new SocialDto.LikeDto();
        // 좋아요를 등록할 로그인 사용자 번호를 설정한다
        request.setUserNumb(44L);
        // 좋아요 대상 유형을 프로필 사진으로 설정한다
        request.setTagtType(Constant.LIKE_TARGET_PROFILE_IMAGE);
        // 좋아요 대상 프로필 파일 번호를 설정한다
        request.setTagtNumb(157L);

        // 서버에서 확인한 프로필 사진 소유자 정보를 생성한다
        SocialDto.LikeDto likeTarget = new SocialDto.LikeDto();
        // 알림 수신자인 사진 소유자 번호를 설정한다
        likeTarget.setTargetUserNumb(31L);
        // 사진 반응 알림을 발송할 수 있도록 알림 상태를 설정한다
        likeTarget.setLikeAlimYsno(Constant.COMM_YES);
        // 현재 프로필 사진과 소유자 조회 결과를 구성한다
        when(feedMapper.getImageLikeTarget(request)).thenReturn(likeTarget);
        // 기존 좋아요가 없는 신규 등록 조건을 구성한다
        when(socialMapper.dupLike(request)).thenReturn(0);
        // 변경 후 좋아요 상세 조회 결과를 구성한다
        when(socialMapper.getLikeDtl(request)).thenReturn(request);
        // 현재 프로필 사진에 좋아요를 등록한다
        ResultData result = socialService.setLike(request);

        // 프로필 사진 좋아요 등록 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 커밋 이후 처리할 사진 좋아요 알림 정보를 확인할 캡처 객체를 생성한다
        ArgumentCaptor<LikeAlimEvent> eventCaptor = ArgumentCaptor.forClass(LikeAlimEvent.class);
        // 사진 좋아요 저장 경로에서는 알림을 직접 보내지 않고 이벤트만 등록하는지 확인한다
        verify(likeAlimPublisher).setLikeAlim(eventCaptor.capture());
        // 사진 좋아요 알림 수신자를 확인한다
        assertEquals(31L, eventCaptor.getValue().getTargetUserNumb());
        // 사진 좋아요 템플릿 코드를 확인한다
        assertEquals(Constant.ALIM_TEMP_CODE_LIKE_PROFILE_IMAGE, eventCaptor.getValue().getTempCode());
        // 사진 알림이 해당 프로필 사진 번호를 이동 대상으로 전달하는지 확인한다
        assertEquals(157L, eventCaptor.getValue().getTagtNumb());
        // 동기 좋아요 경로에서 Redis 닉네임을 조회하지 않는지 확인한다
        verify(tokenRedisService, never()).getUserNick(eq(44L));
        // 동기 좋아요 경로에서 알림 저장 서비스를 호출하지 않는지 확인한다
        verify(alimService, never()).sendAlim(any(), any(), any(), any(), any());
    }

    /**
     * 다른 사용자 프로필 통계가 총 읽은 책과 받은 좋아요에 공개 독후감 조건을 전달하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getProfileStatsPublic() {
        // 다른 사용자 프로필과 같은 공개 범위로 통계를 조회한다
        socialService.getProfileStats(31L);

        // 프로필 통계 SQL에 전달된 조회 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<SocialDto.ProfileStatsDto> statsCaptor = ArgumentCaptor.forClass(SocialDto.ProfileStatsDto.class);
        // 프로필 통계 SQL의 조회 조건을 Capture한다
        verify(socialMapper).getProfileStats(statsCaptor.capture());

        // 다른 사용자 프로필의 독후감 집계가 공개 데이터로 제한되는지 확인한다
        assertEquals(Constant.COMM_YES, statsCaptor.getValue().getPubcYsno());
    }

    /**
     * 본인 마이페이지 통계는 공개 여부와 관계없이 기존 전체 독후감 범위를 유지하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getMyStatsKeepsAll() {
        // 본인 마이페이지와 같은 전체 범위로 통계를 조회한다
        socialService.getMyPageProfileStats(31L);

        // 프로필 통계 SQL에 전달된 조회 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<SocialDto.ProfileStatsDto> statsCaptor = ArgumentCaptor.forClass(SocialDto.ProfileStatsDto.class);
        // 프로필 통계 SQL의 조회 조건을 Capture한다
        verify(socialMapper).getProfileStats(statsCaptor.capture());

        // 본인 화면의 공개 여부 조건이 비어 있어 전체 독후감을 유지하는지 확인한다
        assertNull(statsCaptor.getValue().getPubcYsno());
    }

    /**
     * 좋아요 사용자 목록이 정규화된 대상과 페이지 조건으로 조회되는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void getLikeUsersPage() {
        // 접근 가능한 독후감 대상 조건을 구성한다
        when(socialMapper.getLikeTargetAccessCnt(any(SocialDto.LikeUserReqDto.class))).thenReturn(1);
        // 활성 좋아요 사용자 한 명이 조회되는 조건을 구성한다
        when(socialMapper.getLikeUserList(any(SocialDto.LikeUserReqDto.class)))
                .thenReturn(List.of(new SocialDto.FollowUserDto()));

        // 소문자로 전달된 독후감 대상의 첫 좋아요 사용자 페이지를 조회한다
        ResultData result = socialService.getLikeUserList(44L, "report", 157L, 1);

        // 접근 가능한 좋아요 사용자 목록 조회가 성공하는지 확인한다
        assertEquals(200, result.getCode());
        // Mapper에 전달된 대상과 페이지 조건을 확인할 인자 Capture를 생성한다
        ArgumentCaptor<SocialDto.LikeUserReqDto> reqCaptor = ArgumentCaptor.forClass(SocialDto.LikeUserReqDto.class);
        // 활성 좋아요 사용자 목록 조회 조건을 Capture한다
        verify(socialMapper).getLikeUserList(reqCaptor.capture());
        // 대상 유형이 서버 허용 목록의 대문자 값으로 정규화되는지 확인한다
        assertEquals(Constant.LIKE_TARGET_REPORT, reqCaptor.getValue().getTagtType());
        // 첫 페이지의 시작 위치가 0인지 확인한다
        assertEquals(0, reqCaptor.getValue().getPageOffset());
        // 다음 페이지 판정용 한 건을 더한 조회 크기인지 확인한다
        assertEquals(11, reqCaptor.getValue().getPageLimit());
    }

    /**
     * 접근할 수 없는 대상의 좋아요 사용자 정보가 조회되지 않는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void getLikeUsersReject() {
        // 대상 접근 검증 결과가 없는 조건을 구성한다
        when(socialMapper.getLikeTargetAccessCnt(any(SocialDto.LikeUserReqDto.class))).thenReturn(0);

        // 접근할 수 없는 독후감의 좋아요 사용자 목록을 요청한다
        ResultData result = socialService.getLikeUserList(44L, Constant.LIKE_TARGET_REPORT, 157L, 1);

        // 접근 제한 실패 응답인지 확인한다
        assertEquals(2020, result.getCode());
        // 접근 검증 실패 뒤에는 사용자 목록을 조회하지 않는지 확인한다
        verify(socialMapper, never()).getLikeUserList(any(SocialDto.LikeUserReqDto.class));
    }
}
