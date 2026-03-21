package org.our.sadari.common.constant;

/**
 * fileName       : AuthConstant
 * author         : seungHyeon.Kang
 * date           : 2026-03-15
 * description    : 소셜 로그인 등록 상수
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        seungHyeon.Kang   최초 생성
 */
public class AuthConstant {
    // 소셜 구분 KAKAO
    public static final String PROV_KAKAO = "KA";
    // 소셜 구분 구글
    public static final String PROV_GOOGLE = "GO";
    // 소셜 구분 네이버
    public static final String PROV_NAVER = "NA";
    // 권한 부여 방식 (토큰 요청 시 "authorization_code"로 고정)
    public static final String KAKAO_GRANT_TYPE = "grant_type";
    // 앱의 REST API 키 (카카오 내 애플리케이션 설정에서 확인 가능)
    public static final String KAKAO_CLIENT_ID = "client_id";
    // 인가 코드가 전달될 서비스의 리다이렉트 주소
    public static final String KAKAO_REDIRECT_URI = "redirect_uri";
    // 토큰 발급 시 필수 파라미터인 '인가 코드'의 키 이름
    public static final String KAKAO_CODE = "code";
    // GRANT_TYPE에 할당되는 실제 값 (권한 부여 코드 방식임을 명시)
    public static final String KAKAO_AUTHORIZATION_CODE = "authorization_code";
    // 카카오 인증 서버로부터 토큰을 발급받기 위한 엔드포인트 (Host + Path)
    public static final String KAKAO_AUTHORIZATION_URL = "https://kauth.kakao.com/oauth/token";
}
