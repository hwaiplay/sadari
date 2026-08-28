package org.our.sadari.welcome.dto;

import lombok.Data;

/**
 * fileName       : WelcomePageDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-28
 * description    : 사용자 웰컴 화면에 노출할 배포 페이지 문구와 이미지 및 순서를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        SeungHyeon.Kang    최초 생성
 */
@Data
public class WelcomePageDto {

    // 웰컴페이지 주키
    private Long wlcmNumb;
    // 현재 배포 버전 번호
    private Integer versNumb;
    // 웰컴페이지 소제목
    private String subxTitl;
    // 웰컴페이지 제목
    private String mainTitl;
    // 웰컴페이지 설명
    private String pageDesc;
    // 웰컴페이지 이미지 공개 경로
    private String imgeUrlx;
    // 사용자 노출 순서
    private Integer sortOrdr;
}
