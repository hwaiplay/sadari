package org.our.sadari.user.controller;

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
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResultData getMe(@AuthenticationPrincipal Long userNumb) {
        return userService.getMe(userNumb);
    }

    @PutMapping(value = "/uptProfile", consumes = "multipart/form-data")
    public ResultData uptMe(@AuthenticationPrincipal Long userNumb,
                            @ModelAttribute UserDto userDto,
                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                            @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) {
        return userService.uptMe(userNumb, userDto, profileImage, backgroundImage);
    }
}
