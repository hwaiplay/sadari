package org.our.sadari.reply.service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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

        // 독후감 번호와 댓글 내용이 없으면 등록 대상과 저장 내용을 확정할 수 없으므로 요청을 거부한다
        if (StringUtil.isEmpty(replyDto) || StringUtil.isEmpty(replyDto.getReptNumb())
                || StringUtil.isEmpty(replyDto.getReplCntn())) {
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
        sendReplyReportAlim(userNumb, replyDto);

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
            // 댓글 또는 대댓글 작성자에게 전용 템플릿 알림과 푸시를 발송한다
            sendReplyLikeAlim(userNumb, likeTarget);
        }

        // 등록 직후 서버 기준 좋아요 수와 현재 사용자 상태를 조회해 반환한다
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
        // 댓글 번호를 범용 좋아요 대상 번호로 설정한다
        replyDto.setReplNumb(replNumb);
        // 검증된 댓글 좋아요 요청 객체를 반환한다
        return replyDto;
    }

    /**
     * 신규 댓글 또는 대댓글 좋아요를 해당 댓글 작성자에게 알림으로 발송한다.
     * 본인 댓글 좋아요와 작성자를 확인할 수 없는 댓글은 알림을 만들지 않는다.
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

        // 대상 검증 SQL에서 함께 조회한 활성 좋아요 등록자의 최신 닉네임을 사용한다
        String sendUserNick = likeTarget.getUserNick();

        // 회원 원본에서 닉네임을 확인할 수 없으면 미완성 문구의 알림을 저장하지 않는다
        if (StringUtil.isEmpty(sendUserNick)) {
            // 발신자 닉네임이 없는 댓글 좋아요 알림 처리 없이 호출부로 반환한다
            return;
        }

        // REPLY_LIKE 템플릿의 사용자명 치환값을 담을 객체를 생성한다
        Map<String, Object> replaceMap = new HashMap<>();
        // 좋아요 등록자의 닉네임을 템플릿 사용자명 치환값으로 설정한다
        replaceMap.put("userName", sendUserNick);

        // 댓글 작성자에게 좋아요 알림을 저장하고 해당 댓글이 속한 독후감 상세 링크로 푸시를 예약한다
        alimService.sendAlim(
                likeTarget.getTargetUserNumb()
              , Constant.ALIM_SITU_LIKE
              , Constant.ALIM_TEMP_CODE_REPLY_LIKE
              , likeTarget.getReptNumb()
              , replaceMap
        );
    }

    /**
     * 독후감 작성자에게 댓글 작성자의 닉네임과 독후감 이동 링크가 포함된 알림을 발송한다.
     * 작성자가 자기 독후감에 직접 등록한 댓글은 자기 자신에게 알림을 만들지 않는다.
     *
     * @author HanWon.Jang
     * @param sendUserNumb 댓글을 등록한 사용자 번호
     * @param replyDto 등록된 댓글과 독후감 번호
     */
    private void sendReplyReportAlim(Long sendUserNumb, ReplyDto replyDto) {
        // 댓글이 등록된 독후감의 작성자와 댓글 알림 설정을 조회한다
        ReplyDto reportAlim = replyMapper.getReplyReportAlimDtl(replyDto.getReptNumb());

        // 독후감 작성자를 확인할 수 없거나 작성자가 직접 댓글을 등록했으면 알림을 만들지 않는다
        if (StringUtil.isEmpty(reportAlim) || StringUtil.isEmpty(reportAlim.getTargetUserNumb())
                || reportAlim.getTargetUserNumb().equals(sendUserNumb)) {
            // 독후감 댓글 등록 알림 처리 없이 호출부로 반환한다
            return;
        }

        // 독후감 작성자가 댓글 알림을 껐으면 알림 저장과 푸시 예약을 모두 생략한다
        if (!Constant.COMM_YES.equals(reportAlim.getReplyAlimYsno())) {
            // 독후감 댓글 등록 알림 처리 없이 호출부로 반환한다
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

        // 독후감 작성자에게 댓글 알림을 저장하고 독후감 상세 화면 링크를 포함한 푸시를 예약한다
        alimService.sendAlim(
                reportAlim.getTargetUserNumb()
              , Constant.ALIM_SITU_REPLY
              , Constant.ALIM_TEMP_CODE_REPLY_REPORT
              , replyDto.getReptNumb()
              , replaceMap
        );
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
}
