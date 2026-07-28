package org.our.sadari.global.common.code.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.mapper.CodeMapper;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;

/**
 * fileName       : CodeUtilTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 공통 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class CodeUtilTest {
    // Code 데이터 접근 객체
    @Mock
    private CodeMapper codeMapper;

    // 공통코드 캐시 조회 객체
    private CodeUtil codeUtil;

    /**
     * 각 테스트가 독립된 CodeUtil 인스턴스를 사용하도록 Mock Mapper를 주입한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 공통코드 캐시 단위 테스트 대상을 담을 객체를 생성한다
        codeUtil = new CodeUtil(codeMapper);
    }

    /**
     * 공통코드의 공백과 대소문자 및 중복을 정리한 뒤 요청 그룹별로 결과를 반환하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getCodeGroupListNormalizesAndGroupsRequestedCodes() {
        // 필요한 값으로 불변 객체를 생성한다
        List<String> normalizedCommCodeList = List.of("READ_STAT", "BOOK_COLR");
        // getCodeDto 조회로 후속 처리에 필요한 데이터를 가져온다
        CodeDto readCode = getCodeDto("READ_STAT", "READ");
        // getCodeDto 조회로 후속 처리에 필요한 데이터를 가져온다
        CodeDto colorCode = getCodeDto("BOOK_COLR", "BLUE");

        // CodeGroupList 데이터를 DB에서 조회한다
        when(codeMapper.getCodeGroupList(normalizedCommCodeList))
                .thenReturn(List.of(colorCode, readCode));

        // getCodeGroupList 조회로 후속 처리에 필요한 데이터를 가져온다
        Map<String, List<CodeDto>> result = codeUtil.getCodeGroupList(
                // 필요한 값으로 불변 객체를 생성한다
                List.of(" read_stat ", "BOOK_COLR", "READ_STAT")
        );

        // 필요한 값으로 불변 객체를 생성한다
        assertEquals(List.of(readCode), result.get("READ_STAT"));
        // 필요한 값으로 불변 객체를 생성한다
        assertEquals(List.of(colorCode), result.get("BOOK_COLR"));
        // 의존 객체가 예상한 인자로 호출되었는지 검증한다
        verify(codeMapper).getCodeGroupList(normalizedCommCodeList);
    }

    /**
     * 공통코드 목록이 비어 있으면 Oracle IN 조건을 생성하지 않고 잘못된 요청으로 거절하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getCodeGroupListRejectsEmptyRequestBeforeMapperCall() {
        // 검증 대상 코드가 예상 예외를 발생시키는지 확인한다
        CustomException exception = assertThrows(
                CustomException.class
              , () -> codeUtil.getCodeGroupList(List.of())
        );

        // getResultEnum 조회로 후속 처리에 필요한 데이터를 가져온다
        assertEquals(ResultEnum.COMMON_INVALID_REQUEST, exception.getResultEnum());
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인한다
        verifyNoInteractions(codeMapper);
    }

    /**
     * 테스트용 세부코드 DTO를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param commCode 공통코드
     * @param comdCode 세부코드
     * @return 공통코드와 세부코드가 설정된 DTO
     */
    private CodeDto getCodeDto(String commCode, String comdCode) {
        // 공통코드 항목을 담을 객체를 생성한다
        CodeDto codeDto = new CodeDto();
        // CommCode 업무 값을 codeDto DTO에 설정한다
        codeDto.setCommCode(commCode);
        // ComdCode 업무 값을 codeDto DTO에 설정한다
        codeDto.setComdCode(comdCode);
        // 테스트용 세부코드 DTO를 생성 결과를 반환한다
        return codeDto;
    }
}
