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

    /**
     * 마이페이지 상단 통계 영역에 표시할 social 집계 DTO입니다.
     * 총 읽은 책은 독후감 완료 상태를 기준으로 세지만, 화면 요구사항상 팔로우/팔로워/좋아요와 함께 묶여 표시되므로
     * 조회 책임을 social 영역에 둡니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "마이페이지 프로필 통계 DTO")
    public static class ProfileStatsDto {

        // 로그인 사용자의 사용자 번호입니다. 화면에서 받지 않고 인증 정보에서 채워 조회 조건으로 사용합니다.
        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        // 완료 상태 독후감 수입니다. 마이페이지에서는 사용자가 지금까지 다 읽은 총 권수로 표시합니다.
        @Schema(description = "총 읽은 책 권수", example = "12")
        private int totalReadBookCnt;

        // 내가 팔로우하고 있는 사용자 수입니다.
        @Schema(description = "팔로우 수", example = "8")
        private int followingCnt;

        // 나를 팔로우하고 있는 사용자 수입니다.
        @Schema(description = "팔로워 수", example = "5")
        private int followerCnt;

        // 내 독후감이 받은 좋아요 수입니다.
        @Schema(description = "받은 좋아요 수", example = "42")
        private int receivedLikeCnt;
    }

    /**
     * 팔로우/팔로워 목록 조회 조건 DTO입니다.
     * userNumb는 목록의 주인이고, loginUserNumb는 각 목록 사용자에 대한 현재 로그인 사용자의 팔로우 상태를 계산하는 기준입니다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "팔로우 목록 조회 조건 DTO")
    public static class FollowListReqDto {

        // 목록을 조회할 프로필 주인의 사용자 번호입니다.
        @Schema(description = "목록 주인 사용자 번호", example = "31")
        private Long userNumb;

        // 각 목록 사용자 오른쪽에 표시할 팔로우 상태를 계산하는 로그인 사용자 번호입니다.
        @Schema(description = "로그인 사용자 번호", example = "1", hidden = true)
        private Long loginUserNumb;
    }

    /**
     * 팔로우/팔로워 목록에 표시할 사용자 DTO입니다.
     * 목록의 각 사용자는 프로필 기본 정보와 로그인 사용자 기준 팔로우 버튼명을 함께 가진다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "팔로우 목록 사용자 DTO")
    public static class FollowUserDto {

        // 목록에 표시할 사용자 번호입니다. 프로필 이동 및 팔로우/언팔로우 요청 대상 번호로 사용합니다.
        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        // 목록에 표시할 사용자 닉네임입니다.
        @Schema(description = "닉네임", example = "reader31")
        private String userNick;

        // 목록에 표시할 프로필 이미지 경로입니다.
        @Schema(description = "프로필 이미지 경로")
        private String porfPath;

        // 목록에 표시할 한줄소개입니다.
        @Schema(description = "한줄소개")
        private String intrCntn;

        // 로그인 사용자와 목록 사용자 사이의 팔로우 상태 버튼명입니다.
        @Schema(description = "팔로우 상태명", example = "팔로잉")
        private String followStatName;

        // 목록 사용자가 현재 로그인 사용자 자신인지 여부입니다. 자기 자신은 팔로우 조작 대상에서 제외하기 위해 화면에서 사용합니다.
        @Schema(description = "내 계정 여부", example = "N")
        private String meYsno;
    }
}
