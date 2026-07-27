package org.our.sadari.global.scheduler.service;

/**
 * 정기 작업이 수행할 업무를 정의하는 서비스 인터페이스.
 * 스케줄 실행 시각과 실제 업무 처리를 분리해 수동 실행이나 로그 관리 기능에서도 같은 업무를 재사용할 수 있게 
 *
 * @author Seunghyeon.Kang
 */
public interface ReportDateOverService {

    /**
     * 목표 독서 종료일이 지난 진행 중 독후감을 조회해 알림과 FCM 푸시를 발송
     *
     * @author Seunghyeon.Kang
     */
    void sendReportDateOverAlim();
}
