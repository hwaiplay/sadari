package org.our.sadari.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
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
 * fileName       : UserServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-20
 * description    : 사용자 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-20        SeungHyeon.Kang    최초 생성
 * 2026-07-29        SeungHyeon.Kang    닉네임 공백 및 허용 특수문자와 20자 길이 검증 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    // 닉네임 최대 길이 설정값
    private static final int USER_NICK_MAX_LENGTH = 20;
    // 문자 사이에 단일 공백과 언더바 및 하이픈을 허용하는 닉네임 형식
    private static final Pattern USER_NICK_PATTERN =
            Pattern.compile("^[A-Za-z0-9가-힣]+(?:[ _-][A-Za-z0-9가-힣]+)*$");

    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // File 업무 처리 서비스
    private final FileService fileService;
    // BadWordDetection 업무 처리 서비스
    private final BadWordDetectionService badWordDetectionService;
    // TokenRedis 업무 처리 서비스
    private final TokenRedisService tokenRedisService;

    /**
     * 로그인 사용자의 최신 프로필 정보를 조회한다.
     * 인증 사용자 번호가 없거나 사용자 레코드가 없으면 다시 로그인해야 하므로 인증 실패로 응답한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 프로필 조회 결과
     */
    @Override
    public ResultData getMe(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // UserByNumb 데이터를 DB에서 조회한다
        UserDto user = userMapper.getUserByNumb(userNumb);

        // user 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(user)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        Map<String, String> profile = new HashMap<>();
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("userNick", user.getUserNick());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("porfPath", user.getPorfPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("bgimPath", user.getBgimPath());
        // 후속 처리에 사용할 키와 값을 맵에 저장한다
        profile.put("intrCntn", user.getIntrCntn());
        // 로그인 사용자의 최신 프로필 정보를 조회한 결과를 성공 응답으로 반환한다
        return ResultData.success(profile);
    }

    /**
     * 로그인 사용자의 프로필 정보를 수정한다.
     * 화면에서 별도 닉네임 중복 검사 API를 호출하지 않으므로 저장 요청에서 중복과 욕설을 최종 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 수정할 사용자 프로필 입력값
     * @param profileImage 새 프로필 이미지 파일
     * @param backgroundImage 새 배경 이미지 파일
     * @return 수정 후 최신 프로필 조회 결과
     */
    @Override
    @Transactional
    public ResultData uptMe(Long userNumb, UserDto userDto, MultipartFile profileImage
                          , MultipartFile backgroundImage) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // UserNumb 업무 값을 userDto DTO에 설정한다
        userDto.setUserNumb(userNumb);
        // 닉네임 형식 검증 전에 앞뒤 공백만 제거한다
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick()));
        // IntrCntn 업무 값을 userDto DTO에 설정한다
        userDto.setIntrCntn(StringUtil.normalizePlainText(userDto.getIntrCntn(), 50));

        // 닉네임이 비어 있으면 프로필 저장 요청을 거절한다
        if (StringUtil.isEmpty(userDto.getUserNick())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 화면 검증을 우회한 요청도 같은 닉네임 문자와 구분자 규칙으로 차단한다
        if (!isValidUserNick(userDto.getUserNick())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 닉네임과 한줄소개에서 저장을 차단할 비속어를 조회한다
        Optional<String> badWord = badWordDetectionService.findBadWord(userDto.getUserNick())
                .or(() -> badWordDetectionService.findBadWord(userDto.getIntrCntn()));
        // 닉네임이나 한줄소개에 비속어가 있으면 감지된 단어와 함께 요청을 거절한다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // 본인을 제외한 다른 사용자가 같은 닉네임을 사용하면 중복 저장을 차단한다
        if (userMapper.getUserNickDuplicateCnt(userDto) > 0) {
            // "이미 사용 중인 닉네임이에요."
            return ResultData.fail(ResultEnum.USER_NICK_DUPLICATED);
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // ProfNumb 업무 값을 userDto DTO에 설정한다
            userDto.setProfNumb(fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, userNumb));          //새로운 프로필 사진 존재시 파일 저장
            // BgimNumb 업무 값을 userDto DTO에 설정한다
            userDto.setBgimNumb(fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, userNumb));    //새로운 배경 사진 존재시 파일 저장

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (InvalidImageFileException e) {
            // 앞에서 다른 이미지가 저장되었을 수 있으므로 파일 메타정보와 물리 파일 정리가 실행되게 전체 수정을 롤백한다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // "JPG 또는 PNG 형식의 10MB 이하 이미지 파일만 업로드할 수 있어요."
            return ResultData.fail(ResultEnum.COMMON_IMAGE_INVALID);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (IOException e) {
            // Redis 갱신 실패 시 현재 프로필 수정 트랜잭션을 롤백 상태로 전환한다
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // "수정에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // UserProfile 데이터를 DB에서 수정한다
        int updateCnt = userMapper.uptUserProfile(userDto);

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (updateCnt == 0) {
            // 사용자 UPDATE가 반영되지 않았다면 같은 요청에서 먼저 저장한 이미지도 유지하지 않는다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // "수정에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        /*
         * DB commit이 실패했는데 Redis만 먼저 바뀌면 두 저장소의 닉네임이 달라진다.
         * 실제 커밋이 완료된 직후에만 같은 로그인 세션의 Redis 닉네임을 갱신한다.
         */
        uptUserNickAfterCommit(userNumb, userDto.getUserNick());
        // 로그인 사용자의 프로필 정보를 수정한 결과를 반환한다
        return getMe(userNumb);
    }

    /**
     * 닉네임이 한글, 영문, 숫자와 문자 사이의 단일 허용 구분자로 구성됐는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNick 정규화한 닉네임
     * @return 닉네임 형식 충족 여부
     */
    private boolean isValidUserNick(String userNick) {

        // 닉네임이 최대 길이와 문자 사이의 단일 구분자 형식을 모두 충족하는지 반환한다
        return userNick.length() <= USER_NICK_MAX_LENGTH && USER_NICK_PATTERN.matcher(userNick).matches();
    }

    /**
     * 프로필 DB 트랜잭션이 커밋된 직후 Redis 로그인 사용자 닉네임을 갱신한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 닉네임을 수정한 사용자 번호
     * @param userNick DB에 저장한 최신 닉네임
     */
    private void uptUserNickAfterCommit(Long userNumb, String userNick) {

        Runnable updateUserNick = () -> {
            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try {
                // uptUserNick 업무 로직을 tokenRedisService에 위임한다
                tokenRedisService.uptUserNick(userNumb, userNick);
            }

            // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
            catch (RuntimeException e) {
                /*
                 * Redis 갱신 실패 시 예전 닉네임을 그대로 쓰는 것이 가장 위험하다.
                 * 가능한 경우 닉네임 키를 제거해 다음 로그인 또는 재발급 때 최신 DB 값으로 다시 생성되게 한다.
                 */
                try {
                    // delUserNick 업무 로직을 tokenRedisService에 위임한다
                    tokenRedisService.delUserNick(userNumb);
                }

                // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
                catch (RuntimeException deleteException) {
                    // 실패 원인과 처리 대상을 오류 로그로 남긴다
                    log.error("Redis user nickname cleanup failed. userNumb={}", userNumb, deleteException);
                }

                // 실패 원인과 처리 대상을 오류 로그로 남긴다
                log.error("Redis user nickname update failed. userNumb={}", userNumb, e);
            }
        };

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 * 현재 트랜잭션이 커밋된 후 예약된 후처리를 실행한다
                 *
                 * @author SeungHyeon.Kang
                 * @return 반환값이 없다
                 */
                @Override
                public void afterCommit() {
                    // 검증 대상 작업을 실행한다
                    updateUserNick.run();
                }
            });

            // 현재 트랜잭션이 커밋된 후 예약된 후처리를 실행 결과를 반환한다
            return;
        }

        // 검증 대상 작업을 실행한다
        updateUserNick.run();
    }
}
