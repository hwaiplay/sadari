package org.our.sadari.sadariUser.user.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TokenHistoryDto {
  // 토큰 id
  private Long id;
  
  // 유저 넘버
  private Long userNumb;
  
  // refreshToken
  private String refrTokn;
  
  // 만료 시간
  private LocalDateTime exprDate;
  
  // 생성 시간
  private LocalDateTime cretDate;
  
  // 만료 여부 확인
  public boolean isExpired() {
    return exprDate.isBefore(LocalDateTime.now());
  }
}
