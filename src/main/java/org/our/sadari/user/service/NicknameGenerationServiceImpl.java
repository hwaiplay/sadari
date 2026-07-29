package org.our.sadari.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.user.dto.NicknameSequenceDto;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.NicknameSequenceMapper;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : NicknameGenerationServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 공통코드 조합과 연월별 순번으로 신규 회원 닉네임을 발급한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NicknameGenerationServiceImpl implements NicknameGenerationService {

    // 공통코드 오류나 기존 닉네임 충돌이 발생해도 다른 조합을 선택할 수 있는 최대 시도 횟수
    private static final int NICK_GENERATION_MAX_ATTEMPTS = 100;
    // 연월과 네 자리 순번 앞에 붙는 닉네임 구분자
    private static final String NICK_SUFFIX_SEPARATOR = "_";
    // 네 자리 닉네임 발급 번호 형식
    private static final String NICK_SEQUENCE_FORMAT = "%04d";

    // Code 공통 처리 유틸리티
    private final CodeUtil codeUtil;
    // NicknameSequence 데이터 접근 객체
    private final NicknameSequenceMapper nicknameSequenceMapper;
    // User 데이터 접근 객체
    private final UserMapper userMapper;

    /**
     * 공통코드 조합과 현재 연월의 순번으로 중복되지 않는 신규 회원 닉네임을 발급한다
     *
     * @author SeungHyeon.Kang
     * @return 신규 회원에게 저장할 자동 발급 닉네임
     * @throws IllegalStateException 닉네임 코드가 없거나 발급 가능한 조합을 찾지 못한 경우 발생
     */
    @Transactional
    @Override
    public String setGeneratedNickname() {
        // 신규 회원 닉네임을 한 번의 DB 조회로 조합할 수 있도록 세 공통코드를 일괄 조회한다
        Map<String, List<CodeDto>> codeGroupList = codeUtil.getCodeGroupList(List.of(
                Constant.CODE_NICK_SUBJ
              , Constant.CODE_NICK_PRED
              , Constant.CODE_NICK_ANML
        ));

        // 닉네임 조합에 사용할 주어 목록을 조회한다
        List<CodeDto> subjectList = codeGroupList.get(Constant.CODE_NICK_SUBJ);
        // 닉네임 조합에 사용할 서술어 목록을 조회한다
        List<CodeDto> predicateList = codeGroupList.get(Constant.CODE_NICK_PRED);
        // 닉네임 조합에 사용할 동물 명사 목록을 조회한다
        List<CodeDto> animalList = codeGroupList.get(Constant.CODE_NICK_ANML);

        // 세 구성요소 중 하나라도 없으면 불완전한 닉네임을 발급하지 않도록 가입 처리를 중단한다
        if (StringUtil.isEmpty(subjectList) || StringUtil.isEmpty(predicateList) || StringUtil.isEmpty(animalList)) {
            // 필수 공통코드 누락을 로그인 실패 흐름으로 전달할 예외를 생성한다
            throw new IllegalStateException("닉네임 공통코드가 비어 있습니다.");
        }

        // 기존 닉네임과 충돌하거나 한 조합의 번호가 소진되면 다른 조합으로 재시도한다
        for (int attempt = 0; attempt < NICK_GENERATION_MAX_ATTEMPTS; attempt++) {
            // 주어 공통코드 중 하나를 무작위로 선택한다
            CodeDto subject = getRandomCode(subjectList);
            // 선택한 주어와 자연스럽게 연결되는 서술어 목록을 조회한다
            List<CodeDto> compatiblePredicateList = getCompatiblePredicateList(subject, predicateList);

            // 주어에 연결된 서술어가 없으면 잘못된 코드 조합을 제외하고 다시 선택한다
            if (compatiblePredicateList.isEmpty()) {
                continue;
            }

            // 주어와 연결 가능한 서술어 중 하나를 무작위로 선택한다
            CodeDto predicate = getRandomCode(compatiblePredicateList);
            // 동물 명사 공통코드 중 하나를 무작위로 선택한다
            CodeDto animal = getRandomCode(animalList);
            // 공통코드 표시명을 공백으로 연결해 닉네임 본문을 구성한다
            String nicknameText = String.join(" ", subject.getComdName(), predicate.getComdName(), animal.getComdName());

            // 자동 발급 접미사를 포함해 25자를 넘는 코드 조합은 저장 정책에 맞지 않아 제외한다
            if (nicknameText.length() + Constant.NICK_GENERATED_SUFFIX_LENGTH > Constant.USER_NICK_MAX_LENGTH) {
                continue;
            }

            // 선택한 닉네임 본문에 현재 연월의 다음 순번을 발급한다
            NicknameSequenceDto sequenceDto = setNextNicknameSequence(nicknameText);

            // 한 조합이 네 자리 최대 번호를 모두 사용했으면 다른 조합으로 발급을 이어간다
            if (StringUtil.isEmpty(sequenceDto)) {
                continue;
            }

            // 발급한 연월과 네 자리 순번을 닉네임 본문 뒤에 연결한다
            String generatedNickname = nicknameText + NICK_SUFFIX_SEPARATOR + sequenceDto.getIssuYeam()
                    + String.format(Locale.ROOT, NICK_SEQUENCE_FORMAT, sequenceDto.getLastNumb());

            // 완성된 닉네임의 기존 사용자 중복 여부를 조회할 객체를 생성한다
            UserDto duplicateRequest = new UserDto();
            // 자동 발급한 닉네임을 중복 조회 조건에 설정한다
            duplicateRequest.setUserNick(generatedNickname);

            // 기존 사용자가 같은 닉네임을 사용하지 않을 때만 신규 회원 닉네임으로 확정한다
            if (userMapper.getUserNickDuplicateCnt(duplicateRequest) == 0) {
                // 공통코드 조합과 연월별 순번으로 확정한 닉네임을 반환한다
                return generatedNickname;
            }
        }

        // 제한 횟수 안에 유효한 조합을 찾지 못한 상태를 로그인 실패 흐름으로 전달할 예외를 생성한다
        throw new IllegalStateException("발급 가능한 닉네임을 찾지 못했습니다.");
    }

    /**
     * 코드 목록에서 한 항목을 균등한 확률로 선택한다
     *
     * @author SeungHyeon.Kang
     * @param codeList 무작위 항목을 선택할 공통코드 목록
     * @return 무작위로 선택된 공통코드
     */
    private CodeDto getRandomCode(List<CodeDto> codeList) {
        // 요청마다 독립적인 난수를 사용해 같은 코드 조합으로 가입자가 집중되는 것을 줄인다
        int randomIndex = ThreadLocalRandom.current().nextInt(codeList.size());

        // 계산된 목록 위치의 공통코드를 반환한다
        return codeList.get(randomIndex);
    }

    /**
     * 주어와 같은 조합 식별값을 가진 서술어 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param subject 선택된 닉네임 주어 코드
     * @param predicateList 전체 닉네임 서술어 코드 목록
     * @return 주어와 자연스럽게 연결되는 서술어 목록
     */
    private List<CodeDto> getCompatiblePredicateList(CodeDto subject, List<CodeDto> predicateList) {
        // 연결 가능한 서술어를 원본 정렬 순서대로 담을 목록을 생성한다
        List<CodeDto> compatiblePredicateList = new ArrayList<>();

        // 공통코드 옵션의 조합 식별값이 같은 서술어만 후보에 포함한다
        for (CodeDto predicate : predicateList) {
            // 주어와 서술어의 조합 식별값이 같으면 자연스러운 문구 후보로 추가한다
            if (!StringUtil.isEmpty(subject.getOpt1Code()) && subject.getOpt1Code().equals(predicate.getOpt1Code())) {
                // 검증된 서술어를 무작위 선택 후보 목록에 추가한다
                compatiblePredicateList.add(predicate);
            }

        }

        // 선택한 주어와 연결할 수 있는 서술어 목록을 반환한다
        return compatiblePredicateList;
    }

    /**
     * 닉네임 본문과 현재 연월에 대응하는 다음 네 자리 번호를 원자적으로 발급한다
     *
     * @author SeungHyeon.Kang
     * @param nicknameText 순번을 발급할 닉네임 본문
     * @return 발급 연월과 마지막 번호 또는 번호가 소진된 경우 null
     */
    private NicknameSequenceDto setNextNicknameSequence(String nicknameText) {
        // 닉네임 번호 발급 조건을 담을 객체를 생성한다
        NicknameSequenceDto sequenceRequest = new NicknameSequenceDto();
        // 공통코드로 조합한 닉네임 본문을 번호 발급 조건에 설정한다
        sequenceRequest.setNickText(nicknameText);

        // 기존 행을 갱신하며 동일 조합의 동시 발급 요청을 Oracle 행 잠금으로 직렬화한다
        int updateCnt = nicknameSequenceMapper.uptNicknameSequence(sequenceRequest);

        // 현재 연월에 처음 선택된 조합이면 최초 번호 행을 등록한다
        if (updateCnt == 0) {
            // 최초 행 동시 등록 충돌과 번호 소진 상태를 구분해 처리한다
            try {
                // 현재 연월의 닉네임 조합에 첫 번째 번호를 등록한다
                nicknameSequenceMapper.setNicknameSequence(sequenceRequest);
            }

            // 다른 가입 요청이 같은 조합의 최초 행을 먼저 등록했으면 해당 행을 다시 증가시킨다
            catch (DuplicateKeyException e) {
                // 선행 트랜잭션이 만든 행을 잠근 뒤 다음 번호로 증가시킨다
                updateCnt = nicknameSequenceMapper.uptNicknameSequence(sequenceRequest);

                // 네 자리 번호가 모두 사용된 행은 증가하지 않으므로 다른 닉네임 조합을 선택하게 한다
                if (updateCnt == 0) {
                    // 번호가 소진된 조합임을 나타내는 null을 반환한다
                    return null;
                }

            }

        }

        // 현재 트랜잭션이 잠근 행에서 발급 연월과 증가된 번호를 조회한다
        NicknameSequenceDto issuedSequence = nicknameSequenceMapper.getNicknameSequenceDtl(sequenceRequest);

        // 발급 행이 조회되지 않으면 닉네임 생성이 계속되지 않도록 예외를 발생시킨다
        if (StringUtil.isEmpty(issuedSequence)) {
            // 발급 번호 데이터 무결성 오류를 로그인 실패 흐름으로 전달할 예외를 생성한다
            throw new IllegalStateException("닉네임 발급 번호를 조회하지 못했습니다.");
        }

        // 현재 가입 요청에 배정된 연월별 닉네임 번호를 반환한다
        return issuedSequence;
    }
}
