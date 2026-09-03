package org.our.sadari.global.common.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * fileName       : BadWordDetectionService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-21
 * description    : 공통코드 사전으로 사용자 입력의 비속어와 우회 표현을 탐지함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-21        SeungHyeon.Kang    최초 생성
 * 2026-08-03        SeungHyeon.Kang    공백 경계와 반복 문자 우회 탐지 정책 반영
 */
@Service
@RequiredArgsConstructor
public class BadWordDetectionService {

    // 비속어 목록을 메모리에 보관할 만료 시간임. 10분으로 설정되어 있음
    private static final long BAD_WORD_CACHE_TTL_MILLIS = 10 * 60 * 1000L;

    // 공백이 아닌 같은 문자가 두 번 이상 연속된 우회 구간
    private static final Pattern REPEATED_CHARACTER_PATTERN = Pattern.compile("([^\\p{javaWhitespace}\\p{Z}])\\1+");

    // 공통코드 데이터를 데이터베이스에서 조회해 오는 유틸리티 클래스임
    private final CodeUtil codeUtil;

    // 비속어 목록과 만료 시간을 담고 있는 캐시 객체임
    // volatile 키워드를 사용하여 각 스레드가 CPU 캐시 메모리가 아닌 메인 메모리 RAM을 직접 바라보게 만듦
    // 이를 통해 한 스레드가 캐시를 새 인스턴스로 교체했을 때 다른 스레드들이 변경 사항을 즉시 인지할 수 있음
    private volatile BadWordCache badWordCache = BadWordCache.empty();

    /**
     * 입력된 문자열에서 탐지된 비속어 단어를 찾아 Optional 형태로 반환함
     * 비속어가 발견되더라도 같은 문자열 안에서 EXCP_WORD 허용어가 더 넓은 범위로 감싸고 있으면 정상 표현으로 보고 통과시킴
     * 공백은 단어 경계로 보존하고 숫자·특수문자 제거본과 숫자 보존 정규화본을 순차적으로 검사함
     * 공백이 없는 단어 안에서 같은 문자를 두 번 이상 반복한 구간은 한 글자로 축약한 값과 완전히 제거한 값을 추가 검사함
     * 한글이나 영문 한 글자만 끼워 넣은 경우는 자동 제거하지 않음
     *
     * 예시 처리 과정:
     * 입력값 "시이이이이발"
     * 1단계 normalizedWithoutDigits = "시이이이이발" (공백 보존, 숫자 및 특수문자 제거)
     * 2단계 collapsedRepeatedValue = "시이발" (반복 구간을 한 글자로 축약)
     * 3단계 removedRepeatedValue = "시발" (반복 구간을 제거)
     *
     * @author SeungHyeon.Kang
     * @param value 검사할 사용자 입력 문자열
     * @return 감지된 비속어 단어
     */
    public Optional<String> findBadWord(String value) {
        // 검사할 입력 문자열이 null이거나 빈 값인 경우
        // 메모리 캐시 조회나 정규화 연산을 수행할 필요가 없으므로 즉시 빈 Optional을 반환함
        if (StringUtil.isEmpty(value)) {
            // 입력 문자열에서 처음 탐지된 비속어를 Optional로 반환함
            return Optional.empty();
        }

        // 메모리 캐시에서 아호-코라식 자동자를 가져옴. 만료되었다면 내부적으로 DB에서 비속어 사전을 다시 읽고 자동자를 재생성함
        BadWordCache cache = getBadWordCache();

        // 1단계 변환: 공백 경계와 한글 및 영문만 남기고 특수문자와 숫자를 제거한 문자열임
        String normalizedWithoutDigits = normalizeBadWord(value, false);

        // 2단계 변환: 숫자 포함 비속어 검사를 위해 공백 경계와 숫자를 남겨두고 특수문자만 제거한 문자열임
        String normalizedWithDigits = normalizeBadWord(value, true);

        // 공백 경계를 유지한 정규화본과 반복 문자 변환본에서 일반 및 숫자 포함 비속어를 순차 탐지함
        // 입력 문자열에서 처음 탐지된 비속어를 Optional로 반환함
        return getRepeatedBadWordDtl(cache.badWordMatcher(), cache.exceptionWordMatcher(), normalizedWithoutDigits)
                .or(() -> getRepeatedBadWordDtl(cache.digitBadWordMatcher(), cache.digitExceptionWordMatcher(), normalizedWithDigits));
    }

    /**
     * 메모리에 캐싱된 아호-코라식 자동자 묶음을 반환함
     * 캐시 만료 시 Double Checked Locking 패턴을 사용하여 단 하나의 스레드만 데이터베이스를 조회하도록 제어함
     *
     * @author SeungHyeon.Kang
     * @return 메인 메모리에 적재된 비속어 탐색 캐시
     */
    private BadWordCache getBadWordCache() {
        // 만료 시각 계산에 사용할 현재 시각을 조회함
        long now = System.currentTimeMillis();
        BadWordCache currentCache = badWordCache;

        // 1차 검사: 동기화 블록 밖에서 빠르게 캐시 만료 여부를 확인함
        // 99퍼센트의 정상 요청은 synchronized 락을 획득하는 오버헤드 없이 바로 메인 메모리의 캐시 데이터를 반환함
        if (!currentCache.isExpired(now)) {
            // 메모리에 캐싱된 아호-코라식 자동자 묶음을 반환함
            return currentCache;
        }

        // 캐시가 만료된 경우 여러 스레드가 동시에 DB 조회를 시도하는 것을 막기 위해 동기화 블록에 진입함
        synchronized (this) {

            currentCache = badWordCache;

            // 2차 검사: 락 내부에서 만료 여부를 한 번 더 확인함
            // 락 획득을 위해 대기하던 다른 스레드들이 1등 스레드가 이미 캐시를 갱신해 둔 것을 확인하고
            // 중복해서 데이터베이스를 조회하지 않도록 차단함
            if (!currentCache.isExpired(now)) {
                // 메모리에 캐싱된 아호-코라식 자동자 묶음을 반환함
                return currentCache;
            }

            // 데이터베이스에서 최신 비속어 목록과 예외 허용어 목록을 다시 읽어옴
            // 두 사전을 같은 캐시 생명주기로 관리해야 BADX_WORD만 새로 반영되고 EXCP_WORD는 예전 상태로 남는 불일치를 막을 수 있음
            List<String> reloadedBadWords = loadBadWordsFromCodeList();
            // getExceptionWordList 호출로 처리에 사용할 기준 데이터를 적재함
            List<String> reloadedExceptionWords = getExceptionWordList();
            // 컬렉션 데이터를 순차 처리할 스트림을 생성함
            List<String> digitBadWords = reloadedBadWords.stream()
                    .filter(this::hasDigit)
                    .toList();
            // 컬렉션 데이터를 순차 처리할 스트림을 생성함
            List<String> digitExceptionWords = reloadedExceptionWords.stream()
                    .filter(this::hasDigit)
                    .toList();

            // 읽어온 비속어 리스트로 아호-코라식 자동자를 미리 만든 뒤 캐시에 넣음
            // 이 작업을 캐시 갱신 시점에 한 번만 수행하면 실제 저장/수정 검증 요청에서는 입력 문자열 순회 비용만 발생함
            AhoCorasickMatcher badWordMatcher = AhoCorasickMatcher.from(reloadedBadWords);
            // 비속어 원문에서 탐색용 문자 단위를 생성함
            AhoCorasickMatcher exceptionWordMatcher = AhoCorasickMatcher.from(reloadedExceptionWords);
            // 비속어 원문에서 탐색용 문자 단위를 생성함
            AhoCorasickMatcher digitBadWordMatcher = AhoCorasickMatcher.from(digitBadWords);
            // 비속어 원문에서 탐색용 문자 단위를 생성함
            AhoCorasickMatcher digitExceptionWordMatcher = AhoCorasickMatcher.from(digitExceptionWords);

            // 생성된 자동자와 만료 시각을 담은 새 BadWordCache 인스턴스를 생성하여 참조를 교체함
            // 인스턴스 교체 작업은 원자적 연산이므로 멀티스레드 환경에서 불완전한 상태의 객체가 노출되지 않음
            BadWordCache reloadedCache = new BadWordCache(badWordMatcher, exceptionWordMatcher, digitBadWordMatcher, digitExceptionWordMatcher, now + BAD_WORD_CACHE_TTL_MILLIS);
            badWordCache = reloadedCache;
            // 메모리에 캐싱된 아호-코라식 자동자 묶음을 반환함
            return reloadedCache;
        }
    }

    /**
     * 데이터베이스의 공통코드 테이블에서 BADX_WORD 리스트를 조회하여 문자열 목록으로 변환함
     *
     * @author SeungHyeon.Kang
     * @return 데이터베이스에서 조회한 비속어 문자열 리스트
     */
    private List<String> loadBadWordsFromCodeList() {
        // 데이터베이스의 공통코드 테이블에서 BADX_WORD 리스트를 조회하여 문자열 목록으로 변환 결과를 반환함
        return codeUtil.getCodeList(Constant.CODE_BADX_WORD).stream()
                .map(CodeDto::getComdName)
                .filter(word -> !StringUtil.isEmpty(word))
                .distinct()
                .toList();
    }

    /**
     * 데이터베이스의 공통코드 테이블에서 EXCP_WORD 리스트를 조회하여 문자열 목록으로 변환함
     * EXCP_WORD는 "시발점"처럼 비속어 문자열을 포함하지만 실제 서비스에서는 허용해야 하는 정상 단어 사전임
     *
     * @author SeungHyeon.Kang
     * @return 데이터베이스에서 조회한 비속어 예외 허용어 문자열 리스트
     */
    private List<String> getExceptionWordList() {
        // 데이터베이스의 공통코드 테이블에서 EXCP_WORD 리스트를 조회하여 문자열 목록으로 변환 결과를 반환함
        return codeUtil.getCodeList(Constant.CODE_EXCP_WORD).stream()
                .map(CodeDto::getComdName)
                .filter(word -> !StringUtil.isEmpty(word))
                .distinct()
                .toList();
    }

    /**
     * 아호-코라식 자동자를 사용해 입력 문자열에 포함된 비속어가 있는지 탐지함
     * 여러 비속어가 동시에 걸릴 경우 사용자 알림의 정확도를 위해 가장 긴 단어를 우선 선택함
     *
     * @author SeungHyeon.Kang
     * @param matcher 비속어 사전으로 구성한 아호-코라식 자동자
     * @param value 검사할 대상 문자열
     * @return 탐지된 비속어 중 가장 긴 단어
     */
    private Optional<String> findBadWord(AhoCorasickMatcher matcher, AhoCorasickMatcher exceptionMatcher, String value) {
        // 검사할 대상 문자열이 없으면 contains 비교 자체가 불필요하므로 즉시 반환함
        if (StringUtil.isEmpty(value)) {
            // 아호-코라식 자동자를 사용해 입력 문자열에 포함된 비속어가 있는지 탐지 결과를 반환함
            return Optional.empty();
        }

        // matcher 내부에는 모든 비속어가 트라이와 실패 링크로 컴파일되어 있음
        // 따라서 단어 600개를 각각 contains로 검사하지 않고 입력 문자열의 글자 흐름을 한 번만 따라가며 매칭 결과를 찾음
        // 아호-코라식 자동자를 사용해 입력 문자열에 포함된 비속어가 있는지 탐지 결과를 반환함
        return getLongestBadWordMatch(matcher.findMatches(value), exceptionMatcher.findMatches(value));
    }

    /**
     * 정규화 문자열과 반복 문자 축약본 및 제거본에서 비속어를 순차적으로 탐지함
     * 반복 문자를 한 글자로 줄이는 경우와 반복 구간 자체를 끼워 넣은 경우를 모두 차단함
     *
     * @author SeungHyeon.Kang
     * @param matcher 비속어 사전으로 구성한 아호-코라식 자동자
     * @param exceptionMatcher 예외 허용어 사전으로 구성한 아호-코라식 자동자
     * @param value 공백 경계를 보존한 정규화 문자열
     * @return 탐지된 비속어
     */
    private Optional<String> getRepeatedBadWordDtl(AhoCorasickMatcher matcher, AhoCorasickMatcher exceptionMatcher, String value) {
        // 반복 문자가 없는 일반 입력은 기존 정규화 문자열을 한 번만 탐색함
        Optional<String> matchedWord = findBadWord(matcher, exceptionMatcher, value);

        // 원본 정규화 문자열에서 비속어가 발견되면 반복 문자 변환 없이 결과를 확정함
        if (matchedWord.isPresent()) {
            // 원본 정규화 문자열에서 탐지된 비속어를 반환함
            return matchedWord;
        }

        // 같은 문자가 두 번 이상 이어지지 않으면 추가 정규화와 자동자 재탐색을 생략함
        if (!REPEATED_CHARACTER_PATTERN.matcher(value).find()) {
            // 반복 문자 우회가 없는 정상 입력의 빈 탐지 결과를 반환함
            return Optional.empty();
        }

        // 같은 문자 반복을 한 글자로 축약하여 원래 비속어 글자를 늘인 우회 표현을 복원함
        String collapsedRepeatedValue = REPEATED_CHARACTER_PATTERN.matcher(value).replaceAll("$1");
        // 같은 문자 반복 구간을 제거하여 비속어 사이에 별도 문자를 반복 삽입한 우회 표현을 복원함
        String removedRepeatedValue = REPEATED_CHARACTER_PATTERN.matcher(value).replaceAll("");

        // 정규화 문자열과 반복 문자 축약본 및 제거본에서 비속어를 순차적으로 탐지 결과를 반환함
        return findBadWord(matcher, exceptionMatcher, collapsedRepeatedValue)
                .or(() -> findBadWord(matcher, exceptionMatcher, removedRepeatedValue));
    }

    /**
     * 비속어 매칭 결과 중 EXCP_WORD 허용어 범위 밖에 있는 가장 긴 비속어를 선택함
     * 문장 안에 허용어와 실제 욕설이 함께 있을 수 있으므로 허용어가 하나라도 있으면 전체 문장을 통과시키는 방식은 사용하지 않음
     *
     * 예시:
     * "시발점은 알겠는데 시발"에서 첫 번째 "시발"은 "시발점" 범위 안에 있어 통과하지만,
     * 마지막 "시발"은 어떤 허용어 범위에도 포함되지 않으므로 차단 대상이 됨
     *
     * @author SeungHyeon.Kang
     * @param badWordMatches BADX_WORD 사전으로 탐지한 비속어 위치 목록
     * @param exceptionMatches EXCP_WORD 사전으로 탐지한 허용어 위치 목록
     * @return 허용어 범위 밖에서 발견된 가장 긴 비속어
     */
    private Optional<String> getLongestBadWordMatch(List<MatchedWord> badWordMatches, List<MatchedWord> exceptionMatches) {

        String longestMatchedWord = null;

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록임
        for (MatchedWord badWordMatch : badWordMatches) {
            // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분함
            if (isCoveredByException(badWordMatch, exceptionMatches)) {

                continue;
            }

            // longestMatchedWord 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
            if (StringUtil.isEmpty(longestMatchedWord) || badWordMatch.word().length() > longestMatchedWord.length()) {
                // 탐지 결과에서 원본 비속어를 조회함
                longestMatchedWord = badWordMatch.word();
            }
        }

        // 비속어 매칭 결과 중 EXCP_WORD 허용어 범위 밖에 있는 가장 긴 비속어를 선택 결과를 반환함
        return Optional.ofNullable(longestMatchedWord);
    }

    /**
     * 비속어 매칭 구간이 허용어 매칭 구간 안에 완전히 포함되는지 판단함
     * 부분적으로만 겹치는 경우는 사용자가 허용어 주변에 실제 욕설을 붙였을 가능성이 있으므로 예외 처리하지 않음
     *
     * @author SeungHyeon.Kang
     * @param badWordMatch 검사할 비속어 매칭 구간
     * @param exceptionMatches 허용어 매칭 구간 목록
     * @return 허용어가 비속어 구간을 완전히 감싸는지 여부
     */
    private boolean isCoveredByException(MatchedWord badWordMatch, List<MatchedWord> exceptionMatches) {
        // 비속어 매칭 구간이 허용어 매칭 구간 안에 완전히 포함되는지 판단 결과를 반환함
        return exceptionMatches.stream()
                .anyMatch(exceptionMatch -> exceptionMatch.startIndex() <= badWordMatch.startIndex()
                        // 탐지된 비속어가 끝나는 원문 위치를 조회함
                        && badWordMatch.endIndex() <= exceptionMatch.endIndex());
    }

    /**
     * 비속어 사이에 끼워 넣은 특수문자나 기호를 제거하되 공백은 단어 경계로 보존함
     *
     * @author SeungHyeon.Kang
     * @param value 검사할 원본 문자열
     * @param keepDigits 숫자 보존 여부 옵션
     * @return 유효한 문자만 남긴 정규화 문자열
     */
    private String normalizeBadWord(String value, boolean keepDigits) {
        // keepDigits 옵션이 true이면 한글, 영문, 숫자 및 공백을 제외한 모든 문자를 제거함
        // keepDigits 옵션이 false이면 한글, 영문 및 공백만 남기고 숫자까지 포함한 모든 특수문자를 제거함
        // 유니코드 프로퍼티 표현식을 사용하여 완성형 및 조합형 한글과 영문 대소문자를 정확하게 판별함
        String allowedPattern = keepDigits
                ? "[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}\\p{javaWhitespace}\\p{Z}]"
                : "[^\\p{IsHangul}\\p{IsAlphabetic}\\p{javaWhitespace}\\p{Z}]";
        // 비속어 사이에 끼워 넣은 특수문자나 기호를 제거하기 위해 정규식을 사용하여 정규화 결과를 반환함
        return value.replaceAll(allowedPattern, "");
    }

    /**
     * 비속어 단어 내부에 숫자가 포함되어 있는지 검사함
     *
     * @author SeungHyeon.Kang
     * @param value 검사할 비속어 단어
     * @return 숫자 포함 여부 boolean 값
     */
    private boolean hasDigit(String value) {
        // 비속어 단어 내부에 숫자가 포함되어 있는지 검사 결과를 반환함
        return value.chars().anyMatch(Character::isDigit);
    }

    /**
     * 아호-코라식 탐색 결과 한 건을 표현하는 불변 데이터 객체임
     * 예외 허용어가 실제 비속어 구간을 감싸는지 비교해야 하므로 단어 문자열뿐 아니라 시작/끝 위치를 함께 보관함
     *
     * @author SeungHyeon.Kang
     * @param word 탐지된 단어
     * @param startIndex 입력 문자열에서 단어가 시작되는 인덱스
     * @param endIndex 입력 문자열에서 단어가 끝난 직후 인덱스
     */
    private record MatchedWord(String word, int startIndex, int endIndex) {

    }

    /**
     * 비속어 탐색 자동자와 해당 데이터의 만료 시각을 하나로 묶어 보관하는 불변 데이터 객체임
     * 자바 record 스펙을 사용하여 생성 후 내부 값을 변경할 수 없도록 보장함
     *
     * @author SeungHyeon.Kang
     * @param badWordMatcher 전체 비속어 사전으로 구성한 아호-코라식 자동자
     * @param digitBadWordMatcher 숫자 포함 비속어만 담은 아호-코라식 자동자
     * @param expiresAtMillis 캐시가 만료되는 에포크 밀리초 시각
     */
    private record BadWordCache(AhoCorasickMatcher badWordMatcher, AhoCorasickMatcher exceptionWordMatcher, AhoCorasickMatcher digitBadWordMatcher
                              , AhoCorasickMatcher digitExceptionWordMatcher, long expiresAtMillis) {
        /**
         * 애플리케이션 최초 구동 시 사용할 빈 캐시 인스턴스임
         * 만료 시각을 0으로 설정하여 첫 요청 시 무조건 데이터베이스에서 조회가 일어나도록 유도함
         *
         * @author SeungHyeon.Kang
         * @return 만료 시각이 0인 빈 BadWordCache 객체
         */
        private static BadWordCache empty() {
            // 비속어 사전이 로딩되기 전 사용할 빈 캐시 객체를 반환함
            return new BadWordCache(AhoCorasickMatcher.empty(), AhoCorasickMatcher.empty(), AhoCorasickMatcher.empty(), AhoCorasickMatcher.empty(), 0L);
        }

        /**
         * 현재 시각과 비교하여 캐시가 만료되었는지 여부를 판단함
         * 데이터베이스에 비속어 목록이 존재하지 않더라도 10분의 TTL 시간 동안은 재조회를 방지하기 위해
         * 리스트 크기가 아닌 만료 시각만을 기준으로 판별함
         *
         * @author SeungHyeon.Kang
         * @param nowMillis 비교할 현재 시각의 에포크 밀리초 값
         * @return 만료 여부 boolean 값
         */
        private boolean isExpired(long nowMillis) {
            // 현재 시각과 비교하여 캐시가 만료되었는지 여부를 판단 결과를 반환함
            return nowMillis >= expiresAtMillis;
        }
    }

    /**
     * 비속어 목록을 아호-코라식 알고리즘으로 탐색하기 위한 전용 매처임
     * 여러 단어를 하나의 트라이에 넣고 실패 링크를 구성하여 입력 문자열을 한 번만 순회해도 모든 단어 포함 여부를 판별함
     *
     * @author SeungHyeon.Kang
     */
    private static final class AhoCorasickMatcher {

        // 비속어 탐색 트리 루트 노드
        private final TrieNode root;

        /**
         * 외부에서 직접 생성하지 않고 from 또는 empty 팩토리 메서드로만 만들도록 제한함
         * 트라이 생성 후에는 검사 요청에서 구조가 바뀌지 않아야 하므로 생성 시점에 완성된 root만 보관함
         *
         * @author SeungHyeon.Kang
         * @param root 비속어 사전이 적재된 루트 노드
         */
        private AhoCorasickMatcher(TrieNode root) {

            this.root = root;
        }

        /**
         * 비속어 목록을 아호-코라식 트라이와 실패 링크로 변환함
         * 이 비용은 캐시 갱신 시점에만 발생하고, 이후 여러 저장/수정 요청은 이미 만들어진 자동자를 공유함
         *
         * @author SeungHyeon.Kang
         * @param words 공통코드 BADX_WORD에서 조회한 비속어 목록
         * @return 비속어 목록이 컴파일된 아호-코라식 매처
         */
        private static AhoCorasickMatcher from(List<String> words) {
            // 비속어 탐색 트리의 루트 노드를 담을 객체를 생성함
            TrieNode root = new TrieNode();

            // 공통코드 값이 비어 있거나 중복 제거 후 빈 문자열이 섞여 있으면 트라이에 넣지 않음
            // 빈 문자열을 단어로 등록하면 모든 입력이 매칭되는 심각한 오탐이 발생할 수 있음
            for (String word : words) {
                // word 값이 비어 있을 때 후속 참조를 차단하기 위한 분기임
                if (!StringUtil.isEmpty(word)) {
                    // 비속어를 아호 코라식 탐색 트리에 등록함
                    addWord(root, word);
                }
            }

            // 아호 코라식 탐색 실패 링크를 구성함
            buildFailureLinks(root);
            // 새로 생성한 AhoCorasickMatcher 객체를 반환함
            return new AhoCorasickMatcher(root);
        }

        /**
         * 비속어가 하나도 없을 때 사용할 빈 매처를 생성함
         * DB 장애나 초기 빈 캐시 상황에서도 null 객체가 아닌 정상 매처를 사용하면 호출부의 null 분기를 제거할 수 있음
         *
         * @author SeungHyeon.Kang
         * @return 매칭 결과가 없는 빈 아호-코라식 매처
         */
        private static AhoCorasickMatcher empty() {
            // 새로 생성한 AhoCorasickMatcher 객체를 반환함
            return new AhoCorasickMatcher(new TrieNode());
        }

        /**
         * 입력 문자열에서 가장 긴 비속어 매칭 결과를 찾음
         * 사용자에게 감지 단어를 알려줄 때 짧은 초성어보다 더 구체적인 긴 단어가 유용하므로 전체 순회 중 최장 결과를 보관함
         *
         * 동작 시뮬레이션 예시:
         * 비속어 사전 = ["카카오톡", "카오스"]
         * 사용자 입력 = "카카오스"
         * 1. '카' 읽음 -> root 아래 첫 번째 '카' 노드로 이동
         * 2. '카' 읽음 -> '카' 노드 아래 두 번째 '카' 노드로 이동
         * 3. '오' 읽음 -> 두 번째 '카' 노드 아래 '오' 노드로 이동. (현재 위치: 카카오)
         * 4. '스' 읽음 -> 현재 '오' 노드의 자식에는 '톡'만 있고 '스'가 없음 (매칭 실패)
         * 5. while 루프 실행: node = node.failure 구문을 통해 인덱스를 되돌리지 않고
         *    미리 뚫어둔 실패 링크 포탈을 타서 첫 번째 '카' 노드 아래의 '오' 노드로 0.000001초 만에 순간이동
         * 6. 이동한 '오' 노드의 자식에서 '스'를 찾아 전진 -> '스' 노드 도착
         * 7. '스' 노드에 미리 준비되어 있던 outputs 주머니를 확인 -> ["카오스"] 발견 및 감지 완료
         *
         * @author SeungHyeon.Kang
         * @param value 검사할 사용자 입력 문자열
         * @return 탐지된 가장 긴 비속어
         */
        private List<MatchedWord> findMatches(String value) {

            TrieNode node = root;
            List<MatchedWord> matchedWords = new ArrayList<>();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록임
            for (int index = 0; index < value.length(); ) {
                // 현재 위치의 유니코드 코드 포인트를 확인함
                int codePoint = value.codePointAt(index);
                // 유니코드 문자가 차지하는 길이를 계산함
                index += Character.charCount(codePoint);

                // 현재 노드에서 다음 문자로 이동할 수 없으면 실패 링크를 따라가며 가능한 접미사 상태를 찾음
                // 이 과정 덕분에 입력 문자열 인덱스를 뒤로 되돌리지 않고도 겹치는 단어 후보를 모두 탐지할 수 있음
                while (node != root && !node.children.containsKey(codePoint)) {

                    node = node.failure;
                }

                // 키에 대응하는 값이 없을 때 기본값을 적용함
                node = node.children.getOrDefault(codePoint, root);

                // 실패 링크를 만들 때 부모 출력 목록을 자식에게 합쳐 두었기 때문에 현재 노드의 outputs만 보면 됨
                // 사용자가 해당 노드에 처음 도착했더라도 outputs 안에는 사전 등록 시점에 넣은 완성 단어가 이미 준비되어 있음
                // 여러 단어가 동시에 끝나는 위치라면 모두 기록함
                // 호출부에서 EXCP_WORD 범위와 비교한 뒤 최종적으로 가장 긴 차단 단어를 선택해야 하기 때문임
                for (String matchedWord : node.outputs) {
                    // 탐지된 비속어와 원문 위치를 담을 객체를 생성함
                    matchedWords.add(new MatchedWord(matchedWord, index - matchedWord.length(), index));
                }
            }

            // 입력 문자열에서 가장 긴 비속어 매칭 결과를 찾는다 결과를 반환함
            return matchedWords;
        }

        /**
         * 단어 하나를 트라이에 삽입함
         * codePoint 단위로 순회해 한글, 영문, 숫자 외에 유니코드 조합 문자가 들어오더라도 문자 경계를 안전하게 처리함
         *
         * 동작 방식 예시:
         * "카카오톡"과 "카오스"를 연달아 삽입할 때:
         * 1. "카카오톡" 삽입: root -> '카' -> '카' -> '오' -> '톡' 노드 생성. '톡' 노드의 outputs에 "카카오톡" 추가
         * 2. "카오스" 삽입: root 아래에 이미 '카' 노드가 존재하므로 computeIfAbsent를 통해 기존 '카' 노드를 재사용
         * 3. '카' 노드 자식으로 '오' 노드가 새로 만들어져 가지가 갈라짐 ('카' -> '오' -> '스')
         * 4. '스' 노드의 outputs에 "카오스" 추가
         *
         * @author SeungHyeon.Kang
         * @param root 트라이 루트 노드
         * @param word 삽입할 비속어 단어
         */
        private static void addWord(TrieNode root, String word) {

            TrieNode node = root;

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록임
            for (int index = 0; index < word.length(); ) {
                // 현재 위치의 유니코드 코드 포인트를 확인함
                int codePoint = word.codePointAt(index); // 1. 진짜 유니코드 번호(int)를 뽑아냄
                // 유니코드 문자가 차지하는 길이를 계산함
                index += Character.charCount(codePoint); // 2. 이 글자가 char 몇 칸짜리인지(1 or 2) 보고 그만큼 인덱스를 건너뜀
                // computeIfAbsent 구문으로 이미 존재하는 첫 글자 노드는 새로 만들지 않고 기존 노드를 공유 및 재사용함
                node = node.children.computeIfAbsent(codePoint, ignored -> new TrieNode());
            }

            // 단어의 마지막 글자 노드에 미리 완공된 비속어 명칭을 적어둠
            // 사용자가 탐색할 때 노드에 도착하자마자 이 주머니를 꺼내어 어떤 비속어가 완성되었는지 즉시 판단함
            node.outputs.add(word);
        }

        /**
         * 트라이 노드마다 실패 링크를 구성함
         * 실패 링크는 현재까지 일치한 접두사가 끊겼을 때 재검사를 시작할 가장 긴 접미사 상태를 가리키는 포인터임
         *
         * BFS 및 큐 사용 이유:
         * 3단계 깊이 노드의 실패 링크를 계산하려면 2단계 부모 노드의 실패 링크가 이미 완성이 되어 있어야 함
         * 따라서 깊이 1단계(루트 직속 자식)부터 깊이 2단계, 3단계 순서로 위층에서 아래층으로 내려가며
         * 차례대로 실패 링크를 공사하기 위해 큐(Queue) 대기열을 사용함
         *
         * outputs 병합 이유:
         * 사전 단어가 "시발"과 "시발놈"일 때, "시발놈"의 '놈' 노드 실패 링크는 "시발"의 '발' 노드를 가리킴
         * 이때 child.outputs.addAll 구문을 통해 '발' 노드의 ["시발"]을 '놈' 노드의 outputs에 병합하여
         * '놈' 노드의 outputs가 ["시발놈", "시발"]을 갖게 만든다.
         * 덕분에 긴 단어 탐색 중에도 포함된 짧은 비속어를 누락 없이 동시에 탐지할 수 있음
         *
         * @author SeungHyeon.Kang
         * @param root 실패 링크를 구성할 트라이 루트 노드
         */
        private static void buildFailureLinks(TrieNode root) {

            Queue<TrieNode> queue = new ArrayDeque<>();
            root.failure = root;

            // 루트의 바로 아래 노드(1글자 노드들)는 실패 시 다시 루트로 돌아가면 됨
            // 2단계, 3단계 노드들의 실패 링크를 계산하기 위한 마중물로서 큐에 먼저 세워둠
            for (TrieNode child : root.children.values()) {

                child.failure = root;
                // 처리한 값을 결과 컬렉션에 추가함
                queue.add(child);
            }

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록임
            while (!queue.isEmpty()) {
                // 너비 우선 탐색에서 다음 노드를 꺼냄
                TrieNode current = queue.poll();

                // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록임
                for (Map.Entry<Integer, TrieNode> entry : current.children.entrySet()) {
                    // 현재 항목의 키를 조회함
                    int codePoint = entry.getKey();
                    // 현재 항목의 값을 조회함
                    TrieNode child = entry.getValue();
                    TrieNode failure = current.failure;

                    // 현재 노드의 실패 링크에서 같은 문자 전이가 나올 때까지 계속 거슬러 올라감
                    // 이 계산을 미리 해두면 검사 중에는 문자열을 되감지 않고 상태 이동만으로 다음 후보를 찾을 수 있음
                    while (failure != root && !failure.children.containsKey(codePoint)) {

                        failure = failure.failure;
                    }

                    // 키에 대응하는 값이 없을 때 기본값을 적용함
                    child.failure = failure.children.getOrDefault(codePoint, root);

                    // 실패 노드에서 끝나는 단어도 현재 노드에서 함께 끝난 것으로 봐야 함
                    // 예를 들어 긴 단어와 짧은 단어가 접미사를 공유할 때 누락 없이 탐지하기 위한 병합임
                    child.outputs.addAll(child.failure.outputs);
                    // 처리한 값을 결과 컬렉션에 추가함
                    queue.add(child);
                }
            }
        }
    }

    /**
     * 아호-코라식 트라이의 단일 노드임
     * 각 노드는 다음 문자로 이동하는 자식 맵, 실패 링크, 현재 위치에서 끝나는 비속어 목록을 가짐
     *
     * @author SeungHyeon.Kang
     */
    private static final class TrieNode {

        private final Map<Integer, TrieNode> children = new HashMap<>();
        private final List<String> outputs = new ArrayList<>();
        // 아호 코라식 실패 이동 노드
        private TrieNode failure;
    }
}
