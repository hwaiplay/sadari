package org.our.sadari.test.controller;

import org.our.sadari.test.service.TestService;
import org.our.sadari.test.vo.TestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/api/test")
    public List<TestVO> hello() {
        TestVO testVO = new TestVO();
        List<TestVO> testList = testService.testList(testVO);
        System.out.println("API test");
        return testList;
    }
}