package org.our.sadari.test.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.test.mapper.TestMapper;
import org.our.sadari.test.service.TestService;
import org.our.sadari.test.vo.TestVO;
import org.springframework.stereotype.Service;

/**
 * TestServiceImpl 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;

    /**
     * 테스트 목록 조회 요청을 Mapper로 전달하고 조회 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param vo 처리에 필요한 입력값
     * @return 처리 결과
    */
    @Override
    public List<TestVO> testList(TestVO vo) {
        return testMapper.testList(vo);
    }
}
