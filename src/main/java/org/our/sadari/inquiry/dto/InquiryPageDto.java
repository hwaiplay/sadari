package org.our.sadari.inquiry.dto;

import java.util.List;

/**
 * fileName       : InquiryPageDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 사용자 고객문의 목록과 다음 페이지 여부를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 *
 * @param list 현재 페이지 고객문의 목록
 * @param page 현재 페이지 번호
 * @param hasNext 다음 페이지 존재 여부
 */
public record InquiryPageDto(List<InquiryDto> list, int page, boolean hasNext) {
}
