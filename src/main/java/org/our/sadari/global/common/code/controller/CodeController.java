package org.our.sadari.global.common.code.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.result.ResultData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
@Tag(name = "공통코드", description = "공통코드의 세부코드 목록 조회 API")
public class CodeController {

    private final CodeUtil codeUtil;

    @GetMapping("/{commCode}")
    @Operation(summary = "세부코드 목록 조회", description = "공통코드 값을 기준으로 사용 가능한 세부코드 목록을 조회한다.")
    public ResultData getCodeList(@Parameter(description = "공통코드", example = "READ_STAT")
                                  @PathVariable String commCode) {
        return ResultData.success(codeUtil.getCodeList(commCode));
    }
}
