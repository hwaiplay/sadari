package org.our.sadari.user.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.dto.UserSettingDto;

/**
 * fileName       : UserMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 데이터베이스 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 조회와 완료 처리 추가
 * 2026-08-04        SeungHyeon.Kang    최초 로그인 관심분야 조회와 저장 추가
 * 2026-08-05        SeungHyeon.Kang    회원 관심분야 단일 코드 저장과 현재 선택 조회 반영
 * 2026-08-06        SeungHyeon.Kang    프로필 이미지 교체용 사용자 행 잠금 조회 추가
 * 2026-08-27        SeungHyeon.Kang    공개 프로필 사진 반응 조회 확장
 */
@Mapper
public interface UserMapper {

    /**
     * 아래 코드의 처리 목적을 설명함
     */
    UserDto getUserByIdxx(@Param("userIdxx") String userIdxx);

    /**
     * 아래 코드의 처리 목적을 설명함
     */
    UserDto getUserByNumb(Long userNumb);

    /**
     * 로그인 사용자가 확인하는 현재 프로필 또는 배경 사진의 좋아요와 댓글 집계를 조회함
     *
     * @author SeungHyeon.Kang
     * @param request 로그인 사용자와 사진 소유자 및 대상 유형과 파일 번호
     * @return 현재 사진의 좋아요와 댓글 집계
     */
    UserDto.ImageReactionDto getImageReactionDtl(UserDto.ImageReactionDto request);

    /**
     * 프로필 이미지 교체 중 동시 수정이 발생하지 않도록 사용자 파일 번호를 잠금 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 잠금 조회할 사용자 번호
     * @return 현재 프로필과 배경 파일 번호
     */
    UserDto getUserFileForUpdate(Long userNumb);

    /**
     * 로그인 사용자의 최초 로그인 온보딩 완료 여부를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 온보딩 완료 여부
     */
    String getUserOnboardingYsno(Long userNumb);

    /**
     * 아래 코드의 처리 목적을 설명함
     */
    int setUser(UserDto request);

    /** 신규 회원의 서비스 설정 기본 행을 등록함 */
    int setDefaultUserSetting(@Param("userNumb") Long userNumb);

    /** 로그인 사용자의 설정을 기존 회원 호환 기본값과 함께 조회함 */
    UserSettingDto getUserSettingDtl(@Param("userNumb") Long userNumb);

    /** 알림 범주와 신규 독후감 알림 기본값을 저장함 */
    int uptUserAlimSetting(UserSettingDto request);

    /** 공개 범위와 신규 독후감 공개 기본값을 저장함 */
    int uptUserPrivacySetting(UserSettingDto request);

    /**
     * 아래 코드의 처리 목적을 설명함
     */
    int uptUserProfile(UserDto request);

    /**
     * 최초 로그인 사용자의 닉네임과 온보딩 완료 여부를 함께 수정함
     *
     * @author SeungHyeon.Kang
     * @param request 수정할 사용자 번호와 닉네임
     * @return 수정된 회원 수
     */
    int uptUserOnboarding(UserDto request);

    // getUserNickDuplicateCnt 조회로 후속 처리에 필요한 데이터를 가져옴
    int getUserNickDuplicateCnt(UserDto request);

    /**
     * 회원 상태와 탈퇴 관련 일시를 변경함
     *
     * @author SeungHyeon.Kang
     * @param request 변경할 회원 번호와 상태 정보
     * @return 변경된 회원 수
     */
    int uptUserStatus(UserDto request);

    /**
     * 탈퇴 회원이 작성한 댓글을 삭제 상태로 변경함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 삭제 상태로 변경된 댓글 수
     */
    int uptUserReplyDeleted(Long userNumb);

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회함
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    List<UserDto.UserInterestDto> getUserInterestCatalog();

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    List<UserDto.UserInterestDto> getUserInterestList(Long userNumb);

    /**
     * 로그인 사용자의 기존 독서 관심분야를 전체 삭제함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 삭제할 사용자 번호
     * @return 삭제된 관심분야 수
     */
    int delUserInterests(Long userNumb);

    /**
     * 로그인 사용자가 선택한 독서 관심분야 한 건을 저장함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 저장할 사용자 번호
     * @param interest 저장할 관심분야 세부코드
     * @return 등록된 관심분야 수
     */
    int setUserInterest(@Param("userNumb") Long userNumb
                      , @Param("interest") UserDto.UserInterestDto interest);
}
