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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "사용자", description = "로그인 사용자 프로필 조회와 수정 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "Access Token으로 식별한 로그인 사용자의 프로필 정보를 조회한다.")
    public ResultData getMe(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        return userService.getMe(userNumb);
    }

    @PutMapping(value = "/uptProfile", consumes = "multipart/form-data")
    @Operation(summary = "내 프로필 수정", description = "닉네임, 한줄소개, 프로필 이미지, 배경 이미지를 수정한다.")
    public ResultData uptMe(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb,
                            @ModelAttribute UserDto userDto,
                            @Parameter(description = "프로필 이미지 파일")
                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                            @Parameter(description = "배경 이미지 파일")
                            @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) {
        return userService.uptMe(userNumb, userDto, profileImage, backgroundImage);
    }
}
