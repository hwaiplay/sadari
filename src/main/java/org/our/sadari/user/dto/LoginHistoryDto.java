package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : LoginHistoryDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 로그인 사용자와 접속 환경의 이력 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@Schema(description = "로그인 성공 여부와 접속 환경 이력 DTO", hidden = true)
public class LoginHistoryDto {

    // 로그인 이력 번호
    private Long lognNumb;

    // 로그인 사용자 번호
    private Long userNumb;

    // 로그인 일시
    private LocalDateTime lognDate;

    // 로그인 요청 IP 주소
    private String lognIpxx;

    // 로그인 요청 브라우저 User-Agent
    private String userAgnt;

    // OAuth 로그인 제공자 코드
    private String provCode;
}
