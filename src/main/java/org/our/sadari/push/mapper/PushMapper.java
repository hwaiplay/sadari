package org.our.sadari.push.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.push.dto.PushDto;

/**
 * TB_PSHSUB 푸시 구독 정보를 저장하고 조회하는 MyBatis Mapper입니다.
 * 한 사용자가 여러 브라우저/기기를 가질 수 있으므로 USER_NUMB와 token 값을 함께 기준으로 처리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface PushMapper {

    /**
     * FCM token을 구독 정보로 저장하거나, 이미 있으면 다시 활성화합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 FCM token
     * @return 반영 건수
     */
    int setPushSub(PushDto.PushSubDto req);

    /**
     * 사용자가 현재 브라우저의 푸시 구독을 끌 때 해당 token을 비활성화합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 FCM token
     * @return 반영 건수
     */
    int delPushSub(PushDto.PushSubDto req);

    /**
     * 알림 수신자의 활성 FCM token 목록을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 알림 수신 사용자 번호
     * @return 활성 구독 목록
     */
    List<PushDto.PushSubDto> getActivePushSubList(@Param("userNumb") Long userNumb);
}
