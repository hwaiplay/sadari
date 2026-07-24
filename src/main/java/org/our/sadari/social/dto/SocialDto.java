package org.our.sadari.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 팔로우와 좋아요처럼 사용자 간 social 동작에서 사용하는 DTO 모음입니다.
 * social 기능이 늘어나도 요청값을 개별 원시 파라미터로 흩뿌리지 않고, 목적별 중첩 DTO로 묶어 Mapper까지 전달합니다.
 *
 * @author Seunghyeon.Kang
 */
public class SocialDto {

    /**
     * 팔로우 관계 조회, 등록, 삭제에 사용하는 DTO입니다.
     * 로그인 사용자와 대상 사용자를 한 객체로 묶어 Controller, Service, Mapper가 같은 파라미터 구조를 사용하게 합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "팔로우 요청 및 상태 응답 DTO")
    public static class FollowDto {

        // 로그인 사용자 번호입니다. 화면에서 받지 않고 인증 정보에서 주입합니다.
        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        // 팔로우하거나 팔로우 상태를 조회할 상대 사용자 번호입니다.
        @Schema(description = "상대 사용자 번호", example = "32")
        private Long flowNumb;

        // 화면에 표시할 팔로우 버튼명입니다. 예: 팔로우, 맞팔로우, 팔로잉
        @Schema(description = "화면에 표시할 팔로우 버튼명", example = "팔로우")
        private String followStatName;
    }

    /**
     * 좋아요 등록, 취소, 상태 조회에 사용하는 DTO입니다.
     * TB_LIKEXX가 공용 좋아요 테이블이므로 독후감 번호 전용 reptNumb 대신 대상 타입과 대상 번호를 사용합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "좋아요 요청 및 상태 응답 DTO")
    public static class LikeDto {

        // 로그인 사용자 번호입니다. 화면에서 받지 않고 인증 정보에서 주입합니다.
        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        // 좋아요 대상 유형입니다. 현재 화면에서는 REPORT만 허용합니다.
        @Schema(description = "좋아요 대상 유형", example = "REPORT")
        private String tagtType;

        // 좋아요 대상 번호입니다. REPORT인 경우 TM_REPORT.REPT_NUMB를 의미합니다.
        @Schema(description = "좋아요 대상 번호", example = "1")
        private Long tagtNumb;

        // 대상에 등록된 총 좋아요 수입니다.
        @Schema(description = "좋아요 수", example = "12")
        private Long likeCnt;

        // 로그인 사용자의 좋아요 여부입니다. Y/N 값으로 화면의 버튼 상태를 제어합니다.
        @Schema(description = "로그인 사용자 좋아요 여부", example = "Y")
        private String likeYsno;
    }
}
