package org.our.sadari.user.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * UserDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class UserDto {
    // 아래 처리 단계의 업무 목적을 설명한다.
    private Long userNumb;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private String userIdxx;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private String userNick;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private String userProv;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private String userRole;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private LocalDateTime joinDate;
    // 아래 처리 단계의 업무 목적을 설명한다.
    private Long profNumb;
    private Long bgimNumb;
    private String intrCntn;
    private String porfPath;
    private String bgimPath;
}
