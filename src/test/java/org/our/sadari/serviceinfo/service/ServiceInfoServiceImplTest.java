package org.our.sadari.serviceinfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.serviceinfo.dto.ServiceInfoDto;
import org.our.sadari.serviceinfo.mapper.ServiceInfoMapper;
import org.springframework.context.support.StaticMessageSource;

/**
 * fileName       : ServiceInfoServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 활성 사용자에게만 현재 배포 서비스 정보 목록을 제공하는 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class ServiceInfoServiceImplTest {

    // 서비스 정보 데이터 접근 Mock
    @Mock
    private ServiceInfoMapper serviceInfoMapper;
    // 사용자 서비스 정보 서비스 단위 테스트 대상
    private ServiceInfoServiceImpl serviceInfoService;

    /** 각 테스트에 독립된 사용자 서비스 정보 서비스를 생성한다. */
    @BeforeEach
    void setUp() {
        serviceInfoService = new ServiceInfoServiceImpl(serviceInfoMapper);
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.setUseCodeAsDefaultMessage(true);
        new MessageUtils().setMessageSource(messageSource);
    }

    /** 비활성 사용자는 서비스 정보 카테고리와 배포본을 조회하지 못한다. */
    @Test
    void getServiceInfoInactive() {
        when(serviceInfoMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(0);

        ResultData result = serviceInfoService.getServiceInfoList(7L);

        assertEquals(ResultEnum.FORBIDDEN.getCode(), result.getCode());
        verify(serviceInfoMapper, never()).getServiceInfoList("SVIF_CATE", "Y");
    }

    /** 활성 사용자에게 공통코드 카테고리와 현재 배포본 조회 결과를 그대로 제공한다. */
    @Test
    void getDeployedServiceInfo() {
        List<ServiceInfoDto> rows = List.of(new ServiceInfoDto());
        when(serviceInfoMapper.getActiveUserCnt(7L, "ACTIVE")).thenReturn(1);
        when(serviceInfoMapper.getServiceInfoList("SVIF_CATE", "Y")).thenReturn(rows);

        ResultData result = serviceInfoService.getServiceInfoList(7L);

        assertEquals(200, result.getCode());
        assertSame(rows, result.getData());
    }
}
