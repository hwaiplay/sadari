package org.our.sadari.user.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * UserController 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final FileService fileService;

    /**
     * getMe 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @return 처리 결과
     */
    @GetMapping("/me")
    public ResultData getMe(@AuthenticationPrincipal Long userNumb) {
        // SecurityContext에 인증 principal이 없으면 사용자 조회 자체가 성립하지 않는다.
        // null을 mapper에 넘기면 DB 예외가 2009로 변환될 수 있어 인증 실패로 먼저 분기한다.
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        UserDto user = userMapper.getUserByNumb(userNumb);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(user)) {
            // 토큰은 통과했지만 사용자 레코드가 없으면 세션이 더 이상 유효하지 않은 상태다.
            // 프로필 없음이 아니라 재로그인이 필요한 인증 실패로 응답한다.
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("userNick", user.getUserNick());
        profile.put("porfPath", user.getPorfPath());
        profile.put("bgimPath", user.getBgimPath());
        profile.put("intrCntn", user.getIntrCntn());

        return ResultData.success(profile);
    }

    /**
     * uptMe 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 처리에 필요한 입력값
     * @param userDto 처리에 필요한 입력값
     * @param false 처리에 필요한 입력값
     * @return 처리 결과
     */
    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResultData uptMe(@AuthenticationPrincipal Long userNumb, @ModelAttribute UserDto userDto
                            , @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
                            , @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage) {
        userDto.setUserNumb(userNumb);
        // 아래 처리 단계의 업무 목적을 설명한다.
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick(), 10));
        userDto.setIntrCntn(StringUtil.normalizePlainText(userDto.getIntrCntn(), 50));
        try {
            // 아래 처리 단계의 업무 목적을 설명한다.
            userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));
            userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));
        } catch (IOException e) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        userMapper.uptUserProfile(userDto);
        return getMe(userNumb);
    }
}
