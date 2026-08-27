package org.our.sadari.reply.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.event.LikeAlimEvent;
import org.our.sadari.alim.event.LikeAlimPublisher;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.reply.dto.ReplyDto;
import org.our.sadari.reply.mapper.ReplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReplyServiceImpl
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회, 등록, 수정, 삭제 및 좋아요 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성 및 댓글 조회·등록
 * 2026-07-29        Hanwon.Jang        댓글 알림·작성 여부 조회 추가
 * 2026-08-03        Hanwon.Jang        댓글 수정·삭제·좋아요 처리 추가
 * 2026-08-04        HanWon.Jang        댓글 및 대댓글 좋아요 알림 구현
 * 2026-08-15        SeungHyeon.Kang    부모 댓글 페이지 조회 추가
 * 2026-08-21        SeungHyeon.Kang    독후감별 댓글 알림 설정 적용
 * 2026-08-21        SeungHyeon.Kang    댓글 좋아요 알림 발신자 조회 보강
 * 2026-08-26        HanWon.Jang        좋아요 알림 비동기화
 * 2026-08-27        SeungHyeon.Kang    대상별 댓글 좋아요와 답글 다중 수신자 알림 적용
 * 2026-08-27        SeungHyeon.Kang    댓글 상호작용 알림 템플릿 통합과 동적 대상 저장
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyServiceImpl implements ReplyService {

    // 댓글 바텀시트가 한 번에 조회할 부모 댓글 수
    private static final int REPLY_PAGE_SIZE = 10;

    // 댓글 내용 저장 한도
    private static final int REPLY_CONTENT_MAX_BYTES = 4000;

    // Reply 데이터 접근 객체
    private final ReplyMapper replyMapper;
    // 댓글 비속어 검사 서비스
    private final BadWordDetectionService badWordDetectionService;
    // 사용자별 알림 저장과 푸시 발송 서비스
    private final AlimService alimService;
    // 댓글 작성자의 최신 닉네임 조회 서비스
    private final TokenRedisService tokenRedisService;
    // 댓글 좋아요 커밋 이후 알림 후처리 이벤트 발행기
    private final LikeAlimPublisher likeAlimPublisher;

    /**
     * 로그인 사용자가 작성한 댓글 또는 답글을 등록한다.
     * 댓글 내용과 부모 댓글 번호를 검증하고 신규 댓글은 삭제되지 않은 상태로 저장한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param replyDto 등록할 댓글 또는 답글 정보
     * @return 등록된 댓글 번호를 포함한 처리 결과
     */
    @Override
    @Transactional
    public ResultData setReply(Long userNumb, ReplyDto replyDto) {
        // 인증 사용자 번호가 없으면 작성자를 특정할 수 없으므로 등록을 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        normalizeReplyTarget(replyDto);

        // 대상 번호와 댓글 내용이 없으면 등록 대상과 저장 내용을 확정할 수 없으므로 요청을 거부한다
        if (StringUtil.isEmpty(replyDto) || StringUtil.isEmpty(replyDto.getTagtType())
                || StringUtil.isEmpty(replyDto.getTagtNumb())
                || StringUtil.isEmpty(replyDto.getReplCntn())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 사진 댓글은 사진 소유자 또는 현재 팔로워만 등록할 수 있도록 API 경계에서 접근을 거부한다
        if (!hasImageReplyAccess(userNumb, replyDto.getTagtType(), replyDto.getTagtNumb())) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 사용자 입력의 앞뒤 공백을 제거해 공백만 있는 댓글이 저장되지 않도록 정규화한다
        String normalizedContent = StringUtil.normalizePlainText(replyDto.getReplCntn());

        // 정규화 후 내용이 없거나 저장 한도를 넘으면 DB 오류가 발생하기 전에 요청을 거부한다
        if (StringUtil.isEmpty(normalizedContent)
                || XssUtil.utf8ByteLength(normalizedContent) > REPLY_CONTENT_MAX_BYTES) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 부모 댓글 번호가 전달된 답글은 양수 번호만 허용해 잘못된 참조값을 저장하지 않는다
        if (!StringUtil.isEmpty(replyDto.getUperNumb()) && replyDto.getUperNumb() <= 0) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 댓글에 포함된 비속어를 찾아 공개 화면에 부적절한 내용이 저장되지 않도록 한다
        Optional<String> badWord = badWordDetectionService.findBadWord(normalizedContent);

        // 비속어가 발견되면 해당 단어를 사용자 메시지에 포함해 등록을 중단한다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"에서 {0}은 탐지된 비속어이다
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // 인증 정보에서 확인한 사용자 번호를 댓글 작성자로 설정한다
        replyDto.setUserNumb(userNumb);
        // 검증이 끝난 평문 댓글 내용을 저장값으로 설정한다
        replyDto.setReplCntn(normalizedContent);
        // 신규 댓글은 화면에 노출될 수 있도록 미삭제 상태로 설정한다
        replyDto.setDeltYsno(Constant.COMM_NO);

        // 검증된 댓글을 DB에 등록하고 시퀀스로 생성된 댓글 번호를 DTO에 반영한다
        int insertCnt = replyMapper.setReply(replyDto);

        // 등록된 행이 없으면 댓글 저장이 반영되지 않은 것으로 판단해 실패 응답을 반환한다
        if (insertCnt == 0) {
            // "저장에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 댓글이 등록된 독후감 작성자에게 신규 댓글 알림을 발송한다
        sendReplyTargetAlim(userNumb, replyDto);

        // 등록된 댓글 번호를 화면에서 후속 조회에 사용할 수 있도록 성공 응답으로 반환한다
        return ResultData.success(replyDto.getReplNumb());
    }

    /**
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글의 내용을 수정한다.
     * 내용과 작성자 및 계정 상태를 검증하고 수정 일시를 갱신한다.
     *
     * @author HanWon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param reptNumb 수정할 댓글이 속한 독후감 번호
     * @param replNumb 수정할 댓글 번호
     * @param replyDto 변경할 댓글 내용
     * @return 수정된 댓글 번호를 포함한 처리 결과
     */
    @Override
    @Transactional
    public ResultData uptReply(Long userNumb, Long reptNumb, Long replNumb, ReplyDto replyDto) {
        // 인증 사용자와 수정 대상 및 댓글 내용이 없으면 변경 대상을 확정할 수 없으므로 요청을 거부한다
        if (StringUtil.hasEmpty(userNumb, reptNumb, replNumb, replyDto)
                || StringUtil.isEmpty(replyDto.getReplCntn())
                || StringUtil.isEmpty(replyDto.getEditVersion())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 사용자 입력의 앞뒤 공백을 제거해 공백만 있는 댓글이 저장되지 않도록 정규화한다
        String normalizedContent = StringUtil.normalizePlainText(replyDto.getReplCntn());

        // 정규화 후 내용이 없거나 저장 한도를 넘으면 DB 오류가 발생하기 전에 요청을 거부한다
        if (StringUtil.isEmpty(normalizedContent)
                || XssUtil.utf8ByteLength(normalizedContent) > REPLY_CONTENT_MAX_BYTES) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 댓글에 포함된 비속어를 찾아 공개 화면에 부적절한 내용으로 변경되지 않도록 한다
        Optional<String> badWord = badWordDetectionService.findBadWord(normalizedContent);

        // 비속어가 발견되면 해당 단어를 사용자 메시지에 포함해 수정을 중단한다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"에서 {0}은 탐지된 비속어이다
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // URL에서 확정한 독후감 번호를 수정 조건으로 설정한다
        replyDto.setReptNumb(reptNumb);
        normalizeReplyTarget(replyDto);
        // URL에서 확정한 댓글 번호를 수정 조건으로 설정한다
        replyDto.setReplNumb(replNumb);
        // 인증 정보에서 확인한 사용자 번호를 소유자 조건으로 설정한다
        replyDto.setUserNumb(userNumb);
        // 검증이 끝난 평문 댓글 내용을 변경값으로 설정한다
        replyDto.setReplCntn(normalizedContent);

        // 정상 이용 중인 작성자의 미삭제 댓글만 변경하도록 조건을 포함해 수정한다
        int updateCnt = replyMapper.uptReply(replyDto);

        // 반영된 행이 없으면 다른 사용자의 댓글, 삭제된 댓글 또는 제한 계정 요청으로 판단한다
        if (updateCnt == 0) {
            // 다른 탭이나 기기의 선행 수정 내용을 덮어쓰지 않도록 충돌 결과를 반환한다
            return ResultData.fail(ResultEnum.COMMON_EDIT_CONFLICT);
        }

        // 수정된 댓글 번호를 화면에서 후속 조회에 사용할 수 있도록 성공 응답으로 반환한다
        return ResultData.success(replNumb);
    }

    /** 범용 대상에 등록된 댓글 내용을 수정한다. */
    @Override
    @Transactional
    public ResultData uptReply(Long userNumb, String tagtType, Long tagtNumb, Long replNumb, ReplyDto replyDto) {
        if (StringUtil.isEmpty(replyDto) || !isAllowedTargetType(tagtType)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        if (!hasImageReplyAccess(userNumb, tagtType, tagtNumb)) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }
        replyDto.setTagtType(tagtType.trim().toUpperCase());
        replyDto.setTagtNumb(tagtNumb);
        return uptReply(userNumb, tagtNumb, replNumb, replyDto);
    }

    /**
     * 로그인 사용자가 작성한 미삭제 댓글 또는 답글을 삭제 상태로 전환한다.
     * 자식 답글 구조를 유지하도록 원문 행은 보존하고 삭제 여부만 변경한다.
     *
     * @author HanWon.Jang
     * @param userNumb 댓글 작성자 사용자 번호
     * @param reptNumb 삭제할 댓글이 속한 독후감 번호
     * @param replNumb 삭제할 댓글 번호
     * @return 삭제된 댓글 번호를 포함한 처리 결과
     */
    @Override
    @Transactional
    public ResultData delReply(Long userNumb, Long reptNumb, Long replNumb) {
        // 인증 사용자와 삭제 대상 번호가 없으면 변경 대상을 확정할 수 없으므로 요청을 거부한다
        if (StringUtil.hasEmpty(userNumb, reptNumb, replNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 댓글 삭제 조건을 담을 객체를 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 요청받은 독후감 번호를 삭제 조건으로 설정한다
        replyDto.setReptNumb(reptNumb);
        normalizeReplyTarget(replyDto);
        // 요청받은 댓글 번호를 삭제 조건으로 설정한다
        replyDto.setReplNumb(replNumb);
        // 인증 정보에서 확인한 사용자 번호를 소유자 조건으로 설정한다
        replyDto.setUserNumb(userNumb);

        // 정상 이용 중인 작성자의 미삭제 댓글만 삭제 상태로 전환한다
        int deleteCnt = replyMapper.delReply(replyDto);

        // 반영된 행이 없으면 다른 사용자의 댓글, 삭제된 댓글 또는 제한 계정 요청으로 판단한다
        if (deleteCnt == 0) {
            // "삭제에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED);
        }

        // 삭제 상태로 전환된 댓글 번호를 화면에서 후속 조회에 사용할 수 있도록 성공 응답으로 반환한다
        return ResultData.success(replNumb);
    }

    /** 범용 대상에 등록된 댓글을 삭제 상태로 전환한다. */
    @Override
    @Transactional
    public ResultData delReply(Long userNumb, String tagtType, Long tagtNumb, Long replNumb) {
        if (StringUtil.hasEmpty(userNumb, tagtNumb, replNumb) || !isAllowedTargetType(tagtType)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        if (!hasImageReplyAccess(userNumb, tagtType, tagtNumb)) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }
        ReplyDto replyDto = createTargetRequest(userNumb, tagtType, tagtNumb, replNumb);
        int deleteCnt = replyMapper.delReply(replyDto);
        return deleteCnt == 0
                ? ResultData.fail(ResultEnum.COMMON_DELETE_REJECTED)
                : ResultData.success(replNumb);
    }

    /**
     * 정상 이용 중인 로그인 사용자의 미삭제 댓글 좋아요를 중복 없이 등록한다.
     * 본인 댓글도 동일한 검증 기준으로 허용하고 신규 좋아요일 때 대상 댓글 작성자에게만 알림을 생성한다.
     *
     * @author HanWon.Jang
     * @param userNumb 좋아요를 등록할 사용자 번호
     * @param reptNumb 좋아요 대상 댓글의 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 댓글 좋아요 상태와 좋아요 수
     */
    @Override
    @Transactional
    public ResultData setReplyLike(Long userNumb, Long reptNumb, Long replNumb) {
        // 인증 사용자와 댓글 복합 식별값을 검증한 좋아요 요청 객체를 생성한다
        ReplyDto replyDto = createReplyLikeRequest(userNumb, reptNumb, replNumb);

        // 인증 정보나 양수 식별값이 없으면 좋아요 대상을 확정할 수 없어 요청을 거부한다
        if (StringUtil.isEmpty(replyDto)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 정상 이용 사용자와 미삭제 댓글 여부를 검증하면서 정확한 댓글 작성자를 알림 수신자로 조회한다
        ReplyDto likeTarget = replyMapper.getReplyLikeTarget(replyDto);

        // 정상 이용 중인 사용자와 미삭제 댓글 조합이 아니면 좋아요 등록을 허용하지 않는다
        if (StringUtil.isEmpty(likeTarget)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 복합 기본키 충돌 없이 현재 사용자의 댓글 좋아요를 등록한다
        int insertCnt = replyMapper.setReplyLike(replyDto);

        // 신규 좋아요가 등록된 경우에만 댓글 작성자에게 좋아요 알림을 발송한다
        if (insertCnt > 0) {
            // 댓글 좋아요 커밋 이후 전용 템플릿 알림을 처리하도록 이벤트를 등록한다
            sendReplyLikeAlim(userNumb, likeTarget);
        }

        // 등록 직후 서버 기준 좋아요 수와 현재 사용자 상태를 조회해 반환한다
        return ResultData.success(replyMapper.getReplyLikeDtl(replyDto));
    }

    /** 범용 대상 댓글에 좋아요를 등록한다. */
    @Override
    @Transactional
    public ResultData setReplyLike(Long userNumb, String tagtType, Long tagtNumb, Long replNumb) {
        ReplyDto replyDto = createTargetRequest(userNumb, tagtType, tagtNumb, replNumb);
        if (StringUtil.isEmpty(replyDto)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        ReplyDto likeTarget = replyMapper.getReplyLikeTarget(replyDto);
        if (StringUtil.isEmpty(likeTarget)) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }
        int insertCnt = replyMapper.setReplyLike(replyDto);
        if (insertCnt > 0) {
            sendReplyLikeAlim(userNumb, likeTarget);
        }
        return ResultData.success(replyMapper.getReplyLikeDtl(replyDto));
    }

    /**
     * 정상 이용 중인 로그인 사용자의 미삭제 댓글 좋아요를 취소한다.
     * 존재하지 않는 좋아요 취소도 최종 상태가 동일하므로 멱등 성공으로 처리한다.
     *
     * @author HanWon.Jang
     * @param userNumb 좋아요를 취소할 사용자 번호
     * @param reptNumb 좋아요 대상 댓글의 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 변경 후 댓글 좋아요 상태와 좋아요 수
     */
    @Override
    @Transactional
    public ResultData delReplyLike(Long userNumb, Long reptNumb, Long replNumb) {
        // 인증 사용자와 댓글 복합 식별값을 검증한 좋아요 요청 객체를 생성한다
        ReplyDto replyDto = createReplyLikeRequest(userNumb, reptNumb, replNumb);

        // 인증 정보나 양수 식별값이 없으면 좋아요 대상을 확정할 수 없어 요청을 거부한다
        if (StringUtil.isEmpty(replyDto)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 정상 이용 사용자와 미삭제 댓글 여부를 확인해 취소 가능한 좋아요 대상인지 조회한다
        ReplyDto likeTarget = replyMapper.getReplyLikeTarget(replyDto);

        // 정상 이용 중인 사용자와 미삭제 댓글 조합이 아니면 좋아요 취소도 허용하지 않는다
        if (StringUtil.isEmpty(likeTarget)) {
            // "접근할 수 없는 요청이에요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 현재 사용자가 등록한 댓글 좋아요만 삭제한다
        replyMapper.delReplyLike(replyDto);
        // 취소 직후 서버 기준 좋아요 수와 현재 사용자 상태를 조회해 반환한다
        return ResultData.success(replyMapper.getReplyLikeDtl(replyDto));
    }

    /** 범용 대상 댓글의 좋아요를 취소한다. */
    @Override
    @Transactional
    public ResultData delReplyLike(Long userNumb, String tagtType, Long tagtNumb, Long replNumb) {
        ReplyDto replyDto = createTargetRequest(userNumb, tagtType, tagtNumb, replNumb);
        if (StringUtil.isEmpty(replyDto)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        if (StringUtil.isEmpty(replyMapper.getReplyLikeTarget(replyDto))) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }
        replyMapper.delReplyLike(replyDto);
        return ResultData.success(replyMapper.getReplyLikeDtl(replyDto));
    }

    /**
     * 댓글 좋아요 API의 인증 사용자와 복합 식별값을 Mapper 요청 객체로 변환한다.
     *
     * @author HanWon.Jang
     * @param userNumb 좋아요를 변경할 사용자 번호
     * @param reptNumb 좋아요 대상 댓글의 독후감 번호
     * @param replNumb 좋아요 대상 댓글 번호
     * @return 유효한 댓글 좋아요 요청 객체 또는 null
     */
    private ReplyDto createReplyLikeRequest(Long userNumb, Long reptNumb, Long replNumb) {
        // 누락되거나 양수가 아닌 식별값은 DB 조회 전에 차단한다
        if (StringUtil.hasEmpty(userNumb, reptNumb, replNumb)
                || userNumb <= 0 || reptNumb <= 0 || replNumb <= 0) {
            // 유효한 댓글 좋아요 요청을 만들 수 없음을 반환한다
            return null;
        }

        // 댓글 좋아요 조회와 변경 조건을 담을 객체를 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 인증 사용자 번호를 좋아요 주체로 설정한다
        replyDto.setUserNumb(userNumb);
        // 독후감 번호를 댓글 소속 검증 조건으로 설정한다
        replyDto.setReptNumb(reptNumb);
        replyDto.setTagtType(Constant.LIKE_TARGET_REPORT);
        replyDto.setTagtNumb(reptNumb);
        // 댓글 번호를 범용 좋아요 대상 번호로 설정한다
        replyDto.setReplNumb(replNumb);
        // 검증된 댓글 좋아요 요청 객체를 반환한다
        return replyDto;
    }

    /**
     * 신규 댓글 또는 답글 좋아요의 대상 유형별 커밋 이후 알림 이벤트를 등록한다.
     * 본인 댓글 좋아요와 작성자를 확인할 수 없는 댓글은 이벤트를 만들지 않는다.
     *
     * @author HanWon.Jang
     * @param sendUserNumb 댓글 좋아요를 등록한 사용자 번호
     * @param likeTarget 좋아요 대상 댓글과 알림 수신자 정보
     */
    private void sendReplyLikeAlim(Long sendUserNumb, ReplyDto likeTarget) {
        // 댓글 작성자가 없거나 본인 댓글에 좋아요를 등록했으면 자기 자신에게 알림을 만들지 않는다
        if (StringUtil.isEmpty(likeTarget.getTargetUserNumb())
                || likeTarget.getTargetUserNumb().equals(sendUserNumb)) {
            // 댓글 좋아요 알림 처리 없이 호출부로 반환한다
            return;
        }

        // 지원하지 않는 댓글 대상 유형이면 목적지를 계산할 수 없는 알림을 만들지 않는다
        if (!isReplyTargetType(likeTarget.getTagtType())) {
            // 대상별 댓글 좋아요 알림 처리 없이 호출부로 반환한다
            return;
        }

        // 대상 검증 SQL에서 함께 조회한 활성 좋아요 등록자의 최신 닉네임을 사용한다
        String sendUserNick = likeTarget.getUserNick();

        // 회원 원본에서 닉네임을 확인할 수 없으면 미완성 문구의 알림을 저장하지 않는다
        if (StringUtil.isEmpty(sendUserNick)) {
            // 발신자 닉네임이 없는 댓글 좋아요 알림 처리 없이 호출부로 반환한다
            return;
        }

        Long replyTargetNumb = StringUtil.isEmpty(likeTarget.getTagtNumb())
                ? likeTarget.getReptNumb()
                : likeTarget.getTagtNumb();

        // 댓글 좋아요 응답 경로에서 알림 DB와 FCM 접근을 제거할 커밋 이후 이벤트를 생성한다
        LikeAlimEvent event = new LikeAlimEvent(sendUserNumb, likeTarget.getTargetUserNumb()
                                              , Constant.ALIM_TEMP_CODE_REPLY_LIKE, likeTarget.getTagtType()
                                              , replyTargetNumb, likeTarget.getReplNumb(), sendUserNick);
        // 댓글 좋아요 트랜잭션이 커밋된 경우에만 비동기 알림 작업이 시작되도록 이벤트를 등록한다
        likeAlimPublisher.setLikeAlim(event);
    }

    /**
     * 콘텐츠 소유자와 부모 댓글 작성자에게 댓글 작성자의 닉네임 및 이동 대상 정보가 포함된 알림을 발송한다.
     * 자기 알림과 동일한 수신자는 제외하고 콘텐츠 소유자의 알림 설정을 적용한다.
     *
     * @author HanWon.Jang
     * @param sendUserNumb 댓글을 등록한 사용자 번호
     * @param replyDto 등록된 댓글과 독후감 번호
     */
    private void sendReplyTargetAlim(Long sendUserNumb, ReplyDto replyDto) {
        // 댓글 대상 소유자와 부모 댓글 작성자 및 대상별 알림 정보를 조회한다
        ReplyDto reportAlim = replyMapper.getReplyReportAlimDtl(replyDto);

        // 알림 대상 정보를 확인할 수 없으면 불완전한 알림을 만들지 않는다
        if (StringUtil.isEmpty(reportAlim)) {
            // 댓글 등록 알림 처리 없이 호출부로 반환한다
            return;
        }

        // 동일 수신자의 알림 사유가 겹쳐도 더 구체적인 답글 템플릿을 유지하도록 수신자별 계획을 구성한다
        Map<Long, String> recipientTemplateMap = new LinkedHashMap<>();
        Long contentOwnerNumb = reportAlim.getTargetUserNumb();
        Long parentUserNumb = reportAlim.getParentUserNumb();
        boolean ownerIsParent = !StringUtil.isEmpty(contentOwnerNumb) && contentOwnerNumb.equals(parentUserNumb);

        // 콘텐츠 소유자가 부모 댓글 작성자이면 일반 콘텐츠 댓글보다 직접 답글 알림을 우선한다
        if (ownerIsParent && !contentOwnerNumb.equals(sendUserNumb)) {
            String ownerReplyTemplate = resolveReplyTemplate(replyDto.getTagtType());
            if (!StringUtil.isEmpty(ownerReplyTemplate)) {
                recipientTemplateMap.put(contentOwnerNumb, ownerReplyTemplate);
            }
        }

        // 부모 댓글 작성자가 아닌 콘텐츠 소유자에게는 콘텐츠별 댓글 알림 설정을 적용한다
        if (!ownerIsParent && Constant.COMM_YES.equals(reportAlim.getReplyAlimYsno())
                && !StringUtil.isEmpty(contentOwnerNumb) && !contentOwnerNumb.equals(sendUserNumb)) {
            String contentTemplate = StringUtil.isEmpty(reportAlim.getAlimTempCode())
                    ? Constant.ALIM_TEMP_CODE_REPLY_REPORT
                    : reportAlim.getAlimTempCode();
            recipientTemplateMap.put(contentOwnerNumb, contentTemplate);
        }

        // 콘텐츠 소유자와 다른 부모 댓글 작성자에게는 관계별 직접 답글 템플릿을 적용한다
        if (!StringUtil.isEmpty(parentUserNumb) && !parentUserNumb.equals(sendUserNumb)
                && !parentUserNumb.equals(contentOwnerNumb)) {
            String parentTemplate = resolveReplyTemplate(replyDto.getTagtType());
            if (!StringUtil.isEmpty(parentTemplate)) {
                recipientTemplateMap.put(parentUserNumb, parentTemplate);
            }
        }

        // 자기 알림과 중복 제거 뒤 수신자가 없으면 알림 저장과 푸시 예약을 생략한다
        if (recipientTemplateMap.isEmpty()) {
            // 댓글 등록 알림 처리 없이 호출부로 반환한다
            return;
        }

        // 알림 템플릿의 작성자 문구에 사용할 로그인 사용자의 최신 닉네임을 조회한다
        String sendUserNick = tokenRedisService.getUserNick(sendUserNumb);

        // 로그인 세션에서 닉네임을 확인할 수 없으면 미완성 문구의 알림을 저장하지 않는다
        if (StringUtil.isEmpty(sendUserNick)) {
            // 닉네임 치환이 불가능한 알림 처리 없이 호출부로 반환한다
            return;
        }

        // REPLY_REPORT 템플릿의 사용자명 치환값을 담을 객체를 생성한다
        Map<String, Object> replaceMap = new HashMap<>();
        // 댓글 작성자의 닉네임을 템플릿 사용자명 치환값으로 설정한다
        replaceMap.put("userName", sendUserNick);

        // 클릭 시점의 공통 라우터가 최종 경로를 계산할 수 있도록 이동 대상 번호를 결정한다
        Long notificationTarget = StringUtil.isEmpty(reportAlim.getAlimTagtNumb())
                ? replyDto.getTagtNumb()
                : reportAlim.getAlimTagtNumb();

        // 중복 제거된 각 수신자에게 같은 원본 콘텐츠로 이동하는 댓글 알림을 저장하고 푸시를 예약한다
        for (Map.Entry<Long, String> recipientPlan : recipientTemplateMap.entrySet()) {
            // 현재 수신자에게 대상 유형별 댓글 알림을 발송한다
            alimService.sendAlim(
                    recipientPlan.getKey()
                  , Constant.ALIM_SITU_REPLY
                  , recipientPlan.getValue()
                  , replyDto.getTagtType()
                  , notificationTarget
                  , replyDto.getReplNumb()
                  , replaceMap
            );
        }
    }

    /** 원본 콘텐츠 유형이 동적 이동을 지원하면 공통 대댓글 템플릿을 반환한다. */
    private String resolveReplyTemplate(String tagtType) {
        // 알림 클릭 시점의 관계는 목적지 조회에서 판단하므로 문구가 같은 대댓글은 하나의 템플릿을 사용한다
        if (isReplyTargetType(tagtType)) {
            // 지원 대상에 공통 대댓글 템플릿 코드를 반환한다
            return Constant.ALIM_TEMP_CODE_REPLY_TO_COMMENT;
        }

        // 지원하지 않는 원본 콘텐츠에는 대댓글 알림을 만들지 않도록 빈 템플릿을 반환한다
        return null;
    }

    /**
     * 댓글 알림의 동적 이동을 지원하는 원본 콘텐츠 유형인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 댓글이 연결된 원본 콘텐츠 유형
     * @return 독후감 또는 프로필 및 배경사진 여부
     */
    private boolean isReplyTargetType(String tagtType) {
        // 알림 대상과 댓글 API가 함께 지원하는 세 가지 원본 콘텐츠 유형만 허용한다
        return Constant.LIKE_TARGET_REPORT.equals(tagtType)
                || Constant.LIKE_TARGET_PROFILE_IMAGE.equals(tagtType)
                || Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(tagtType);
    }

    /**
     * 독후감 번호에 연결된 댓글과 답글 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 댓글 목록을 조회하는 로그인 사용자 번호
     * @param reptNumb 댓글 목록을 조회할 독후감 번호
     * @param page 조회할 부모 댓글 페이지 번호
     * @return 독후감 댓글과 답글 목록 조회 결과
     */
    @Override
    public ResultData getReplyList(Long userNumb, Long reptNumb, int page) {
        // 로그인 사용자 번호가 없으면 본인 댓글 여부를 판별할 수 없으므로 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 독후감 번호가 없으면 댓글 조회 대상을 특정할 수 없으므로 조회를 중단한다
        if (StringUtil.isEmpty(reptNumb)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 댓글 목록 조회 조건을 담을 객체를 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 요청받은 독후감 번호를 댓글 목록 조회 조건으로 설정한다
        replyDto.setReptNumb(reptNumb);
        replyDto.setTagtType(Constant.LIKE_TARGET_REPORT);
        replyDto.setTagtNumb(reptNumb);
        // 각 댓글의 로그인 사용자 작성 여부를 조회할 사용자 번호를 설정한다
        replyDto.setUserNumb(userNumb);
        // 요청 페이지를 첫 페이지 이상으로 보정한다
        int normalizedPage = Math.max(page, 1);
        // 부모 댓글 페이지 시작 위치를 조회 조건으로 설정한다
        replyDto.setPageOffset((normalizedPage - 1) * REPLY_PAGE_SIZE);
        // 다음 부모 댓글 페이지 판정용 한 건을 추가해 조회한다
        replyDto.setPageLimit(REPLY_PAGE_SIZE + 1);
        // 현재 부모 댓글 페이지와 연결된 답글을 함께 조회한다
        List<ReplyDto> searchedList = replyMapper.getReplyList(replyDto);
        // Mapper가 빈 값을 반환해도 페이지 응답을 유지하도록 빈 목록으로 보정한다
        List<ReplyDto> safeList = StringUtil.isEmpty(searchedList) ? List.of() : searchedList;
        // 조회 순서를 유지하며 부모 댓글 식별값을 중복 없이 수집한다
        Set<Long> parentNumbSet = new LinkedHashSet<>();

        // 각 행의 부모 댓글 번호를 현재 페이지 순서대로 확인한다
        for (ReplyDto reply : safeList) {
            // 부모 댓글은 자기 번호를, 답글은 참조 부모 번호를 페이지 식별값으로 사용한다
            Long parentNumb = StringUtil.isEmpty(reply.getUperNumb()) ? reply.getReplNumb() : reply.getUperNumb();
            // 현재 행이 속한 부모 댓글 번호를 페이지 집합에 추가한다
            parentNumbSet.add(parentNumb);
        }

        // 제한 수보다 부모 댓글이 한 건 더 있으면 다음 페이지가 존재한다
        boolean hasNext = parentNumbSet.size() > REPLY_PAGE_SIZE;
        // 화면에 전달할 부모 댓글 번호만 현재 페이지 크기로 제한한다
        Set<Long> visibleParentNumbSet = new LinkedHashSet<>();
        // 부모 댓글 순서를 유지하며 현재 페이지 크기만 선택한다
        for (Long parentNumb : parentNumbSet) {
            // 화면 페이지 크기에 도달하면 다음 페이지 판정용 부모 댓글을 제외한다
            if (visibleParentNumbSet.size() >= REPLY_PAGE_SIZE) {

                break;
            }

            // 현재 페이지에 포함할 부모 댓글 번호를 추가한다
            visibleParentNumbSet.add(parentNumb);
        }

        // 다음 페이지 판정용 부모 댓글과 연결 답글을 응답에서 제외한다
        List<ReplyDto> visibleList = safeList.stream()
                .filter(reply -> visibleParentNumbSet.contains(
                        StringUtil.isEmpty(reply.getUperNumb()) ? reply.getReplNumb() : reply.getUperNumb()
                ))
                .toList();
        // 부모 댓글 페이지와 각 부모의 답글 및 다음 페이지 여부를 반환한다
        return ResultData.success(new PageDto<>(visibleList, normalizedPage, hasNext));
    }

    /** 범용 대상에 연결된 댓글과 답글 목록을 조회한다. */
    @Override
    public ResultData getReplyList(Long userNumb, String tagtType, Long tagtNumb, int page) {
        return getReplyList(userNumb, tagtType, tagtNumb, null, page);
    }

    /** 범용 대상에서 알림이 지정한 댓글 묶음을 우선한 댓글 목록을 조회한다. */
    @Override
    public ResultData getReplyList(Long userNumb, String tagtType, Long tagtNumb, Long focusReplNumb, int page) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }
        if (StringUtil.isEmpty(tagtNumb) || !isAllowedTargetType(tagtType)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }
        if (!hasImageReplyAccess(userNumb, tagtType, tagtNumb)) {
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }
        ReplyDto replyDto = createTargetRequest(userNumb, tagtType, tagtNumb, null);
        if (!StringUtil.isEmpty(focusReplNumb) && focusReplNumb > 0) {
            replyDto.setFocusReplNumb(focusReplNumb);
        }
        int normalizedPage = Math.max(page, 1);
        replyDto.setPageOffset((normalizedPage - 1) * REPLY_PAGE_SIZE);
        replyDto.setPageLimit(REPLY_PAGE_SIZE + 1);
        List<ReplyDto> searchedList = replyMapper.getReplyList(replyDto);
        List<ReplyDto> safeList = StringUtil.isEmpty(searchedList) ? List.of() : searchedList;
        Set<Long> parentNumbSet = new LinkedHashSet<>();
        for (ReplyDto reply : safeList) {
            parentNumbSet.add(StringUtil.isEmpty(reply.getUperNumb()) ? reply.getReplNumb() : reply.getUperNumb());
        }
        boolean hasNext = parentNumbSet.size() > REPLY_PAGE_SIZE;
        if (hasNext) {
            Long overflowParentNumb = parentNumbSet.stream().skip(REPLY_PAGE_SIZE).findFirst().orElse(null);
            safeList = safeList.stream()
                    .filter(reply -> !java.util.Objects.equals(
                            StringUtil.isEmpty(reply.getUperNumb()) ? reply.getReplNumb() : reply.getUperNumb(),
                            overflowParentNumb))
                    .toList();
        }
        return ResultData.success(new PageDto<>(safeList, normalizedPage, hasNext));
    }

    /** 기존 독후감 요청을 포함한 댓글 대상 식별값을 범용 구조로 정규화한다. */
    private void normalizeReplyTarget(ReplyDto replyDto) {
        if (StringUtil.isEmpty(replyDto)) {
            return;
        }
        if (StringUtil.isEmpty(replyDto.getTagtType()) && !StringUtil.isEmpty(replyDto.getReptNumb())) {
            replyDto.setTagtType(Constant.LIKE_TARGET_REPORT);
        }
        if (StringUtil.isEmpty(replyDto.getTagtNumb()) && !StringUtil.isEmpty(replyDto.getReptNumb())) {
            replyDto.setTagtNumb(replyDto.getReptNumb());
        }
        if (!StringUtil.isEmpty(replyDto.getTagtType())) {
            replyDto.setTagtType(replyDto.getTagtType().trim().toUpperCase());
        }
    }

    /** 범용 댓글 대상 유형이 서비스에서 허용한 값인지 확인한다. */
    private boolean isAllowedTargetType(String tagtType) {
        if (StringUtil.isEmpty(tagtType)) {
            return false;
        }
        String normalizedType = tagtType.trim().toUpperCase();
        return Constant.LIKE_TARGET_REPORT.equals(normalizedType)
                || Constant.LIKE_TARGET_PROFILE_IMAGE.equals(normalizedType)
                || Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(normalizedType);
    }

    /** 사진 댓글 대상에 대해 소유자 또는 현재 팔로워 접근 여부를 확인한다. */
    private boolean hasImageReplyAccess(Long userNumb, String tagtType, Long tagtNumb) {
        String normalizedTargetType = StringUtil.isEmpty(tagtType) ? null : tagtType.trim().toUpperCase();
        boolean imageTarget = Constant.LIKE_TARGET_PROFILE_IMAGE.equals(normalizedTargetType)
                || Constant.LIKE_TARGET_BACKGROUND_IMAGE.equals(normalizedTargetType);
        if (!imageTarget) {
            return true;
        }
        ReplyDto accessRequest = createTargetRequest(userNumb, normalizedTargetType, tagtNumb, null);
        return !StringUtil.isEmpty(accessRequest) && replyMapper.getReplyTargetAccessCount(accessRequest) > 0;
    }

    /** 범용 댓글 Mapper 요청 객체를 생성한다. */
    private ReplyDto createTargetRequest(Long userNumb, String tagtType, Long tagtNumb, Long replNumb) {
        if (StringUtil.hasEmpty(userNumb, tagtNumb) || !isAllowedTargetType(tagtType)
                || userNumb <= 0 || tagtNumb <= 0 || (!StringUtil.isEmpty(replNumb) && replNumb <= 0)) {
            return null;
        }
        ReplyDto replyDto = new ReplyDto();
        replyDto.setUserNumb(userNumb);
        replyDto.setTagtType(tagtType.trim().toUpperCase());
        replyDto.setTagtNumb(tagtNumb);
        replyDto.setReptNumb(Constant.LIKE_TARGET_REPORT.equals(replyDto.getTagtType()) ? tagtNumb : null);
        replyDto.setReplNumb(replNumb);
        return replyDto;
    }
}
