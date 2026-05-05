package org.our.sadari.sadariBook.dto;

import lombok.Data;

/**
 * packageName    : org.our.sadari.sadariBook.dto
 * fileName       : ReportRequestDto.java
 * author         : Hanwon.Jang
 * date           : 2026-05-04
 * description    : 요청 전용 Wrapper DTO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-04       Hanwon.Jang       최초 생성
 */

@Data
public class ReportRequestDto {
  // 책 DTO
  private BookDto bookDto;
  // 독후감 DTO
  private ReportDto reportDto;
}
