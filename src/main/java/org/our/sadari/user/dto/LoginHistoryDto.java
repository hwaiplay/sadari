package org.our.sadari.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * LoginHistoryDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class LoginHistoryDto {

    // 아래 처리 단계의 업무 목적을 설명한다.
    private Long lognNumb;

    // 아래 처리 단계의 업무 목적을 설명한다.
    private Long userNumb;

    // 아래 처리 단계의 업무 목적을 설명한다.
    private LocalDateTime lognDate;

    // 아래 처리 단계의 업무 목적을 설명한다.
    private String lognIpxx;

    // 아래 처리 단계의 업무 목적을 설명한다.
    private String userAgnt;

    // 아래 처리 단계의 업무 목적을 설명한다.
    private String provCode;
}
