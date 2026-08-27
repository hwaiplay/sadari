package org.our.sadari.global.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * fileName       : StringUtilTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 공통 문자열 정규화와 빈 값 판정 계약을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 */
class StringUtilTest {

    /**
     * 지원 타입별 빈 값과 존재하는 값을 기존 공통 정책대로 구분하는지 검증한다
     *
     * @author HanWon.Jang
     */
    @Test
    void isEmptySupportsTypes() {
        // null과 공백 문자열은 저장 가능한 내용이 없는 값으로 판정한다
        assertTrue(StringUtil.isEmpty(null));
        // 공백만 포함한 문자열은 빈 값으로 판정한다
        assertTrue(StringUtil.isEmpty("   "));
        // 빈 목록은 조회 결과가 없는 값으로 판정한다
        assertTrue(StringUtil.isEmpty(List.of()));
        // 빈 맵은 조회 결과가 없는 값으로 판정한다
        assertTrue(StringUtil.isEmpty(Map.of()));
        // 빈 객체 배열은 전달값이 없는 상태로 판정한다
        assertTrue(StringUtil.isEmpty(new Object[0]));
        // 내용이 있는 문자열은 유효한 값으로 판정한다
        assertFalse(StringUtil.isEmpty("sadari"));
        // primitive 배열은 기존 계약대로 존재하는 객체로 판정한다
        assertFalse(StringUtil.isEmpty(new byte[0]));
    }

    /**
     * 여러 필수값 중 하나라도 비어 있으면 누락 상태로 판정하는지 검증한다
     *
     * @author HanWon.Jang
     */
    @Test
    void hasEmptyChecksAllValues() {
        // 누락된 값이 포함된 요청은 빈 상태로 판정한다
        assertTrue(StringUtil.hasEmpty("book", null, 1L));
        // 모든 값이 존재하는 요청은 유효한 상태로 판정한다
        assertFalse(StringUtil.hasEmpty("book", 1L, List.of("report")));
        // 가변 인자 배열 자체가 없으면 필수값 누락 상태로 판정한다
        assertTrue(StringUtil.hasEmpty((Object[]) null));
    }

    /**
     * 사용자 평문 입력의 공백과 최대 길이를 기존 저장 계약대로 정규화하는지 검증한다
     *
     * @author HanWon.Jang
     */
    @Test
    void normalizePlainText() {
        // 앞뒤 공백을 제거한 사용자 입력을 반환하는지 확인한다
        assertEquals("독서 기록", StringUtil.normalizePlainText("  독서 기록  "));
        // 공백 제거 뒤 최대 길이를 적용한 사용자 입력을 반환하는지 확인한다
        assertEquals("독서", StringUtil.normalizePlainText(" 독서 기록 ", 2));
        // 공백만 있는 입력은 누락값으로 통일하는지 확인한다
        assertNull(StringUtil.normalizePlainText("   "));
    }

    /**
     * 문자열 길이 제한과 접미사 적용 시 기존 반환 계약을 유지하는지 검증한다
     *
     * @author HanWon.Jang
     */
    @Test
    void cutStringKeepsContract() {
        // 최대 길이를 초과한 문자열은 지정 길이와 접미사로 반환하는지 확인한다
        assertEquals("sad...", StringUtil.cutString("sadari", "...", 3));
        // 최대 길이 안의 문자열은 접미사 없이 원본으로 반환하는지 확인한다
        assertEquals("sad", StringUtil.cutString("sad", "...", 3));
        // 비어 있는 입력은 선택 입력 계약에 맞춰 null로 반환하는지 확인한다
        assertNull(StringUtil.cutString(" ", 3));
    }
}
