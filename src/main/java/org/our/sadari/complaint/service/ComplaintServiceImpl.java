package org.our.sadari.complaint.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ComplaintServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 중복을 차단하고 서버에서 확정한 원문을 접수 시점 스냅샷으로 저장한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·중복 신고 차단
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    // 현재 사용자 화면에서 신고를 허용하는 대상 유형
    private static final Set<String> ALLOWED_TARGET_TYPES = Set.of(
            Constant.COMPLAINT_TARGET_USER,
            Constant.COMPLAINT_TARGET_REPORT,
            Constant.COMPLAINT_TARGET_REPLY
    );
    // 신고 데이터 접근 객체
    private final ComplaintMapper complaintMapper;

    /**
     * 활성 사용자의 동일 대상 재신고를 차단하고 대상 원문 스냅샷과 신고 사유를 접수한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param complaintCreateDto 신고 대상과 사유
     * @return 신고 번호 또는 접수 실패 응답
     */
    @Override
    @Transactional
    public ResultData setComplaint(Long userNumb, ComplaintCreateDto complaintCreateDto) {

        // 인증 사용자가 아니거나 활성 회원이 아니면 신고 접수를 허용하지 않는다
        if (StringUtil.isEmpty(userNumb)
                || !Constant.USER_STAT_ACTIVE.equals(complaintMapper.getUserStat(userNumb))) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 신고 대상과 사유의 기본 형식이 유효하지 않으면 저장하지 않는다
        if (!isValidRequest(complaintCreateDto)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 검증된 신고 대상 유형의 앞뒤 공백을 제거한다
        String tagtType = complaintCreateDto.getTagtType().trim();
        // 검증된 신고 사유의 앞뒤 공백을 제거한다
        String cmplRson = complaintCreateDto.getCmplRson().trim();
        // 선택 입력인 신고 상세 내용을 저장 형식으로 정규화한다
        String cmplCntn = normalizeContent(complaintCreateDto.getCmplCntn());
        // 예약된 모임 유형 등 아직 사용자 화면에서 지원하지 않는 대상을 차단한다
        if (!ALLOWED_TARGET_TYPES.contains(tagtType)
                || complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET, tagtType) != 1
                || complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON, cmplRson) != 1) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 기타 사유에는 관리자가 판단할 수 있는 상세 내용이 반드시 있어야 한다
        if (Constant.COMPLAINT_REASON_OTHER.equals(cmplRson) && StringUtil.isEmpty(cmplCntn)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 처리 상태와 관계없이 같은 사용자가 이미 신고한 대상은 다시 접수하지 않는다
        if (complaintMapper.dupComplaint(userNumb, tagtType, complaintCreateDto.getTagtNumb()) > 0) {
            // "동일한 대상은 다시 신고할 수 없어요."
            return ResultData.fail(ResultEnum.COMPLAINT_DUPLICATED);
        }

        // 대상 유형별 원본 테이블에서 신고 시점의 실제 내용과 소유자를 조회한다
        ComplaintDto target = getTargetDtl(tagtType, complaintCreateDto.getTagtNumb(), userNumb);
        // 없거나 삭제된 대상 및 본인 소유 대상은 신고할 수 없다
        if (StringUtil.isEmpty(target) || StringUtil.isEmpty(target.getTagtUser())) {
            // "저장에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 서버에서 확정한 신고 저장값을 담을 객체를 생성한다
        ComplaintDto complaint = new ComplaintDto();
        // 검증된 신고 대상 유형을 설정한다
        complaint.setTagtType(tagtType);
        // 검증된 신고 대상 번호를 설정한다
        complaint.setTagtNumb(complaintCreateDto.getTagtNumb());
        // 대상 원본이 삭제된 뒤에도 신고 대상 사용자를 식별할 소유자 번호를 설정한다
        complaint.setTagtUser(target.getTagtUser());
        // 원본 테이블에서 조회한 접수 시점 대상 내용을 설정한다
        complaint.setTagtCntn(target.getTagtCntn());
        // 활성 공통코드로 검증한 신고 사유를 설정한다
        complaint.setCmplRson(cmplRson);
        // 선택 입력을 정규화한 신고 상세 내용을 설정한다
        complaint.setCmplCntn(cmplCntn);
        // 사전 조회 뒤 동시에 도착한 동일 대상 신고는 DB 고유 제약 결과로 다시 차단한다
        try {
            // 서버에서 확정한 대상 원문과 신고값을 하나의 이력으로 저장한다
            complaintMapper.setComplaint(complaint, userNumb);
        }

        // 같은 사용자와 대상의 선행 신고가 먼저 저장되었으면 중복 신고 안내로 변환한다
        catch (DuplicateKeyException e) {
            // "동일한 대상은 다시 신고할 수 없어요."
            return ResultData.fail(ResultEnum.COMPLAINT_DUPLICATED);
        }

        // 새로 접수된 신고 번호를 반환한다
        return ResultData.success(complaint.getCmplNumb());
    }

    /**
     * 신고 대상과 사유 및 상세 내용의 저장 가능 형식을 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param request 검증할 신고 요청
     * @return 저장 가능한 기본 형식이면 true
     */
    private boolean isValidRequest(ComplaintCreateDto request) {

        if (StringUtil.isEmpty(request)
                || StringUtil.hasEmpty(request.getTagtType(), request.getTagtNumb(), request.getCmplRson())
                || request.getTagtNumb() < 1) {
            // 필수값이 없거나 대상 번호가 유효하지 않으면 false를 반환한다
            return false;
        }

        // 선택 입력인 신고 상세 내용을 저장 형식으로 정규화한다
        String content = normalizeContent(request.getCmplCntn());
        // 신고 상세 내용이 DB 최대 길이를 넘지 않는지 확인한다
        return content == null || content.length() <= 1000;
    }

    /**
     * 선택 입력인 신고 상세 내용의 앞뒤 공백과 빈 문자열을 정규화한다.
     *
     * @author SeungHyeon.Kang
     * @param content 정규화할 신고 상세 내용
     * @return 앞뒤 공백을 제거한 내용 또는 빈 값이면 null
     */
    private String normalizeContent(String content) {

        if (StringUtil.isEmpty(content) || content.trim().isEmpty()) {
            // 입력하지 않은 신고 상세 내용은 null로 반환한다
            return null;
        }

        // 실제 관리자가 확인할 신고 상세 내용의 앞뒤 공백을 제거한다
        // 실제 관리자가 확인할 신고 상세 내용만 반환한다
        return content.trim();
    }

    /**
     * 신고 대상 유형에 고정된 원본 테이블에서 서버가 실제 내용과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 시점 대상 정보 또는 신고할 수 없는 대상이면 null
     */
    private ComplaintDto getTargetDtl(String tagtType, Long tagtNumb, Long userNumb) {

        // 사용자 요청값으로 테이블명을 만들지 않고 허용된 Mapper 구문만 선택한다
        return switch (tagtType) {
            // 사용자 신고는 활성 프로필 원문 조회 결과를 사용한다
            case Constant.COMPLAINT_TARGET_USER ->
                    complaintMapper.getUserTargetDtl(tagtNumb, userNumb);
            // 독후감 신고는 공개 독후감 원문 조회 결과를 사용한다
            case Constant.COMPLAINT_TARGET_REPORT ->
                    complaintMapper.getReportTargetDtl(tagtNumb, userNumb);
            // 댓글 신고는 삭제되지 않은 공개 독후감 댓글 원문 조회 결과를 사용한다
            case Constant.COMPLAINT_TARGET_REPLY ->
                    complaintMapper.getReplyTargetDtl(tagtNumb, userNumb);
            // 허용 집합 외 대상 유형은 원문이 없는 요청으로 처리한다
            default -> null;
        };
    }
}
