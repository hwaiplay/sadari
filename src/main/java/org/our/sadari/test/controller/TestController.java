package org.our.sadari.test.controller;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.test.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/api/test")
    public String hello() {
        return MessageUtils.getMessage("auth.token.invalid");
    }

}