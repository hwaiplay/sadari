package org.our.sadari.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : UserController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 API를 제공한다
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
    /**
     * 로그인 사용자의 프로필 정보를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 사용자 번호
     * @return 업무 처리 성공 또는 실패 응답
     */
    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "Access Token으로 식별한 로그인 사용자의 프로필 정보를 조회한다.")
    public ResultData getMe(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자의 프로필 정보를 조회 결과를 반환한다
        return userService.getMe(userNumb);
    }

    /**
     * 최초 로그인 사용자의 닉네임을 확정하고 온보딩을 완료한다
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
        // 최초 로그인 사용자의 닉네임 저장과 온보딩 완료 결과를 반환한다
        return userService.uptOnboarding(userNumb, userDto);
    }

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    @GetMapping("/interests/catalog")
    @Operation(summary = "독서 관심분야 목록 조회", description = "최초 로그인에서 선택할 활성 독서 관심분야를 조회한다.")
    public ResultData getUserInterestCatalog() {
        // 최초 로그인 사용자가 선택할 수 있는 활성 관심분야를 반환한다
        return userService.getUserInterestCatalog();
    }

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    @GetMapping("/interests")
    @Operation(summary = "내 독서 관심분야 조회", description = "로그인 사용자가 현재 저장한 독서 관심분야를 조회한다.")
    public ResultData getUserInterestList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자가 현재 선택한 관심분야를 반환한다
        return userService.getUserInterestList(userNumb);
    }

    /**
     * 로그인 사용자의 독서 관심분야를 선택 목록으로 전체 교체한다
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
        // 로그인 사용자의 관심분야 전체 교체 결과를 반환한다
        return userService.uptUserInterests(userNumb, request);
    }

    /**
     * 로그인 사용자의 프로필과 이미지를 수정한다
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
        // 로그인 사용자의 프로필과 이미지를 수정 결과를 반환한다
        return userService.uptMe(userNumb, userDto, profileImage, backgroundImage);
    }
}
