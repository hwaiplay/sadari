package org.our.sadari.complaint.dto;

import lombok.Data;

/**
 * fileName       : ComplaintDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 접수 시 저장할 대상 스냅샷과 사유를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성·자동 조치 대상 및 버전 정보 추가
 */
@Data
public class ComplaintDto {

    // 신고 번호
    private Long cmplNumb;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 대상 버전 SHA-256 해시
    private String tagtHash;

    // 신고 대상 소유 사용자 번호
    private Long tagtUser;

    // 신고 대상 내용 스냅샷
    private String tagtCntn;

    // 비공개 신고 이미지 증거 번호
    private Long evdcNumb;

    // 프로필 사진 신고 대상의 파일 번호
    private Long fileNumb;

    // 프로필 사진 신고 대상의 원본 파일명
    private String origName;

    // 프로필 사진 신고 대상의 서버 저장 파일명
    private String storName;

    // 프로필 사진 신고 대상의 접근 경로
    private String filePath;

    // 프로필 사진 신고 대상의 MIME 유형
    private String mimeType;

    // 신고 사유 세부코드
    private String cmplRson;

    // 신고 상세 내용
    private String cmplCntn;
}
