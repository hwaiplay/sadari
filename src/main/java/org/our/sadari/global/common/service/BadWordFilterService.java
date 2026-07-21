package org.our.sadari.global.common.service;

import com.vane.badwordfiltering.BadWordFiltering;
import java.util.Comparator;
import java.util.Optional;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class BadWordFilterService {

    private BadWordFiltering badWordFiltering;

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

        BadWordFiltering filter = getBadWordFiltering();
        String normalizedValue = normalizeObfuscatedBadWord(value);

        return findBadWord(filter, value.replace(" ", ""))
                .or(() -> findBadWord(filter, normalizedValue));
    }

    /**
     * 긴 단어부터 비교하여 짧은 초성어보다 실제 감지된 욕설 단어가 우선 반환되도록 한다.
     * @author Seonghyeon.Kang
     * @param filter 욕설 사전
     * @param value 검사할 입력값
     * @return 감지된 욕설
     */
    private Optional<String> findBadWord(BadWordFiltering filter, String value) {
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        return filter.stream()
                .filter(value::contains)
                .max(Comparator.comparingInt(String::length));
    }

    /**
     * 욕설 사이에 끼운 숫자, 공백, 특수문자를 제거하고 실제 문자만 남긴다.
     * @author Seonghyeon.Kang
     * @param value 검사할 입력값
     * @return 우회문자를 제거한 문자열
     */
    private String normalizeObfuscatedBadWord(String value) {
        return value.replaceAll("[^\\p{IsHangul}\\p{IsAlphabetic}]", "");
    }

    private BadWordFiltering getBadWordFiltering() {
        if (StringUtil.isEmpty(badWordFiltering)) {
            badWordFiltering = new BadWordFiltering();
        }

        return badWordFiltering;
    }
}
