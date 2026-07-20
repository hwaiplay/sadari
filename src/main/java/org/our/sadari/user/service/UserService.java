package org.our.sadari.user.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 조회와 수정 업무를 처리하는 Service 계약입니다.
 * Controller는 요청 매핑만 담당하고, 사용자 검증과 저장 흐름은 이 계층에서 수행합니다.
 *
 * @author Seunghyeon.Kang
 */
public interface UserService {

    /**
     * 로그인 사용자의 프로필 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 프로필 조회 결과
     */
    ResultData getMe(Long userNumb);

    /**
     * 로그인 사용자의 프로필 정보와 이미지를 수정합니다.
     * 닉네임 중복 검사와 욕설 필터링 같은 업무 검증은 구현체에서 수행합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 수정할 사용자 프로필 입력값
     * @param profileImage 새 프로필 이미지 파일
     * @param backgroundImage 새 배경 이미지 파일
     * @return 수정 후 최신 프로필 조회 결과
     */
    ResultData uptMe(Long userNumb, UserDto userDto, MultipartFile profileImage, MultipartFile backgroundImage);
}
