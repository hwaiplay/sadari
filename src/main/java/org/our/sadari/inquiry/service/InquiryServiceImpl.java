package org.our.sadari.inquiry.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.inquiry.dto.InquiryAnswerDto;
import org.our.sadari.inquiry.dto.InquiryCreateDto;
import org.our.sadari.inquiry.dto.InquiryDto;
import org.our.sadari.inquiry.dto.InquiryPageDto;
import org.our.sadari.inquiry.mapper.InquiryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : InquiryServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 정상 또는 이용정지 사용자의 고객문의를 안전하게 접수하고 조회한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    // 사용자 화면의 페이지당 고객문의 개수
    private static final int PAGE_SIZE = 20;
    // 이용정지 이의제기 카테고리 코드
    private static final String SUSPENSION_APPEAL = "SUSPENSION_APPEAL";
    // 고객문의 데이터 접근 객체
    private final InquiryMapper inquiryMapper;

    @Override
    public ResultData getInquiryList(Long userNumb, int page) {

        if (!isInquiryUser(userNumb)) {
            // 고객문의 접근이 허용되지 않은 계정 상태 안내를 반환한다
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }
        int normalizedPage = Math.max(page, 1);
        List<InquiryDto> inquiries = inquiryMapper.getInquiryList(
                userNumb, (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE + 1
        );
        boolean hasNext = inquiries.size() > PAGE_SIZE;
        List<InquiryDto> currentPage = hasNext ? inquiries.subList(0, PAGE_SIZE) : inquiries;
        // 현재 페이지 문의 목록과 다음 페이지 여부를 반환한다
        return ResultData.success(new InquiryPageDto(currentPage, normalizedPage, hasNext));
    }

    @Override
    @Transactional
    public ResultData getInquiryDtl(Long userNumb, Long inqrNumb) {

        if (!isInquiryUser(userNumb)) {
            // 고객문의 접근이 허용되지 않은 계정 상태 안내를 반환한다
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }
        if (StringUtil.isEmpty(inqrNumb) || inqrNumb < 1) {
            // 유효하지 않은 고객문의 번호 안내를 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        InquiryDto inquiry = inquiryMapper.getInquiryDtl(userNumb, inqrNumb);
        if (StringUtil.isEmpty(inquiry)) {
            // 본인이 작성한 고객문의를 찾지 못한 안내를 반환한다
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }
        List<InquiryAnswerDto> answers = inquiryMapper.getInquiryAnswerList(inqrNumb);
        inquiry.setAnswers(answers);
        inquiryMapper.uptInquiryAnswerRead(userNumb, inqrNumb);
        if (!StringUtil.isEmpty(answers)) {
            answers.forEach(answer -> answer.setReadYsno(Constant.COMM_YES));
        }
        // 문의 본문과 읽음 처리된 관리자 답변 목록을 반환한다
        return ResultData.success(inquiry);
    }

    @Override
    @Transactional
    public ResultData setInquiry(Long userNumb, InquiryCreateDto inquiryCreateDto) {

        if (!isInquiryUser(userNumb)) {
            // 고객문의 접근이 허용되지 않은 계정 상태 안내를 반환한다
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }
        if (!isValidInquiry(inquiryCreateDto)) {
            // 필수 고객문의 입력값 오류 안내를 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        if (inquiryMapper.getInquiryCategoryCnt(inquiryCreateDto.getInqrCatg()) != 1) {
            // 사용 중이지 않은 고객문의 카테고리 안내를 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        InquiryDto inquiry = new InquiryDto();
        inquiry.setInqrCatg(inquiryCreateDto.getInqrCatg().trim());
        inquiry.setInqrTitl(inquiryCreateDto.getInqrTitl().trim());
        inquiry.setInqrCntn(inquiryCreateDto.getInqrCntn().trim());
        if (SUSPENSION_APPEAL.equals(inquiry.getInqrCatg())) {
            Long spndNumb = inquiryMapper.getLatestSuspensionNumb(userNumb);
            if (StringUtil.isEmpty(spndNumb)) {
                // 연결할 이용정지 이력이 없는 이의제기 안내를 반환한다
                return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
            }
            if (inquiryMapper.getOpenSuspensionInquiryCnt(userNumb, spndNumb) > 0) {
                // 동일 이용정지에 이미 처리 중인 이의제기가 있다는 안내를 반환한다
                return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
            }
            inquiry.setSpndNumb(spndNumb);
        }
        inquiryMapper.setInquiry(inquiry, userNumb);
        // 새로 접수된 고객문의 번호를 반환한다
        return ResultData.success(inquiry.getInqrNumb());
    }

    /**
     * 고객문의 기능을 이용할 수 있는 현재 계정 상태인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 사용자 번호
     * @return 정상 또는 이용정지 회원이면 true
     */
    private boolean isInquiryUser(Long userNumb) {

        if (StringUtil.isEmpty(userNumb)) {
            // 인증 사용자가 없으면 고객문의 접근을 허용하지 않는다
            return false;
        }
        String userStat = inquiryMapper.getUserStat(userNumb);
        // 정상 회원과 이용정지 회원만 1차 고객문의에 접근하도록 상태를 반환한다
        return Constant.USER_STAT_ACTIVE.equals(userStat)
                || Constant.USER_STAT_SUSPENDED.equals(userStat);
    }

    /**
     * 고객문의 등록에 필요한 문자열과 길이를 검증한다
     *
     * @author SeungHyeon.Kang
     * @param inquiryCreateDto 검증할 고객문의 등록값
     * @return 등록 가능한 값이면 true
     */
    private boolean isValidInquiry(InquiryCreateDto inquiryCreateDto) {

        if (StringUtil.isEmpty(inquiryCreateDto)
                || StringUtil.hasEmpty(inquiryCreateDto.getInqrCatg(), inquiryCreateDto.getInqrTitl()
                , inquiryCreateDto.getInqrCntn())) {
            // 필수 입력값이 없으면 등록을 허용하지 않는다
            return false;
        }
        String title = inquiryCreateDto.getInqrTitl().trim();
        String content = inquiryCreateDto.getInqrCntn().trim();
        // DB 저장 길이와 일치하는 제목 및 본문 길이 검증 결과를 반환한다
        return !title.isEmpty() && title.length() <= 200
                && !content.isEmpty() && content.getBytes(StandardCharsets.UTF_8).length <= 4000;
    }
}
