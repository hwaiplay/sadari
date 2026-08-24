package org.our.sadari.complaint.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.our.sadari.complaint.config.ComplaintAutoActionProperties;
import org.our.sadari.complaint.dto.ComplaintActionDto;
import org.our.sadari.complaint.dto.ComplaintCreateDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.dto.ComplaintEvidenceDto;
import org.our.sadari.complaint.mapper.ComplaintMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.BadWordDetectionService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
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
 * 2026-08-22        SeungHyeon.Kang    버전별 자동 조치·이미지 증거 및 입력 검증 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComplaintServiceImpl implements ComplaintService {

    // 신고자가 작성하는 상세 내용의 최대 문자 수
    private static final int COMPLAINT_CONTENT_MAX_LENGTH = 500;
    // 현재 사용자 화면에서 신고를 허용하는 대상 유형
    private static final Set<String> ALLOWED_TARGET_TYPES = Set.of(
            Constant.COMPLAINT_TARGET_USER,
            Constant.COMPLAINT_TARGET_REPORT,
            Constant.COMPLAINT_TARGET_REPLY,
            Constant.COMPLAINT_TARGET_PROFILE,
            Constant.COMPLAINT_TARGET_BACKGROUND,
            Constant.COMPLAINT_TARGET_INTRO
    );
    // 신고 데이터 접근 객체
    private final ComplaintMapper complaintMapper;
    // 신고 대상별 자동 조치 임계치 설정
    private final ComplaintAutoActionProperties autoActionProperties;
    // 프로필 사진 참조 해제 뒤 파일 메타정보와 물리 파일을 정리하는 서비스
    private final FileService fileService;
    // 신고 시점 프로필 사진의 실제 원본을 읽는 비공개 파일 저장소
    private final FileStorage fileStorage;
    // 신고 상세 내용에 포함된 비속어를 저장 전에 탐지하는 서비스
    private final BadWordDetectionService badWordDetectionService;

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
        if (StringUtil.isEmpty(userNumb) || !Constant.USER_STAT_ACTIVE.equals(complaintMapper.getUserStat(userNumb))) {
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
        if (!ALLOWED_TARGET_TYPES.contains(tagtType) || complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_TARGET, tagtType) != 1
                || complaintMapper.getActiveCodeCnt(Constant.CODE_COMPLAINT_REASON, cmplRson) != 1) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 기타 사유에는 관리자가 판단할 수 있는 상세 내용이 반드시 있어야 한다
        if (Constant.COMPLAINT_REASON_OTHER.equals(cmplRson) && StringUtil.isEmpty(cmplCntn)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 신고자가 작성한 상세 내용에서 저장을 차단할 비속어를 조회한다
        Optional<String> badWord = badWordDetectionService.findBadWord(cmplCntn);
        // 비속어가 발견되면 감지된 단어를 안내하고 신고 접수를 중단한다
        if (badWord.isPresent()) {
            // "욕설이나 비속어는 사용할 수 없어요.\n감지된 단어: {0}"
            return ResultData.fail(ResultEnum.COMMON_BAD_WORD_INCLUDED, badWord.get());
        }

        // 대상 유형별 원본 테이블에서 신고 시점의 실제 내용과 소유자를 조회한다
        ComplaintDto target = getTargetDtl(tagtType, complaintCreateDto.getTagtNumb(), userNumb);
        // 없거나 삭제된 대상 및 본인 소유 대상은 신고할 수 없다
        if (StringUtil.isEmpty(target) || StringUtil.isEmpty(target.getTagtUser())) {
            // "저장에 실패했어요.\n다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 텍스트 또는 실제 이미지 원본으로 변경 불가능한 대상 버전 해시를 계산한다
        byte[] evidenceBytes = getEvidenceBytes(tagtType, target);
        if (isImageTarget(tagtType) && evidenceBytes == null) {
            // 이미지 원본 증거를 확보하지 못한 신고는 파일명만으로 접수하지 않는다
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }
        // 같은 번호의 수정 전후 콘텐츠가 섞이지 않도록 현재 버전 해시를 설정한다
        String tagtHash = getTargetHash(tagtType, target.getTagtCntn(), evidenceBytes);

        // 처리 상태와 관계없이 같은 사용자가 이미 신고한 동일 대상 버전은 다시 접수하지 않는다
        if (complaintMapper.dupComplaint(userNumb, tagtType, complaintCreateDto.getTagtNumb(), tagtHash) > 0) {
            // "동일한 대상은 다시 신고할 수 없어요."
            return ResultData.fail(ResultEnum.COMPLAINT_DUPLICATED);
        }

        // 서버에서 확정한 신고 저장값을 담을 객체를 생성한다
        ComplaintDto complaint = new ComplaintDto();
        // 검증된 신고 대상 유형을 설정한다
        complaint.setTagtType(tagtType);
        // 검증된 신고 대상 번호를 설정한다
        complaint.setTagtNumb(complaintCreateDto.getTagtNumb());
        // 내용 수정 전후 신고를 분리할 대상 버전 해시를 설정한다
        complaint.setTagtHash(tagtHash);
        // 대상 원본이 삭제된 뒤에도 신고 대상 사용자를 식별할 소유자 번호를 설정한다
        complaint.setTagtUser(target.getTagtUser());
        // 원본 테이블에서 조회한 접수 시점 대상 내용을 설정한다
        complaint.setTagtCntn(target.getTagtCntn());
        // 이미지 자동 조치 뒤 파일을 정리할 현재 파일 번호를 설정한다
        complaint.setFileNumb(target.getFileNumb());
        // 이미지 신고는 실제 원본을 관리자 전용 증거에 저장하고 연결 번호를 설정한다
        if (isImageTarget(tagtType)) {
            complaint.setEvdcNumb(setEvidence(complaint, target, evidenceBytes));
        }
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
        // 신고 상세 내용이 서버 정책의 최대 길이를 넘지 않는지 확인한다
        return content == null || content.length() <= COMPLAINT_CONTENT_MAX_LENGTH;
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
            // 배경사진 신고는 현재 파일 참조와 원본 파일명을 잠금 조회한다
            case Constant.COMPLAINT_TARGET_BACKGROUND ->
                    complaintMapper.getBackgroundTargetDtl(tagtNumb, userNumb);
            // 한줄소개 신고는 현재 표시 중인 소개 원문을 잠금 조회한다
            case Constant.COMPLAINT_TARGET_INTRO ->
                    complaintMapper.getIntroTargetDtl(tagtNumb, userNumb);
            // 허용 집합 외 대상 유형은 원문이 없는 요청으로 처리한다
            default -> null;
        };
    }

    /**
     * 이미지 신고일 때만 검증된 내부 저장소에서 실제 원본 바이트를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param target 신고 대상 파일 메타정보
     * @return 실제 이미지 원본, 이미지 신고가 아니면 null
     */
    private byte[] getEvidenceBytes(String tagtType, ComplaintDto target) {

        // 텍스트 신고 대상은 기존 내용 스냅샷만 저장하므로 바이너리 증거를 만들지 않는다
        if (!isImageTarget(tagtType)) {
            // 이미지 증거가 필요하지 않음을 반환한다
            return null;
        }

        // DB 파일 경로를 상위 경로 이동이 불가능한 내부 저장소 객체 키로 변환한다
        String objectKey = getStoredObjectKey(tagtType, target);
        if (StringUtil.isEmpty(objectKey)) {
            // 외부 URL이나 허용 범위 밖의 경로는 관리자 증거로 복제하지 않는다
            return null;
        }

        try {
            // 비공개 저장소에서 현재 이미지의 실제 원본을 조회한다
            Optional<StoredFile> storedFile = fileStorage.getFile(objectKey);
            if (storedFile.isEmpty() || storedFile.get().bytes().length == 0) {
                // 저장소에 실제 원본이 없으면 파일명만으로 신고를 접수하지 않는다
                return null;
            }
            // 저장소가 확정한 MIME 유형을 증거 메타정보에 우선 적용한다
            target.setMimeType(storedFile.get().contentType());
            // 실제 신고 시점의 이미지 원본 바이트를 반환한다
            return storedFile.get().bytes();
        }

        // 파일 저장소 읽기 오류는 불완전한 증거 접수 대신 저장 실패로 처리한다
        catch (IOException e) {
            // 호출부가 신고 접수를 중단하도록 빈 증거를 반환한다
            return null;
        }
    }

    /**
     * 이미지 접근 경로를 대상 유형에 맞는 안전한 내부 저장소 객체 키로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param target 이미지 파일 메타정보
     * @return 검증된 이미지 하위 객체 키 또는 허용되지 않으면 null
     */
    private String getStoredObjectKey(String tagtType, ComplaintDto target) {

        // 파일 경로와 저장 파일명이 모두 있어야 저장소 객체를 검증할 수 있다
        if (StringUtil.isEmpty(target)
                || StringUtil.hasEmpty(target.getFilePath(), target.getStorName())
                || !target.getFilePath().startsWith("/uploads/")) {
            // 외부 경로나 불완전한 파일 메타정보는 거부한다
            return null;
        }

        // 브라우저 접근 접두사를 제외한 저장소 상대 경로를 정규화한다
        Path storedPath = Paths.get(target.getFilePath().substring("/uploads/".length())).normalize();
        // 신고 유형에 대응하는 이미지 저장소 루트를 확정한다
        Path expectedRoot = Constant.COMPLAINT_TARGET_PROFILE.equals(tagtType)
                ? Paths.get("profile") : Paths.get("background");
        // 이미지 루트 아래 날짜와 파일명으로 구성된 경로인지 확인한다
        if (storedPath.isAbsolute() || storedPath.getNameCount() != 3
                || !storedPath.startsWith(expectedRoot)
                || !target.getStorName().equals(storedPath.getFileName().toString())) {
            // 상위 경로 이동 또는 다른 이미지 유형은 신고 증거 원본으로 읽지 않는다
            return null;
        }

        // 운영체제 경로 구분자를 저장소 공통 객체 키 구분자로 변환해 반환한다
        return storedPath.toString().replace('\\', '/');
    }

    /**
     * 신고 대상이 원본 바이트 증거를 보존하는 이미지 유형인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @return 프로필 사진 또는 배경사진이면 true
     */
    private boolean isImageTarget(String tagtType) {
        // 프로필과 배경사진만 실제 원본 바이트 증거를 저장한다
        return Constant.COMPLAINT_TARGET_PROFILE.equals(tagtType)
                || Constant.COMPLAINT_TARGET_BACKGROUND.equals(tagtType);
    }

    /**
     * 대상 유형과 실제 원문 또는 이미지 바이트로 변경 불가능한 버전 해시를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtCntn 텍스트 대상 내용 스냅샷
     * @param evidenceBytes 프로필 이미지 실제 원본 바이트
     * @return 소문자 64자리 SHA-256 해시
     */
    private String getTargetHash(String tagtType, String tagtCntn, byte[] evidenceBytes) {

        try {
            // 모든 대상 유형에서 동일한 SHA-256 알고리즘을 사용한다
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 유형이 다른 대상의 동일 바이트가 같은 버전으로 표현되지 않도록 구분자를 포함한다
            digest.update((tagtType + "\u0000").getBytes(StandardCharsets.UTF_8));
            // 프로필 사진은 파일명이 아니라 실제 이미지 바이트를 해시 입력으로 사용한다
            if (evidenceBytes != null) {
                // 신고 시점 원본 이미지 바이트를 해시에 반영한다
                digest.update(evidenceBytes);
            } else {
                // 텍스트 대상은 DB에서 조회한 실제 스냅샷 전체를 해시에 반영한다
                digest.update(Optional.ofNullable(tagtCntn).orElse("").getBytes(StandardCharsets.UTF_8));
            }
            // DB CHAR(64) 컬럼에 저장할 소문자 16진수 해시를 반환한다
            return HexFormat.of().formatHex(digest.digest());
        }

        // 모든 지원 JDK에 필수인 SHA-256을 사용할 수 없으면 데이터 정합성을 보장할 수 없다
        catch (NoSuchAlgorithmException e) {
            // 신고 접수 전체를 롤백하도록 실행 불가능 상태로 변환한다
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    /**
     * 동일 이미지 버전의 관리자 전용 증거를 한 번만 저장하고 연결 번호를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 신고 대상 버전 식별정보
     * @param target 이미지 파일 메타정보
     * @param evidenceBytes 실제 이미지 원본 바이트
     * @return 신고와 연결할 비공개 증거 번호
     */
    private Long setEvidence(ComplaintDto complaint, ComplaintDto target, byte[] evidenceBytes) {

        // 동일 대상 버전에서 먼저 접수된 증거가 있으면 원본 바이트를 중복 저장하지 않는다
        Long evidenceNumb = complaintMapper.getEvidenceNumb(
                complaint.getTagtType(), complaint.getTagtNumb(), complaint.getTagtHash()
        );
        if (!StringUtil.isEmpty(evidenceNumb)) {
            // 기존 관리자 전용 증거 번호를 반환한다
            return evidenceNumb;
        }

        // 신고 시점 이미지 원본과 메타정보를 담을 증거 객체를 생성한다
        ComplaintEvidenceDto evidence = new ComplaintEvidenceDto();
        // 신고 대상 유형을 설정한다
        evidence.setTagtType(complaint.getTagtType());
        // 신고 대상 번호를 설정한다
        evidence.setTagtNumb(complaint.getTagtNumb());
        // 신고 대상 버전 해시를 설정한다
        evidence.setTagtHash(complaint.getTagtHash());
        // 신고 대상 소유 사용자 번호를 설정한다
        evidence.setTagtUser(complaint.getTagtUser());
        // 업로드 당시 원본 파일명을 설정한다
        evidence.setOrigName(target.getOrigName());
        // 저장소에서 확인한 이미지 MIME 유형을 설정한다
        evidence.setMimeType(Optional.ofNullable(target.getMimeType()).orElse("application/octet-stream"));
        // 실제 증거 바이트 크기를 설정한다
        evidence.setFileSize((long) evidenceBytes.length);
        // 관리자 전용으로 보관할 실제 원본 바이트를 설정한다
        evidence.setEvdcData(evidenceBytes);
        // 같은 대상 버전의 증거 한 건을 저장한다
        int insertCount = complaintMapper.setEvidence(evidence);
        if (insertCount != 1 || StringUtil.isEmpty(evidence.getEvdcNumb())) {
            // 증거와 신고 연결이 불완전하게 저장되지 않도록 전체 트랜잭션을 롤백한다
            throw new IllegalStateException("Complaint evidence was not saved.");
        }
        // 새로 저장한 관리자 전용 증거 번호를 반환한다
        return evidence.getEvdcNumb();
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
                complaint.getTagtType(), complaint.getTagtNumb(), complaint.getTagtHash()
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
        // 같은 대상 번호에서도 실제 신고된 버전만 식별하도록 해시를 설정한다
        action.setTagtHash(complaint.getTagtHash());
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
                complaint.getTagtType(), complaint.getTagtNumb(), complaint.getTagtHash(), resultContent
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
            // 독후감은 관리자 완전 삭제 권한을 보존하고 공개 여부만 비공개로 변경한다
            case Constant.COMPLAINT_TARGET_REPORT -> uptAutoReportPrivate(complaint, threshold);
            // 댓글은 답글 연결을 보존할 수 있도록 삭제 여부만 변경한다
            case Constant.COMPLAINT_TARGET_REPLY -> delAutoReply(complaint, threshold);
            // 프로필 사진은 파일 참조를 해제하고 기본 이미지 상태로 변경한다
            case Constant.COMPLAINT_TARGET_PROFILE -> uptAutoProfile(complaint, threshold);
            // 배경사진은 파일 참조를 해제하고 기본 이미지 상태로 변경한다
            case Constant.COMPLAINT_TARGET_BACKGROUND -> uptAutoBackground(complaint, threshold);
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
            // 독후감 신고는 비공개 전환 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_REPORT -> Constant.COMPLAINT_ACTION_HIDE_REPORT;
            // 댓글 신고는 논리 삭제 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_REPLY -> Constant.COMPLAINT_ACTION_DELETE_REPLY;
            // 프로필 사진 신고는 기본 이미지 초기화 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_PROFILE -> Constant.COMPLAINT_ACTION_RESET_PROFILE;
            // 배경사진 신고는 기본 배경 초기화 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_BACKGROUND -> Constant.COMPLAINT_ACTION_RESET_BACKGROUND;
            // 한줄소개 신고는 Null 초기화 유형으로 기록한다
            case Constant.COMPLAINT_TARGET_INTRO -> Constant.COMPLAINT_ACTION_CLEAR_INTRO;
            // 자동 조치 대상이 아닌 유형은 이력 코드로 변환하지 않는다
            default -> throw new IllegalStateException("Unsupported complaint auto action type.");
        };
    }

    /**
     * 독후감 원본과 연결 데이터를 보존하면서 공개 여부만 비공개로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 비공개로 변경할 독후감 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 독후감 자동 비공개 전환 결과 설명
     */
    private String uptAutoReportPrivate(ComplaintDto complaint, int threshold) {
        // 서버에서 잠금 조회한 소유자와 일치하는 공개 독후감만 비공개로 변경한다
        int updateCount = complaintMapper.uptAutoReportPrivate(
                complaint.getTagtNumb(), complaint.getTagtUser()
        );
        // 잠금 조회한 독후감이 비공개로 변경되지 않으면 신고와 조치 결과를 함께 롤백한다
        validateActionCount(updateCount);
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "동일 버전 누적 신고 " + threshold + "건에 따른 독후감 비공개 전환";
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
     * 배경사진 참조를 제거하고 더 이상 사용하지 않는 파일을 정리한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 자동 초기화할 배경사진 정보
     * @param threshold 자동 조치를 발생시킨 신고 임계치
     * @return 배경사진 자동 초기화 결과 설명
     */
    private String uptAutoBackground(ComplaintDto complaint, int threshold) {
        // 활성·비활성화·삭제 대기 회원의 현재 배경사진 참조를 제거한다
        int updateCount = complaintMapper.uptAutoBackground(complaint.getTagtUser());
        // 잠금 조회한 배경사진이 변경되지 않으면 신고와 조치 결과를 함께 롤백한다
        validateActionCount(updateCount);
        // 프로필과 배경에서 더 이상 참조하지 않는 파일은 커밋 뒤 물리 저장소까지 정리한다
        fileService.delFile(complaint.getFileNumb());
        // 자동 조치 이력과 신고 처리 내용에 저장할 결과를 반환한다
        return "누적 신고 " + threshold + "건에 따른 배경사진 기본 이미지 초기화";
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
