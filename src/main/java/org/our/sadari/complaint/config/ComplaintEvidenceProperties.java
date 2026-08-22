package org.our.sadari.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * fileName       : ComplaintEvidenceProperties
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 관리자 전용 신고 이미지 증거의 보존과 정리 범위를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "complaint.evidence")
public class ComplaintEvidenceProperties {

    // 최종 처리 뒤 프로필 이미지 신고 증거 보존 일수
    private int retentionDays = 180;

    // 한 번의 스케줄 실행에서 삭제할 최대 증거 수
    private int cleanupBatchSize = 100;
}
