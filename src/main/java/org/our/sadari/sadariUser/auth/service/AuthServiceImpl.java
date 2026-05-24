package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.sadariUser.auth.dto.KakaoAccountDto;
import org.our.sadari.sadariUser.auth.dto.KakaoTokenDto;
import org.our.sadari.sadariUser.auth.entity.TokenHistoryEntity;
import org.our.sadari.sadariUser.auth.entity.UserEntity;
import org.our.sadari.sadariUser.auth.provider.KakaoAuthProvider;
import org.our.sadari.sadariUser.auth.repository.TokenHistoryRepository;
import org.our.sadari.sadariUser.auth.repository.UserRepository;
import org.our.sadari.sadariUser.user.dto.TokenHistoryDto;
import org.our.sadari.sadariUser.user.dto.UserDto;
import org.our.sadari.sadariUser.user.mapper.TokenHistoryMapper;
import org.our.sadari.sadariUser.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    private final KakaoAuthProvider kakaoAuthProvider;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;
    private final TokenHistoryMapper tokenMapper;

    /**
     * 로그인 로직 (회원가입 여부 확인 및 JWT 발급)
     * @param: 발급된 카카오 액세스 토큰
     * @return: 컨트롤러에서 전달받은 인가 코드
     */
    @Transactional
    @Override
    public TokenDto kakaoLogin(String code) throws JsonProcessingException {

        // 카카오로부터 토큰 발급
        KakaoTokenDto kakaoTokenDto = kakaoAuthProvider.getKakaoToken(code);
        // 가져온 토큰으로 유저 정보 조회
        KakaoAccountDto kakaoAccountDto = kakaoAuthProvider.getKakaoAccount(kakaoTokenDto);

        // VO에서 필요한 사용자 정보 추출
        String providerId = String.valueOf(kakaoAccountDto.id);                       // 카카오 고유 식별자
        String nickName = kakaoAccountDto.kakao_account.profile.nickname;             // 카카오 닉네임
        String profileImg = kakaoAccountDto.kakao_account.profile.profile_image_url;  // 카카오 프로필 이미지

        UserDto userDto = new UserDto();
    
        try {

            // DB 조회: 기존에 가입된 사용자인지 확인
            UserDto getUser = userMapper.getUserByIdxx(providerId);
            
            // 소셜 로그인 유형 설정
            userDto.setUserProv(AuthConstant.PROV_KAKAO);
            // 고유 id 설정
            userDto.setUserIdxx(providerId);
            // 유저 권한 설정
            userDto.setUserRole(AuthConstant.ROLE_USER);
            // 닉네임 설정
            userDto.setUserNick(nickName);
            // 프로필 이미지 설정
            userDto.setPorfPath(profileImg);

            if (getUser == null) {
                // 신규 사용자라면 회원가입 처리
                userMapper.setUser(userDto);
                log.info("회원가입 완료 {}", providerId);
            }

            // 회원 번호 set
            userDto.setUserNumb(getUser.getUserNumb());

            log.info("회원정보 {}", userDto);

        } catch (Exception e){
            
            log.error("회원등록 중 오류발생 {}", e.getMessage());
            log.debug("회원등록 중 오류발생: ", e.getStackTrace());

            throw e;
        }

        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole());
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb());

        // 기존 토큰 삭제
        tokenMapper.deleteToken(userDto.getUserNumb());

        // 새 토큰 저장
        TokenHistoryDto tokenHistoryDto = new TokenHistoryDto();
        tokenHistoryDto.setUserNumb(userDto.getUserNumb());
        tokenHistoryDto.setRefrTokn(refreshToken);
        tokenHistoryDto.setCretDate(LocalDateTime.now());
        tokenHistoryDto.setExprDate(LocalDateTime.now().plusDays(7));
        
        tokenMapper.setToken(tokenHistoryDto);    

        // 서비스 전용 JWT 토큰 발급 및 반환
        TokenDto token = new TokenDto(accessToken, refreshToken);

        log.debug("로그인 처리 및 JWT 발급 완료: {}", token);

        return token;
    }
}