package org.our.sadari.sadariUser.user.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariUser.dto
 * fileName       : UserDto
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : 유저저 DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       최초 생성
 */

@Data
public class UserDto {
    // 유저 번호
    private Long userNumb;
    // 소셜 ID 값
    private String userIdxx;
    // 회원 닉네임
    private String userNick;
    // 회원 소셜 로그인 타입(카카오, 네이버 등)
    private String userProv;
    // 회원등급
    private String userRole;
    // 가입일
    private LocalDateTime joinDate;
    // 프로필 사진 경로
    private String porfPath;
}
