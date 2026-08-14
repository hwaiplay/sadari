package org.our.sadari.timer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * fileName       : ReadingTimerProperties
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 독서 타이머 출석과 세션 보존 기준을 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reading-timer")
public class ReadingTimerProperties {

    // 하루 출석으로 인정할 최소 독서 시간 초
    private long attendanceMinSeconds = 600L;
    // 단일 세션에서 기록할 수 있는 최대 독서 시간 초
    private long maxSessionSeconds = 14400L;
    // 일별 출석 경계를 계산할 서비스 시간대
    private String zoneId = "Asia/Seoul";
    // 완료 세션 상세 보존 일수
    private int detailRetentionDays = 365;
}
