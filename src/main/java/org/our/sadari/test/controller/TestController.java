package org.our.sadari.test.controller;

import lombok.RequiredArgsConstructor;

import org.our.sadari.auth.service.AuthServiceImpl;
import org.our.sadari.test.service.TestService;
import org.our.sadari.test.vo.TestVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final AuthServiceImpl authServiceImpl;


    @GetMapping("/api/test")
    public List<TestVO> hello() {
        return testService.testList(new TestVO());
    }

    @GetMapping("/test")
    public String test(@RequestParam String accessToken) throws JsonProcessingException {
        return authServiceImpl.getKakaoAccount(accessToken);
    }
}