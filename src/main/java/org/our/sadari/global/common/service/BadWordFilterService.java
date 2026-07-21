package org.our.sadari.global.common.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadWordFilterService {

    private final CodeUtil codeUtil;

    /**
     * 원문과 우회문자 제거본을 함께 검사하여 숫자/특수문자를 끼운 욕설까지 차단한다.
     * @author Seonghyeon.Kang
     * @param value 검사할 입력값
     * @return 욕설 포함 여부
     */
    public boolean hasBadWord(String value) {
        return findBadWord(value).isPresent();
    }

    /**
     * 원문과 우회문자 제거본에서 감지된 욕설을 찾아 메시지에 사용할 단어를 반환한다.
     * @author Seonghyeon.Kang
     * @param value 검사할 입력값
     * @return 감지된 욕설
     */
    public Optional<String> findBadWord(String value) {
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        List<String> badWords = getBadWords();
        String blankRemovedValue = value.replace(" ", "");
        String normalizedWithoutDigits = normalizeObfuscatedBadWord(value, false);
        String normalizedWithDigits = normalizeObfuscatedBadWord(value, true);

        return findBadWord(badWords, blankRemovedValue)
                .or(() -> findBadWord(badWords, normalizedWithoutDigits))
                .or(() -> findDigitBadWord(badWords, normalizedWithDigits));
    }

    /**
     * BADX_WORD 코드리스트의 상세 코드명을 욕설 사전으로 사용한다.
     * DB 공통코드를 수정하면 배포 없이 필터링 단어를 조정할 수 있도록 코드값이 아닌 이름을 검사 기준으로 삼는다.
     *
     * @author Seonghyeon.Kang
     * @return 공통코드에 등록된 욕설 단어 목록
     */
    private List<String> getBadWords() {
        return codeUtil.getCodeList(Constant.CODE_BADX_WORD).stream()
                .map(CodeDto::getComdName)
                .filter(word -> !StringUtil.isEmpty(word))
                .distinct()
                .toList();
    }

    /**
     * 긴 단어부터 비교하여 짧은 초성어보다 실제 감지된 욕설 단어가 우선 반환되도록 한다.
     *
     * @author Seonghyeon.Kang
     * @param badWords 욕설 사전
     * @param value 검사할 입력값
     * @return 감지된 욕설
     */
    private Optional<String> findBadWord(List<String> badWords, String value) {
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        return badWords.stream()
                .filter(value::contains)
                .max(Comparator.comparingInt(String::length));
    }

    /**
     * 숫자가 포함된 욕설은 숫자를 제거한 문자열에서는 과하게 탐지될 수 있으므로 숫자를 보존한 정규화 문자열에서 별도 검사한다.
     *
     * @author Seonghyeon.Kang
     * @param badWords 욕설 사전
     * @param value 숫자를 보존하고 특수문자만 제거한 입력값
     * @return 감지된 욕설
     */
    private Optional<String> findDigitBadWord(List<String> badWords, String value) {
        return findBadWord(
                badWords.stream()
                        .filter(this::hasDigit)
                        .toList(),
                value
        );
    }

    /**
     * 욕설 사이에 끼운 숫자, 공백, 특수문자를 제거하고 실제 문자만 남긴다.
     * @author Seonghyeon.Kang
     * @param value 검사할 입력값
     * @return 우회문자를 제거한 문자열
     */
    private String normalizeObfuscatedBadWord(String value, boolean keepDigits) {
        String allowedPattern = keepDigits ? "[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}]" : "[^\\p{IsHangul}\\p{IsAlphabetic}]";
        return value.replaceAll(allowedPattern, "");
    }

    /**
     * 숫자가 실제 단어 구성인지 판단해 숫자 우회문자 제거 검사와 구분한다.
     *
     * @author Seonghyeon.Kang
     * @param value 검사할 욕설 사전 단어
     * @return 숫자 포함 여부
     */
    private boolean hasDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }
}
