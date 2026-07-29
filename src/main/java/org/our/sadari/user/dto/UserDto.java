package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

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
 */
@Data
@Schema(description = "사용자 프로필 DTO")
public class UserDto {

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "OAuth 제공자 사용자 ID")
    private String userIdxx;

    @Schema(description = "최대 20자 닉네임. 한글, 영문, 숫자와 문자 사이의 단일 공백, 언더바, 하이픈을 사용할 수 있다.", example = "reader_31")
    private String userNick;

    @Schema(description = "로그인 제공자", example = "KAKAO")
    private String userProv;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String userRole;

    @Schema(description = "회원 상태", example = "ACTIVE")
    private String userStat;

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
}
