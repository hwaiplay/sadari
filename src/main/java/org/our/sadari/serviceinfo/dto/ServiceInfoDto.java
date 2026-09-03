package org.our.sadari.serviceinfo.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ServiceInfoDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 서비스 정보 카테고리와 현재 배포 버전의 HTML 본문 및 최근 수정 일시를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성 및 수정 일시 추가
 */
@Data
public class ServiceInfoDto {

    // 서비스 정보 카테고리 상세코드
    private String cateCode;
    // 서비스 정보 카테고리명
    private String cateName;
    // 현재 배포 버전 번호
    private Integer versNumb;
    // 현재 배포 서비스 정보 제목
    private String svciTitl;
    // 정제된 서비스 정보 HTML 본문
    private String svciCntn;
    // 현재 배포 서비스 정보 수정 일시
    private LocalDateTime updtDate;
    // 배포 일시
    private LocalDateTime dplyDate;
}
