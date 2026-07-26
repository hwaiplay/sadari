package org.our.sadari.global.common.code.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 화면과 업무 로직에서 사용하는 공통코드의 단건 및 일괄 조회 API를 제공한다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
@Tag(name = "공통코드", description = "공통코드의 세부코드 목록 조회 API")
public class CodeController {

    private final CodeUtil codeUtil;

    /**
     * 하나의 공통코드에 등록된 사용 가능한 세부코드 목록을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param commCode 조회할 공통코드
     * @return 세부코드 목록이 포함된 공통 응답
     */
    @GetMapping("/{commCode}")
    @Operation(summary = "세부코드 목록 조회", description = "공통코드 값을 기준으로 사용 가능한 세부코드 목록을 조회한다.")
    public ResultData getCodeList(@Parameter(description = "공통코드", example = "READ_STAT")
                                  @PathVariable String commCode) {
        return ResultData.success(codeUtil.getCodeList(commCode));
    }

    /**
     * 여러 공통코드에 등록된 세부코드 목록을 한 번의 요청으로 일괄 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param commCodeList 쉼표로 구분하여 조회할 공통코드 목록
     * @return 공통코드별 세부코드 목록이 포함된 공통 응답
     */
    @GetMapping
    @Operation(summary = "세부코드 목록 일괄 조회", description = "여러 공통코드 값을 기준으로 사용 가능한 세부코드 목록을 한 번에 조회한다.")
    public ResultData getCodeGroupList(@Parameter(description = "조회할 공통코드 목록", example = "READ_STAT,BOOK_COLR")
                                           @RequestParam(name = "commCodes") List<String> commCodeList) {
        return ResultData.success(codeUtil.getCodeGroupList(commCodeList));
    }
}
