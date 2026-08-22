package org.our.sadari.complaint.dto;

import lombok.Data;

/**
 * fileName       : ComplaintEvidenceDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 관리자 전용 프로필 사진 신고 증거 원본을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintEvidenceDto {

    // 신고 증거 번호
    private Long evdcNumb;

    // 신고 대상 유형 세부코드
    private String tagtType;

    // 신고 대상 번호
    private Long tagtNumb;

    // 신고 대상 버전 SHA-256 해시
    private String tagtHash;

    // 신고 대상 소유 사용자 번호
    private Long tagtUser;

    // 증거 원본 파일명
    private String origName;

    // 증거 MIME 유형
    private String mimeType;

    // 증거 파일 크기 바이트
    private Long fileSize;

    // 비공개 증거 원본 바이트
    private byte[] evdcData;
}
