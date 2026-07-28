package org.our.sadari.global.scheduler.service;

/**
 * fileName       : ReportDateOverService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
public interface ReportDateOverService {

    /**
     * 목표 독서 종료일이 지난 진행 중 독후감을 조회해 알림과 FCM 푸시를 발송
     *
     * @author SeungHyeon.Kang
     */
    void sendReportDateOverAlim();
}
