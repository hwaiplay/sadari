package org.our.sadari.global.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;

/**
 * fileName       : BadWordDetectionServiceTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-03
 * description    : 비속어 우회 표현과 공백 경계 탐지 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-03        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class BadWordDetectionServiceTest {

    // 비속어와 예외 허용어 공통코드 조회 객체
    @Mock
    private CodeUtil codeUtil;

    // 비속어 탐지 단위 테스트 대상
    private BadWordDetectionService badWordDetectionService;

    /**
     * 핵심 비속어와 정상 표현 예외 사전을 각 테스트에 동일하게 설정한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 비속어 탐지에 사용할 핵심 사전을 설정한다
        when(codeUtil.getCodeList(Constant.CODE_BADX_WORD))
                .thenReturn(getCodeList("시발", "씨발", "18년"));
        // 정상 표현 안의 비속어 문자열을 허용할 예외 사전을 설정한다
        when(codeUtil.getCodeList(Constant.CODE_EXCP_WORD))
                .thenReturn(getCodeList("시발점"));
        // 공통코드 Mock을 주입한 비속어 탐지 서비스를 생성한다
        badWordDetectionService = new BadWordDetectionService(codeUtil);
    }

    /**
     * 공백이 없는 비속어 안에 같은 문자를 두 번 이상 반복 삽입해도 원래 비속어를 탐지하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBadWordRepeatedChars() {
        // 반복 구간을 제거해야 복원되는 비속어를 탐지하는지 확인한다
        assertEquals(Optional.of("시발"), badWordDetectionService.findBadWord("시이이이이발"));
        // 원래 비속어 글자를 반복한 표현을 축약하여 탐지하는지 확인한다
        assertEquals(Optional.of("씨발"), badWordDetectionService.findBadWord("씨씨씨발"));
        // 비속어 사이에 반복 삽입한 다른 한글 문자를 제거하여 탐지하는지 확인한다
        assertEquals(Optional.of("시발"), badWordDetectionService.findBadWord("시ㅋㅋㅋ발"));
    }

    /**
     * 비속어를 구성하는 글자 사이에 공백 문자가 있으면 서로 다른 단어로 보고 허용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBadWordKeepsSpaces() {
        // 정상 문장의 서로 다른 단어에 걸친 시 발 문자열을 허용하는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("한번 시작 시 발행해야함").isEmpty());
        // 여러 일반 공백으로 분리한 문자열을 하나의 비속어로 합치지 않는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("시   발").isEmpty());
        // 탭으로 분리한 문자열도 하나의 비속어로 합치지 않는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("시\t발").isEmpty());
        // 줄바꿈되지 않는 공백으로 분리한 문자열도 하나의 비속어로 합치지 않는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("시\u00A0발").isEmpty());
        // 반복 문자 주변에 공백이 있으면 반복 구간 제거 후에도 단어 경계를 유지하는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("시 이이이 발").isEmpty());
    }

    /**
     * 공백이 없는 특수문자와 숫자 삽입 우회 표현은 기존 정책대로 탐지하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBadWordObfuscation() {
        // 공백이 없는 특수문자를 제거해 복원되는 비속어를 탐지하는지 확인한다
        assertEquals(Optional.of("시발"), badWordDetectionService.findBadWord("시!발"));
        // 숫자를 보존한 전용 사전으로 숫자 포함 비속어를 탐지하는지 확인한다
        assertEquals(Optional.of("18년"), badWordDetectionService.findBadWord("1!8년"));
    }

    /**
     * 반복 문자 제거 후 정상 허용어가 복원되면 포함된 비속어를 차단하지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBadWordKeepsExceptions() {
        // 반복 문자 우회 정규화 후 시발점 전체가 허용어로 감싸지는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("시이이이이발점").isEmpty());
    }

    /**
     * 신체 표현과 성정체성 표현을 비속어 사전에 두지 않으면 일반 입력으로 허용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBadWordAllowsSafeTerms() {
        // 신체 표현을 비속어로 차단하지 않는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("보지").isEmpty());
        // 성정체성 표현을 비속어로 차단하지 않는지 확인한다
        assertTrue(badWordDetectionService.findBadWord("게이").isEmpty());
    }

    /**
     * 테스트용 공통코드 세부 목록을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param words 세부코드명으로 설정할 문자열 목록
     * @return 세부코드명이 설정된 공통코드 DTO 목록
     */
    private List<CodeDto> getCodeList(String... words) {
        // 입력 순서를 유지하여 탐지 사전으로 변환한다
        // 세부코드명이 설정된 공통코드 DTO 목록을 반환한다
        return List.of(words).stream()
                .map(this::getCodeDto)
                .toList();
    }

    /**
     * 테스트용 공통코드 세부 DTO를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param word 세부코드명으로 설정할 문자열
     * @return 세부코드명이 설정된 공통코드 DTO
     */
    private CodeDto getCodeDto(String word) {
        // 비속어 또는 허용어 세부코드명을 담을 객체를 생성한다
        CodeDto codeDto = new CodeDto();
        // 탐지 사전에 적재할 세부코드명을 설정한다
        codeDto.setComdName(word);
        // 세부코드명이 설정된 공통코드 DTO를 반환한다
        return codeDto;
    }
}
