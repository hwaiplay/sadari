package org.our.sadari.user.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.dto.UserSettingDto;
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
 * 2026-08-04        SeungHyeon.Kang    최초 로그인 관심분야 선택 기능 추가
 * 2026-08-05        SeungHyeon.Kang    현재 선택한 관심분야 조회 기능 추가
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
     * 로그인 사용자가 선택한 프로필 또는 배경 이미지를 임시 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageFile 임시 저장할 이미지
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 서버 미리보기와 임시 식별값
     */
    ResultData setProfileImageDraft(Long userNumb, MultipartFile imageFile, String imageType);

    /**
     * 로그인 사용자의 만료되지 않은 프로필 이미지 임시 선택본을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 복원 가능한 임시 이미지 목록
     */
    ResultData getProfileImageDraftList(Long userNumb);

    /**
     * 로그인 사용자의 특정 유형 프로필 이미지 임시 선택본을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 삭제 처리 결과
     */
    ResultData delProfileImageDraft(Long userNumb, String imageType);

    /**
     * 최초 로그인 사용자의 닉네임을 저장하고 온보딩을 완료한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 사용자가 확정한 닉네임
     * @return 온보딩 완료 후 최신 프로필 조회 결과
     */
    ResultData uptOnboarding(Long userNumb, UserDto userDto);

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    ResultData getUserInterestCatalog();

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    ResultData getUserInterestList(Long userNumb);

    /**
     * 로그인 사용자의 독서 관심분야를 선택 목록으로 전체 교체한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 선택한 관심분야 목록
     * @return 관심분야 저장 결과
     */
    ResultData uptUserInterests(Long userNumb, UserDto.UserInterestReqDto request);

    /** 로그인 사용자의 알림과 공개 범위 설정을 조회한다. */
    ResultData getUserSetting(Long userNumb);

    /** 로그인 사용자의 선택형 알림 설정을 저장한다. */
    ResultData uptUserAlimSetting(Long userNumb, UserSettingDto request);

    /** 로그인 사용자의 공개 범위 설정을 저장한다. */
    ResultData uptUserPrivacySetting(Long userNumb, UserSettingDto request);
}
