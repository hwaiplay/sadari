package org.our.sadari.global.common.code.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.global.common.code.dto.CodeDto;

/**
 * 공통코드와 세부코드 조회 SQL을 연결하는 MyBatis Mapper이다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface CodeMapper {

    /**
     * 하나의 공통코드에 속한 사용 가능한 세부코드 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @return 정렬 순서가 적용된 세부코드 목록
     */
    List<CodeDto> getCodeList(@Param("commCode") String commCode);

    /**
     * 여러 공통코드에 속한 사용 가능한 세부코드를 한 번의 SQL로 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param commCodeList 조회할 공통코드 목록
     * @return 공통코드와 정렬 순서가 적용된 세부코드 목록
     */
    List<CodeDto> getCodeGroupList(@Param("commCodeList") List<String> commCodeList);

    /**
     * 공통코드와 세부코드에 해당하는 표시 이름을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @param comdCode 조회할 세부코드
     * @param optCode 선택적으로 비교할 옵션 코드
     * @return 코드 표시 이름
     */
    String getCodeName(@Param("commCode") String commCode
                     , @Param("comdCode") String comdCode
                     , @Param("optCode") String optCode);
}
