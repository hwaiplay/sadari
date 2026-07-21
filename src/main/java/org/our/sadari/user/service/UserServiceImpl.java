package org.our.sadari.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordFilterService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 조회와 수정 업무를 처리하는 Service 구현체입니다.
 * 닉네임 중복 검사, 욕설 필터링, 이미지 저장, 사용자 정보 갱신을 한 흐름에서 처리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FileService fileService;
    private final BadWordFilterService badWordFilterService;

    /**
     * 로그인 사용자의 최신 프로필 정보를 조회합니다.
     * 인증 사용자 번호가 없거나 사용자 레코드가 없으면 다시 로그인해야 하므로 인증 실패로 응답합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 프로필 조회 결과
     */
    @Override
    public ResultData getMe(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        UserDto user = userMapper.getUserByNumb(userNumb);

        if (StringUtil.isEmpty(user)) {
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
     * 로그인 사용자의 프로필 정보를 수정합니다.
     * 화면에서 별도 닉네임 중복 검사 API를 호출하지 않으므로 저장 요청에서 중복과 욕설을 최종 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 수정할 사용자 프로필 입력값
     * @param profileImage 새 프로필 이미지 파일
     * @param backgroundImage 새 배경 이미지 파일
     * @return 수정 후 최신 프로필 조회 결과
     */
    @Override
    public ResultData uptMe(Long userNumb, UserDto userDto, MultipartFile profileImage, MultipartFile backgroundImage) {

        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        userDto.setUserNumb(userNumb);
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick(), 10));
        userDto.setIntrCntn(StringUtil.normalizePlainText(userDto.getIntrCntn(), 50));

        //닉네임 없는 경우 실패 리턴
        if (StringUtil.isEmpty(userDto.getUserNick())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        //욕설 포함된 경우 실패 리턴
        Optional<String> badWord = badWordFilterService.findBadWord(userDto.getUserNick())
                .or(() -> badWordFilterService.findBadWord(userDto.getIntrCntn()));
        if (badWord.isPresent()) {
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }
        //이미 사용중인 닉네임이 있을 시 실패 리턴
        if (userMapper.getUserNickDuplicateCnt(userDto) > 0) {
            return ResultData.fail(ResultEnum.USER_NICK_DUPLICATED);
        }

        try {

            userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));          //새로운 프로필 사진 존재시 파일 저장
            userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));    //새로운 배경 사진 존재시 파일 저장

        } catch (IOException e) {
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        userMapper.uptUserProfile(userDto);
        return getMe(userNumb);
    }
}
