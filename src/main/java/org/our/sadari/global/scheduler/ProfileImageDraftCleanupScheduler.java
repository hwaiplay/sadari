package org.our.sadari.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.file.service.FileService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * fileName       : ProfileImageDraftCleanupScheduler
 * author         : SeungHyeon.Kang
 * date           : 2026-08-06
 * description    : 보존 시간이 지난 프로필 이미지 임시 파일을 정리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-06        SeungHyeon.Kang    최초 생성
 */
@Component
@RequiredArgsConstructor
public class ProfileImageDraftCleanupScheduler {

    // 프로필 이미지 임시 파일 업무 처리 서비스
    private final FileService fileService;

    /**
     * 10분마다 30분 보존 시간을 지난 임시 이미지를 삭제함
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(fixedDelayString = "${scheduler.profile-image-draft-cleanup-delay-ms:600000}")
    public void delExpiredProfileDrafts() {
        // 만료된 사용자별 임시 원본과 미리보기 정리를 파일 서비스에 위임함
        fileService.delExpiredProfileDrafts();
    }
}
