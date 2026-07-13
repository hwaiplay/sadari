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
     * 지정한 공통코드 그룹에서 첫 번째 세부코드 값을 조회한다.
     * 화면이나 요청에서 선택값이 누락되었을 때 DB에 정의된 기본 순서의 코드를 기본값으로 사용하기 위해 제공한다.
     * 코드 목록이 비어 있으면 기본값을 결정할 수 없으므로 null을 반환한다.
     * @Author Seunghyeon.Kang
     * @param commCode 첫 번째 세부코드를 조회할 공통코드 그룹
     * @return 공통코드 그룹의 첫 번째 세부코드 값, 목록이 없으면 null
     */
    public String getFirstCode(String commCode) {
        return getCodeList(commCode).stream()
                .findFirst()
                .map(CodeDto::getComdCode)
                .orElse(null);
    }

    /**
     * 지정한 공통코드 그룹 안에 요청한 세부코드가 존재하는지 확인한다.
     * 등록 또는 수정 요청에서 상태코드, 색상코드처럼 DB 코드로 관리되는 값의 유효성을 검증할 때 사용한다.
     * 세부코드 비교는 대소문자 차이로 인한 불필요한 실패를 줄이기 위해 대소문자를 구분하지 않는다.
     * @Author Seunghyeon.Kang
     * @param commCode 세부코드 존재 여부를 확인할 공통코드 그룹
     * @param comdCode 유효성을 검증할 세부코드 값
     * @return 공통코드 그룹 안에 세부코드가 존재하면 true, 없으면 false
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
