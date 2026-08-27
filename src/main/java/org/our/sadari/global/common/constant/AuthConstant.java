package org.our.sadari.global.common.constant;

/**
 * fileName       : AuthConstant
 * author         : SeungHyeon.Kang
 * date           : 2026-03-21
 * description    : 공통 처리에 사용하는 상수와 코드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 코드를 풀네임으로 변경
 * 2026-08-13        SeungHyeon.Kang    로컬 임시 회원 제공자 코드 추가
 * 2026-08-26        SeungHyeon.Kang         Kakao URL 설정 분리
 */
public class AuthConstant {

    // Kakao 로그인 제공자 코드
    public static final String PROV_KAKAO = "KAKAO";
    // Google 로그인 제공자 코드
    public static final String PROV_GOOGLE = "GOOGLE";
    // Naver 로그인 제공자 코드
    public static final String PROV_NAVER = "NAVER";
    // 개발 및 검증용 임시 회원 제공자 코드
    public static final String PROV_TEMP = "TEMP";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String ROLE_USER = "USER";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String ROLE_ADMIN = "ADMIN";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String KAKAO_GRANT_TYPE = "grant_type";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String KAKAO_CLIENT_ID = "client_id";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String KAKAO_REDIRECT_URI = "redirect_uri";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String KAKAO_CODE = "code";
    // 아래 처리 단계의 업무 목적을 설명한다.
    public static final String KAKAO_AUTHORIZATION_CODE = "authorization_code";
}
