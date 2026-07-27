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
 * 공통코드 일괄 조회 시 입력 정규화와 코드별 그룹화 정책을 검증한다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class CodeUtilTest {

    @Mock
    private CodeMapper codeMapper;

    private CodeUtil codeUtil;

    /**
     * 각 테스트가 독립된 CodeUtil 인스턴스를 사용하도록 Mock Mapper를 주입한다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        codeUtil = new CodeUtil(codeMapper);
    }

    /**
     * 공통코드의 공백과 대소문자 및 중복을 정리한 뒤 요청 그룹별로 결과를 반환하는지 검증한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getCodeGroupListNormalizesAndGroupsRequestedCodes() {
        List<String> normalizedCommCodeList = List.of("READ_STAT", "BOOK_COLR");
        CodeDto readCode = getCodeDto("READ_STAT", "READ");
        CodeDto colorCode = getCodeDto("BOOK_COLR", "BLUE");

        when(codeMapper.getCodeGroupList(normalizedCommCodeList))
                .thenReturn(List.of(colorCode, readCode));

        Map<String, List<CodeDto>> result = codeUtil.getCodeGroupList(
                List.of(" read_stat ", "BOOK_COLR", "READ_STAT")
        );

        assertEquals(List.of(readCode), result.get("READ_STAT"));
        assertEquals(List.of(colorCode), result.get("BOOK_COLR"));
        verify(codeMapper).getCodeGroupList(normalizedCommCodeList);
    }

    /**
     * 공통코드 목록이 비어 있으면 Oracle IN 조건을 생성하지 않고 잘못된 요청으로 거절하는지 검증한다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void getCodeGroupListRejectsEmptyRequestBeforeMapperCall() {
        CustomException exception = assertThrows(
                CustomException.class
              , () -> codeUtil.getCodeGroupList(List.of())
        );

        assertEquals(ResultEnum.COMMON_INVALID_REQUEST, exception.getResultEnum());
        verifyNoInteractions(codeMapper);
    }

    /**
     * 테스트용 세부코드 DTO를 생성한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 공통코드
     * @param comdCode 세부코드
     * @return 공통코드와 세부코드가 설정된 DTO
     */
    private CodeDto getCodeDto(
            String commCode
          , String comdCode
    ) {
        CodeDto codeDto = new CodeDto();
        codeDto.setCommCode(commCode);
        codeDto.setComdCode(comdCode);
        return codeDto;
    }
}
