package org.our.sadari.timer.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.timer.dto.ReadingTimerDto;

/**
 * fileName       : ReadingTimerService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머와 주간 출석 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 * 2026-08-20        SeungHyeon.Kang    목표시간 알림 통합·도서누적 페이지 조회
 */
public interface ReadingTimerService {

    /**
     * 로그인 사용자의 현재 타이머와 이번 주 출석 현황을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 타이머 화면 요약 데이터
     */
    ResultData getTimerSummary(Long userNumb);

    /**
     * 로그인 사용자의 도서별 누적 독서 시간을 최근 기록순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 현재 페이지 도서별 누적시간과 다음 페이지 여부
     */
    ResultData getBookTimePage(Long userNumb, int page);

    /**
     * 중복 실행 요청을 흡수하며 새 독서 타이머를 시작한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 시작 요청 정보
     * @return 시작 후 타이머 화면 요약 데이터
     */
    ResultData setTimer(Long userNumb, ReadingTimerDto.Request request);

    /**
     * 실행 중인 타이머를 재개, 일시정지 또는 완료 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tmrxNumb 변경할 세션 번호
     * @param request 변경할 상태 정보
     * @return 변경 후 타이머 화면 요약 데이터
     */
    ResultData uptTimer(Long userNumb, Long tmrxNumb, ReadingTimerDto.Request request);

    /**
     * 목표 독서시간이 지난 실행 세션의 알림을 발송한다
     *
     * @author SeungHyeon.Kang
     */
    void sendTimerAlim();

    /**
     * 계정 상태 변경 직전에 실행 중인 독서 시간을 확정하고 타이머를 완료한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태를 변경할 사용자 번호
     */
    void uptTimerWithdrawal(Long userNumb);

    /**
     * 보존기간이 지난 완료 세션 상세를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @return 삭제한 세션 수
     */
    int delExpiredTimer();
}
