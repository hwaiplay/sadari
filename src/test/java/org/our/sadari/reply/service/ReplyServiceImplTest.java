package org.our.sadari.reply.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.reply.dto.ReplyDto;
import org.our.sadari.reply.mapper.ReplyMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : ReplyServiceImplTest
 * author         : Hanwon.Jang
 * date           : 2026-07-31
 * description    : 댓글 등록 알림과 본인 댓글 수정 및 삭제 처리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-31        Hanwon.Jang        최초 생성
 * 2026-08-03        HanWon.Jang        본인 댓글 수정 및 삭제 서비스 검증 추가
 * 2026-08-03        HanWon.Jang        댓글 등록 및 수정 비속어 차단 검증 추가
 * 2026-08-03        HanWon.Jang        댓글 좋아요 등록, 취소 및 접근 제한 검증 추가
 * 2026-08-04        HanWon.Jang        댓글 및 대댓글 좋아요 알림 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class ReplyServiceImplTest {

    // 댓글 데이터 접근 객체
    @Mock
    private ReplyMapper replyMapper;

    // 댓글 비속어 검사 서비스
    @Mock
    private BadWordDetectionService badWordDetectionService;

    // 사용자별 알림 저장과 푸시 발송 서비스
    @Mock
    private AlimService alimService;

    // 로그인 사용자 닉네임 조회 서비스
    @Mock
    private TokenRedisService tokenRedisService;

    // 댓글 등록과 수정 및 삭제 단위 테스트 대상
    private ReplyServiceImpl replyService;

    /**
     * 댓글 등록 서비스의 의존 객체를 연결한다.
     *
     * @author Hanwon.Jang
     */
    @BeforeEach
    void setUp() {

        // 비속어 차단 응답의 다국어 메시지와 치환값을 실제 프로퍼티 기준으로 검증할 메시지 소스를 생성한다
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 서버 공통 메시지 프로퍼티를 테스트 메시지 조회 기준으로 설정한다
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 프로퍼티 파일 인코딩을 설정한다
        messageSource.setDefaultEncoding("UTF-8");
        // ResultData 실패 응답이 실제 공통 메시지 소스를 사용하도록 정적 조회 객체를 초기화한다
        new MessageUtils().setMessageSource(messageSource);

        // 댓글 등록 단위 테스트 대상을 생성한다
        replyService = new ReplyServiceImpl(
                replyMapper
              , badWordDetectionService
              , alimService
              , tokenRedisService
        );
    }

    /**
     * 댓글을 저장한 뒤 독후감 작성자에게 REPLY_REPORT 템플릿 알림을 전송하는지 검증한다.
     *
     * @author Hanwon.Jang
     */
    @Test
    void setReplySendsAlim() {

        // 등록할 댓글 요청을 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 독후감 번호를 댓글 요청에 설정한다
        replyDto.setReptNumb(157L);
        // 생성될 댓글 번호를 댓글 요청에 설정한다
        replyDto.setReplNumb(8L);
        // 댓글 내용을 댓글 요청에 설정한다
        replyDto.setReplCntn("좋은 글 잘 읽었습니다.");

        // 댓글 내용에서 비속어가 검출되지 않는 조건을 구성한다
        when(badWordDetectionService.findBadWord("좋은 글 잘 읽었습니다."))
                .thenReturn(Optional.empty());
        // 댓글 한 건이 저장되는 조건을 구성한다
        when(replyMapper.setReply(replyDto)).thenReturn(1);
        // 독후감 작성자 사용자 번호가 조회되는 조건을 구성한다
        when(replyMapper.getReplyReportUserNumb(157L)).thenReturn(31L);
        // 댓글 작성자의 닉네임이 조회되는 조건을 구성한다
        when(tokenRedisService.getUserNick(44L)).thenReturn("댓글작성자");
        // 알림 저장이 정상 처리되는 조건을 구성한다
        when(alimService.sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_REPLY)
              , eq("REPLY_REPORT")
              , eq(157L)
              , any()
        )).thenReturn(ResultData.success());

        // 댓글을 등록한다
        ResultData result = replyService.setReply(44L, replyDto);

        // 댓글 등록 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 등록된 댓글 번호를 확인한다
        assertEquals(8L, result.getData());
        // 댓글 알림 템플릿 상수가 등록된 템플릿 코드와 일치하는지 확인한다
        assertEquals("REPLY_REPORT", Constant.ALIM_TEMP_CODE_REPLY_REPORT);

        @SuppressWarnings("unchecked")
        // 알림 문구 치환값을 확인할 캡처 객체를 생성한다
        ArgumentCaptor<Map<String, Object>> replaceMapCaptor = ArgumentCaptor.forClass(Map.class);
        // 독후감 작성자에게 댓글 등록 알림이 전송되는지 확인한다
        verify(alimService).sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_REPLY)
              , eq(Constant.ALIM_TEMP_CODE_REPLY_REPORT)
              , eq(157L)
              , replaceMapCaptor.capture()
        );
        // 알림 문구에 댓글 작성자 닉네임이 전달되는지 확인한다
        assertEquals("댓글작성자", replaceMapCaptor.getValue().get("userName"));
    }

    /**
     * 댓글 등록 내용에서 비속어가 탐지되면 데이터 저장과 알림 발송을 중단하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setReplyRejectsBadWord() {

        // 비속어 검증 대상 댓글 요청을 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 독후감 번호를 댓글 요청에 설정한다
        replyDto.setReptNumb(157L);
        // 비속어 우회 표현이 포함된 내용을 댓글 요청에 설정한다
        replyDto.setReplCntn("시이이이발");

        // 공통 비속어 필터가 우회 표현에서 비속어를 탐지하는 조건을 구성한다
        when(badWordDetectionService.findBadWord("시이이이발"))
                .thenReturn(Optional.of("시발"));

        // 비속어가 포함된 댓글 등록을 요청한다
        ResultData result = replyService.setReply(44L, replyDto);

        // 비속어 포함 공통 실패 코드를 확인한다
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 차단 응답에 탐지된 비속어가 사용자 메시지 치환값으로 전달되는지 확인한다
        assertTrue(result.getMessage().contains("시발"));
        // 비속어가 탐지된 댓글은 DB 저장과 알림 처리까지 진행되지 않는지 확인한다
        verifyNoInteractions(replyMapper, alimService, tokenRedisService);
    }

    /**
     * 본인 댓글의 공백을 정규화한 내용을 댓글 식별값과 함께 수정하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void uptReplyNormalizesContent() {

        // 수정할 댓글 내용을 담은 요청을 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 앞뒤 공백이 포함된 변경 내용을 댓글 요청에 설정한다
        replyDto.setReplCntn("  수정한 댓글입니다.  ");

        // 정규화한 댓글 내용에서 비속어가 검출되지 않는 조건을 구성한다
        when(badWordDetectionService.findBadWord("수정한 댓글입니다."))
                .thenReturn(Optional.empty());
        // 본인 댓글 한 건이 수정되는 조건을 구성한다
        when(replyMapper.uptReply(replyDto)).thenReturn(1);

        // 로그인 사용자가 작성한 댓글을 수정한다
        ResultData result = replyService.uptReply(44L, 157L, 8L, replyDto);

        // 댓글 수정 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 수정된 댓글 번호를 확인한다
        assertEquals(8L, result.getData());
        // URL에서 전달한 독후감 번호가 수정 조건에 설정됐는지 확인한다
        assertEquals(157L, replyDto.getReptNumb());
        // URL에서 전달한 댓글 번호가 수정 조건에 설정됐는지 확인한다
        assertEquals(8L, replyDto.getReplNumb());
        // 인증 사용자 번호가 소유자 조건에 설정됐는지 확인한다
        assertEquals(44L, replyDto.getUserNumb());
        // 정규화한 댓글 내용이 변경값으로 설정됐는지 확인한다
        assertEquals("수정한 댓글입니다.", replyDto.getReplCntn());
        // 작성자와 미삭제 및 정상 이용 계정 조건을 포함하는 Mapper가 호출됐는지 확인한다
        verify(replyMapper).uptReply(replyDto);
    }

    /**
     * 댓글 수정 내용에서 비속어가 탐지되면 기존 댓글의 DB 변경을 중단하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void uptReplyRejectsBadWord() {

        // 비속어 검증 대상 댓글 수정 요청을 생성한다
        ReplyDto replyDto = new ReplyDto();
        // 특수문자로 우회한 비속어가 포함된 변경 내용을 설정한다
        replyDto.setReplCntn("시*발");

        // 공통 비속어 필터가 특수문자 우회 표현에서 비속어를 탐지하는 조건을 구성한다
        when(badWordDetectionService.findBadWord("시*발"))
                .thenReturn(Optional.of("시발"));

        // 비속어가 포함된 본인 댓글 수정을 요청한다
        ResultData result = replyService.uptReply(44L, 157L, 8L, replyDto);

        // 비속어 포함 공통 실패 코드를 확인한다
        assertEquals(ResultEnum.COMMON_BAD_WORD_INCLUDED.getCode(), result.getCode());
        // 차단 응답에 탐지된 비속어가 사용자 메시지 치환값으로 전달되는지 확인한다
        assertTrue(result.getMessage().contains("시발"));
        // 비속어가 탐지된 댓글은 DB 수정까지 진행되지 않는지 확인한다
        verifyNoInteractions(replyMapper, alimService, tokenRedisService);
    }

    /**
     * 본인 댓글을 자식 답글 구조가 보존되는 논리 삭제 Mapper로 전달하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void delReplyMarksOwnedDeleted() {

        // 댓글 삭제 조건을 검증할 캡처 객체를 생성한다
        ArgumentCaptor<ReplyDto> replyDtoCaptor = ArgumentCaptor.forClass(ReplyDto.class);
        // 정상 이용 중인 본인 댓글 한 건이 삭제 상태로 전환되는 조건을 구성한다
        when(replyMapper.delReply(any(ReplyDto.class))).thenReturn(1);

        // 로그인 사용자가 작성한 댓글을 삭제 상태로 전환한다
        ResultData result = replyService.delReply(44L, 157L, 8L);

        // 댓글 삭제 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 삭제된 댓글 번호를 확인한다
        assertEquals(8L, result.getData());
        // Mapper에 전달된 복합 식별값과 작성자 조건을 확인한다
        verify(replyMapper).delReply(replyDtoCaptor.capture());
        // 삭제할 댓글의 독후감 번호가 조건에 설정됐는지 확인한다
        assertEquals(157L, replyDtoCaptor.getValue().getReptNumb());
        // 삭제할 댓글 번호가 조건에 설정됐는지 확인한다
        assertEquals(8L, replyDtoCaptor.getValue().getReplNumb());
        // 인증 사용자 번호가 소유자 조건에 설정됐는지 확인한다
        assertEquals(44L, replyDtoCaptor.getValue().getUserNumb());
    }

    /**
     * 정상 이용 사용자가 미삭제 댓글에 좋아요를 등록하면 작성자 알림과 최신 집계 상태를 반환하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setReplyLikeReturnsDtl() {

        // 좋아요 등록 요청 조건을 검증할 캡처 객체를 생성한다
        ArgumentCaptor<ReplyDto> replyDtoCaptor = ArgumentCaptor.forClass(ReplyDto.class);
        // 댓글 좋아요 알림을 받을 댓글 작성자 정보를 생성한다
        ReplyDto likeTarget = new ReplyDto();
        // 알림 링크에 사용할 독후감 번호를 설정한다
        likeTarget.setReptNumb(157L);
        // 좋아요 대상 댓글 번호를 설정한다
        likeTarget.setReplNumb(8L);
        // 좋아요 알림을 받을 댓글 작성자 번호를 설정한다
        likeTarget.setTargetUserNumb(31L);
        // 정상 이용 사용자가 접근할 수 있는 미삭제 댓글과 작성자 조건을 구성한다
        when(replyMapper.getReplyLikeTarget(any(ReplyDto.class))).thenReturn(likeTarget);
        // 신규 댓글 좋아요 한 건이 등록되는 조건을 구성한다
        when(replyMapper.setReplyLike(any(ReplyDto.class))).thenReturn(1);
        // 댓글 좋아요 등록자의 최신 닉네임이 조회되는 조건을 구성한다
        when(tokenRedisService.getUserNick(44L)).thenReturn("좋아요사용자");
        // 댓글 좋아요 등록 후 반환할 최신 상태를 생성한다
        ReplyDto likeDetail = new ReplyDto();
        // 좋아요 등록 후 집계 수를 설정한다
        likeDetail.setLikeCnt(3L);
        // 현재 사용자의 좋아요 상태를 설정한다
        likeDetail.setLikeYsno(Constant.COMM_YES);
        // 등록 후 최신 댓글 좋아요 상태가 조회되는 조건을 구성한다
        when(replyMapper.getReplyLikeDtl(any(ReplyDto.class))).thenReturn(likeDetail);

        // 로그인 사용자의 댓글 좋아요를 등록한다
        ResultData result = replyService.setReplyLike(44L, 157L, 8L);

        // 댓글 좋아요 등록 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 댓글 좋아요 전용 템플릿 코드가 확정된 REPLY_LIKE 값인지 확인한다
        assertEquals("REPLY_LIKE", Constant.ALIM_TEMP_CODE_REPLY_LIKE);
        // 댓글 좋아요가 등록 Mapper에 전달됐는지 확인한다
        verify(replyMapper).setReplyLike(replyDtoCaptor.capture());
        // 좋아요 주체 사용자 번호를 확인한다
        assertEquals(44L, replyDtoCaptor.getValue().getUserNumb());
        // 좋아요 대상 독후감 번호를 확인한다
        assertEquals(157L, replyDtoCaptor.getValue().getReptNumb());
        // 좋아요 대상 댓글 번호를 확인한다
        assertEquals(8L, replyDtoCaptor.getValue().getReplNumb());
        @SuppressWarnings("unchecked")
        // 댓글 좋아요 알림의 템플릿 치환값을 확인할 캡처 객체를 생성한다
        ArgumentCaptor<Map<String, Object>> replaceMapCaptor = ArgumentCaptor.forClass(Map.class);
        // 해당 댓글 작성자에게 REPLY_LIKE 템플릿으로 알림이 전송되는지 확인한다
        verify(alimService).sendAlim(
                eq(31L)
              , eq(Constant.ALIM_SITU_LIKE)
              , eq(Constant.ALIM_TEMP_CODE_REPLY_LIKE)
              , eq(157L)
              , replaceMapCaptor.capture()
        );
        // 알림 문구에 좋아요 등록자 닉네임이 전달되는지 확인한다
        assertEquals("좋아요사용자", replaceMapCaptor.getValue().get("userName"));
        // 서버가 조회한 최신 좋아요 상세 응답을 확인한다
        assertEquals(likeDetail, result.getData());
    }

    /**
     * 정상 이용 사용자가 미삭제 댓글의 좋아요를 취소하면 최신 집계 상태를 반환하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void delReplyLikeReturnsDtl() {

        // 정상 이용 사용자가 접근할 수 있는 미삭제 댓글 조건을 생성한다
        ReplyDto likeTarget = new ReplyDto();
        // 좋아요 취소 대상 댓글 작성자 번호를 설정한다
        likeTarget.setTargetUserNumb(31L);
        // 정상 이용 사용자가 접근할 수 있는 미삭제 댓글과 작성자 조건을 구성한다
        when(replyMapper.getReplyLikeTarget(any(ReplyDto.class))).thenReturn(likeTarget);
        // 댓글 좋아요 취소 후 반환할 최신 상태를 생성한다
        ReplyDto likeDetail = new ReplyDto();
        // 좋아요 취소 후 집계 수를 설정한다
        likeDetail.setLikeCnt(2L);
        // 현재 사용자의 좋아요 취소 상태를 설정한다
        likeDetail.setLikeYsno(Constant.COMM_NO);
        // 취소 후 최신 댓글 좋아요 상태가 조회되는 조건을 구성한다
        when(replyMapper.getReplyLikeDtl(any(ReplyDto.class))).thenReturn(likeDetail);

        // 로그인 사용자의 댓글 좋아요를 취소한다
        ResultData result = replyService.delReplyLike(44L, 157L, 8L);

        // 댓글 좋아요 취소 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 현재 사용자의 댓글 좋아요가 삭제 Mapper에 전달됐는지 확인한다
        verify(replyMapper).delReplyLike(any(ReplyDto.class));
        // 좋아요 취소는 기존 알림 삭제나 신규 푸시를 만들지 않는지 확인한다
        verifyNoInteractions(alimService, tokenRedisService);
        // 서버가 조회한 최신 좋아요 상세 응답을 확인한다
        assertEquals(likeDetail, result.getData());
    }

    /**
     * 비활성 계정 또는 삭제 댓글처럼 접근할 수 없는 대상에는 좋아요 변경을 차단하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setReplyLikeRejectsTarget() {

        // 정상 이용 사용자와 미삭제 댓글 조건을 만족하지 않는 요청을 구성한다
        when(replyMapper.getReplyLikeTarget(any(ReplyDto.class))).thenReturn(null);

        // 접근할 수 없는 댓글에 좋아요 등록을 요청한다
        ResultData result = replyService.setReplyLike(44L, 157L, 8L);

        // 접근 거부 공통 실패 코드를 확인한다
        assertEquals(ResultEnum.COMMON_ACCESS_REJECTED.getCode(), result.getCode());
        // 대상 검증 이후 좋아요 등록이나 상세 조회가 호출되지 않는지 확인한다
        verify(replyMapper).getReplyLikeTarget(any(ReplyDto.class));
    }

    /**
     * 이미 등록된 댓글 좋아요의 멱등 요청에는 알림을 다시 생성하지 않는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setReplyLikeNoRenotify() {

        // 이미 좋아요가 등록된 미삭제 댓글과 작성자 정보를 생성한다
        ReplyDto likeTarget = new ReplyDto();
        // 기존 좋아요 대상 댓글 작성자 번호를 설정한다
        likeTarget.setTargetUserNumb(31L);
        // 정상 이용 사용자가 접근할 수 있는 미삭제 댓글과 작성자 조건을 구성한다
        when(replyMapper.getReplyLikeTarget(any(ReplyDto.class))).thenReturn(likeTarget);
        // 중복 좋아요가 INSERT IGNORE에 의해 추가되지 않는 조건을 구성한다
        when(replyMapper.setReplyLike(any(ReplyDto.class))).thenReturn(0);
        // 멱등 요청 후 반환할 기존 좋아요 상태를 생성한다
        ReplyDto likeDetail = new ReplyDto();
        // 기존 좋아요 상태를 설정한다
        likeDetail.setLikeYsno(Constant.COMM_YES);
        // 기존 좋아요 상세가 조회되는 조건을 구성한다
        when(replyMapper.getReplyLikeDtl(any(ReplyDto.class))).thenReturn(likeDetail);

        // 이미 좋아요한 댓글에 같은 등록 요청을 다시 전달한다
        ResultData result = replyService.setReplyLike(44L, 157L, 8L);

        // 멱등 좋아요 등록 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 신규 좋아요가 아니면 닉네임 조회와 알림 발송을 하지 않는지 확인한다
        verifyNoInteractions(alimService, tokenRedisService);
    }

    /**
     * 본인이 작성한 대댓글에 좋아요를 등록해도 자기 알림을 생성하지 않는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void setReplyLikeNoSelfAlim() {

        // 본인이 작성한 대댓글 좋아요 대상 정보를 생성한다
        ReplyDto likeTarget = new ReplyDto();
        // 알림 링크에 사용할 독후감 번호를 설정한다
        likeTarget.setReptNumb(157L);
        // 좋아요 대상 대댓글 번호를 설정한다
        likeTarget.setReplNumb(9L);
        // 좋아요 등록자와 같은 대댓글 작성자 번호를 설정한다
        likeTarget.setTargetUserNumb(44L);
        // 정상 이용 사용자가 접근할 수 있는 본인 대댓글 조건을 구성한다
        when(replyMapper.getReplyLikeTarget(any(ReplyDto.class))).thenReturn(likeTarget);
        // 본인 대댓글 좋아요 한 건이 등록되는 조건을 구성한다
        when(replyMapper.setReplyLike(any(ReplyDto.class))).thenReturn(1);
        // 본인 좋아요 등록 후 반환할 상세 상태를 생성한다
        ReplyDto likeDetail = new ReplyDto();
        // 본인 좋아요 상태를 설정한다
        likeDetail.setLikeYsno(Constant.COMM_YES);
        // 등록 후 좋아요 상세가 조회되는 조건을 구성한다
        when(replyMapper.getReplyLikeDtl(any(ReplyDto.class))).thenReturn(likeDetail);

        // 본인이 작성한 대댓글에 좋아요를 등록한다
        ResultData result = replyService.setReplyLike(44L, 157L, 9L);

        // 본인 대댓글 좋아요 등록 성공 응답을 확인한다
        assertEquals(200, result.getCode());
        // 본인 댓글 좋아요에는 닉네임 조회와 알림 발송을 하지 않는지 확인한다
        verifyNoInteractions(alimService, tokenRedisService);
    }
}
