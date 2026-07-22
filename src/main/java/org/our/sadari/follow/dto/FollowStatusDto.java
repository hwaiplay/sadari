package org.our.sadari.follow.dto;

import lombok.Data;

/**
 * 팔로우 버튼 상태 조회 결과를 화면에 전달하는 DTO입니다.
 * Oracle 함수 FN_GET_FOLW_STAT이 반환한 버튼명을 그대로 담아 타인 프로필 화면에서 표시합니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class FollowStatusDto {

    // 화면에 표시할 팔로우 버튼명입니다. 예: 팔로우, 팔로잉, 맞팔로우
    private String followStatName;
}
