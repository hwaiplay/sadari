package org.our.sadari.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * 2026-07-29        SeungHyeon.Kang    닉네임 최대 길이를 25자로 확장
 * 2026-07-30        SeungHyeon.Kang    최초 로그인 닉네임 확정과 온보딩 완료 처리 추가
 * 2026-08-04        SeungHyeon.Kang    최초 로그인 관심분야 조회와 저장 추가
 * 2026-08-05        SeungHyeon.Kang    회원 관심분야 단일 코드 검증과 현재 선택 조회 반영
 * 2026-08-06        SeungHyeon.Kang    프로필과 배경 이미지 교체 후 기존 파일 삭제 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

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
        // 첫 로그인 전용 화면의 재노출 여부를 판단할 완료 상태를 저장한다
        profile.put("onbdYsno", user.getOnbdYsno());
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

        // 동시에 들어온 프로필 수정이 서로의 신규 파일을 고아 파일로 만들지 않도록 현재 사용자 행을 잠금 조회한다
        UserDto currentUser = userMapper.getUserFileForUpdate(userNumb);

        // 인증 사용자 행이 사라진 경우 파일을 새로 만들지 않고 인증 실패로 종료한다
        if (StringUtil.isEmpty(currentUser)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
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

        // 새 프로필 이미지가 저장되었으면 사용자 참조에서 교체된 이전 파일을 정리한다
        if (!StringUtil.isEmpty(userDto.getProfNumb()) && !StringUtil.isEmpty(currentUser.getProfNumb())
                && !userDto.getProfNumb().equals(currentUser.getProfNumb())) {
            // DB 커밋 이후에만 이전 프로필 물리 파일이 삭제되도록 메타정보 정리와 후처리를 등록한다
            fileService.delFile(currentUser.getProfNumb());
        }

        // 새 배경 이미지가 저장되었으면 사용자 참조에서 교체된 이전 파일을 정리한다
        if (!StringUtil.isEmpty(userDto.getBgimNumb()) && !StringUtil.isEmpty(currentUser.getBgimNumb())
                && !userDto.getBgimNumb().equals(currentUser.getBgimNumb())) {
            // DB 커밋 이후에만 이전 배경 물리 파일이 삭제되도록 메타정보 정리와 후처리를 등록한다
            fileService.delFile(currentUser.getBgimNumb());
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
     * 최초 로그인 사용자의 닉네임을 검증해 저장하고 온보딩 완료 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param userDto 사용자가 확정한 닉네임
     * @return 온보딩 완료 후 최신 프로필 조회 결과
     */
    @Override
    @Transactional
    public ResultData uptOnboarding(Long userNumb, UserDto userDto) {
        // 인증 사용자 번호가 없으면 온보딩 상태를 변경하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 요청 본문이 없으면 닉네임 검증을 진행할 수 없으므로 요청을 거절한다
        if (StringUtil.isEmpty(userDto)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 수정 대상을 로그인 사용자로 제한한다
        userDto.setUserNumb(userNumb);
        // 닉네임 형식 검증 전에 앞뒤 공백만 제거한다
        userDto.setUserNick(StringUtil.normalizePlainText(userDto.getUserNick()));

        // 닉네임이 비어 있으면 온보딩 완료 요청을 거절한다
        if (StringUtil.isEmpty(userDto.getUserNick())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 화면 검증을 우회한 요청도 프로필과 동일한 닉네임 규칙으로 차단한다
        if (!isValidUserNick(userDto.getUserNick())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 온보딩 닉네임에 저장을 차단할 비속어가 있는지 조회한다
        Optional<String> badWord = badWordDetectionService.findBadWord(userDto.getUserNick());

        // 비속어가 포함된 닉네임은 신규 사용자의 첫 프로필로 저장하지 않는다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // 본인을 제외한 다른 사용자가 같은 닉네임을 사용하면 중복 저장을 차단한다
        if (userMapper.getUserNickDuplicateCnt(userDto) > 0) {
            // "이미 사용 중인 닉네임이에요."
            return ResultData.fail(ResultEnum.USER_NICK_DUPLICATED);
        }

        // 닉네임 저장과 온보딩 완료 상태를 동일한 사용자 행에 원자적으로 반영한다
        int updateCnt = userMapper.uptUserOnboarding(userDto);

        // 이미 완료했거나 사용자가 사라져 수정되지 않은 요청은 성공으로 처리하지 않는다
        if (updateCnt == 0) {
            // 온보딩 상태가 바뀌지 않았으므로 현재 쓰기 트랜잭션을 롤백한다
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // "수정에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_UPDATE_REJECTED);
        }

        // DB 커밋 뒤 현재 로그인 세션의 닉네임도 같은 값으로 갱신한다
        uptUserNickAfterCommit(userNumb, userDto.getUserNick());
        // 온보딩 완료 상태가 포함된 최신 사용자 프로필을 반환한다
        return getMe(userNumb);
    }

    /**
     * 최초 로그인 화면에 노출할 활성 독서 관심분야를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 대분류와 세부코드가 포함된 관심분야 목록
     */
    @Override
    public ResultData getUserInterestCatalog() {
        // 관리자 공통코드에서 활성 상태인 독서 관심분야를 조회한다
        List<UserDto.UserInterestDto> interestCatalog = userMapper.getUserInterestCatalog();
        // 최초 로그인 화면이 대분류별 선택 항목을 구성할 수 있도록 전체 목록을 반환한다
        return ResultData.success(interestCatalog);
    }

    /**
     * 로그인 사용자가 현재 선택한 독서 관심분야를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 선택한 관심분야 목록
     */
    @Override
    public ResultData getUserInterestList(Long userNumb) {
        // 인증 사용자 번호가 없으면 관심분야를 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요. 다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 사용자가 현재 저장한 독서 관심분야를 조회한다
        List<UserDto.UserInterestDto> interests = userMapper.getUserInterestList(userNumb);
        // 현재 선택된 독서 관심분야 목록을 반환한다
        return ResultData.success(interests);
    }

    /**
     * 로그인 사용자의 독서 관심분야를 검증한 선택 목록으로 전체 교체한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param request 선택한 관심분야 목록
     * @return 관심분야 저장 결과
     */
    @Override
    @Transactional
    public ResultData uptUserInterests(Long userNumb, UserDto.UserInterestReqDto request) {
        // 인증 사용자와 요청 본문이 모두 있어야 본인의 관심분야를 변경할 수 있다
        if (StringUtil.hasEmpty(userNumb, request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 요청 본문에서 사용자가 선택한 관심분야 목록을 가져온다
        List<UserDto.UserInterestDto> requestedInterests = request.getInterestList();
        // 빈 목록은 관심분야를 건너뛴 사용자에게 허용하므로 Null만 빈 목록으로 보정한다
        if (StringUtil.isEmpty(requestedInterests)) {
            // 검증과 저장에서 같은 빈 목록을 사용하도록 초기화한다
            requestedInterests = List.of();
        }

        // 현재 활성 CATE_CODE 하위 세부코드만 저장할 수 있도록 허용 코드를 구성한다
        Set<String> validInterestCodes = new HashSet<>();
        // 활성 관심분야 세부코드를 허용 목록에 추가한다
        for (UserDto.UserInterestDto interest : userMapper.getUserInterestCatalog()) {
            // CATE_CODE 그룹은 고정값이므로 세부코드만 저장 검증에 사용한다
            validInterestCodes.add(interest.getIntrCode());
        }

        Set<String> requestedInterestCodes = new HashSet<>();
        // 선택 목록의 유효성과 중복 여부를 저장 전에 모두 확인한다
        for (UserDto.UserInterestDto interest : requestedInterests) {
            // 비어 있거나 비활성인 관심분야 세부코드는 저장하지 않는다
            if (StringUtil.isEmpty(interest) || StringUtil.isEmpty(interest.getIntrCode())) {
                // "요청값이 올바르지 않아요."
                return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
            }

            // CATE_CODE의 활성 하위 코드가 아니거나 같은 코드가 반복되면 전체 요청을 거절한다
            if (!validInterestCodes.contains(interest.getIntrCode())
                    || !requestedInterestCodes.add(interest.getIntrCode())) {
                // "요청값이 올바르지 않아요."
                return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
            }
        }

        // 전체 교체 정책에 따라 기존 관심분야를 먼저 삭제한다
        userMapper.delUserInterests(userNumb);
        // 검증된 선택 조합을 사용자 관심분야로 저장한다
        for (UserDto.UserInterestDto interest : requestedInterests) {
            // 같은 트랜잭션에서 선택 항목 한 건을 저장한다
            userMapper.setUserInterest(userNumb, interest);
        }

        // 선택 항목이 없더라도 건너뛰기 상태를 정상 저장 결과로 반환한다
        return ResultData.success();
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
        return userNick.length() <= Constant.USER_NICK_MAX_LENGTH && USER_NICK_PATTERN.matcher(userNick).matches();
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
