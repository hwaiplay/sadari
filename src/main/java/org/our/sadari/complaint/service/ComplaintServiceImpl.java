package org.our.sadari.complaint.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ComplaintServiceImpl
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 독후감과 댓글 신고의 권한 및 중복 여부를 검증하여 접수한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    // 신고 데이터 접근 객체
    private final ComplaintMapper complaintMapper;
    // 신고 상세 내용 비속어 검사 서비스
    private final BadWordDetectionService badWordDetectionService;

    /**
     * {@inheritDoc}
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param request 신고 대상과 사유 입력값
     * @return 접수된 신고 번호 응답
     */
    @Override
    @Transactional
    public ResultData setComplaint(Long userNumb, ComplaintCreateDto request) {

        // 필수 신고값이 없으면 대상 조회와 저장을 수행하지 않는다
        if (!isValidRequest(userNumb, request)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 앞뒤 공백이 신고 코드 검증에 영향을 주지 않도록 정규화한다
        String tagtType = request.getTagtType().trim();
        // 신고 사유 공통코드 비교를 위해 앞뒤 공백을 제거한다
        String cmplRson = request.getCmplRson().trim();
        // 공백만 입력된 상세 사유는 저장하지 않도록 null로 정규화한다
        String cmplCntn = StringUtil.isEmpty(request.getCmplCntn()) ? null : request.getCmplCntn().trim();

        // 활성 회원 행을 잠가 동일 사용자의 동시 중복 신고가 함께 저장되지 않게 한다
        if (StringUtil.isEmpty(complaintMapper.getActiveUserNumbForUpdate(userNumb))) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 이번 출시 범위인 독후감과 댓글 이외의 대상 코드는 허용하지 않는다
        if (!Constant.COMPLAINT_TARGET_REPORT.equals(tagtType)
                && !Constant.COMPLAINT_TARGET_REPLY.equals(tagtType)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 관리자에서 사용 중인 신고 사유 공통코드만 접수한다
        if (complaintMapper.getComplaintReasonCnt(cmplRson) != 1) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 기타 사유는 관리자가 판단할 수 있는 상세 설명이 반드시 있어야 한다
        if (Constant.COMPLAINT_REASON_OTHER.equals(cmplRson) && StringUtil.isEmpty(cmplCntn)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 입력된 상세 사유가 있으면 관리자에게 전달하기 전에 비속어를 검사한다
        Optional<String> badWord = StringUtil.isEmpty(cmplCntn)
                ? Optional.empty()
                : badWordDetectionService.findBadWord(cmplCntn);
        // 신고 상세 사유에 비속어가 있으면 해당 내용을 저장하지 않는다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // 대상 유형에 따라 서버 원본에서 실제 콘텐츠 작성자를 조회한다
        Long targetOwnerNumb;
        // 공개된 작성 완료 독후감의 작성자를 조회한다
        if (Constant.COMPLAINT_TARGET_REPORT.equals(tagtType)) {
            targetOwnerNumb = complaintMapper.getReportOwnerNumb(request.getTagtNumb());
        } else {
            // 공개 독후감에 남아 있는 미삭제 댓글의 작성자를 조회한다
            targetOwnerNumb = complaintMapper.getReplyOwnerNumb(request.getTagtNumb());
        }

        // 삭제되거나 비공개 처리된 대상은 새 신고를 접수하지 않는다
        if (StringUtil.isEmpty(targetOwnerNumb)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 본인이 작성한 콘텐츠를 본인이 신고하는 요청은 저장하지 않는다
        if (userNumb.equals(targetOwnerNumb)) {
            // "본인이 작성한 콘텐츠는 신고할 수 없어요."
            return ResultData.fail(ResultEnum.COMPLAINT_SELF_REJECTED);
        }

        // 검증된 신고값을 저장용 데이터 객체로 구성한다
        ComplaintDto complaint = new ComplaintDto();
        // 인증된 신고자 번호를 저장 데이터에 설정한다
        complaint.setUserNumb(userNumb);
        // 검증된 대상 유형을 저장 데이터에 설정한다
        complaint.setTagtType(tagtType);
        // 검증된 대상 번호를 저장 데이터에 설정한다
        complaint.setTagtNumb(request.getTagtNumb());
        // 검증된 사유 코드를 저장 데이터에 설정한다
        complaint.setCmplRson(cmplRson);
        // 정규화한 선택 상세 사유를 저장 데이터에 설정한다
        complaint.setCmplCntn(cmplCntn);

        // 같은 사용자가 같은 대상을 신고한 이력이 있으면 재접수를 차단한다
        if (complaintMapper.getDupComplaintCnt(complaint) > 0) {
            // "이미 신고한 콘텐츠예요."
            return ResultData.fail(ResultEnum.COMPLAINT_DUPLICATED);
        }

        // 모든 검증을 통과한 신고를 관리자 검토 대기 상태로 저장한다
        if (complaintMapper.setComplaint(complaint) != 1 || StringUtil.isEmpty(complaint.getCmplNumb())) {
            // "저장에 실패했어요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 사용자가 제출한 신고의 접수 번호를 반환한다
        return ResultData.success(complaint.getCmplNumb());
    }

    /**
     * 신고 등록에 필요한 인증값과 요청값의 기본 형식 및 길이를 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param request 신고 대상과 사유 입력값
     * @return 기본 등록 조건을 만족하면 true
     */
    private boolean isValidRequest(Long userNumb, ComplaintCreateDto request) {

        // 필수 객체와 코드 및 대상 번호가 있어야 세부 업무 검증을 진행한다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(request)
                || StringUtil.hasEmpty(request.getTagtType(), request.getTagtNumb(), request.getCmplRson())) {
            // 누락된 필수값이 있으면 유효하지 않은 요청으로 판정한다
            return false;
        }

        // 테이블 길이를 초과하는 상세 내용과 유효하지 않은 대상 번호를 차단한다
        return request.getTagtNumb() > 0
                && (StringUtil.isEmpty(request.getCmplCntn()) || request.getCmplCntn().length() <= 1000);
    }
}
