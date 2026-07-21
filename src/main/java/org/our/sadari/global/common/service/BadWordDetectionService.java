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
 *    의도적으로 기호나 숫자를 섞어 넣은 우회 비속어까지 탐지한다.
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
     * 원문, 특수문자 제거본, 숫자 보존 정규화본을 순차적으로 검사한다.
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

        // 1단계 변환 공백을 모두 제거하여 띄어쓰기로 우회한 비속어를 검사하기 위한 문자열이다.
        String blankRemovedValue = value.replace(" ", "");

        // 2단계 변환 한글과 영문만 남기고 특수문자 및 숫자를 모두 제거한 문자열이다.
        String normalizedWithoutDigits = normalizeObfuscatedBadWord(value, false);

        // 3단계 변환 숫자 포함 비속어 검사를 위해 숫자는 남겨두고 특수문자만 제거한 문자열이다.
        String normalizedWithDigits = normalizeObfuscatedBadWord(value, true);

        // Optional.or 메서드를 사용하여 단계별로 비속어를 탐지한다.
        // 람다식을 사용한 지연 평가 방식으로 작동하므로
        // 앞 단계에서 비속어가 발견되면 뒤 단계의 검사 로직은 실행되지 않고 즉시 종료되어 CPU 자원을 아낀다.
        return findBadWord(cache.badWordMatcher(), blankRemovedValue)
                .or(() -> findBadWord(cache.badWordMatcher(), normalizedWithoutDigits))
                .or(() -> findDigitBadWord(cache.digitBadWordMatcher(), normalizedWithDigits));
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

        // 1차 검사 동기화 블록 밖에서 빠르게 캐시 만료 여부를 확인한다.
        // 99퍼센트의 정상 요청은 synchronized 락을 획득하는 오버헤드 없이 바로 메인 메모리의 캐시 데이터를 반환한다.
        if (!currentCache.isExpired(now)) {
            return currentCache;
        }

        // 캐시가 만료된 경우 여러 스레드가 동시에 DB 조회를 시도하는 것을 막기 위해 동기화 블록에 진입한다.
        synchronized (this) {
            currentCache = badWordCache;

            // 2차 검사 락 내부에서 만료 여부를 한 번 더 확인한다.
            // 락 획득을 위해 대기하던 다른 스레드들이 1등 스레드가 이미 캐시를 갱신해 둔 것을 확인하고
            // 중복해서 데이터베이스를 조회하지 않도록 차단한다.
            if (!currentCache.isExpired(now)) {
                return currentCache;
            }

            // 데이터베이스에서 최신 비속어 목록을 다시 읽어온다.
            List<String> reloadedBadWords = loadBadWordsFromCodeList();
            List<String> digitBadWords = reloadedBadWords.stream()
                    .filter(this::hasDigit)
                    .toList();

            // 읽어온 비속어 리스트로 아호-코라식 자동자를 미리 만든 뒤 캐시에 넣는다.
            // 이 작업을 캐시 갱신 시점에 한 번만 수행하면 실제 저장/수정 검증 요청에서는 입력 문자열 순회 비용만 발생한다.
            AhoCorasickMatcher badWordMatcher = AhoCorasickMatcher.from(reloadedBadWords);
            AhoCorasickMatcher digitBadWordMatcher = AhoCorasickMatcher.from(digitBadWords);

            // 생성된 자동자와 만료 시각을 담은 새 BadWordCache 인스턴스를 생성하여 참조를 교체한다.
            // 인스턴스 교체 작업은 원자적 연산이므로 멀티스레드 환경에서 불완전한 상태의 객체가 노출되지 않는다.
            BadWordCache reloadedCache = new BadWordCache(
                    badWordMatcher,
                    digitBadWordMatcher,
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
     * 아호-코라식 자동자를 사용해 입력 문자열에 포함된 비속어가 있는지 탐지한다.
     * 여러 비속어가 동시에 걸릴 경우 사용자 알림의 정확도를 위해 가장 긴 단어를 우선 선택한다.
     *
     * @author Seonghyeon.Kang
     * @param matcher 비속어 사전으로 구성한 아호-코라식 자동자
     * @param value 검사할 대상 문자열
     * @return 탐지된 비속어 중 가장 긴 단어
     */
    private Optional<String> findBadWord(AhoCorasickMatcher matcher, String value) {
        // 검사할 대상 문자열이 없으면 contains 비교 자체가 불필요하므로 즉시 반환한다.
        if (StringUtil.isEmpty(value)) {
            return Optional.empty();
        }

        // matcher 내부에는 모든 비속어가 트라이와 실패 링크로 컴파일되어 있다.
        // 따라서 단어 600개를 각각 contains로 검사하지 않고 입력 문자열의 글자 흐름을 한 번만 따라가며 매칭 결과를 찾는다.
        return matcher.findLongest(value);
    }

    /**
     * 숫자가 포함된 비속어 전용 아호-코라식 자동자로 특수문자만 제거된 입력 문자열을 비교한다.
     * 숫자가 포함된 비속어를 일반 문자열 정규화 단계에서 검사하면 정상적인 단어가 잘못 걸리는 오탐을 방지하기 위함이다.
     *
     * @author Seonghyeon.Kang
     * @param digitMatcher 숫자 포함 비속어만 담은 아호-코라식 자동자
     * @param value 숫자가 보존되고 특수문자만 제거된 입력 문자열
     * @return 탐지된 숫자 포함 비속어
     */
    private Optional<String> findDigitBadWord(AhoCorasickMatcher digitMatcher, String value) {
        return findBadWord(digitMatcher, value);
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
            AhoCorasickMatcher digitBadWordMatcher,
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
         * @author Seonghyeon.Kang
         * @param value 검사할 사용자 입력 문자열
         * @return 탐지된 가장 긴 비속어
         */
        private Optional<String> findLongest(String value) {
            TrieNode node = root;
            String longestMatchedWord = null;

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
                // 여러 단어가 동시에 끝나는 위치라면 최장 단어를 남겨 사용자 메시지의 감지 단어 정확도를 높인다.
                for (String matchedWord : node.outputs) {
                    if (longestMatchedWord == null || matchedWord.length() > longestMatchedWord.length()) {
                        longestMatchedWord = matchedWord;
                    }
                }
            }

            return Optional.ofNullable(longestMatchedWord);
        }

        /**
         * 단어 하나를 트라이에 삽입한다.
         * codePoint 단위로 순회해 한글, 영문, 숫자 외에 유니코드 조합 문자가 들어오더라도 문자 경계를 안전하게 처리한다.
         *
         * @author Seonghyeon.Kang
         * @param root 트라이 루트 노드
         * @param word 삽입할 비속어 단어
         */
        private static void addWord(TrieNode root, String word) {
            TrieNode node = root;

            for (int index = 0; index < word.length(); ) {
                int codePoint = word.codePointAt(index);
                index += Character.charCount(codePoint);
                node = node.children.computeIfAbsent(codePoint, ignored -> new TrieNode());
            }

            node.outputs.add(word);
        }

        /**
         * 트라이 노드마다 실패 링크를 구성한다.
         * 실패 링크는 현재까지 일치한 접두사가 끊겼을 때 재검사를 시작할 가장 긴 접미사 상태를 가리키는 포인터이다.
         *
         * @author Seonghyeon.Kang
         * @param root 실패 링크를 구성할 트라이 루트 노드
         */
        private static void buildFailureLinks(TrieNode root) {
            Queue<TrieNode> queue = new ArrayDeque<>();
            root.failure = root;

            // 루트의 바로 아래 노드는 실패 시 다시 루트로 돌아가면 된다.
            // 이 초기화가 되어 있어야 BFS 과정에서 모든 하위 노드의 실패 링크를 안전하게 계산할 수 있다.
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
