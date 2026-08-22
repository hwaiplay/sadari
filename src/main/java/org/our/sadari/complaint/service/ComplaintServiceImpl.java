package org.our.sadari.complaint.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.complaint.config.ComplaintAutoActionProperties;
import org.our.sadari.complaint.dto.ComplaintActionDto;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ComplaintServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 접수와 대상별 누적 임계치 자동 조치를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·누적 자동 조치 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    // 현재 사용자 화면에서 신고를 허용하는 대상 유형
    private static final Set<String> ALLOWED_TARGET_TYPES = Set.of(
            Constant.COMPLAINT_TARGET_USER,
            Constant.COMPLAINT_TARGET_REPORT,
            Constant.COMPLAINT_TARGET_REPLY,
            Constant.COMPLAINT_TARGET_PROFILE,
            Constant.COMPLAINT_TARGET_INTRO
    );
    // 신고 데이터 접근 객체
    private final ComplaintMapper complaintMapper;
    // 신고 대상별 자동 조치 임계치 설정
    private final ComplaintAutoActionProperties autoActionProperties;
    // 프로필 사진 참조 해제 뒤 파일 메타정보와 물리 파일을 정리하는 서비스
    private final FileService fileService;

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
        // 프로필 사진 자동 조치 뒤 파일을 정리할 현재 파일 번호를 설정한다
        complaint.setFileNumb(target.getFileNumb());
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

        // 새 신고를 포함한 유효 누적 건수가 임계치에 도달하면 같은 트랜잭션에서 자동 조치한다
        setAutoAction(complaint);

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
            // 프로필 사진 신고는 현재 파일 참조와 원본 파일명을 잠금 조회한다
            case Constant.COMPLAINT_TARGET_PROFILE ->
                    complaintMapper.getProfileTargetDtl(tagtNumb, userNumb);
            // 한줄소개 신고는 현재 표시 중인 소개 원문을 잠금 조회한다
            case Constant.COMPLAINT_TARGET_INTRO ->
                    complaintMapper.getIntroTargetDtl(tagtNumb, userNumb);
            // 허용 집합 외 대상 유형은 원문이 없는 요청으로 처리한다
            default -> null;
        };
    }

    /**
     * 반려를 제외한 동일 대상의 신고가 설정 임계치 배수에 도달하면 자동 조치한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 새로 저장된 신고와 대상 정보
     */
    private void setAutoAction(ComplaintDto complaint) {
        // 대상 유형에 고정된 자동 조치 임계치를 조회한다
        int threshold = autoActionProperties.getThreshold(complaint.getTagtType());

        // 사용자 전체 신고처럼 자동 조치 대상이 아닌 유형은 관리자 검토 상태로 유지한다
        if (threshold < 1) {
            // 자동 조치 없이 신고 접수를 마친다
            return;
        }

        // 반려 신고를 제외한 현재 대상의 유효 누적 신고 건수를 조회한다
        int complaintCount = complaintMapper.getAutoActionCmplCnt(
                complaint.getTagtType(), complaint.getTagtNumb()
        );

        // 임계치 미만이거나 정확한 임계치 배수가 아니면 다음 신고까지 조치하지 않는다
        if (complaintCount < threshold || complaintCount % threshold != 0) {
            // 자동 조치 없이 신고 접수를 마친다
            return;
        }

        // 대상 유형별 삭제 또는 초기화를 실행하고 결과 설명을 조회한다
        String resultContent = applyAutoAction(complaint, threshold);
        // 자동 조치 결과를 변경 불가능한 이력으로 저장할 객체를 생성한다
        ComplaintActionDto action = new ComplaintActionDto();
        // 자동 조치 대상 유형을 설정한다
        action.setTagtType(complaint.getTagtType());
        // 자동 조치 대상 번호를 설정한다
        action.setTagtNumb(complaint.getTagtNumb());
        // 물리 삭제 뒤에도 조치 시점 소유자를 확인할 사용자 번호를 설정한다
        action.setTagtUser(complaint.getTagtUser());
        // 대상 유형에 대응하는 자동 조치 유형을 설정한다
        action.setActnType(getActionType(complaint.getTagtType()));
        // 조치가 실제 원본에 반영된 결과 코드를 설정한다
        action.setRsltCode(Constant.COMPLAINT_RESULT_APPLIED);
        // 설정 파일에서 읽은 대상별 임계치를 설정한다
        action.setThrsCntt(threshold);
        // 조치 판단 시점의 유효 누적 신고 건수를 설정한다
        action.setCmplCntt(complaintCount);
        // 같은 대상이 다시 신고될 때 5건 단위로 조치 이력을 분리할 순번을 설정한다
        action.setActnOrdr(complaintCount / threshold);
        // 자동 조치를 발생시킨 신규 신고 번호를 설정한다
        action.setTrigCmpl(complaint.getCmplNumb());
        // 관리자가 결과를 확인할 수 있는 처리 설명을 설정한다
        action.setRsltCntn(resultContent);
        // 같은 대상과 조치 순번의 결과를 한 번만 저장한다
        int actionCount = complaintMapper.setAutoAction(action);

        // 결과 이력이 저장되지 않으면 원본 조치만 남지 않도록 전체 신고 트랜잭션을 롤백한다
        if (actionCount != 1) {
            throw new IllegalStateException("Complaint auto action history was not saved.");
        }

        // 현재 대상의 접수 또는 검토 중 신고를 조치 완료 상태로 일괄 종결한다
        int complaintUpdateCount = complaintMapper.uptAutoComplaints(
                complaint.getTagtType(), complaint.getTagtNumb(), resultContent
        );

        // 자동 조치를 발생시킨 신규 신고까지 종결되지 않으면 전체 신고 트랜잭션을 롤백한다
        if (complaintUpdateCount < 1) {
            throw new IllegalStateException("Complaint auto action reports were not updated.");
        }
    }

    /**
     * 신고 대상 유형에 맞춰 독후감·댓글 삭제 또는 프로필 정보를 초기화한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 조치할 신고 대상 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 자동 조치 결과 설명
     */
    private String applyAutoAction(ComplaintDto complaint, int threshold) {
        // 대상 유형별 데이터 보존 정책에 맞는 조치만 실행한다
        return switch (complaint.getTagtType()) {
            // 독후감은 연결 댓글과 좋아요를 정리한 뒤 원본을 완전 삭제한다
            case Constant.COMPLAINT_TARGET_REPORT -> delAutoReport(complaint, threshold);
            // 댓글은 답글 연결을 보존할 수 있도록 삭제 여부만 변경한다
            case Constant.COMPLAINT_TARGET_REPLY -> delAutoReply(complaint, threshold);
            // 프로필 사진은 파일 참조를 해제하고 기본 이미지 상태로 변경한다
            case Constant.COMPLAINT_TARGET_PROFILE -> uptAutoProfile(complaint, threshold);
            // 한줄소개는 현재 원문을 보존하지 않고 Null로 초기화한다
            case Constant.COMPLAINT_TARGET_INTRO -> uptAutoIntro(complaint, threshold);
            // 설정 임계치가 없는 대상이 이 경로에 진입하면 정합성 오류로 전체 접수를 롤백한다
            default -> throw new IllegalStateException("Unsupported complaint auto action target.");
        };
    }

    /**
     * 신고 대상 유형을 자동 조치 이력의 조치 유형 코드로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @return 자동 조치 유형 세부코드
     */
    private String getActionType(String tagtType) {
        // 신고 대상과 실행한 조치 유형을 공통코드 기준으로 일대일 매핑한다
        return switch (tagtType) {
            // 독후감 신고는 완전 삭제 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_REPORT -> Constant.COMPLAINT_ACTION_DELETE_REPORT;
            // 댓글 신고는 논리 삭제 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_REPLY -> Constant.COMPLAINT_ACTION_DELETE_REPLY;
            // 프로필 사진 신고는 기본 이미지 초기화 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_PROFILE -> Constant.COMPLAINT_ACTION_RESET_PROFILE;
            // 한줄소개 신고는 Null 초기화 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_INTRO -> Constant.COMPLAINT_ACTION_CLEAR_INTRO;
            // 자동 조치 대상이 아닌 유형은 이력 코드로 변환하지 않는다
            default -> throw new IllegalStateException("Unsupported complaint auto action type.");
        };
    }

    /**
     * 독후감과 연결 데이터를 외래키 순서에 맞춰 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 삭제할 독후감 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 독후감 자동 삭제 결과 설명
     */
    private String delAutoReport(ComplaintDto complaint, int threshold) {
        // 댓글과 답글의 좋아요가 댓글 물리 삭제를 막지 않도록 먼저 정리한다
        complaintMapper.delAutoReplLike(complaint.getTagtNumb());
        // 자기 참조 외래키가 있는 대댓글을 최상위 댓글보다 먼저 삭제한다
        complaintMapper.delAutoChildReply(complaint.getTagtNumb());
        // 연결된 최상위 댓글을 삭제한다
        complaintMapper.delAutoReplyList(complaint.getTagtNumb());
        // 독후감 자체의 좋아요를 삭제한다
        complaintMapper.delAutoReportLike(complaint.getTagtNumb());
        // 서버에서 잠금 조회한 소유자와 일치하는 독후감 원본을 삭제한다
        int updateCount = complaintMapper.delAutoReport(
                complaint.getTagtNumb(), complaint.getTagtUser()
        );
        // 잠금 조회한 독후감이 삭제되지 않으면 일부 연결 데이터만 남지 않도록 롤백한다
        validateActionCount(updateCount);
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "누적 신고 " + threshold + "건에 따른 독후감 완전 삭제";
    }

    /**
     * 댓글 원본 행을 보존하면서 삭제 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 삭제할 댓글 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 댓글 자동 삭제 결과 설명
     */
    private String delAutoReply(ComplaintDto complaint, int threshold) {
        // 서버에서 잠금 조회한 소유자와 일치하는 댓글만 삭제 상태로 변경한다
        int updateCount = complaintMapper.delAutoReply(
                complaint.getTagtNumb(), complaint.getTagtUser()
        );
        // 잠금 조회한 댓글이 변경되지 않으면 신고와 조치 결과를 함께 롤백한다
        validateActionCount(updateCount);
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "누적 신고 " + threshold + "건에 따른 댓글 삭제 상태 변경";
    }

    /**
     * 프로필 사진 참조를 제거하고 더 이상 사용하지 않는 파일을 정리한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 초기화할 프로필 사진 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 프로필 사진 자동 초기화 결과 설명
     */
    private String uptAutoProfile(ComplaintDto complaint, int threshold) {
        // 활성·비활성화·삭제 대기 회원의 현재 프로필 사진 참조를 제거한다
        int updateCount = complaintMapper.uptAutoProfile(complaint.getTagtUser());
        // 잠금 조회한 프로필 사진이 변경되지 않으면 신고와 조치 결과를 함께 롤백한다
        validateActionCount(updateCount);
        // 프로필과 배경에서 더 이상 참조하지 않는 파일은 커밋 뒤 물리 저장소까지 정리한다
        fileService.delFile(complaint.getFileNumb());
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "누적 신고 " + threshold + "건에 따른 프로필 사진 기본 이미지 초기화";
    }

    /**
     * 회원의 현재 한줄소개를 Null로 초기화한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 초기화할 한줄소개 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 한줄소개 자동 초기화 결과 설명
     */
    private String uptAutoIntro(ComplaintDto complaint, int threshold) {
        // 활성·비활성화·삭제 대기 회원의 현재 한줄소개를 Null로 변경한다
        int updateCount = complaintMapper.uptAutoIntro(complaint.getTagtUser());
        // 잠금 조회한 한줄소개가 변경되지 않으면 신고와 조치 결과를 함께 롤백한다
        validateActionCount(updateCount);
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "누적 신고 " + threshold + "건에 따른 한줄소개 초기화";
    }

    /**
     * 잠금 조회한 신고 대상 원본에 자동 조치가 한 건 반영됐는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param updateCount 자동 조치 반영 건수
     */
    private void validateActionCount(int updateCount) {
        // 대상 원본 한 건이 변경되지 않으면 부분 조치를 방지하기 위해 전체 트랜잭션을 롤백한다
        if (updateCount != 1) {
            throw new IllegalStateException("Complaint auto action target was not updated.");
        }
    }
}
