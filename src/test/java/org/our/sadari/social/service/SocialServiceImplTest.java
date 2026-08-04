package org.our.sadari.social.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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
        when(userMapper.getUserByNumb(31L)).thenReturn(new UserDto());
        // 프로필 통계 SQL이 빈 기본 통계를 반환하도록 설정한다
        when(socialMapper.getProfileStats(any(SocialDto.ProfileStatsDto.class)))
                .thenReturn(new SocialDto.ProfileStatsDto());
    }

    /**
     * 다른 사용자 프로필 통계가 총 읽은 책과 받은 좋아요에 공개 독후감 조건을 전달하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getProfileStatsUsesPublicFilterForSocialProfile() {
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
    void getMyPageProfileStatsKeepsAllReports() {
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
