package org.our.sadari.test.service;

import org.our.sadari.test.vo.TestVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TestService {
    List<TestVO> testList(TestVO vo);
}
