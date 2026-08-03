package org.our.sadari.reply.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.reply.dto.ReplyDto;
import org.our.sadari.reply.mapper.ReplyMapper;

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
    void setReplySendsReplyReportAlimToReportWriter() {

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
     * 본인 댓글의 공백을 정규화한 내용을 댓글 식별값과 함께 수정하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void uptReplyUpdatesOwnedReplyWithNormalizedContent() {

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
     * 본인 댓글을 자식 답글 구조가 보존되는 논리 삭제 Mapper로 전달하는지 검증한다.
     *
     * @author HanWon.Jang
     */
    @Test
    void delReplyMarksOwnedReplyAsDeleted() {

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
}
