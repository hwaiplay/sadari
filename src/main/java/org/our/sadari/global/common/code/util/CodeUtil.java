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
 * 공통코드와 세부코드의 목록, 존재 여부 및 표시 이름을 조회하는 공통 유틸리티이다.
 * 코드 조회가 필요한 각 도메인이 CodeMapper에 직접 의존하지 않도록 공통 조회 진입점을 제공한다.
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
     * 하나의 공통코드에 등록된 사용 가능한 세부코드 목록을 정렬 순서대로 조회한다.
     * 사용 중지된 세부코드는 Mapper 조회 조건에서 제외된다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 세부코드 목록을 조회할 공통코드
     * @return 사용 가능한 세부코드 DTO 목록
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
        if (commCodeList == null || commCodeList.isEmpty() || commCodeList.size() > CODE_GROUP_QUERY_MAX_SIZE) {
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
     * 공통코드에 등록된 사용 가능한 세부코드 중 정렬 순서가 가장 앞선 코드값을 조회한다.
     * 기본 선택값이 필요한 화면이나 업무 로직에서 사용하며, 조회 결과가 없으면 null을 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 첫 번째 세부코드를 조회할 공통코드
     * @return 첫 번째 세부코드값 또는 조회 결과가 없을 경우 null
     */
    public String getFirstCode(String commCode) {
        return getCodeList(commCode).stream()
                .findFirst()
                .map(CodeDto::getComdCode)
                .orElse(null);
    }

    /**
     * 전달받은 세부코드가 지정한 공통코드의 사용 가능한 코드 목록에 존재하는지 확인한다.
     * 영문 코드값은 대소문자를 구분하지 않고 비교한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 존재 여부를 확인할 공통코드
     * @param comdCode 존재 여부를 확인할 세부코드
     * @return 세부코드가 사용 가능한 목록에 존재하면 true, 그렇지 않으면 false
     */
    public boolean existsCode(String commCode, String comdCode) {
        return getCodeList(commCode).stream()
                .anyMatch(code -> code.getComdCode().equalsIgnoreCase(comdCode));
    }

    /**
     * 공통코드와 세부코드에 대응하는 화면 표시용 코드명을 조회한다.
     * 옵션 조건이 필요하지 않은 일반 코드명 조회에서 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 코드명을 조회할 공통코드
     * @param comdCode 코드명을 조회할 세부코드
     * @return 공통코드와 세부코드에 대응하는 코드명
     */
    public String getCodeName(String commCode, String comdCode) {
        return getCodeName(commCode, comdCode, null);
    }

    /**
     * 공통코드, 세부코드 및 선택 옵션값에 대응하는 화면 표시용 코드명을 조회한다.
     * 옵션값이 전달되면 데이터베이스 코드명 조회 함수가 해당 옵션 조건까지 반영한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 코드명을 조회할 공통코드
     * @param comdCode 코드명을 조회할 세부코드
     * @param optCode 코드명 조회에 추가로 적용할 옵션 코드
     * @return 공통코드, 세부코드 및 옵션 조건에 대응하는 코드명
     */
    public String getCodeName(String commCode, String comdCode, String optCode) {
        return codeMapper.getCodeName(commCode, comdCode, optCode);
    }
}
