package org.our.sadari.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.exception.InvalidImageFileException;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 조회와 수정 업무를 처리하는 Service 구현체입니다.
 * 닉네임 중복 검사, 욕설 필터링, 이미지 저장, 사용자 정보 갱신을 한 흐름에서 처리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FileService fileService;
    private final BadWordDetectionService badWordDetectionService;
    private final TokenRedisService tokenRedisService;

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
    @Transactional
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
        Optional<String> badWord = badWordDetectionService.findBadWord(userDto.getUserNick())
                .or(() -> badWordDetectionService.findBadWord(userDto.getIntrCntn()));
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

        } catch (InvalidImageFileException e) {
            // 앞에서 다른 이미지가 저장되었을 수 있으므로 파일 메타정보와 물리 파일 정리가 실행되게 전체 수정을 롤백한다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultData.fail(ResultEnum.COMMON_IMAGE_INVALID);
        } catch (IOException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        int updateCnt = userMapper.uptUserProfile(userDto);

        if (updateCnt == 0) {
            // 사용자 UPDATE가 반영되지 않았다면 같은 요청에서 먼저 저장한 이미지도 유지하지 않는다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        /*
         * DB commit이 실패했는데 Redis만 먼저 바뀌면 두 저장소의 닉네임이 달라진다.
         * 실제 커밋이 완료된 직후에만 같은 로그인 세션의 Redis 닉네임을 갱신한다.
         */
        uptUserNickAfterCommit(userNumb, userDto.getUserNick());
        return getMe(userNumb);
    }

    /**
     * 프로필 DB 트랜잭션이 커밋된 직후 Redis 로그인 사용자 닉네임을 갱신한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 닉네임을 수정한 사용자 번호
     * @param userNick DB에 저장한 최신 닉네임
     */
    private void uptUserNickAfterCommit(Long userNumb, String userNick) {
        Runnable updateUserNick = () -> {
            try {
                tokenRedisService.uptUserNick(userNumb, userNick);
            } catch (RuntimeException e) {
                /*
                 * Redis 갱신 실패 시 예전 닉네임을 그대로 쓰는 것이 가장 위험하다.
                 * 가능한 경우 닉네임 키를 제거해 다음 로그인 또는 재발급 때 최신 DB 값으로 다시 생성되게 한다.
                 */
                try {
                    tokenRedisService.delUserNick(userNumb);
                } catch (RuntimeException deleteException) {
                    log.error("Redis user nickname cleanup failed. userNumb={}", userNumb, deleteException);
                }

                log.error("Redis user nickname update failed. userNumb={}", userNumb, e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    updateUserNick.run();
                }
            });
            return;
        }

        updateUserNick.run();
    }
}
