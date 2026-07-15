package org.our.sadari.global.common.code.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.mapper.CodeMapper;
import org.springframework.stereotype.Component;

/**
 * 공통코드 조회와 코드명 변환을 한 곳에서 처리하는 유틸 컴포넌트입니다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
public class CodeUtil {

    private final CodeMapper codeMapper;

    /**
     * 공통코드에 속한 세부코드 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @return 사용 가능한 세부코드 목록
     */
    public List<CodeDto> getCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 공통코드에 등록된 첫 번째 세부코드 값을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @return 첫 번째 세부코드 값, 목록이 비어 있으면 null
     */
    public String getFirstCode(String commCode) {
        return getCodeList(commCode).stream()
                .findFirst()
                .map(CodeDto::getComdCode)
                .orElse(null);
    }

    /**
     * 공통코드 하위에 특정 세부코드가 존재하는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 확인할 공통코드
     * @param comdCode 확인할 세부코드
     * @return 세부코드가 존재하면 true, 존재하지 않으면 false
     */
    public boolean existsCode(String commCode, String comdCode) {
        return getCodeList(commCode).stream()
                .anyMatch(code -> code.getComdCode().equalsIgnoreCase(comdCode));
    }

    /**
     * 공통코드와 세부코드로 코드명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @param comdCode 조회할 세부코드
     * @return 코드명
     */
    public String getCodeName(String commCode, String comdCode) {
        return getCodeName(commCode, comdCode, null);
    }

    /**
     * 공통코드, 세부코드, 옵션코드로 코드명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @param comdCode 조회할 세부코드
     * @param optCode FN_GET_CODE_NAME 세 번째 파라미터로 전달할 옵션코드
     * @return 옵션 조건까지 반영한 코드명
     */
    public String getCodeName(String commCode, String comdCode, String optCode) {
        return codeMapper.getCodeName(commCode, comdCode, optCode);
    }
}
