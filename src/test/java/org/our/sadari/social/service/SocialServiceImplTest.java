package org.our.sadari.social.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.report.mapper.ReportMapper;
import org.our.sadari.social.dto.SocialDto;
import org.our.sadari.social.mapper.SocialMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;

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
 */
@ExtendWith(MockitoExtension.class)
class SocialServiceImplTest {

    // Social 데이터 접근 객체
    @Mock
    private SocialMapper socialMapper;

    // Report 데이터 접근 객체
    @Mock
    private ReportMapper reportMapper;

    // User 데이터 접근 객체
    @Mock
    private UserMapper userMapper;

    // 알림 업무 처리 서비스
    @Mock
    private AlimService alimService;

    // Token Redis 업무 처리 서비스
    @Mock
    private TokenRedisService tokenRedisService;

    // 프로필 통계 서비스 단위 테스트 대상
    private SocialServiceImpl socialService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 소셜 서비스 구현체를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 소셜 서비스 단위 테스트 대상을 생성한다
        socialService = new SocialServiceImpl(socialMapper, reportMapper, userMapper, alimService, tokenRedisService);
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
    void setLikeSkipsAlimWhenDisabled() {
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
}
