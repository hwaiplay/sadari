package org.our.sadari.global.common.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * 공통코드 BADX_WORD 기반으로 사용자 입력 데이터의 비속어 포함 여부를 검사하는 서비스 클래스이다.
 * 공통코드 EXCP_WORD 기반의 허용어 사전도 함께 사용하여 "시발점"처럼 비속어 문자열을 포함하지만 정상 표현인 단어를 오탐하지 않도록 처리한다.
 *
 * 주요 동작 특징
 * 1. 데이터베이스 접근 최소화
 *    사용자가 독후감이나 댓글을 저장할 때마다 데이터베이스를 조회하면 성능 저하가 발생하므로
 *    메인 메모리 RAM에 비속어 목록을 캐싱하여 재사용한다.
 *
 * 2. 멀티스레드 안전성 보장
 *    스프링 싱글톤 객체 내부에서 volatile 키워드와 Double Checked Locking 패턴을 사용하여
 *    여러 스레드가 동시에 접근하더라도 데이터베이스 중복 조회 및 캐시 불일치 문제를 방지한다.
 *
 * 3. 변종 우회 문자 차단
 *    공백 제거, 특수문자 제거, 숫자 포함 문자 정규화를 거쳐
 *    의도적으로 기호나 숫자 또는 한글/영문 끼워넣기를 섞어 넣은 우회 비속어까지 탐지한다.
 *
 * 4. 아호-코라식 문자열 탐색
 *    비속어 사전 전체를 매번 순회하지 않고 캐시 갱신 시점에 트라이와 실패 링크를 미리 구성한다.
 *    실제 검사 시에는 사용자 입력 문자열을 한 번만 순회하면서 포함된 비속어를 찾는다.
 *
 * @author Seonghyeon.Kang
 */
@Service
@RequiredArgsConstructor
public class BadWordDetectionService {

    // 비속어 목록을 메모리에 보관할 만료 시간이다. 10분으로 설정되어 있다.
    private static final long BAD_WORD_CACHE_TTL_MILLIS = 10 * 60 * 1000L;

    // 공통코드 데이터를 데이터베이스에서 조회해 오는 유틸리티 클래스이다.
    private final CodeUtil codeUtil;

    // 비속어 목록과 만료 시간을 담고 있는 캐시 객체이다.
    // volatile 키워드를 사용하여 각 스레드가 CPU 캐시 메모리가 아닌 메인 메모리 RAM을 직접 바라보게 만든다.
    // 이를 통해 한 스레드가 캐시를 새 인스턴스로 교체했을 때 다른 스레드들이 변경 사항을 즉시 인지할 수 있다.
    private volatile BadWordCache badWordCache = BadWordCache.empty();

    /**
     * 입력된 문자열에서 탐지된 비속어 단어를 찾아 Optional 형태로 반환한다.
     * 비속어가 발견되더라도 같은 문자열 안에서 EXCP_WORD 허용어가 더 넓은 범위로 감싸고 있으면 정상 표현으로 보고 통과시킨다.
     * 원문, 특수문자 제거본, 한글/영문 끼워넣기 우회본, 숫자 보존 정규화본을 순차적으로 검사한다.
     *
     * 예시 처리 과정:
     * 입력값 "안 녕 씨 아 발 1 8 년"
     * 1단계 blankRemovedValue = "안녕씨아발18년" (띄어쓰기 replace)
     * 2단계 normalizedWithoutDigits = "안녕씨아발년" (숫자 및 특수문자 제거)
     * 3단계 normalizedWithDigits = "안녕씨아발18년" (특수문자만 제거, 숫자 보존)
     *
     * @author Seonghyeon.Kang
     * @param value 검사할 사용자 입력 문자열
     * @return 감지된 비속어 단어
     */
    public Optional<String> findBadWord(String value) {
        // 검사할 입력 문자열이 null이거나 빈 값인 경우
        // 메모리 캐시 조회나 정규화 연산을 수행할 필요가 없으므로 즉시 빈 Optional을 반환한다.
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        // 메모리 캐시에서 아호-코라식 자동자를 가져온다. 만료되었다면 내부적으로 DB에서 비속어 사전을 다시 읽고 자동자를 재생성한다.
        BadWordCache cache = getBadWordCache();

        // 1단계 변환: 공백을 모두 제거하여 띄어쓰기로 우회한 비속어를 검사하기 위한 문자열이다.
        String blankRemovedValue = value.replace(" ", "");

        // 2단계 변환: 한글과 영문만 남기고 특수문자 및 숫자를 모두 제거한 문자열이다.
        String normalizedWithoutDigits = normalizeObfuscatedBadWord(value, false);

        // 3단계 변환: 숫자 포함 비속어 검사를 위해 숫자는 남겨두고 특수문자만 제거한 문자열이다.
        String normalizedWithDigits = normalizeObfuscatedBadWord(value, true);

        // Optional.or 메서드를 사용하여 단계별로 비속어를 탐지한다.
        // 람다식을 사용한 지연 평가 방식으로 작동하므로
        // 앞 단계에서 비속어가 발견되면 뒤 단계의 검사 로직은 실행되지 않고 즉시 종료되어 CPU 자원을 아낀다.
        return findBadWord(cache.badWordMatcher(), cache.exceptionWordMatcher(), blankRemovedValue)
                .or(() -> findBadWord(cache.badWordMatcher(), cache.exceptionWordMatcher(), normalizedWithoutDigits))
                .or(() -> findBadWordWithLetterGap(cache.badWordMatcher(), cache.exceptionWordMatcher(), normalizedWithoutDigits))
                .or(() -> findDigitBadWord(cache.digitBadWordMatcher(), cache.digitExceptionWordMatcher(), normalizedWithDigits));
    }

    /**
     * 메모리에 캐싱된 아호-코라식 자동자 묶음을 반환한다.
     * 캐시 만료 시 Double Checked Locking 패턴을 사용하여 단 하나의 스레드만 데이터베이스를 조회하도록 제어한다.
     *
     * @author Seonghyeon.Kang
     * @return 메인 메모리에 적재된 비속어 탐색 캐시
     */
    private BadWordCache getBadWordCache() {
        long now = System.currentTimeMillis();
        BadWordCache currentCache = badWordCache;

        // 1차 검사: 동기화 블록 밖에서 빠르게 캐시 만료 여부를 확인한다.
        // 99퍼센트의 정상 요청은 synchronized 락을 획득하는 오버헤드 없이 바로 메인 메모리의 캐시 데이터를 반환한다.
        if (!currentCache.isExpired(now)) {
            return currentCache;
        }

        // 캐시가 만료된 경우 여러 스레드가 동시에 DB 조회를 시도하는 것을 막기 위해 동기화 블록에 진입한다.
        synchronized (this) {
            currentCache = badWordCache;

            // 2차 검사: 락 내부에서 만료 여부를 한 번 더 확인한다.
            // 락 획득을 위해 대기하던 다른 스레드들이 1등 스레드가 이미 캐시를 갱신해 둔 것을 확인하고
            // 중복해서 데이터베이스를 조회하지 않도록 차단한다.
            if (!currentCache.isExpired(now)) {
                return currentCache;
            }

            // 데이터베이스에서 최신 비속어 목록과 예외 허용어 목록을 다시 읽어온다.
            // 두 사전을 같은 캐시 생명주기로 관리해야 BADX_WORD만 새로 반영되고 EXCP_WORD는 예전 상태로 남는 불일치를 막을 수 있다.
            List<String> reloadedBadWords = loadBadWordsFromCodeList();
            List<String> reloadedExceptionWords = loadExceptionWordsFromCodeList();
            List<String> digitBadWords = reloadedBadWords.stream()
                    .filter(this::hasDigit)
                    .toList();
            List<String> digitExceptionWords = reloadedExceptionWords.stream()
                    .filter(this::hasDigit)
                    .toList();

            // 읽어온 비속어 리스트로 아호-코라식 자동자를 미리 만든 뒤 캐시에 넣는다.
            // 이 작업을 캐시 갱신 시점에 한 번만 수행하면 실제 저장/수정 검증 요청에서는 입력 문자열 순회 비용만 발생한다.
            AhoCorasickMatcher badWordMatcher = AhoCorasickMatcher.from(reloadedBadWords);
            AhoCorasickMatcher exceptionWordMatcher = AhoCorasickMatcher.from(reloadedExceptionWords);
            AhoCorasickMatcher digitBadWordMatcher = AhoCorasickMatcher.from(digitBadWords);
            AhoCorasickMatcher digitExceptionWordMatcher = AhoCorasickMatcher.from(digitExceptionWords);

            // 생성된 자동자와 만료 시각을 담은 새 BadWordCache 인스턴스를 생성하여 참조를 교체한다.
            // 인스턴스 교체 작업은 원자적 연산이므로 멀티스레드 환경에서 불완전한 상태의 객체가 노출되지 않는다.
            BadWordCache reloadedCache = new BadWordCache(
                    badWordMatcher,
                    exceptionWordMatcher,
                    digitBadWordMatcher,
                    digitExceptionWordMatcher,
                    now + BAD_WORD_CACHE_TTL_MILLIS
            );
            badWordCache = reloadedCache;

            return reloadedCache;
        }
    }

    /**
     * 데이터베이스의 공통코드 테이블에서 BADX_WORD 리스트를 조회하여 문자열 목록으로 변환한다.
     *
     * @author Seonghyeon.Kang
     * @return 데이터베이스에서 조회한 비속어 문자열 리스트
     */
    private List<String> loadBadWordsFromCodeList() {
        return codeUtil.getCodeList(Constant.CODE_BADX_WORD).stream()
                .map(CodeDto::getComdName)
                .filter(word -> !StringUtil.isEmpty(word))
                .distinct()
                .toList();
    }

    /**
     * 데이터베이스의 공통코드 테이블에서 EXCP_WORD 리스트를 조회하여 문자열 목록으로 변환한다.
     * EXCP_WORD는 "시발점"처럼 비속어 문자열을 포함하지만 실제 서비스에서는 허용해야 하는 정상 단어 사전이다.
     *
     * @author Seonghyeon.Kang
     * @return 데이터베이스에서 조회한 비속어 예외 허용어 문자열 리스트
     */
    private List<String> loadExceptionWordsFromCodeList() {
        return codeUtil.getCodeList(Constant.CODE_EXCP_WORD).stream()
                .map(CodeDto::getComdName)
                .filter(word -> !StringUtil.isEmpty(word))
                .distinct()
                .toList();
    }

    /**
     * 아호-코라식 자동자를 사용해 입력 문자열에 포함된 비속어가 있는지 탐지한다.
     * 여러 비속어가 동시에 걸릴 경우 사용자 알림의 정확도를 위해 가장 긴 단어를 우선 선택한다.
     *
     * @author Seonghyeon.Kang
     * @param matcher 비속어 사전으로 구성한 아호-코라식 자동자
     * @param value 검사할 대상 문자열
     * @return 탐지된 비속어 중 가장 긴 단어
     */
    private Optional<String> findBadWord(AhoCorasickMatcher matcher, AhoCorasickMatcher exceptionMatcher, String value) {
        // 검사할 대상 문자열이 없으면 contains 비교 자체가 불필요하므로 즉시 반환한다.
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        // matcher 내부에는 모든 비속어가 트라이와 실패 링크로 컴파일되어 있다.
        // 따라서 단어 600개를 각각 contains로 검사하지 않고 입력 문자열의 글자 흐름을 한 번만 따라가며 매칭 결과를 찾는다.
        // 단, EXCP_WORD 허용어가 같은 구간을 감싸는 경우에는 정상 단어로 판단해야 하므로 위치 정보를 함께 비교한다.
        return findLongestBadWordOutsideException(matcher.findMatches(value), exceptionMatcher.findMatches(value));
    }

    /**
     * 비속어 글자 사이에 한글이나 영문 한 글자 또는 같은 글자가 연속으로 끼어든 우회 문자열을 탐지한다.
     * 예를 들어 비속어가 "씨발"이면 "씨아발", "씨아아발"처럼 같은 끼워넣기 문자가 반복된 형태까지 차단한다.
     *
     * @author Seonghyeon.Kang
     * @param matcher 비속어 사전으로 구성한 아호-코라식 자동자
     * @param value 한글과 영문만 남긴 검사 대상 문자열
     * @return 탐지된 비속어 중 가장 긴 단어
     */
    private Optional<String> findBadWordWithLetterGap(AhoCorasickMatcher matcher, AhoCorasickMatcher exceptionMatcher, String value) {
        // 끼워넣기 우회 검사는 정규화된 문자열이 있어야 의미가 있으므로 빈 값이면 즉시 종료한다.
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        // 일반 아호-코라식 검사는 정확한 연속 문자열 탐색이고, 이 메서드는 글자 사이의 제한된 노이즈를 허용하는 보강 검사이다.
        // 두 검사를 분리하면 기본 탐색 성능은 유지하면서 사용자가 의도적으로 한글/영문을 끼워 넣은 경우만 추가로 처리할 수 있다.
        return findLongestBadWordOutsideException(
                matcher.findMatchesWithSingleRepeatedLetterGap(value),
                exceptionMatcher.findMatches(value)
        );
    }

    /**
     * 숫자가 포함된 비속어 전용 아호-코라식 자동자로 특수문자만 제거된 입력 문자열을 비교한다.
     * 숫자가 포함된 비속어를 일반 문자열 정규화 단계에서 검사하면 정상적인 단어가 잘못 걸리는 오탐을 방지하기 위함이다.
     *
     * 예시:
     * 일반 문자열 정규화에서 숫자를 일률적으로 제거하면 "18년"이 "년"으로 변해 정상적인 문장이 오탐될 수 있다.
     * 따라서 숫자가 들어간 비속어는 숫자를 보존한 상태에서 별도 매처로 검사한다.
     *
     * @author Seonghyeon.Kang
     * @param digitMatcher 숫자 포함 비속어만 담은 아호-코라식 자동자
     * @param value 숫자가 보존되고 특수문자만 제거된 입력 문자열
     * @return 탐지된 숫자 포함 비속어
     */
    private Optional<String> findDigitBadWord(AhoCorasickMatcher digitMatcher, AhoCorasickMatcher digitExceptionMatcher, String value) {
        return findBadWord(digitMatcher, digitExceptionMatcher, value);
    }

    /**
     * 비속어 매칭 결과 중 EXCP_WORD 허용어 범위 밖에 있는 가장 긴 비속어를 선택한다.
     * 문장 안에 허용어와 실제 욕설이 함께 있을 수 있으므로 허용어가 하나라도 있으면 전체 문장을 통과시키는 방식은 사용하지 않는다.
     *
     * 예시:
     * "시발점은 알겠는데 시발"에서 첫 번째 "시발"은 "시발점" 범위 안에 있어 통과하지만,
     * 마지막 "시발"은 어떤 허용어 범위에도 포함되지 않으므로 차단 대상이 된다.
     *
     * @author Seonghyeon.Kang
     * @param badWordMatches BADX_WORD 사전으로 탐지한 비속어 위치 목록
     * @param exceptionMatches EXCP_WORD 사전으로 탐지한 허용어 위치 목록
     * @return 허용어 범위 밖에서 발견된 가장 긴 비속어
     */
    private Optional<String> findLongestBadWordOutsideException(List<MatchedWord> badWordMatches, List<MatchedWord> exceptionMatches) {
        String longestMatchedWord = null;

        for (MatchedWord badWordMatch : badWordMatches) {
            if (isCoveredByException(badWordMatch, exceptionMatches)) {
                continue;
            }

            if (longestMatchedWord == null || badWordMatch.word().length() > longestMatchedWord.length()) {
                longestMatchedWord = badWordMatch.word();
            }
        }

        return Optional.ofNullable(longestMatchedWord);
    }

    /**
     * 비속어 매칭 구간이 허용어 매칭 구간 안에 완전히 포함되는지 판단한다.
     * 부분적으로만 겹치는 경우는 사용자가 허용어 주변에 실제 욕설을 붙였을 가능성이 있으므로 예외 처리하지 않는다.
     *
     * @author Seonghyeon.Kang
     * @param badWordMatch 검사할 비속어 매칭 구간
     * @param exceptionMatches 허용어 매칭 구간 목록
     * @return 허용어가 비속어 구간을 완전히 감싸는지 여부
     */
    private boolean isCoveredByException(MatchedWord badWordMatch, List<MatchedWord> exceptionMatches) {
        return exceptionMatches.stream()
                .anyMatch(exceptionMatch -> exceptionMatch.startIndex() <= badWordMatch.startIndex()
                        && badWordMatch.endIndex() <= exceptionMatch.endIndex());
    }

    /**
     * 비속어 사이에 끼워 넣은 특수문자나 기호를 제거하기 위해 정규식을 사용하여 정규화한다.
     *
     * @author Seonghyeon.Kang
     * @param value 검사할 원본 문자열
     * @param keepDigits 숫자 보존 여부 옵션
     * @return 유효한 문자만 남긴 정규화 문자열
     */
    private String normalizeObfuscatedBadWord(String value, boolean keepDigits) {
        // keepDigits 옵션이 true이면 한글, 영문, 숫자를 제외한 모든 문자를 제거한다.
        // keepDigits 옵션이 false이면 한글, 영문만 남기고 숫자까지 포함한 모든 특수문자를 제거한다.
        // 유니코드 프로퍼티 표현식을 사용하여 완성형 및 조합형 한글과 영문 대소문자를 정확하게 판별한다.
        String allowedPattern = keepDigits
                ? "[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}]"
                : "[^\\p{IsHangul}\\p{IsAlphabetic}]";

        return value.replaceAll(allowedPattern, "");
    }

    /**
     * 비속어 단어 내부에 숫자가 포함되어 있는지 검사한다.
     *
     * @author Seonghyeon.Kang
     * @param value 검사할 비속어 단어
     * @return 숫자 포함 여부 boolean 값
     */
    private boolean hasDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }

    /**
     * 아호-코라식 탐색 결과 한 건을 표현하는 불변 데이터 객체이다.
     * 예외 허용어가 실제 비속어 구간을 감싸는지 비교해야 하므로 단어 문자열뿐 아니라 시작/끝 위치를 함께 보관한다.
     *
     * @author Seonghyeon.Kang
     * @param word 탐지된 단어
     * @param startIndex 입력 문자열에서 단어가 시작되는 인덱스
     * @param endIndex 입력 문자열에서 단어가 끝난 직후 인덱스
     */
    private record MatchedWord(String word, int startIndex, int endIndex) {
    }

    /**
     * 비속어 탐색 자동자와 해당 데이터의 만료 시각을 하나로 묶어 보관하는 불변 데이터 객체이다.
     * 자바 record 스펙을 사용하여 생성 후 내부 값을 변경할 수 없도록 보장한다.
     *
     * @author Seonghyeon.Kang
     * @param badWordMatcher 전체 비속어 사전으로 구성한 아호-코라식 자동자
     * @param digitBadWordMatcher 숫자 포함 비속어만 담은 아호-코라식 자동자
     * @param expiresAtMillis 캐시가 만료되는 에포크 밀리초 시각
     */
    private record BadWordCache(
            AhoCorasickMatcher badWordMatcher,
            AhoCorasickMatcher exceptionWordMatcher,
            AhoCorasickMatcher digitBadWordMatcher,
            AhoCorasickMatcher digitExceptionWordMatcher,
            long expiresAtMillis
    ) {

        /**
         * 애플리케이션 최초 구동 시 사용할 빈 캐시 인스턴스이다.
         * 만료 시각을 0으로 설정하여 첫 요청 시 무조건 데이터베이스에서 조회가 일어나도록 유도한다.
         *
         * @author Seonghyeon.Kang
         * @return 만료 시각이 0인 빈 BadWordCache 객체
         */
        private static BadWordCache empty() {
            return new BadWordCache(
                    AhoCorasickMatcher.empty(),
                    AhoCorasickMatcher.empty(),
                    AhoCorasickMatcher.empty(),
                    AhoCorasickMatcher.empty(),
                    0L
            );
        }

        /**
         * 현재 시각과 비교하여 캐시가 만료되었는지 여부를 판단한다.
         * 데이터베이스에 비속어 목록이 존재하지 않더라도 10분의 TTL 시간 동안은 재조회를 방지하기 위해
         * 리스트 크기가 아닌 만료 시각만을 기준으로 판별한다.
         *
         * @author Seonghyeon.Kang
         * @param nowMillis 비교할 현재 시각의 에포크 밀리초 값
         * @return 만료 여부 boolean 값
         */
        private boolean isExpired(long nowMillis) {
            return nowMillis >= expiresAtMillis;
        }
    }

    /**
     * 비속어 목록을 아호-코라식 알고리즘으로 탐색하기 위한 전용 매처이다.
     * 여러 단어를 하나의 트라이에 넣고 실패 링크를 구성하여 입력 문자열을 한 번만 순회해도 모든 단어 포함 여부를 판별한다.
     *
     * @author Seonghyeon.Kang
     */
    private static final class AhoCorasickMatcher {

        private final TrieNode root;

        /**
         * 외부에서 직접 생성하지 않고 from 또는 empty 팩토리 메서드로만 만들도록 제한한다.
         * 트라이 생성 후에는 검사 요청에서 구조가 바뀌지 않아야 하므로 생성 시점에 완성된 root만 보관한다.
         *
         * @author Seonghyeon.Kang
         * @param root 비속어 사전이 적재된 루트 노드
         */
        private AhoCorasickMatcher(TrieNode root) {
            this.root = root;
        }

        /**
         * 비속어 목록을 아호-코라식 트라이와 실패 링크로 변환한다.
         * 이 비용은 캐시 갱신 시점에만 발생하고, 이후 여러 저장/수정 요청은 이미 만들어진 자동자를 공유한다.
         *
         * @author Seonghyeon.Kang
         * @param words 공통코드 BADX_WORD에서 조회한 비속어 목록
         * @return 비속어 목록이 컴파일된 아호-코라식 매처
         */
        private static AhoCorasickMatcher from(List<String> words) {
            TrieNode root = new TrieNode();

            // 공통코드 값이 비어 있거나 중복 제거 후 빈 문자열이 섞여 있으면 트라이에 넣지 않는다.
            // 빈 문자열을 단어로 등록하면 모든 입력이 매칭되는 심각한 오탐이 발생할 수 있다.
            for (String word : words) {
                if (!StringUtil.isEmpty(word)) {
                    addWord(root, word);
                }
            }

            buildFailureLinks(root);
            return new AhoCorasickMatcher(root);
        }

        /**
         * 비속어가 하나도 없을 때 사용할 빈 매처를 생성한다.
         * DB 장애나 초기 빈 캐시 상황에서도 null 객체가 아닌 정상 매처를 사용하면 호출부의 null 분기를 제거할 수 있다.
         *
         * @author Seonghyeon.Kang
         * @return 매칭 결과가 없는 빈 아호-코라식 매처
         */
        private static AhoCorasickMatcher empty() {
            return new AhoCorasickMatcher(new TrieNode());
        }

        /**
         * 입력 문자열에서 가장 긴 비속어 매칭 결과를 찾는다.
         * 사용자에게 감지 단어를 알려줄 때 짧은 초성어보다 더 구체적인 긴 단어가 유용하므로 전체 순회 중 최장 결과를 보관한다.
         *
         * 동작 시뮬레이션 예시:
         * 비속어 사전 = ["카카오톡", "카오스"]
         * 사용자 입력 = "카카오스"
         * 1. '카' 읽음 -> root 아래 첫 번째 '카' 노드로 이동.
         * 2. '카' 읽음 -> '카' 노드 아래 두 번째 '카' 노드로 이동.
         * 3. '오' 읽음 -> 두 번째 '카' 노드 아래 '오' 노드로 이동. (현재 위치: 카카오)
         * 4. '스' 읽음 -> 현재 '오' 노드의 자식에는 '톡'만 있고 '스'가 없음 (매칭 실패).
         * 5. while 루프 실행: node = node.failure 구문을 통해 인덱스를 되돌리지 않고
         *    미리 뚫어둔 실패 링크 포탈을 타서 첫 번째 '카' 노드 아래의 '오' 노드로 0.000001초 만에 순간이동.
         * 6. 이동한 '오' 노드의 자식에서 '스'를 찾아 전진 -> '스' 노드 도착.
         * 7. '스' 노드에 미리 준비되어 있던 outputs 주머니를 확인 -> ["카오스"] 발견 및 감지 완료.
         *
         * @author Seonghyeon.Kang
         * @param value 검사할 사용자 입력 문자열
         * @return 탐지된 가장 긴 비속어
         */
        private List<MatchedWord> findMatches(String value) {
            TrieNode node = root;
            List<MatchedWord> matchedWords = new ArrayList<>();

            for (int index = 0; index < value.length(); ) {
                int codePoint = value.codePointAt(index);
                index += Character.charCount(codePoint);

                // 현재 노드에서 다음 문자로 이동할 수 없으면 실패 링크를 따라가며 가능한 접미사 상태를 찾는다.
                // 이 과정 덕분에 입력 문자열 인덱스를 뒤로 되돌리지 않고도 겹치는 단어 후보를 모두 탐지할 수 있다.
                while (node != root && !node.children.containsKey(codePoint)) {
                    node = node.failure;
                }

                node = node.children.getOrDefault(codePoint, root);

                // 실패 링크를 만들 때 부모 출력 목록을 자식에게 합쳐 두었기 때문에 현재 노드의 outputs만 보면 된다.
                // 사용자가 해당 노드에 처음 도착했더라도 outputs 안에는 사전 등록 시점에 넣은 완성 단어가 이미 준비되어 있다.
                // 여러 단어가 동시에 끝나는 위치라면 모두 기록한다.
                // 호출부에서 EXCP_WORD 범위와 비교한 뒤 최종적으로 가장 긴 차단 단어를 선택해야 하기 때문이다.
                for (String matchedWord : node.outputs) {
                    matchedWords.add(new MatchedWord(matchedWord, index - matchedWord.length(), index));
                }
            }

            return matchedWords;
        }

        /**
         * 비속어 각 글자 사이에 한글 또는 영문 한 종류가 끼어든 경우까지 포함하여 가장 긴 매칭 단어를 찾는다.
         * 정확한 아호-코라식 탐색으로 잡히지 않는 "씨아발", "fxxuck" 같은 우회 입력을 보강하기 위한 제한적 근사 탐색이다.
         *
         * @author Seonghyeon.Kang
         * @param value 한글과 영문만 남긴 검사 대상 문자열
         * @return 탐지된 가장 긴 비속어
         */
        private List<MatchedWord> findMatchesWithSingleRepeatedLetterGap(String value) {
            int[] codePoints = value.codePoints().toArray();
            List<MatchedWord> matchedWords = new ArrayList<>();

            for (int startIndex = 0; startIndex < codePoints.length; startIndex++) {
                matchedWords.addAll(findMatchesWithSingleRepeatedLetterGap(codePoints, startIndex));
            }

            return matchedWords;
        }

        /**
         * 특정 시작 위치에서 비속어 글자 사이의 제한된 끼워넣기 문자를 허용하며 트라이를 따라간다.
         * 한 전이 구간마다 하나의 한글/영문 문자 또는 같은 문자의 연속만 건너뛰도록 제한해 과도한 오탐을 막는다.
         *
         * 동작 시뮬레이션 예시:
         * 입력값 = "씨아아아발" (비속어 사전: "씨발")
         * 1. '씨' 노드 매칭 성공.
         * 2. 다음 글자 '아'를 만남 -> '씨' 노드의 자식에 '아'가 없으므로 정규 검사 실패.
         * 3. 끼워넣기 검사 발동 -> '아'가 연속으로 나오는지 확인하면서 인덱스를 넘김 ("아아아" 통과).
         * 4. 끼워넣기 문자가 끝난 직후 다음 글자인 '발'을 확인 -> '씨' 노드의 자식 '발'과 매칭 성공.
         * 5. "씨발" 탐지 성공.
         *
         * @author Seonghyeon.Kang
         * @param codePoints 검사 대상 문자열을 코드포인트 배열로 변환한 값
         * @param startIndex 검사를 시작할 코드포인트 배열 위치
         * @return 해당 시작 위치에서 탐지된 가장 긴 비속어
         */
        private List<MatchedWord> findMatchesWithSingleRepeatedLetterGap(int[] codePoints, int startIndex) {
            TrieNode node = root;
            List<MatchedWord> matchedWords = new ArrayList<>();
            int index = startIndex;

            while (index < codePoints.length) {
                int codePoint = codePoints[index];
                TrieNode nextNode = node.children.get(codePoint);

                // 현재 문자로 바로 다음 트라이 노드에 갈 수 있으면 가장 정상적인 매칭 경로이므로 그대로 진행한다.
                if (nextNode != null) {
                    node = nextNode;
                    index++;
                    addMatches(matchedWords, node.outputs, startIndex, index);
                    continue;
                }

                // 아직 비속어 첫 글자도 맞추지 못한 상태라면 끼워넣기 문자를 허용할 근거가 없다.
                // 시작 글자부터 다른 경우는 이 위치에서의 근사 탐색을 중단하고 다음 시작 위치로 넘긴다.
                if (node == root) {
                    break;
                }

                // 끼워넣기 허용 대상은 한글/영문 한 종류로 제한한다.
                // 숫자와 특수문자는 앞선 정규화 단계에서 이미 별도 처리하므로 여기서 다시 허용하지 않는다.
                if (!isHangulOrAlphabetic(codePoint)) {
                    break;
                }

                int gapCodePoint = codePoint;

                // 사용자가 같은 글자를 여러 번 늘려 쓰는 우회도 같은 한 종류의 끼워넣기 문자로 본다.
                // 예를 들어 "씨아아아발"은 "씨"와 "발" 사이에 "아"가 반복된 것으로 보고 한 번에 건너뛴다.
                while (index < codePoints.length && codePoints[index] == gapCodePoint) {
                    index++;
                }

                // 한 종류의 끼워넣기 문자를 건너뛴 직후에는 반드시 원래 비속어의 다음 글자가 와야 한다.
                // 다른 한글/영문이 또 나오면 서로 다른 노이즈가 연속된 것이므로 과도한 오탐 방지를 위해 중단한다.
                if (index >= codePoints.length) {
                    break;
                }

                nextNode = node.children.get(codePoints[index]);
                if (nextNode == null) {
                    break;
                }

                node = nextNode;
                index++;
                addMatches(matchedWords, node.outputs, startIndex, index);
            }

            return matchedWords;
        }

        /**
         * 현재 위치에서 완성된 출력 단어 목록을 매칭 결과 목록에 추가한다.
         * 끼워넣기 우회 탐색에서는 실제 입력에서 비속어 글자 사이에 허용된 노이즈 문자가 들어갈 수 있으므로
         * 단어 길이로 시작 위치를 역산하지 않고 탐색을 시작한 위치와 현재 위치를 그대로 범위로 사용한다.
         *
         * @author Seonghyeon.Kang
         * @param matchedWords 누적할 매칭 결과 목록
         * @param outputWords 현재 트라이 노드에서 끝나는 단어 목록
         * @param startIndex 탐색 시작 위치
         * @param endIndex 탐색이 끝난 직후 위치
         */
        private void addMatches(List<MatchedWord> matchedWords, List<String> outputWords, int startIndex, int endIndex) {
            for (String outputWord : outputWords) {
                matchedWords.add(new MatchedWord(outputWord, startIndex, endIndex));
            }
        }

        /**
         * 끼워넣기 우회 문자로 허용할 문자인지 판단한다.
         * 한글과 영문 외의 숫자, 기호, 공백은 이미 다른 정규화 단계에서 처리하므로 이 분기에서는 제외한다.
         *
         * @author Seonghyeon.Kang
         * @param codePoint 검사할 유니코드 코드포인트
         * @return 한글 또는 영문 여부
         */
        private boolean isHangulOrAlphabetic(int codePoint) {
            Character.UnicodeScript unicodeScript = Character.UnicodeScript.of(codePoint);

            return unicodeScript == Character.UnicodeScript.HANGUL
                    || unicodeScript == Character.UnicodeScript.LATIN;
        }

        /**
         * 단어 하나를 트라이에 삽입한다.
         * codePoint 단위로 순회해 한글, 영문, 숫자 외에 유니코드 조합 문자가 들어오더라도 문자 경계를 안전하게 처리한다.
         *
         * 동작 방식 예시:
         * "카카오톡"과 "카오스"를 연달아 삽입할 때:
         * 1. "카카오톡" 삽입: root -> '카' -> '카' -> '오' -> '톡' 노드 생성. '톡' 노드의 outputs에 "카카오톡" 추가.
         * 2. "카오스" 삽입: root 아래에 이미 '카' 노드가 존재하므로 computeIfAbsent를 통해 기존 '카' 노드를 재사용.
         * 3. '카' 노드 자식으로 '오' 노드가 새로 만들어져 가지가 갈라짐 ('카' -> '오' -> '스').
         * 4. '스' 노드의 outputs에 "카오스" 추가.
         *
         * @author Seonghyeon.Kang
         * @param root 트라이 루트 노드
         * @param word 삽입할 비속어 단어
         */
        private static void addWord(TrieNode root, String word) {
            TrieNode node = root;

            for (int index = 0; index < word.length(); ) {
                int codePoint = word.codePointAt(index); // 1. 진짜 유니코드 번호(int)를 뽑아낸다.
                index += Character.charCount(codePoint); // 2. 이 글자가 char 몇 칸짜리인지(1 or 2) 보고 그만큼 인덱스를 건너뛴다
                // computeIfAbsent 구문으로 이미 존재하는 첫 글자 노드는 새로 만들지 않고 기존 노드를 공유 및 재사용한다.
                node = node.children.computeIfAbsent(codePoint, ignored -> new TrieNode());
            }

            // 단어의 마지막 글자 노드에 미리 완공된 비속어 명칭을 적어둔다.
            // 사용자가 탐색할 때 노드에 도착하자마자 이 주머니를 꺼내어 어떤 비속어가 완성되었는지 즉시 판단한다.
            node.outputs.add(word);
        }

        /**
         * 트라이 노드마다 실패 링크를 구성한다.
         * 실패 링크는 현재까지 일치한 접두사가 끊겼을 때 재검사를 시작할 가장 긴 접미사 상태를 가리키는 포인터이다.
         *
         * BFS 및 큐 사용 이유:
         * 3단계 깊이 노드의 실패 링크를 계산하려면 2단계 부모 노드의 실패 링크가 이미 완성이 되어 있어야 한다.
         * 따라서 깊이 1단계(루트 직속 자식)부터 깊이 2단계, 3단계 순서로 위층에서 아래층으로 내려가며
         * 차례대로 실패 링크를 공사하기 위해 큐(Queue) 대기열을 사용한다.
         *
         * outputs 병합 이유:
         * 사전 단어가 "시발"과 "시발놈"일 때, "시발놈"의 '놈' 노드 실패 링크는 "시발"의 '발' 노드를 가리킨다.
         * 이때 child.outputs.addAll 구문을 통해 '발' 노드의 ["시발"]을 '놈' 노드의 outputs에 병합하여
         * '놈' 노드의 outputs가 ["시발놈", "시발"]을 갖게 만든다.
         * 덕분에 긴 단어 탐색 중에도 포함된 짧은 비속어를 누락 없이 동시에 탐지할 수 있다.
         *
         * @author Seonghyeon.Kang
         * @param root 실패 링크를 구성할 트라이 루트 노드
         */
        private static void buildFailureLinks(TrieNode root) {
            Queue<TrieNode> queue = new ArrayDeque<>();
            root.failure = root;

            // 루트의 바로 아래 노드(1글자 노드들)는 실패 시 다시 루트로 돌아가면 된다.
            // 2단계, 3단계 노드들의 실패 링크를 계산하기 위한 마중물로서 큐에 먼저 세워둔다.
            for (TrieNode child : root.children.values()) {
                child.failure = root;
                queue.add(child);
            }

            while (!queue.isEmpty()) {
                TrieNode current = queue.poll();

                for (Map.Entry<Integer, TrieNode> entry : current.children.entrySet()) {
                    int codePoint = entry.getKey();
                    TrieNode child = entry.getValue();
                    TrieNode failure = current.failure;

                    // 현재 노드의 실패 링크에서 같은 문자 전이가 나올 때까지 계속 거슬러 올라간다.
                    // 이 계산을 미리 해두면 검사 중에는 문자열을 되감지 않고 상태 이동만으로 다음 후보를 찾을 수 있다.
                    while (failure != root && !failure.children.containsKey(codePoint)) {
                        failure = failure.failure;
                    }

                    child.failure = failure.children.getOrDefault(codePoint, root);

                    // 실패 노드에서 끝나는 단어도 현재 노드에서 함께 끝난 것으로 봐야 한다.
                    // 예를 들어 긴 단어와 짧은 단어가 접미사를 공유할 때 누락 없이 탐지하기 위한 병합이다.
                    child.outputs.addAll(child.failure.outputs);
                    queue.add(child);
                }
            }
        }
    }

    /**
     * 아호-코라식 트라이의 단일 노드이다.
     * 각 노드는 다음 문자로 이동하는 자식 맵, 실패 링크, 현재 위치에서 끝나는 비속어 목록을 가진다.
     *
     * @author Seonghyeon.Kang
     */
    private static final class TrieNode {

        private final Map<Integer, TrieNode> children = new HashMap<>();
        private final List<String> outputs = new ArrayList<>();
        private TrieNode failure;
    }
}
