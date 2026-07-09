package org.our.sadari.global.common.code.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.global.common.code.dto.CodeDto;

@Mapper
public interface CodeMapper {

    List<CodeDto> getCodeList(@Param("commCode") String commCode);

    String getCodeName(
            @Param("commCode") String commCode,
            @Param("comdCode") String comdCode,
            @Param("optCode") String optCode
    );
}
