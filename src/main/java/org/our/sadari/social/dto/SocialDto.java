package org.our.sadari.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : SocialDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 사용자 검색과 팔로우 및 좋아요 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-08-04        SeungHyeon.Kang    프로필 독후감 공개 범위 조회 조건 추가
 * 2026-08-15        SeungHyeon.Kang    친구 상태·목록 조회 조건 추가
 * 2026-08-26        SeungHyeon.Kang        활성 좋아요 사용자 목록 추가
 * 2026-08-28        HanWon.Jang        활성 사용자 검색 조건 추가
 * 2026-09-03        HanWon.Jang        차단 관계 조회 조건 추가
 */
@Schema(description = "사용자 검색과 팔로우 및 좋아요 API DTO 컨테이너", hidden = true)
public class SocialDto {

    /**
     * 팔로우 관계 조회, 등록, 삭제에 사용하는 DTO이다.
     * 로그인 사용자와 대상 사용자를 한 객체로 묶어 Controller, Service, Mapper가 같은 파라미터 구조를 사용하게 한다.
     *
     * @author SeungHyeon.Kang
     */
    // 로그인 사용자와 상대 사용자의 팔로우 관계
    @Data
    @Schema(description = "팔로우 요청 및 상태 응답 DTO")
    public static class FollowDto {

        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        @Schema(description = "상대 사용자 번호", example = "32")
        private Long flowNumb;

        @Schema(description = "화면에 표시할 팔로우 버튼명", example = "팔로우"
              , allowableValues = {"팔로우", "팔로잉", "맞팔로우", "친구"})
        private String followStatName;
    }

    /**
     * 좋아요 등록, 취소, 상태 조회에 사용하는 DTO이다.
     * TB_LIKEXX가 공용 좋아요 테이블이므로 독후감 번호 전용 reptNumb 대신 대상 타입과 대상 번호를 사용한다.
     *
     * @author SeungHyeon.Kang
     */
    // 공용 좋아요 대상과 로그인 사용자의 좋아요 상태
    @Data
    @Schema(description = "좋아요 요청 및 상태 응답 DTO")
    public static class LikeDto {

        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        @Schema(description = "좋아요 대상 유형", example = "REPORT",
                allowableValues = {"REPORT", "PROFILE_IMAGE", "BACKGROUND_IMAGE"})
        private String tagtType;

        @Schema(description = "좋아요 대상 번호", example = "1")
        private Long tagtNumb;

        @Schema(description = "좋아요 수", example = "12")
        private Long likeCnt;

        @Schema(description = "로그인 사용자 좋아요 여부", example = "Y", allowableValues = {"Y", "N"})
        private String likeYsno;

        @Schema(description = "좋아요 대상 독후감 작성자 사용자 번호", example = "32")
        private Long targetUserNumb;

        @Schema(description = "좋아요 대상 독후감의 좋아요 알림 여부", example = "Y", allowableValues = {"Y", "N"}, hidden = true)
        private String likeAlimYsno;

        @Schema(description = "좋아요 알림 템플릿 코드", hidden = true)
        private String alimTempCode;

        @Schema(description = "알림 이동 대상 번호", hidden = true)
        private Long alimTagtNumb;
    }

    /**
     * 프로필 상단 통계 영역에 표시할 social 집계 DTO이다.
     * 총 읽은 책은 독후감 완료 상태를 기준으로 세지만, 화면 요구사항상 팔로우/팔로워/좋아요와 함께 묶여 표시되므로
     * 조회 책임을 social 영역에 둔다.
     *
     * @author SeungHyeon.Kang
     */
    // 프로필에 표시할 독서와 소셜 활동 집계
    @Data
    @Schema(description = "프로필 통계 DTO")
    public static class ProfileStatsDto {

        @Schema(description = "통계를 조회하는 로그인 사용자 번호", example = "44", hidden = true)
        private Long loginUserNumb;

        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long userNumb;

        @Schema(description = "독후감 공개 여부 조회 조건", example = "Y", hidden = true)
        private String pubcYsno;

        @Schema(description = "총 읽은 책 권수", example = "12")
        private int totalReadBookCnt;

        @Schema(description = "팔로우 수", example = "8")
        private int followingCnt;

        @Schema(description = "팔로워 수", example = "5")
        private int followerCnt;

        @Schema(description = "받은 좋아요 수", example = "42")
        private int receivedLikeCnt;
    }

    /**
     * 팔로우/팔로워 목록 조회 조건 DTO이다.
     * userNumb는 목록의 주인이고, loginUserNumb는 각 목록 사용자에 대한 현재 로그인 사용자의 팔로우 상태를 계산하는 기준이다.
     *
     * @author SeungHyeon.Kang
     */
    // 팔로워 또는 팔로잉 목록 조회 조건
    @Data
    @Schema(description = "팔로우 목록 조회 조건 DTO")
    public static class FollowListReqDto {

        @Schema(description = "목록 주인 사용자 번호", example = "31")
        private Long userNumb;

        @Schema(description = "로그인 사용자 번호", example = "1", hidden = true)
        private Long loginUserNumb;

        @Schema(description = "팔로우 목록 조회 시작 위치", example = "0", hidden = true)
        private Integer pageOffset;

        @Schema(description = "다음 페이지 판정을 포함한 조회 건수", example = "11", hidden = true)
        private Integer pageLimit;
    }

    /**
     * 피드에서 활성 사용자를 닉네임으로 검색할 때 사용하는 조회 조건 DTO이다.
     * 로그인 사용자 기준 관계 정렬과 페이지 범위를 한 객체로 Mapper에 전달한다.
     *
     * @author HanWon.Jang
     */
    // 활성 사용자 닉네임 검색과 관계 정렬 조건
    @Data
    @Schema(description = "활성 사용자 검색 조건 DTO")
    public static class UserSearchReqDto {

        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long loginUserNumb;

        @Schema(description = "닉네임 검색어", example = "reader", hidden = true)
        private String keyword;

        @Schema(description = "사용자 검색 시작 위치", example = "0", hidden = true)
        private Integer pageOffset;

        @Schema(description = "다음 페이지 판정을 포함한 조회 건수", example = "11", hidden = true)
        private Integer pageLimit;
    }

    /**
     * 팔로우/팔로워 목록에 표시할 사용자 DTO이다.
     * 목록의 각 사용자는 프로필 기본 정보와 로그인 사용자 기준 팔로우 버튼명을 함께 가진다.
     *
     * @author SeungHyeon.Kang
     */
    // 팔로워 또는 팔로잉 목록의 단일 사용자 정보
    @Data
    @Schema(description = "팔로우 목록 사용자 DTO")
    public static class FollowUserDto {

        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        @Schema(description = "닉네임", example = "reader31")
        private String userNick;

        @Schema(description = "프로필 이미지 경로")
        private String porfPath;

        @Schema(description = "한줄소개")
        private String intrCntn;

        @Schema(description = "팔로우 상태명", example = "팔로잉"
              , allowableValues = {"팔로우", "팔로잉", "맞팔로우", "친구"})
        private String followStatName;

        @Schema(description = "내 계정 여부", example = "N", allowableValues = {"Y", "N"})
        private String meYsno;
    }

    /**
     * 좋아요 사용자 목록 조회 조건 DTO이다.
     * 로그인 사용자와 대상 식별값 및 페이지 조건을 함께 전달해 접근 검증과 목록 조회가 같은 기준을 사용하게 한다.
     *
     * @author SeungHyeon.Kang
     */
    // 좋아요 사용자 목록의 대상과 페이지 조회 조건
    @Data
    @Schema(description = "좋아요 사용자 목록 조회 조건 DTO")
    public static class LikeUserReqDto {

        @Schema(description = "로그인 사용자 번호", example = "31", hidden = true)
        private Long loginUserNumb;

        @Schema(description = "좋아요 대상 유형", example = "REPORT", hidden = true)
        private String tagtType;

        @Schema(description = "좋아요 대상 번호", example = "1", hidden = true)
        private Long tagtNumb;

        @Schema(description = "좋아요 사용자 목록 조회 시작 위치", example = "0", hidden = true)
        private Integer pageOffset;

        @Schema(description = "다음 페이지 판정을 포함한 조회 건수", example = "11", hidden = true)
        private Integer pageLimit;
    }
}
