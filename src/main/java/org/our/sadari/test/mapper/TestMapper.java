package org.our.sadari.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.test.vo.TestVO;

import java.util.List;

@Mapper
public interface TestMapper {

    List<TestVO> testList(TestVO vo);

}
