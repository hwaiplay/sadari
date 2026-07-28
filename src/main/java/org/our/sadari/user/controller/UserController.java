package org.our.sadari.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
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
