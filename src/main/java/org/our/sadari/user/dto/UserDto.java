package org.our.sadari.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import org.our.sadari.global.file.util.FileUrlUtil;

/**
 * fileName       : UserDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 요청과 응답 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-07-29        SeungHyeon.Kang    닉네임 문자·길이 설명 정리
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 완료 여부 추가
 * 2026-08-04        SeungHyeon.Kang       최초 로그인 관심분야 선택 데이터 추가
 * 2026-08-05        SeungHyeon.Kang       관심분야 요청에서 공통코드 그룹 제거
 * 2026-08-07        SeungHyeon.Kang    닉네임 공백 금지 계약 반영
 * 2026-08-15        SeungHyeon.Kang    소셜 프로필 접근 안내용 회원 상태명 추가
 * 2026-08-26        HanWon.Jang         화면용 배경사진 경로 추가
 * 2026-08-27        SeungHyeon.Kang    사진 반응 조회 사용자 분리
 */
@Data
@Schema(description = "사용자 프로필 DTO")
public class UserDto {

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "OAuth 제공자 사용자 ID")
    private String userIdxx;

    @Schema(description = "최대 25자 닉네임. 공백 없이 한글, 영문, 숫자와 문자 사이의 단일 언더바 또는 하이픈을 사용할 수 있다.", example = "마음이따뜻한코끼리_26090001")
    private String userNick;

    @Schema(description = "로그인 제공자", example = "KAKAO")
    private String userProv;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String userRole;

    @Schema(description = "회원 상태", example = "ACTIVE")
    private String userStat;

    @Schema(description = "회원 상태명", example = "정상 회원")
    private String userStatName;

    @Schema(description = "최초 로그인 온보딩 완료 여부", example = "N")
    private String onbdYsno;

    @Schema(description = "회원 탈퇴 요청일시")
    private LocalDateTime wthdDate;

    @Schema(description = "회원 영구 삭제 예정일시")
    private LocalDateTime deltDate;

    @Schema(description = "가입일시")
    private LocalDateTime joinDate;

    @Schema(description = "프로필 이미지 파일 번호")
    private Long profNumb;

    @Schema(description = "배경 이미지 파일 번호")
    private Long bgimNumb;

    @Schema(description = "사용자 직접 사진 변경 피드 기록 여부", hidden = true)
    private String imageFeedYsno;

    @Schema(description = "최종 저장할 프로필 이미지 임시 식별값")
    private String profileImageDraftToken;

    @Schema(description = "최종 저장할 배경 이미지 임시 식별값")
    private String backgroundImageDraftToken;

    @Schema(description = "한줄소개", example = "추리소설을 좋아합니다.")
    private String intrCntn;

    @Schema(description = "프로필 이미지 경로")
    private String porfPath;

    @Schema(description = "배경 이미지 경로")
    private String bgimPath;

    /**
     * 일반 프로필 화면에서 사용할 축소 배경사진 경로를 반환함
     *
     * @author HanWon.Jang
     * @return 화면용 배경사진 경로
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "일반 화면용 배경 이미지 경로", accessMode = Schema.AccessMode.READ_ONLY)
    public String getBgimDisplayPath() {
        // DB 원본 경로는 유지하고 API 직렬화 시 화면용 파생본 URL을 추가함
        return FileUrlUtil.getBgDisplayPath(bgimPath);
    }

    /**
     * 마이페이지의 현재 프로필 또는 배경 사진에 표시할 좋아요와 댓글 집계를 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @Schema(description = "사용자 사진 좋아요와 댓글 집계")
    public static class ImageReactionDto {

        @Schema(description = "로그인 사용자 번호", hidden = true)
        @JsonIgnore
        private Long userNumb;

        @Schema(description = "사진 소유자 사용자 번호", hidden = true)
        @JsonIgnore
        private Long ownerUserNumb;

        @Schema(description = "사진 대상 유형", allowableValues = {"PROFILE_IMAGE", "BACKGROUND_IMAGE"})
        private String tagtType;

        @Schema(description = "사진 파일 번호")
        private Long tagtNumb;

        @Schema(description = "좋아요 수", example = "12")
        private Long likeCnt;

        @Schema(description = "로그인 사용자 좋아요 여부", example = "Y", allowableValues = {"Y", "N"})
        private String likeYsno;

        @Schema(description = "댓글 수", example = "3")
        private Long replCnt;
    }

    /**
     * 회원이 선택하거나 화면에 노출할 독서 관심분야 항목을 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @Schema(description = "독서 관심분야 항목")
    public static class UserInterestDto {

        @Schema(description = "관심분야 대분류명", example = "문학")
        private String intrCnam;

        @Schema(description = "관심분야 세부코드", example = "NOVEL")
        private String intrCode;

        @Schema(description = "관심분야 세부코드명", example = "소설")
        private String intrName;

        @Schema(description = "대분류 정렬 순서", example = "1")
        private Integer cgrpOrdr;

        @Schema(description = "세부코드 정렬 순서", example = "1")
        private Integer codeOrdr;
    }

    /**
     * 최초 로그인에서 저장할 독서 관심분야 목록을 전달함
     *
     * @author SeungHyeon.Kang
     */
    @Data
    @Schema(description = "독서 관심분야 저장 요청")
    public static class UserInterestReqDto {

        @Schema(description = "선택한 독서 관심분야 목록")
        private List<UserInterestDto> interestList;
    }
}
