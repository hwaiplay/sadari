package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * fileName       : UserDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 * 2026-07-29        SeungHyeon.Kang    닉네임 허용 문자와 최대 길이 설명 확장
 * 2026-07-29        SeungHyeon.Kang    닉네임 최대 길이를 25자로 확장
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 완료 여부 추가
 * 2026-08-04        SeungHyeon.Kang       최초 로그인 관심분야 선택 데이터 추가
 * 2026-08-05        SeungHyeon.Kang       관심분야 요청에서 공통코드 그룹 제거
 */
@Data
@Schema(description = "사용자 프로필 DTO")
public class UserDto {

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "OAuth 제공자 사용자 ID")
    private String userIdxx;

    @Schema(description = "최대 25자 닉네임. 한글, 영문, 숫자와 문자 사이의 단일 공백, 언더바, 하이픈을 사용할 수 있다.", example = "마음이 따뜻한 코끼리_26090001")
    private String userNick;

    @Schema(description = "로그인 제공자", example = "KAKAO")
    private String userProv;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String userRole;

    @Schema(description = "회원 상태", example = "ACTIVE")
    private String userStat;

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

    @Schema(description = "한줄소개", example = "추리소설을 좋아합니다.")
    private String intrCntn;

    @Schema(description = "프로필 이미지 경로")
    private String porfPath;

    @Schema(description = "배경 이미지 경로")
    private String bgimPath;

    /**
     * 회원이 선택하거나 화면에 노출할 독서 관심분야 항목을 전달한다
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
     * 최초 로그인에서 저장할 독서 관심분야 목록을 전달한다
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
