package org.our.sadari.sadariUser.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : LoginHistoryDto.java
 * author         : Seunghyeon.Kang
 * date           : 2026-07-13
 * description    : 로그인 성공 이력을 TB_LOGHIS 테이블에 저장하기 위한 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-13        Seunghyeon.Kang    TB_TOKHIS 토큰 이력 구조를 TB_LOGHIS 로그인 이력 구조로 전환
 */
@Data
public class LoginHistoryDto {

    // 로그인 이력 고유 번호이며 LOGHIS_SEQ로 채번한다.
    private Long lognNumb;

    // 로그인에 성공한 사용자 번호이다.
    private Long userNumb;

    // 로그인이 성공한 서버 기준 일시이다.
    private LocalDateTime lognDate;

    // 프록시 헤더 또는 원격 주소에서 계산한 로그인 요청 IP 주소이다.
    private String lognIpxx;

    // 로그인 요청 브라우저 또는 앱의 User-Agent 값이다.
    private String userAgnt;

    // 로그인 공급자 코드이며 현재 카카오 로그인은 KAKAO로 저장한다.
    private String provCode;
}
