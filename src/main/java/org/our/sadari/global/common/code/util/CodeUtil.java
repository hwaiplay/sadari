package org.our.sadari.global.common.code.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.mapper.CodeMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeUtil {

    private final CodeMapper codeMapper;

    public List<CodeDto> getCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * First detail code of the common code is returned for default value handling.
     * @Author Seunghyeon.Kang
     * @param commCode Common code group.
     * @return First detail code or null.
     */
    public String getFirstCode(String commCode) {
        return getCodeList(commCode).stream()
                .findFirst()
                .map(CodeDto::getComdCode)
                .orElse(null);
    }

    /**
     * Detail code existence is checked inside the requested common code group.
     * @Author Seunghyeon.Kang
     * @param commCode Common code group.
     * @param comdCode Detail code to validate.
     * @return Whether the detail code exists in the common code group.
     */
    public boolean existsCode(String commCode, String comdCode) {
        return getCodeList(commCode).stream()
                .anyMatch(code -> code.getComdCode().equalsIgnoreCase(comdCode));
    }

    public String getCodeName(String commCode, String comdCode) {
        return getCodeName(commCode, comdCode, null);
    }

    public String getCodeName(String commCode, String comdCode, String optCode) {
        return codeMapper.getCodeName(commCode, comdCode, optCode);
    }
}
