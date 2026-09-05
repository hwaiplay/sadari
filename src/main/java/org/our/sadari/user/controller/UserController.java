package org.our.sadari.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.dto.UserSettingDto;
import org.our.sadari.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : UserController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 온보딩 완료 API 추가
 * 2026-08-04        SeungHyeon.Kang    최초 로그인 관심분야 선택 API 추가
 * 2026-08-05        SeungHyeon.Kang    현재 선택한 관심분야 조회 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "사용자", description = "로그인 사용자 프로필 조회와 수정 API")
public class UserController {

    // User 업무 처리 서비스
    private final UserService userService;

    /** 로그인 사용자의 알림과 공개 범위 설정을 조회함 */
    @GetMapping("/settings")
    @Operation(summary = "사용자 설정 조회", description = "알림 범주와 공개 범위 및 신규 독후감 기본값을 조회한다.")
    public ResultData getUserSetting(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        return userService.getUserSetting(userNumb);
    }

    /** 로그인 사용자의 화면 언어 설정을 저장함 */
    @PutMapping("/settings/language")
    @Operation(summary = "언어 설정 저장", description = "한국어 또는 영어 화면 사용 여부를 계정 설정으로 저장한다.")
    public ResultData uptUserLanguageSetting(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                            , @Valid @RequestBody UserSettingDto request) {
        return userService.uptUserLanguageSetting(userNumb, request);
    }

    /** 로그인 사용자의 선택형 알림 설정을 저장함 */
    @PutMapping("/settings/notifications")
    @Operation(summary = "알림 설정 저장", description = "알림센터와 푸시 생성에 함께 적용할 선택형 알림 범주를 저장한다.")
    public ResultData uptUserAlimSetting(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                       , @Valid @RequestBody UserSettingDto request) {
        return userService.uptUserAlimSetting(userNumb, request);
    }

    /** 로그인 사용자의 공개 범위 설정을 저장함 */
    @PutMapping("/settings/privacy")
    @Operation(summary = "공개 범위 설정 저장", description = "독서 통계와 목표 및 사진 피드와 신규 독후감 공개 기본값을 저장한다.")
    public ResultData uptUserPrivacySetting(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                          , @Valid @RequestBody UserSettingDto request) {
        return userService.uptUserPrivacySetting(userNumb, request);
    }
    /**
     * 로그인 사용자의 프로필 정보를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 사용자 번호
     * @return 업무 처리 성공 또는 실패 응답
     */
    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "Access Token으로 식별한 로그인 사용자의 프로필 정보를 조회한다.")
    public ResultData getMe(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자의 프로필 정보를 조회 결과를 반환함
        return userService.getMe(userNumb);
    }

    /**
     * 최초 로그인 사용자의 닉네임을 확정하고 온보딩을 완료함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 사용자가 확정한 닉네임
     * @return 온보딩 완료 후 최신 프로필 응답
     */
    @PutMapping("/onboarding")
    @Operation(summary = "최초 로그인 온보딩 완료", description = "닉네임을 저장하고 최초 로그인 웰컴 화면을 완료한다.")
    public ResultData uptOnboarding(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                   , @Valid @RequestBody UserDto userDto) {
        // 최초 로그인 사용자의 닉네임 저장과 온보딩 완료 결과를 반환함
        return userService.uptOnboarding(userNumb, userDto);
    }

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회함
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    @GetMapping("/interests/catalog")
    @Operation(summary = "독서 관심분야 목록 조회", description = "최초 로그인에서 선택할 활성 독서 관심분야를 조회한다.")
    public ResultData getUserInterestCatalog() {
        // 최초 로그인 사용자가 선택할 수 있는 활성 관심분야를 반환함
        return userService.getUserInterestCatalog();
    }

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    @GetMapping("/interests")
    @Operation(summary = "내 독서 관심분야 조회", description = "로그인 사용자가 현재 저장한 독서 관심분야를 조회한다.")
    public ResultData getUserInterestList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자가 현재 선택한 관심분야를 반환함
        return userService.getUserInterestList(userNumb);
    }

    /**
     * 로그인 사용자의 독서 관심분야를 선택 목록으로 전체 교체함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 선택한 관심분야 목록
     * @return 관심분야 저장 결과
     */
    @PutMapping("/interests")
    @Operation(summary = "독서 관심분야 저장", description = "최초 로그인 사용자가 선택한 관심분야를 전체 교체한다.")
    public ResultData uptUserInterests(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                     , @Valid @RequestBody UserDto.UserInterestReqDto request) {
        // 로그인 사용자의 관심분야 전체 교체 결과를 반환함
        return userService.uptUserInterests(userNumb, request);
    }

    /**
     * 로그인 사용자의 프로필과 이미지를 수정함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 사용자 번호
     * @param userDto 수정할 닉네임과 한 줄 소개
     * @param profileImage 새로 저장할 프로필 이미지 파일
     * @param backgroundImage 새로 저장할 배경 이미지 파일
     * @return 업무 처리 성공 또는 실패 응답
     */
    @PutMapping(value = "/uptProfile", consumes = "multipart/form-data")
    @Operation(summary = "내 프로필 수정", description = "닉네임, 한줄소개, 프로필 이미지, 배경 이미지를 수정한다.")
    public ResultData uptMe(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                          , @ModelAttribute UserDto userDto
                          , @Parameter(description = "프로필 이미지 파일") @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
                          , @Parameter(description = "배경 이미지 파일") @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) {
        // 로그인 사용자의 프로필과 이미지를 수정 결과를 반환함
        return userService.uptMe(userNumb, userDto, profileImage, backgroundImage);
    }

    /**
     * 앨범에서 선택한 프로필 또는 배경 이미지를 사용자 전용 임시 저장소에 보관함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageFile 임시 저장할 이미지
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 서버가 생성한 미리보기와 임시 식별값
     */
    @PostMapping(value = "/profile-image-drafts", consumes = "multipart/form-data")
    @Operation(summary = "프로필 이미지 임시 저장", description = "선택한 이미지를 비공개 임시 저장소에 보관하고 서버 미리보기를 반환한다.")
    public ResultData setProfileImageDraft(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                         , @RequestParam("imageFile") MultipartFile imageFile
                                         , @RequestParam("imageType") String imageType) {
        // 로그인 사용자 전용 임시 이미지 저장 결과를 반환함
        return userService.setProfileImageDraft(userNumb, imageFile, imageType);
    }

    /**
     * 앱 재시작 뒤에도 만료되지 않은 임시 이미지 선택본을 복원함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 복원 가능한 임시 이미지 목록
     */
    @GetMapping("/profile-image-drafts")
    @Operation(summary = "프로필 이미지 임시 선택 조회", description = "로그인 사용자의 만료되지 않은 임시 이미지 선택본을 조회한다.")
    public ResultData getProfileImageDraftList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자에게만 해당 사용자의 임시 이미지 목록을 반환함
        return userService.getProfileImageDraftList(userNumb);
    }

    /**
     * 프로필 편집 취소 시 특정 유형의 임시 이미지를 삭제함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 삭제 처리 결과
     */
    @DeleteMapping("/profile-image-drafts")
    @Operation(summary = "프로필 이미지 임시 선택 삭제", description = "취소한 프로필 또는 배경 임시 이미지를 즉시 삭제한다.")
    public ResultData delProfileImageDraft(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                         , @RequestParam("imageType") String imageType) {
        // 로그인 사용자의 해당 유형 임시 이미지 삭제 결과를 반환함
        return userService.delProfileImageDraft(userNumb, imageType);
    }
}
