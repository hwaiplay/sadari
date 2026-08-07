package org.our.sadari.notice.dto;

import java.util.List;

/**
 * fileName       : NoticePageDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 배포 공지 목록과 다음 페이지 여부를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 *
 * @param list 현재 페이지 공지 목록
 * @param page 현재 페이지 번호
 * @param hasNext 다음 페이지 존재 여부
 */
public record NoticePageDto(List<NoticeDto> list, int page, boolean hasNext) {
}
