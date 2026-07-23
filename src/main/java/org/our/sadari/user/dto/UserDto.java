package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * UserDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@Schema(description = "사용자 프로필 DTO")
public class UserDto {

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "OAuth 제공자 사용자 ID")
    private String userIdxx;

    @Schema(description = "닉네임. 한글, 영문, 숫자를 사용할 수 있다.", example = "reader31")
    private String userNick;

    @Schema(description = "로그인 제공자", example = "KAKAO")
    private String userProv;

    @Schema(description = "사용자 권한", example = "ROLE_USER")
    private String userRole;

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
