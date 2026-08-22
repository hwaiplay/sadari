package org.our.sadari.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.our.sadari.global.common.constant.Constant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * fileName       : ComplaintAutoActionProperties
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상별 누적 자동 조치 임계치를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "complaint.auto-action")
public class ComplaintAutoActionProperties {

    // 독후감 자동 비공개 전환 신고 임계치
    private int bookReportThreshold = 5;
    // 댓글 자동 삭제 신고 임계치
    private int replyThreshold = 5;
    // 프로필 사진 자동 초기화 신고 임계치
    private int profileImageThreshold = 5;
    // 한줄소개 자동 초기화 신고 임계치
    private int introductionThreshold = 5;

    /**
     * 신고 대상 유형에 대응하는 자동 조치 임계치를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @return 대상별 임계치, 자동 조치 대상이 아니면 0
     */
    public int getThreshold(String tagtType) {
        // 자동 조치 대상 유형은 설정 파일의 개별 임계치와 고정 매핑한다
        return switch (tagtType) {
            // 독후감은 비공개 전환 기준을 반환한다
            case Constant.COMPLAINT_TARGET_REPORT -> bookReportThreshold;
            // 댓글은 논리 삭제 기준을 반환한다
            case Constant.COMPLAINT_TARGET_REPLY -> replyThreshold;
            // 프로필 사진은 기본 이미지 초기화 기준을 반환한다
            case Constant.COMPLAINT_TARGET_PROFILE -> profileImageThreshold;
            // 한줄소개는 Null 초기화 기준을 반환한다
            case Constant.COMPLAINT_TARGET_INTRO -> introductionThreshold;
            // 사용자 전체 신고와 예약 대상은 관리자가 직접 검토한다
            default -> 0;
        };
    }
}
