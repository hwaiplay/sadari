package org.our.sadari.complaint.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.complaint.dto.ComplaintActionDto;
import org.our.sadari.complaint.dto.ComplaintDto;
import org.our.sadari.complaint.dto.ComplaintEvidenceDto;
import org.our.sadari.complaint.dto.ComplaintResultDto;
import org.our.sadari.complaint.dto.ComplaintResultEventDto;
import org.our.sadari.complaint.dto.ComplaintResultItemDto;

import java.util.List;

/**
 * fileName       : ComplaintMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 원문과 누적 건수를 조회하고 신고 및 자동 조치 이력을 저장한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·버전별 자동 조치와 증거 보관 추가
 * 2026-08-24        HanWon.Jang        신고 결과 확인 연동
 */
@Mapper
public interface ComplaintMapper {

    /**
     * 신고 접수 가능 여부를 판단할 현재 회원 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @return 현재 회원 상태 코드
     */
    String getUserStat(@Param("userNumb") Long userNumb);

    /**
     * 요청한 신고 대상 유형 또는 사유가 활성 공통코드인지 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param commCode 공통코드
     * @param comdCode 세부코드
     * @return 활성 코드 건수
     */
    int getActiveCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 동일 사용자가 같은 대상 버전을 신고한 이력이 있는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 신고자 사용자 번호
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 동일 대상 버전 신고 건수
     */
    int dupComplaint(@Param("userNumb") Long userNumb, @Param("tagtType") String tagtType
                   , @Param("tagtNumb") Long tagtNumb, @Param("tagtHash") String tagtHash);

    /**
     * 신고 시점에 저장할 다른 사용자의 프로필 내용과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 사용자 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 프로필 내용과 소유자
     */
    ComplaintDto getUserTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 공개 독후감 본문과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 독후감 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 독후감 본문과 소유자
     */
    ComplaintDto getReportTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 공개 독후감 댓글 본문과 소유자를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 댓글 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 댓글 본문과 소유자
     */
    ComplaintDto getReplyTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 현재 프로필 사진 정보와 소유자를 잠금 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 사용자 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 프로필 사진 정보와 소유자
     */
    ComplaintDto getProfileTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 현재 배경사진 정보와 소유자를 잠금 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 사용자 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 배경사진 정보와 소유자
     */
    ComplaintDto getBackgroundTargetDtl(@Param("tagtNumb") Long tagtNumb
                                       , @Param("userNumb") Long userNumb);

    /**
     * 신고 시점에 저장할 다른 사용자의 현재 한줄소개와 소유자를 잠금 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 사용자 번호
     * @param userNumb 신고자 사용자 번호
     * @return 신고 대상 한줄소개와 소유자
     */
    ComplaintDto getIntroTargetDtl(@Param("tagtNumb") Long tagtNumb, @Param("userNumb") Long userNumb);

    /**
     * 동일 대상 버전의 비공개 이미지 증거 번호를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 기존 신고 증거 번호 또는 없으면 null
     */
    Long getEvidenceNumb(@Param("tagtType") String tagtType, @Param("tagtNumb") Long tagtNumb
                       , @Param("tagtHash") String tagtHash);

    /**
     * 관리자 전용 이미지 신고 증거 원본을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param evidence 신고 대상 버전과 이미지 원본
     * @return 저장된 신고 증거 수
     */
    int setEvidence(ComplaintEvidenceDto evidence);

    /**
     * 서버에서 확정한 대상 내용과 신고 사유를 접수 이력으로 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param complaint 신고 대상과 사유 및 내용 스냅샷
     * @param userNumb 신고자 사용자 번호
     * @return 저장된 신고 행 수
     */
    int setComplaint(@Param("complaint") ComplaintDto complaint, @Param("userNumb") Long userNumb);

    /**
     * 반려를 제외한 동일 대상 버전의 유효 누적 신고 건수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 자동 조치 판단에 사용하는 누적 신고 건수
     */
    int getAutoActionCmplCnt(@Param("tagtType") String tagtType, @Param("tagtNumb") Long tagtNumb
                           , @Param("tagtHash") String tagtHash);

    /**
     * 신고 누적 임계치에 도달한 독후감을 비공개로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 독후감 번호
     * @param tagtUser 독후감 소유 사용자 번호
     * @return 비공개로 변경된 독후감 수
     */
    int uptAutoReportPrivate(@Param("tagtNumb") Long tagtNumb, @Param("tagtUser") Long tagtUser);

    /**
     * 신고 누적 임계치에 도달한 댓글을 삭제 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtNumb 신고 대상 댓글 번호
     * @param tagtUser 댓글 소유 사용자 번호
     * @return 변경된 댓글 수
     */
    int delAutoReply(@Param("tagtNumb") Long tagtNumb, @Param("tagtUser") Long tagtUser);

    /**
     * 신고 누적 임계치에 도달한 프로필 사진을 기본 이미지 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtUser 신고 대상 사용자 번호
     * @return 변경된 사용자 수
     */
    int uptAutoProfile(@Param("tagtUser") Long tagtUser);

    /**
     * 신고 누적 임계치에 도달한 배경사진을 기본 이미지 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtUser 신고 대상 사용자 번호
     * @return 변경된 사용자 수
     */
    int uptAutoBackground(@Param("tagtUser") Long tagtUser);

    /**
     * 신고 누적 임계치에 도달한 한줄소개를 Null로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtUser 신고 대상 사용자 번호
     * @return 변경된 사용자 수
     */
    int uptAutoIntro(@Param("tagtUser") Long tagtUser);

    /**
     * 신고 누적 자동 조치 결과를 변경 불가능한 이력으로 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param action 자동 조치 대상과 결과
     * @return 저장된 자동 조치 결과 수
     */
    int setAutoAction(ComplaintActionDto action);

    /**
     * 동일 대상의 접수 또는 검토 중 신고를 자동 조치 완료 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @param procCntn 자동 조치 처리 설명
     * @return 조치 완료로 변경된 신고 수
     */
    int uptAutoComplaints(@Param("tagtType") String tagtType, @Param("tagtNumb") Long tagtNumb
                        , @Param("tagtHash") String tagtHash, @Param("procCntn") String procCntn);

    /**
     * 자동 조치로 종결된 동일 대상 버전 신고의 신고자별 미확인 결과를 생성한다.
     *
     * @author HanWon.Jang
     * @param tagtType 신고 대상 유형
     * @param tagtNumb 신고 대상 번호
     * @param tagtHash 신고 대상 버전 SHA-256 해시
     * @return 생성된 신고 조치 결과 수
     */
    ComplaintResultEventDto getAutoResultEventDtl(@Param("tagtType") String tagtType
                                                 , @Param("tagtNumb") Long tagtNumb
                                                 , @Param("tagtHash") String tagtHash);

    /** 신고 조치 사용자 안내 이벤트를 저장한다. */
    int setResultEvent(ComplaintResultEventDto event);

    /** 자동 조치로 종결된 신고의 유효한 신고자 수신 결과를 생성한다. */
    int setAutoReporterResults(@Param("eventNumb") Long eventNumb
                              , @Param("tagtType") String tagtType
                              , @Param("tagtNumb") Long tagtNumb
                              , @Param("tagtHash") String tagtHash);

    /** 조치 시점에 보존 대상인 피신고자의 수신 결과를 생성한다. */
    int setTargetResult(@Param("eventNumb") Long eventNumb, @Param("cmplNumb") Long cmplNumb
                       , @Param("userNumb") Long userNumb);

    /**
     * 활성 사용자가 아직 확인하지 않은 신고 조치 결과를 요약 조회한다.
     *
     * @author HanWon.Jang
     * @param userNumb 인증 사용자 번호
     * @return 미확인 결과 건수와 조회 시점의 마지막 결과 번호
     */
    List<ComplaintResultItemDto> getPendingResultList(@Param("userNumb") Long userNumb
                                                     , @Param("maxSize") int maxSize);

    /**
     * 활성 사용자의 조회 시점 마지막 번호까지 미확인 결과를 확인 처리한다.
     *
     * @author HanWon.Jang
     * @param userNumb 인증 사용자 번호
     * @param resultNumb 조회 시점의 마지막 결과 번호
     * @return 확인 처리된 신고 조치 결과 수
     */
    int uptResultConfirm(@Param("userNumb") Long userNumb, @Param("resultNumb") Long resultNumb);

    /**
     * 최종 처리 뒤 정책 보존기간이 지난 비공개 이미지 증거를 제한 건수만큼 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param retentionDays 최종 처리 후 증거 보존 일수
     * @param batchSize 한 번에 삭제할 최대 증거 수
     * @return 삭제된 신고 증거 수
     */
    int delExpiredEvidence(@Param("retentionDays") int retentionDays, @Param("batchSize") int batchSize);
}
