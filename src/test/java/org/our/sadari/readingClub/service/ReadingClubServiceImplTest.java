package org.our.sadari.readingClub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.readingClub.dto.ReadingClubDto;
import org.our.sadari.readingClub.mapper.ReadingClubMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : ReadingClubServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 독서 모임 서비스의 모임원 프로필 접근 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ReadingClubServiceImplTest {

    // 독서 모임 데이터 접근 객체
    @Mock
    private ReadingClubMapper readingClubMapper;

    // 사용자 입력 비속어 검사 서비스
    @Mock
    private BadWordDetectionService badWordDetectionService;

    // 독서 모임 서비스 단위 테스트 대상
    private ReadingClubServiceImpl readingClubService;

    /**
     * 각 테스트가 독립된 Mock 의존성을 사용하는 독서 모임 서비스 구현체를 구성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 실패 응답에서 사용할 서버 공통 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 테스트 조회 기준으로 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 공통 메시지 소스를 사용하도록 초기화한다
        new MessageUtils().setMessageSource(messageSource);

        // 독서 모임 서비스 단위 테스트 대상을 생성한다
        readingClubService = new ReadingClubServiceImpl(readingClubMapper, badWordDetectionService);
    }

    /**
     * 활성 모임원이 프로필 노출 조건을 통과한 모임원 목록을 조회하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getClubMemberListReturnsVisibleMembersForActiveMember() {
        // 조회 요청 사용자의 활성 모임원 관계를 구성한다
        ReadingClubDto.MemberDto requester = new ReadingClubDto.MemberDto();
        requester.setMembStat("ACTIVE");

        // 모임원 목록에 반환할 프로필 정보를 구성한다
        ReadingClubDto.MemberProfileDto profile = new ReadingClubDto.MemberProfileDto();
        profile.setUserNumb(20L);
        profile.setUserNick("모임원");
        profile.setMembRole("MEMBER");

        // 조회 요청 사용자의 모임원 관계와 노출 가능한 목록을 반환한다
        when(readingClubMapper.getClubMember(10L, 20L)).thenReturn(requester);
        when(readingClubMapper.getClubMemberList(10L)).thenReturn(List.of(profile));

        // 활성 모임원으로 모임원 프로필 목록을 조회한다
        ResultData result = readingClubService.getClubMemberList(20L, 10L);

        // 성공 코드와 조회한 프로필 목록을 검증한다
        assertEquals(200, result.getCode());
        assertEquals(List.of(profile), result.getData());
        verify(readingClubMapper).getClubMemberList(10L);
    }

    /**
     * 활성 모임원 관계가 없는 사용자의 프로필 목록 조회를 거절하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getClubMemberListRejectsNonMember() {
        // 조회 요청 사용자의 모임원 관계가 없도록 구성한다
        when(readingClubMapper.getClubMember(10L, 20L)).thenReturn(null);

        // 모임 외부 사용자로 모임원 프로필 목록을 조회한다
        ResultData result = readingClubService.getClubMemberList(20L, 10L);

        // 접근 거절 코드와 목록 SQL 미호출을 검증한다
        assertEquals(ResultEnum.COMMON_ACCESS_REJECTED.getCode(), result.getCode());
        verify(readingClubMapper, never()).getClubMemberList(10L);
    }
}
