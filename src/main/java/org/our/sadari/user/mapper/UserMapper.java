package org.our.sadari.user.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserDto;

/**
 * fileName       : UserMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 조회와 완료 처리 추가
 * 2026-08-04        SeungHyeon.Kang    최초 로그인 관심분야 조회와 저장 추가
 * 2026-08-05        SeungHyeon.Kang    회원 관심분야 단일 코드 저장과 현재 선택 조회 반영
 * 2026-08-06        SeungHyeon.Kang    프로필 이미지 교체용 사용자 행 잠금 조회 추가
 */
@Mapper
public interface UserMapper {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    UserDto getUserByIdxx(@Param("userIdxx") String userIdxx);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    UserDto getUserByNumb(Long userNumb);

    /**
     * 프로필 이미지 교체 중 동시 수정이 발생하지 않도록 사용자 파일 번호를 잠금 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 잠금 조회할 사용자 번호
     * @return 현재 프로필과 배경 파일 번호
     */
    UserDto getUserFileForUpdate(Long userNumb);

    /**
     * 로그인 사용자의 최초 로그인 온보딩 완료 여부를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 온보딩 완료 여부
     */
    String getUserOnboardingYsno(Long userNumb);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int setUser(UserDto request);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int uptUserProfile(UserDto request);

    /**
     * 최초 로그인 사용자의 닉네임과 온보딩 완료 여부를 함께 수정한다.
     *
     * @author SeungHyeon.Kang
     * @param request 수정할 사용자 번호와 닉네임
     * @return 수정된 회원 수
     */
    int uptUserOnboarding(UserDto request);

    // getUserNickDuplicateCnt 조회로 후속 처리에 필요한 데이터를 가져온다
    int getUserNickDuplicateCnt(UserDto request);

    /**
     * 회원 상태와 탈퇴 관련 일시를 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param request 변경할 회원 번호와 상태 정보
     * @return 변경된 회원 수
     */
    int uptUserStatus(UserDto request);

    /**
     * 탈퇴 회원이 작성한 댓글을 삭제 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 삭제 상태로 변경된 댓글 수
     */
    int uptUserReplyDeleted(Long userNumb);

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    List<UserDto.UserInterestDto> getUserInterestCatalog();

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    List<UserDto.UserInterestDto> getUserInterestList(Long userNumb);

    /**
     * 로그인 사용자의 기존 독서 관심분야를 전체 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 삭제할 사용자 번호
     * @return 삭제된 관심분야 수
     */
    int delUserInterests(Long userNumb);

    /**
     * 로그인 사용자가 선택한 독서 관심분야 한 건을 저장한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 저장할 사용자 번호
     * @param interest 저장할 관심분야 세부코드
     * @return 등록된 관심분야 수
     */
    int setUserInterest(@Param("userNumb") Long userNumb
                      , @Param("interest") UserDto.UserInterestDto interest);
}
