package org.our.sadari.test.service.impl;

import lombok.RequiredArgsConstructor;
import org.our.sadari.test.mapper.TestMapper;
import org.our.sadari.test.service.TestService;
import org.our.sadari.test.vo.TestVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;

    @Override
    public List<TestVO> testList(TestVO vo) {
        return testMapper.testList(vo);
    }
}