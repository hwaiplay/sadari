package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : UserSuspensionDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자에게 공개할 회원 정지 상태와 기간 정보를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "회원 정지 상태 DTO")
public class UserSuspensionDto {

    // 회원 정지 이력 번호
    private Long spndNumb;

    // 정지 대상 내부 회원번호
    private Long userNumb;

    // 정지 직전 회원 상태
    private String prevStat;

    // 기간 또는 무기한 정지 유형
    private String spndType;

    // 사용자에게 표시할 정지 유형명
    private String spndTypeName;

    // 회원 정지 사유 코드
    private String spndRson;

    // 사용자에게 표시할 정지 사유명
    private String spndRsonName;

    // 정지 이력 처리 상태
    private String spndStat;

    // 사용자에게 표시할 정지 상태명
    private String spndStatName;

    // 정지 시작일시
    private LocalDateTime strtDate;

    // 기간 정지 종료 예정일시
    private LocalDateTime endxDate;

    // 관리자 해제 또는 기간 만료일시
    private LocalDateTime rlesDate;
}
