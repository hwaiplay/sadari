package org.our.sadari.global.common.code.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.mapper.CodeMapper;
import org.springframework.stereotype.Component;

/**
 * CodeUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
public class CodeUtil {

    private final CodeMapper codeMapper;

    /**
     * getCodeList 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 처리에 필요한 입력값
     * @return 처리 결과
     */
    public List<CodeDto> getCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * getFirstCode 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 처리에 필요한 입력값
     * @return 처리 결과
     */
    public String getFirstCode(String commCode) {
        return getCodeList(commCode).stream()
                .findFirst()
                .map(CodeDto::getComdCode)
                .orElse(null);
    }

    /**
     * existsCode 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 처리에 필요한 입력값
     * @param comdCode 처리에 필요한 입력값
     * @return 처리 결과
     */
    public boolean existsCode(String commCode, String comdCode) {
        return getCodeList(commCode).stream()
                .anyMatch(code -> code.getComdCode().equalsIgnoreCase(comdCode));
    }

    /**
     * getCodeName 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 처리에 필요한 입력값
     * @param comdCode 처리에 필요한 입력값
     * @return 처리 결과
     */
    public String getCodeName(String commCode, String comdCode) {
        return getCodeName(commCode, comdCode, null);
    }

    /**
     * getCodeName 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 처리에 필요한 입력값
     * @param comdCode 처리에 필요한 입력값
     * @param optCode 처리에 필요한 입력값
     * @return 처리 결과
     */
    public String getCodeName(String commCode, String comdCode, String optCode) {
        return codeMapper.getCodeName(commCode, comdCode, optCode);
    }
}
