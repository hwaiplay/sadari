package org.our.sadari.complaint.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.complaint.config.ComplaintEvidenceProperties;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ComplaintEvidenceCleanupService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 최종 처리 후 보존기간이 지난 관리자 전용 신고 이미지 증거를 삭제한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class ComplaintEvidenceCleanupService {

    // 신고 증거 정리 SQL 접근 객체
    private final ComplaintMapper complaintMapper;

    // 신고 증거 보존기간과 실행당 처리 제한 설정
    private final ComplaintEvidenceProperties evidenceProperties;

    /**
     * 미처리 신고가 없고 최종 처리 뒤 정책 보존기간이 지난 이미지 증거를 정리한다.
     *
     * @author SeungHyeon.Kang
     */
    @Scheduled(cron = "${complaint.evidence.cleanup-cron:0 20 4 * * *}")
    @Transactional
    public void delExpiredEvidence() {

        // 설정값이 비정상이면 광범위한 증거 삭제를 실행하지 않는다
        if (evidenceProperties.getRetentionDays() < 1 || evidenceProperties.getCleanupBatchSize() < 1) {
            // 잘못된 보존 정책을 운영 로그에 남기고 이번 실행을 종료한다
            log.error("Complaint evidence cleanup configuration is invalid. retentionDays={}, batchSize={}"
                    , evidenceProperties.getRetentionDays(), evidenceProperties.getCleanupBatchSize());
            return;
        }

        // 최종 처리 기준을 만족한 증거만 설정된 최대 건수까지 물리 삭제한다
        int deleteCount = complaintMapper.delExpiredEvidence(
                evidenceProperties.getRetentionDays(), evidenceProperties.getCleanupBatchSize()
        );
        // 실제 삭제가 있을 때만 운영 추적 로그를 남긴다
        if (deleteCount > 0) {
            // 정리 결과와 적용된 보존기간을 기록한다
            log.info("Expired complaint evidence deleted. count={}, retentionDays={}"
                    , deleteCount, evidenceProperties.getRetentionDays());
        }
    }
}
