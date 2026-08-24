package org.our.sadari.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * fileName       : ComplaintResultProperties
 * author         : HanWon.Jang
 * date           : 2026-08-24
 * description    : 한 번에 표시할 미확인 신고 조치 결과 범위를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-24        HanWon.Jang        최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "complaint.result")
public class ComplaintResultProperties {

    // 한 번의 팝업에 표시할 최대 신고 조치 결과 수
    private int maxSize = 5;
}
