package org.our.sadari.test.service.impl;

import lombok.RequiredArgsConstructor;
import org.our.sadari.test.mapper.TestMapper;
import org.our.sadari.test.service.TestService;
import org.our.sadari.test.vo.TestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private TestMapper testMapper;

    @Override
    public List<TestVO> testList(TestVO vo) {
        System.out.println("API service");
        return testMapper.testList(vo);
    }
}
