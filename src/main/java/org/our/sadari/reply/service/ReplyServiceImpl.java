package org.our.sadari.reply.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
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
 * author         : Hanwon.Jang
 * date           : 2026-07-28
 * description    : 댓글과 답글의 조회 및 등록 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        Hanwon.Jang        최초 생성
 * 2026-07-28        Hanwon.Jang        댓글 조회 및 등록 구현
 * 2026-07-29        HanWon.Jang        댓글 등록 시 독후감 작성자 알림 발송
 * 2026-07-29        HanWon.Jang        로그인 사용자 작성 댓글 여부 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyServiceImpl implements ReplyService {

    // 댓글 내용의 Oracle VARCHAR2 저장 한도
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

        // 정규화 후 내용이 없거나 Oracle 저장 한도를 넘으면 DB 오류가 발생하기 전에 요청을 거부한다
        if (StringUtil.isEmpty(normalizedContent) || XssUtil.utf8ByteLength(normalizedContent) > REPLY_CONTENT_MAX_BYTES) {
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
            // "입력한 내용에 사용할 수 없는 단어가 포함되어 있어요.\n확인 후 다시 입력해주세요.\n\n확인된 단어: {0}"
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
     * 독후감 작성자에게 댓글 작성자의 닉네임과 독후감 이동 링크가 포함된 알림을 발송한다.
     * 작성자가 자기 독후감에 직접 등록한 댓글은 자기 자신에게 알림을 만들지 않는다.
     *
     * @author HanWon.Jang
     * @param sendUserNumb 댓글을 등록한 사용자 번호
     * @param replyDto 등록된 댓글과 독후감 번호
     */
    private void sendReplyReportAlim(Long sendUserNumb, ReplyDto replyDto) {
        // 댓글이 등록된 독후감의 작성자를 알림 수신자로 조회한다
        Long reportUserNumb = replyMapper.getReplyReportUserNumb(replyDto.getReptNumb());

        // 독후감 작성자를 확인할 수 없거나 작성자가 직접 댓글을 등록했으면 알림을 만들지 않는다
        if (StringUtil.isEmpty(reportUserNumb) || reportUserNumb.equals(sendUserNumb)) {
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
                reportUserNumb
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
     * @return 독후감 댓글과 답글 목록 조회 결과
     */
    @Override
    public ResultData getReplyList(Long userNumb, Long reptNumb) {
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

        // 독후감 번호에 연결된 댓글과 답글 목록을 성공 응답으로 반환한다
        return ResultData.success(replyMapper.getReplyList(replyDto));
    }
}
