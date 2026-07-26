package org.our.sadari.global.common.code.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.mapper.CodeMapper;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultEnum;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * CodeUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Component
@RequiredArgsConstructor
public class CodeUtil {

    /**
     * 비정상적으로 큰 IN 조건 생성을 막기 위해 한 요청에서 허용하는 공통코드 최대 개수를 제한한다.
     */
    private static final int CODE_GROUP_QUERY_MAX_SIZE = 20;

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
     * 여러 공통코드의 세부코드를 한 번에 조회하고 공통코드별 목록으로 그룹화한다.
     *
     * @author Seunghyeon.Kang
     * @param commCodeList 조회할 공통코드 목록
     * @return 요청한 공통코드를 키로 사용하는 세부코드 목록
     * @throws CustomException 공통코드 목록이 비어 있거나 허용 개수를 초과한 경우 발생
     */
    public Map<String, List<CodeDto>> getCodeGroupList(List<String> commCodeList) {
        // 빈 IN 조건은 Oracle 문법 오류를 만들 수 있으므로 Mapper를 호출하기 전에 요청을 차단한다.
        if (commCodeList == null
                || commCodeList.isEmpty()
                || commCodeList.size() > CODE_GROUP_QUERY_MAX_SIZE) {
            throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }

        Set<String> normalizedCommCodeSet = new LinkedHashSet<>();
        for (String commCode : commCodeList) {
            // 공백 코드를 조용히 제외하면 잘못된 화면 설정을 발견하기 어려우므로 요청 자체를 실패 처리한다.
            if (commCode == null || commCode.isBlank()) {
                throw new CustomException(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
            }
            normalizedCommCodeSet.add(commCode.trim().toUpperCase(Locale.ROOT));
        }

        List<String> normalizedCommCodeList = new ArrayList<>(normalizedCommCodeSet);
        Map<String, List<CodeDto>> codeGroupList = new LinkedHashMap<>();

        // 등록된 세부코드가 없는 공통코드도 빈 배열로 반환하여 프론트에서 안정적으로 구조 분해할 수 있게 한다.
        for (String commCode : normalizedCommCodeList) {
            codeGroupList.put(commCode, new ArrayList<>());
        }

        List<CodeDto> codeList = codeMapper.getCodeGroupList(normalizedCommCodeList);
        if (codeList == null) {
            return codeGroupList;
        }

        for (CodeDto codeDto : codeList) {
            // DB 결과가 요청 범위를 벗어나거나 식별값이 없으면 응답 그룹을 오염시키지 않고 제외한다.
            if (codeDto == null || !codeGroupList.containsKey(codeDto.getCommCode())) {
                continue;
            }
            codeGroupList.get(codeDto.getCommCode()).add(codeDto);
        }

        return codeGroupList;
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
