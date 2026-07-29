package org.our.sadari.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.user.dto.NicknameSequenceDto;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.NicknameSequenceMapper;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;

/**
 * fileName       : NicknameGenerationServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 신규 회원 자동 닉네임의 조합과 동시 발급 처리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class NicknameGenerationServiceImplTest {

    // 공통코드 조회 유틸리티
    @Mock
    private CodeUtil codeUtil;

    // 닉네임 번호 발급 데이터 접근 객체
    @Mock
    private NicknameSequenceMapper nicknameSequenceMapper;

    // 사용자 데이터 접근 객체
    @Mock
    private UserMapper userMapper;

    // 신규 회원 자동 닉네임 발급 테스트 대상
    private NicknameGenerationServiceImpl nicknameGenerationService;

    /**
     * 각 테스트가 독립된 자동 닉네임 발급 서비스를 사용하도록 의존 객체를 주입한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 자동 닉네임 발급 단위 테스트 대상을 생성한다
        nicknameGenerationService = new NicknameGenerationServiceImpl(codeUtil, nicknameSequenceMapper, userMapper);
        // 주어와 서술어 및 동물 명사가 한 개씩 있는 테스트용 공통코드를 설정한다
        when(codeUtil.getCodeGroupList(List.of(
                Constant.CODE_NICK_SUBJ
              , Constant.CODE_NICK_PRED
              , Constant.CODE_NICK_ANML
        ))).thenReturn(getNicknameCodeGroupList());
    }

    /**
     * 현재 연월에 처음 선택된 문구에 첫 번째 번호를 붙여 닉네임을 발급하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setGeneratedNicknameIssuesFirstSequence() {
        // 현재 연월에 닉네임 조합이 아직 없도록 번호 갱신 결과를 설정한다
        when(nicknameSequenceMapper.uptNicknameSequence(any(NicknameSequenceDto.class))).thenReturn(0);
        // 닉네임 조합의 최초 번호 등록 결과를 설정한다
        when(nicknameSequenceMapper.setNicknameSequence(any(NicknameSequenceDto.class))).thenReturn(1);
        // 최초 발급 연월과 번호가 조회되도록 결과를 설정한다
        when(nicknameSequenceMapper.getNicknameSequenceDtl(any(NicknameSequenceDto.class)))
                .thenReturn(getIssuedSequence(1));
        // 자동 발급 닉네임이 기존 회원과 중복되지 않도록 조회 결과를 설정한다
        when(userMapper.getUserNickDuplicateCnt(any(UserDto.class))).thenReturn(0);

        // 공통코드 조합과 첫 번째 번호로 신규 회원 닉네임을 발급한다
        String generatedNickname = nicknameGenerationService.setGeneratedNickname();

        // 현재 연월의 첫 번째 번호가 붙은 닉네임인지 검증한다
        assertEquals("마음이 따뜻한 코끼리_26090001", generatedNickname);
    }

    /**
     * 같은 닉네임 조합의 최초 행이 동시에 등록되면 선행 행을 갱신해 다음 번호를 발급하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setGeneratedNicknameRetriesConcurrentInitialInsert() {
        // 최초 갱신에는 행이 없고 동시 등록 충돌 후 재시도에는 행이 갱신되도록 설정한다
        when(nicknameSequenceMapper.uptNicknameSequence(any(NicknameSequenceDto.class))).thenReturn(0, 1);
        // 다른 가입 요청이 최초 번호 행을 먼저 등록한 상황을 재현한다
        when(nicknameSequenceMapper.setNicknameSequence(any(NicknameSequenceDto.class)))
                .thenThrow(new DuplicateKeyException("duplicate nickname sequence"));
        // 동시 등록 충돌 후 두 번째 번호가 조회되도록 결과를 설정한다
        when(nicknameSequenceMapper.getNicknameSequenceDtl(any(NicknameSequenceDto.class)))
                .thenReturn(getIssuedSequence(2));
        // 자동 발급 닉네임이 기존 회원과 중복되지 않도록 조회 결과를 설정한다
        when(userMapper.getUserNickDuplicateCnt(any(UserDto.class))).thenReturn(0);

        // 동시 등록 충돌을 복구하고 다음 번호로 신규 회원 닉네임을 발급한다
        String generatedNickname = nicknameGenerationService.setGeneratedNickname();

        // 선행 가입자 다음의 두 번째 번호가 붙은 닉네임인지 검증한다
        assertEquals("마음이 따뜻한 코끼리_26090002", generatedNickname);
    }

    /**
     * 자동 닉네임 조합에 사용할 테스트용 공통코드 그룹을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 주어와 서술어 및 동물 명사 공통코드 그룹
     */
    private Map<String, List<CodeDto>> getNicknameCodeGroupList() {
        // 공통코드 그룹별 테스트 항목을 담을 맵을 생성한다
        Map<String, List<CodeDto>> codeGroupList = new LinkedHashMap<>();
        // 테스트용 닉네임 주어를 생성한다
        CodeDto subject = getCode("SUBJ_0001", "마음이", "PAIR_0001");
        // 테스트용 닉네임 서술어를 생성한다
        CodeDto predicate = getCode("PRED_0001", "따뜻한", "PAIR_0001");
        // 테스트용 닉네임 동물 명사를 생성한다
        CodeDto animal = getCode("ANML_0001", "코끼리", null);
        // 주어 공통코드 목록을 테스트 그룹에 설정한다
        codeGroupList.put(Constant.CODE_NICK_SUBJ, List.of(subject));
        // 서술어 공통코드 목록을 테스트 그룹에 설정한다
        codeGroupList.put(Constant.CODE_NICK_PRED, List.of(predicate));
        // 동물 명사 공통코드 목록을 테스트 그룹에 설정한다
        codeGroupList.put(Constant.CODE_NICK_ANML, List.of(animal));

        // 자동 닉네임 발급에 사용할 테스트 공통코드 그룹을 반환한다
        return codeGroupList;
    }

    /**
     * 테스트용 닉네임 세부코드를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param comdCode 세부코드
     * @param comdName 닉네임에 표시할 문구
     * @param opt1Code 주어와 서술어를 연결할 조합 식별값
     * @return 닉네임 세부코드
     */
    private CodeDto getCode(String comdCode, String comdName, String opt1Code) {
        // 테스트용 세부코드 정보를 담을 객체를 생성한다
        CodeDto codeDto = new CodeDto();
        // 세부코드 식별값을 설정한다
        codeDto.setComdCode(comdCode);
        // 닉네임에 표시할 코드명을 설정한다
        codeDto.setComdName(comdName);
        // 주어와 서술어를 연결할 조합 식별값을 설정한다
        codeDto.setOpt1Code(opt1Code);

        // 구성요소 문구와 조합 식별값이 설정된 세부코드를 반환한다
        return codeDto;
    }

    /**
     * 테스트용 닉네임 번호 발급 결과를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param lastNumb 마지막으로 발급된 번호
     * @return 발급 연월과 마지막 번호가 설정된 결과
     */
    private NicknameSequenceDto getIssuedSequence(int lastNumb) {
        // 테스트용 번호 발급 결과를 담을 객체를 생성한다
        NicknameSequenceDto sequenceDto = new NicknameSequenceDto();
        // 닉네임 발급 연월을 설정한다
        sequenceDto.setIssuYeam("2609");
        // 마지막 발급 번호를 설정한다
        sequenceDto.setLastNumb(lastNumb);

        // 발급 연월과 마지막 번호가 설정된 결과를 반환한다
        return sequenceDto;
    }
}
