package org.our.sadari.user.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : UserService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-20
 * description    : 사용자 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-20        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 완료 기능 추가
 */
public interface UserService {
    /**
     * 로그인 사용자의 프로필 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 프로필 조회 결과
     */
    ResultData getMe(Long userNumb);

    /**
     * 로그인 사용자의 프로필 정보와 이미지를 수정한다.
     * 닉네임 중복 검사와 욕설 필터링 같은 업무 검증은 구현체에서 수행한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 수정할 사용자 프로필 입력값
     * @param profileImage 새 프로필 이미지 파일
     * @param backgroundImage 새 배경 이미지 파일
     * @return 수정 후 최신 프로필 조회 결과
     */
    ResultData uptMe(Long userNumb, UserDto userDto, MultipartFile profileImage, MultipartFile backgroundImage);

    /**
     * 최초 로그인 사용자의 닉네임을 저장하고 온보딩을 완료한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 사용자가 확정한 닉네임
     * @return 온보딩 완료 후 최신 프로필 조회 결과
     */
    ResultData uptOnboarding(Long userNumb, UserDto userDto);
}
