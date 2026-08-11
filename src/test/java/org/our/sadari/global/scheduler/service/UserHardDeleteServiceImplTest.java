package org.our.sadari.global.scheduler.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.mapper.UserHardDeleteMapper;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserWithdrawalDto;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : UserHardDeleteServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-06
 * description    : 영구 탈퇴 회원의 DB 데이터와 물리 파일 삭제 연계를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-06        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class UserHardDeleteServiceImplTest {

    // 영구 삭제 대상 데이터 접근 객체 대역
    @Mock
    private UserHardDeleteMapper userHardDeleteMapper;
    // 영구 탈퇴 회원 파일 정리 서비스 대역
    @Mock
    private FileService fileService;
    // 영구 탈퇴 회원 인증 정보 정리 서비스 대역
    @Mock
    private TokenRedisService tokenRedisService;
    // 스케줄러 로그 안전 처리 객체 대역
    @Mock
    private SchedulerLogSupport schedulerLogSupport;
    // 영구 탈퇴 파일 삭제 연계 검증 대상 서비스
    @InjectMocks
    private UserHardDeleteServiceImpl userHardDeleteService;

    /**
     * 각 테스트에서 영구 삭제 대상 조회 제한 건수를 설정한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 영구 삭제 대상 한 건을 조회할 최대 처리 건수를 설정한다
        ReflectionTestUtils.setField(userHardDeleteService, "maxSize", 100);
    }

    /**
     * 영구 탈퇴 회원의 파일 정보를 DB 삭제 전에 확보하고 삭제 커밋 후 물리 파일 정리를 등록하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delUsersSetsFileCleanup() {
        // 영구 삭제 대상 회원 정보를 생성한다
        UserWithdrawalDto target = new UserWithdrawalDto();
        // 영구 삭제할 사용자 번호를 설정한다
        target.setUserNumb(31L);

        // 영구 삭제 전에 확보할 프로필 파일 메타정보를 생성한다
        FileDto profileFile = new FileDto();
        // 영구 삭제할 프로필 파일 번호를 설정한다
        profileFile.setFileNumb(10L);
        // 영구 삭제할 프로필 파일명을 설정한다
        profileFile.setStorName("profile.png");
        // 영구 삭제할 프로필 파일 접근 경로를 설정한다
        profileFile.setFilePath("/uploads/profile/260804/profile.png");
        // 영구 탈퇴 스케줄러가 사용자 한 건을 조회하도록 설정한다
        when(userHardDeleteMapper.getHardDeleteTargetList(100)).thenReturn(List.of(target));
        // 프로시저 실행 전에 해당 사용자의 파일 정보를 반환하도록 설정한다
        when(fileService.getFileListByRegiUser(31L)).thenReturn(List.of(profileFile));
        // 한 건 성공 상태를 스케줄러 완료 코드로 변환하도록 설정한다
        when(schedulerLogSupport.getSchedulerExecStatus(1, 0)).thenReturn("SUCCESS");
        // 스케줄러 실행 로그 번호를 반환하도록 설정한다
        when(schedulerLogSupport.setSchedulerLogSafely(any())).thenReturn(100L);

        // 유예기간이 끝난 회원의 영구 삭제를 실행한다
        userHardDeleteService.delPendingUsers();

        // 파일 조회와 DB 삭제 및 물리 파일 후처리 호출 순서를 검증할 객체를 생성한다
        InOrder deleteOrder = inOrder(fileService, userHardDeleteMapper);
        // DB 메타정보 삭제 전에 물리 파일 경로를 확보하는지 확인한다
        deleteOrder.verify(fileService).getFileListByRegiUser(31L);
        // 파일 경로 확보 뒤 회원과 파일 메타정보를 삭제하는지 확인한다
        deleteOrder.verify(userHardDeleteMapper).delHardDeleteUser(31L);
        // 회원 원본 삭제와 함께 모든 Redis 세션 및 인증 캐시를 제거하는지 확인한다
        verify(tokenRedisService).delAllUserInfo(31L);
        // DB 삭제 뒤 커밋 후 물리 파일 정리를 등록하는지 확인한다
        deleteOrder.verify(fileService).delFilesAfterCommit(List.of(profileFile));
        // 영구 삭제 실행 결과를 스케줄러 로그에 최종 반영하는지 확인한다
        verify(schedulerLogSupport).uptSchedulerLogSafely(any());
    }
}
