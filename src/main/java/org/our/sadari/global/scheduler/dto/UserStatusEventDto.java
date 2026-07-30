package org.our.sadari.global.scheduler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : UserStatusEventDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 회원 상태 변경 Outbox 이벤트와 현재 DB 상태를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    정지 이력 식별값 추가
 */
@Data
@Schema(description = "회원 상태 변경 Outbox 이벤트 DTO", hidden = true)
public class UserStatusEventDto {

    // Outbox 이벤트 번호
    private Long evntNumb;

    // 사용자 서비스 처리 이벤트 유형
    private String evntType;

    // 상태 변경 대상 내부 회원번호
    private Long userNumb;

    // 동기화 상태를 변경할 정지 이력 번호
    private Long spndNumb;

    // 처리 시점의 DB 회원 상태
    private String userStat;

    // Outbox 이벤트 등록일시
    private LocalDateTime regiDate;
}
