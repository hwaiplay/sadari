package org.our.sadari.readingClub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * fileName       : ClubChatSyncDto
 * author         : SeungHyeon.Kang
 * date           : 2026-11-26
 * description    : 새 메시지와 기존 메시지의 미읽음 수만 분리하여 전달
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-26        SeungHyeon.Kang    채팅 증분 동기화 응답 추가
 */
@Data
@Schema(description = "채팅 증분 동기화")
public class ClubChatSyncDto {

    @Schema(description = "마지막 조회 이후 새 메시지", example = "[]")
    private List<ReadingClubDto.ClubChatDto> messages;
    @Schema(description = "기존 최근 메시지의 미읽음 수", example = "[{\"chatNumb\":100,\"unreadCnt\":1}]")
    private List<ReadCount> readCounts;

    /**
     * fileName       : ReadCount
     * author         : SeungHyeon.Kang
     * date           : 2026-11-26
     * description    : 본문 재전송 없이 메시지별 미읽음 수 전달
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-11-26        SeungHyeon.Kang    미읽음 수 전용 응답 추가
     */
    @Data
    @Schema(description = "메시지 미읽음 수")
    public static class ReadCount {

        @Schema(description = "채팅 번호", example = "100")
        private Long chatNumb;
        @Schema(description = "미읽음 회원 수", example = "1")
        private Long unreadCnt;
    }
}
