package org.our.sadari.global.file.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.user.dto.UserDto;

/**
 * fileName       : FileUrlUtilTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 원본 배경사진 경로의 화면용 URL 변환 계약을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang         최초 생성
 */
class FileUrlUtilTest {

    /** 내부 배경사진 경로에만 화면용 variant가 추가되는지 검증함 */
    @Test
    void getBgPathAddsVariant() {
        String filePath = "/uploads/background/260826/123e4567-e89b-12d3-a456-426614174000.jpg";

        // DB 원본 경로를 화면용 파생본 URL로 변환함
        String displayPath = FileUrlUtil.getBgDisplayPath(filePath);

        // 원본 경로 뒤에 화면용 variant만 추가되는지 확인함
        assertEquals(filePath + "?variant=display", displayPath);
    }

    /** 외부 URL과 빈 값은 기존 표시 계약을 유지하는지 검증함 */
    @Test
    void getBgPathKeepsOther() {
        String externalPath = "https://example.com/background.jpg";

        // 서버가 파생본을 만들 수 없는 외부 URL과 빈 값을 변환함
        String keptExternalPath = FileUrlUtil.getBgDisplayPath(externalPath);
        String keptEmptyPath = FileUrlUtil.getBgDisplayPath(null);

        // 지원하지 않는 경로를 손상시키지 않는지 확인함
        assertEquals(externalPath, keptExternalPath);
        assertNull(keptEmptyPath);
    }

    /** 사용자와 피드 DTO가 SQL 원본 경로와 별도의 화면용 응답 필드를 제공하는지 검증함 */
    @Test
    void dtoUsesDisplayField() {
        String filePath = "/uploads/background/260826/123e4567-e89b-12d3-a456-426614174000.jpg";
        UserDto userDto = new UserDto();
        userDto.setBgimPath(filePath);
        FeedDto feedDto = new FeedDto();
        feedDto.setTagtType(Constant.FILE_TYPE_BACKGROUND);
        feedDto.setContentImagePath(filePath);
        ObjectMapper objectMapper = new ObjectMapper();

        // 실제 API 직렬화와 같은 방식으로 DTO 응답 필드명을 확인함
        JsonNode userJson = objectMapper.valueToTree(userDto);
        JsonNode feedJson = objectMapper.valueToTree(feedDto);

        // 원본 필드는 유지하고 화면용 필드는 display variant 경로를 제공하는지 확인함
        assertEquals(filePath, userJson.path("bgimPath").asText());
        assertEquals(filePath + "?variant=display", userJson.path("bgimDisplayPath").asText());
        assertEquals(filePath, feedJson.path("contentImagePath").asText());
        assertEquals(filePath + "?variant=display", feedJson.path("contentImageDisplayPath").asText());
        // Java 메서드 축약명이 별도 JSON 속성으로 노출되지 않는지 확인함
        assertTrue(feedJson.path("contentDisplayPath").isMissingNode());
    }
}
