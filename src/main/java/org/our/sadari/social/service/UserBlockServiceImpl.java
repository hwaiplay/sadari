package org.our.sadari.social.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.social.dto.UserBlockDto;
import org.our.sadari.social.mapper.UserBlockMapper;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : UserBlockServiceImpl
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 상태와 양방향 격리 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockServiceImpl implements UserBlockService {

    // 차단 사용자 관리 화면의 페이지당 사용자 수
    private static final int BLOCK_PAGE_SIZE = 10;

    // 사용자 차단 관계 데이터 접근 객체
    private final UserBlockMapper userBlockMapper;
    // 사용자 원본 데이터 접근 객체
    private final UserMapper userMapper;

    /**
     * 두 사용자 사이에 어느 방향이든 차단 관계가 있는지 조회한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 상대 사용자 번호
     * @return 양방향 격리 여부
     */
    @Override
    public boolean isBlocked(Long userNumb, Long targetUserNumb) {
        // 본인 조회와 인증 정보가 없는 내부 흐름은 차단 관계로 판단하지 않는다
        if (StringUtil.hasEmpty(userNumb, targetUserNumb) || userNumb.equals(targetUserNumb)) {
            // 두 사용자 사이에 적용할 차단 관계가 없음을 반환한다
            return false;
        }

        // 어느 한쪽이 만든 차단 관계라도 존재하면 양방향 격리 상태로 반환한다
        return userBlockMapper.getBlockCnt(userNumb, targetUserNumb) > 0;
    }

    /**
     * 차단 관계 등록과 양방향 팔로우 삭제를 하나의 트랜잭션으로 처리한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 차단 대상 사용자 번호
     * @return 차단 완료 여부
     */
    @Override
    @Transactional
    public ResultData setBlock(Long userNumb, Long targetUserNumb) {
        // 인증 정보가 없으면 차단 관계의 소유자를 확정할 수 없어 요청을 거부한다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 대상이 없거나 본인이면 유효한 차단 관계가 아니므로 요청을 거부한다
        if (StringUtil.isEmpty(targetUserNumb) || userNumb.equals(targetUserNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 대상 원본이 물리 삭제되었으면 차단 주체나 상태를 구분하지 않는 공통 응답을 사용한다
        UserDto targetUser = userMapper.getUserByNumb(targetUserNumb);
        // 사용자 원본이 없으면 차단 관계를 저장하지 않는다
        if (StringUtil.isEmpty(targetUser)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 차단과 팔로우 등록이 동시에 실행되어도 차단 완료 뒤 관계가 남지 않도록 사용자 쌍을 잠근다
        lockUsers(userNumb, targetUserNumb);
        // 로그인 사용자가 소유한 방향의 차단 관계를 멱등 등록한다
        UserBlockDto blockDto = createBlockDto(userNumb, targetUserNumb);
        // 검증된 차단 관계를 저장한다
        userBlockMapper.setBlock(blockDto);
        // 차단 당사자 사이에서 직접 발송된 수락 전 모임 초대를 삭제한다
        userBlockMapper.delBlockInvitations(blockDto);
        // 어느 한쪽이 모임장인 모임의 상대방 처리 중 가입 신청과 답변을 삭제한다
        userBlockMapper.delBlockApplications(blockDto);
        // 차단 사용자 사이의 기존 팔로우 관계를 양방향 모두 삭제한다
        userBlockMapper.delBlockFollows(blockDto);
        // 현재 차단 상태가 확정되었음을 성공 응답으로 반환한다
        return ResultData.success(true);
    }

    /**
     * 로그인 사용자가 소유한 한 방향의 차단 관계만 멱등 해제한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 차단 해제 대상 사용자 번호
     * @return 차단 해제 완료 여부
     */
    @Override
    @Transactional
    public ResultData delBlock(Long userNumb, Long targetUserNumb) {
        // 인증 정보가 없으면 삭제할 차단 관계의 소유자를 확정할 수 없다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 대상이 없거나 본인이면 다른 관계를 잘못 삭제하지 않도록 요청을 거부한다
        if (StringUtil.isEmpty(targetUserNumb) || userNumb.equals(targetUserNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 로그인 사용자가 생성한 한 방향의 차단 관계만 삭제한다
        userBlockMapper.delBlock(createBlockDto(userNumb, targetUserNumb));
        // 이미 해제된 요청도 현재 상태와 일치하므로 성공으로 반환한다
        return ResultData.success(true);
    }

    /**
     * 로그인 사용자가 직접 차단한 사용자만 최신 차단순 페이지로 조회한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 차단 사용자 페이지
     */
    @Override
    public ResultData getBlockList(Long userNumb, int page) {
        // 인증 정보가 없으면 개인 차단 목록을 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 첫 페이지보다 작은 요청은 첫 페이지로 보정한다
        int normalizedPage = Math.max(page, 1);
        // 차단 목록 페이지 조건을 담을 객체를 생성한다
        UserBlockDto request = new UserBlockDto();
        // 로그인 사용자가 소유한 차단 방향만 조회하도록 사용자 번호를 설정한다
        request.setUserNumb(userNumb);
        // 요청 페이지의 시작 위치를 설정한다
        request.setPageOffset((normalizedPage - 1) * BLOCK_PAGE_SIZE);
        // 다음 페이지 판정용 한 건을 추가한 조회 수를 설정한다
        request.setPageLimit(BLOCK_PAGE_SIZE + 1);
        // 최신 차단순으로 사용자 목록을 조회한다
        List<UserBlockDto> searchedList = userBlockMapper.getBlockList(request);
        // Mapper가 빈 값을 반환해도 안정적인 빈 페이지로 보정한다
        List<UserBlockDto> safeList = StringUtil.isEmpty(searchedList) ? List.of() : searchedList;
        // 한 건을 더 조회했으면 다음 페이지가 존재하는 것으로 판정한다
        boolean hasNext = safeList.size() > BLOCK_PAGE_SIZE;
        // 화면에는 현재 페이지 크기까지만 전달한다
        List<UserBlockDto> visibleList = hasNext ? safeList.subList(0, BLOCK_PAGE_SIZE) : safeList;
        // 차단 목록과 현재 페이지 및 다음 페이지 여부를 반환한다
        return ResultData.success(new PageDto<>(visibleList, normalizedPage, hasNext));
    }

    /**
     * 차단과 팔로우 저장이 교차 실행되지 않도록 사용자 원본을 작은 번호부터 잠근다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 상대 사용자 번호
     */
    @Override
    public void lockUsers(Long userNumb, Long targetUserNumb) {
        // 항상 같은 잠금 순서를 사용하도록 작은 사용자 번호를 먼저 결정한다
        long firstUserNumb = Math.min(userNumb, targetUserNumb);
        // 큰 사용자 번호를 두 번째 잠금 대상으로 결정한다
        long secondUserNumb = Math.max(userNumb, targetUserNumb);
        // 두 사용자 원본을 순서대로 잠가 반대 방향 동시 요청의 교착을 줄인다
        userBlockMapper.lockUsers(firstUserNumb, secondUserNumb);
    }

    /**
     * 차단 주체와 대상을 Mapper 요청 객체로 구성한다
     *
     * @author HanWon.Jang
     * @param userNumb 차단을 소유한 사용자 번호
     * @param targetUserNumb 차단 대상 사용자 번호
     * @return 차단 관계 요청 객체
     */
    private UserBlockDto createBlockDto(Long userNumb, Long targetUserNumb) {
        // 차단 관계를 전달할 요청 객체를 생성한다
        UserBlockDto blockDto = new UserBlockDto();
        // 차단 관계를 소유한 로그인 사용자 번호를 설정한다
        blockDto.setUserNumb(userNumb);
        // 차단 대상 사용자 번호를 설정한다
        blockDto.setBlocNumb(targetUserNumb);
        // 완성된 차단 관계 요청 객체를 반환한다
        return blockDto;
    }
}
