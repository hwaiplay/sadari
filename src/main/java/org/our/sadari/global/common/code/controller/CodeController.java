package org.our.sadari.global.common.code.controller;

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
public class CodeController {

    private final CodeUtil codeUtil;

    @GetMapping("/{commCode}")
    public ResultData getCodeList(@PathVariable String commCode) {
        return ResultData.success(codeUtil.getCodeList(commCode));
    }
}
