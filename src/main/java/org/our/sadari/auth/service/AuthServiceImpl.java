package org.our.sadari.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.auth.entity.UserEntity;
import org.our.sadari.auth.provider.JwtProvider;
import org.our.sadari.auth.provider.KakaoAuthProvider;
import org.our.sadari.auth.repository.UserRepository;
import org.our.sadari.auth.dto.KakaoAccountDto;
import org.our.sadari.auth.dto.KakaoTokenDto;
import org.our.sadari.common.constant.AuthConstant;
import org.springframework.stereotype.Service;

/**
 * fileName       : AuthServiceImpl
 * author         : seungHyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 소셜 로그인 토큰 발급 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        seungHyeon.Kang   최초 생성
 * 2026-03-17        hanWon.jang       리팩터리 및 JWT 토큰 발급
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final KakaoAuthProvider kakaoAuthProvider;
    private final JwtProvider jwtProvider;

    /**
     * 로그인 로직 (회원가입 여부 확인 및 JWT 발급)
     * @param: 발급된 카카오 액세스 토큰
     * @return: 컨트롤러에서 전달받은 인가 코드
     */
    @Override
    public String kakaoLogin(String code) throws JsonProcessingException {

        // 카카오로부터 토큰 발급
        KakaoTokenDto kakaoTokenDto = kakaoAuthProvider.getKakaoToken(code);
        // 가져온 토큰으로 유저 정보 조회
        KakaoAccountDto kakaoAccountDto = kakaoAuthProvider.getKakaoAccount(kakaoTokenDto);

        // VO에서 필요한 사용자 정보 추출
        String providerId = String.valueOf(kakaoAccountDto.id);                       // 카카오 고유 식별자
        String nickName = kakaoAccountDto.kakao_account.profile.nickname;             // 카카오 닉네임
        String profileImg = kakaoAccountDto.kakao_account.profile.profile_image_url;  // 카카오 프로필 이미지

        UserEntity userEntity = new UserEntity();
        try {
            // DB 조회: 기존에 가입된 사용자인지 확인
            userEntity = userRepository.findByUserIdxx(providerId)
                    .orElseGet(() -> {
                        // 신규 사용자라면 회원가입 처리 (DB 저장)
                        UserEntity newUserEntity = UserEntity.builder()
                                .nickname(nickName)
                                .userProv(AuthConstant.PROV_KAKAO)
                                .userIdxx(providerId)
                                .porfPath(profileImg)
                                .build();
                        return userRepository.save(newUserEntity);
                    });

            log.info("회원가입 완료 {}", providerId);

        } catch (Exception e){
            log.error("회원등록 중 오류발생 {}", e.getMessage());
            log.debug("회원등록 중 오류발생: ", e.getStackTrace());
        }

        // 서비스 전용 JWT 토큰 발급 및 반환
        String token = jwtProvider.createToken(userEntity.getUserNumb(), userEntity.getUserIdxx());

        log.debug("로그인 처리 및 JWT 발급 완료: {}", token);

        return token;
    }

}
