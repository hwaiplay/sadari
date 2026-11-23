package org.our.sadari.report.service;

import org.our.sadari.global.common.logging.LogSafe;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.LocaleUtil;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.report.dto.ReportDto;
import org.our.sadari.report.dto.ReportTranslationDto;
import org.our.sadari.report.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : ReportTranslationService
 * author         : HanWon.Jang
 * date           : 2026-09-07
 * description    : Google 번역 호출과 독후감 번역 캐시 및 월간 사용량을 관리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-07        HanWon.Jang        최초 생성
 * 2026-09-08        HanWon.Jang        본문 기준 번역 언어 판정
 * 2026-09-19        SeungHyeon.Kang         운영 로그 및 안전한 오류 진단
 * 2026-11-23        SeungHyeon.Kang    외부 번역과 저장 트랜잭션 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportTranslationService {

    // 월간 번역 가능 글자 수를 원자적으로 확인하고 실제 호출분만 예약하는 Lua 스크립트
    private static final String RESERVE_CHARS_LUA = """
            local usedChars = tonumber(redis.call('GET', KEYS[1]) or '0')
            local requestChars = tonumber(ARGV[1])
            local limitChars = tonumber(ARGV[2])
            if usedChars + requestChars > limitChars then
                return -1
            end
            local nextChars = redis.call('INCRBY', KEYS[1], requestChars)
            if redis.call('TTL', KEYS[1]) < 0 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
            end
            return nextChars
            """;
    // 월별 앱 전체 번역 사용 글자 수 Redis 키 접두사
    private static final String MONTHLY_USAGE_KEY_PREFIX = "report:translation:chars:month:";
    // 월간 사용량을 예약하는 Redis 스크립트 실행 객체
    private static final DefaultRedisScript<Long> RESERVE_CHARS_SCRIPT =
            new DefaultRedisScript<>(RESERVE_CHARS_LUA, Long.class);
    // Redis가 월간 한도를 초과한 요청에 반환하는 결과값
    private static final long MONTHLY_LIMIT_EXCEEDED = -1L;
    // 만료 후 다른 요청이 얻은 잠금을 이전 소유자가 삭제하지 않도록 소유권 확인
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    // Google Cloud Translation Basic REST API 주소
    @Value("${google.translation.url}")
    private String translationUrl;
    // 서버에서만 사용하는 Google Cloud Translation API 키
    @Value("${google.translation.api-key:}")
    private String translationApiKey;
    // 앱 전체 Google 번역 월간 최대 글자 수
    @Value("${google.translation.monthly-character-limit:500000}")
    private long monthlyCharacterLimit;
    // Google 월간 사용량 경계와 맞출 번역 쿼터 시간대
    @Value("${google.translation.quota-zone:America/Los_Angeles}")
    private String quotaZone;
    // 인스턴스 간 동일 독후감 번역의 잠금 유효시간과 최대 대기시간
    @Value("${google.translation.lock-seconds:30}")
    private long lockSeconds;

    // 독후감 원문과 번역 캐시 데이터 접근 객체
    private final ReportMapper reportMapper;
    // 월간 번역 사용량을 공유할 Redis 문자열 연산 객체
    private final StringRedisTemplate redisTemplate;
    // Google 번역 REST API 호출 객체
    private final RestTemplate restTemplate;
    // 외부 통신 전후에만 짧은 DB 트랜잭션을 여는 관리자
    private final PlatformTransactionManager transactionManager;

    /**
     * 공개 독후감 목록에 원문 언어와 캐시 및 월간 잔여량을 반영한 번역 버튼 표시 여부를 설정
     *
     * @author HanWon.Jang
     * @param reports 공개 독후감 목록
     */
    public void applyReportAvailability(List<ReportDto> reports) {
        // 목록이 비어 있으면 Redis 조회 없이 번역 버튼 표시 계산을 종료
        if (StringUtil.isEmpty(reports)) {
            // 번역 가능 여부를 설정할 독후감이 없어 종료
            return;
        }

        // 한 요청 안의 모든 카드가 같은 월간 잔여량을 기준으로 버튼 표시 여부를 판단하도록 한 번만 조회
        long remainingChars = getRemainingChars();

        // 각 공개 독후감에 원문 언어와 유효 캐시 및 남은 글자 수를 함께 적용
        for (ReportDto report : reports) {
            // 카드별 번역 버튼 표시 여부를 서버 응답에 설정
            report.setTrnsAvaiYsno(getAvailability(
                    report.getLangCode(), report.getReptCntn(), report.getTrnsCacheYsno(), remainingChars));
        }
    }

    /**
     * 공개 피드 목록에 독후감 원문 언어와 캐시 및 월간 잔여량을 반영한 번역 버튼 표시 여부를 설정
     *
     * @author HanWon.Jang
     * @param feedList 공개 피드 목록
     */
    public void applyFeedAvailability(List<FeedDto> feedList) {
        // 목록이 비어 있으면 Redis 조회 없이 번역 버튼 표시 계산을 종료
        if (StringUtil.isEmpty(feedList)) {
            // 번역 가능 여부를 설정할 피드가 없어 종료
            return;
        }

        // 한 요청 안의 모든 피드가 같은 월간 잔여량을 기준으로 버튼 표시 여부를 판단하도록 한 번만 조회
        long remainingChars = getRemainingChars();

        // 독후감 피드만 원문 언어와 유효 캐시 및 남은 글자 수를 함께 적용
        for (FeedDto feed : feedList) {
            // 사진 피드는 원문 번역 대상이 아니므로 번역 버튼을 숨김
            if (!Constant.LIKE_TARGET_REPORT.equals(feed.getTagtType())) {
                // 번역 대상이 아닌 피드에 버튼 숨김 값을 설정
                feed.setTrnsAvaiYsno(Constant.COMM_NO);

                continue;
            }

            // 독후감 피드의 번역 버튼 표시 여부를 서버 응답에 설정
            feed.setTrnsAvaiYsno(getAvailability(
                    feed.getLangCode(), feed.getReptCntn(), feed.getTrnsCacheYsno(), remainingChars));
        }
    }

    /**
     * 로그인 사용자가 볼 수 있는 공개 독후감을 현재 표시 언어로 번역하고 결과를 캐시
     *
     * @author HanWon.Jang
     * @param userNumb 번역을 요청한 로그인 사용자 번호
     * @param reptNumb 번역할 공개 독후감 번호
     * @return 캐시 여부와 번역문을 담은 처리 결과
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResultData setReportTranslation(Long userNumb, Long reptNumb) {
        // 인증 사용자와 독후감 번호가 없으면 외부 번역 호출 전에 요청을 거부
        if (StringUtil.hasEmpty(userNumb, reptNumb) || reptNumb <= 0) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 유효 캐시는 Redis 잠금 장애와 무관하게 기존 경로로 반환
        ResultData cached = getTranslationResult(userNumb, reptNumb, false);
        // 접근 거절과 캐시 적중 및 번역 불가 결과는 외부 호출 없이 확정
        if (!StringUtil.isEmpty(cached)) {
            // 외부 번역이 필요하지 않은 현재 요청 결과 반환
            return cached;
        }

        // 기존 원문 행 잠금과 같은 독후감 단위의 인스턴스 간 실행 직렬화
        String lockKey = "report:translation:lock:" + reptNumb;
        // 만료 이후의 새 소유자를 구분할 요청별 잠금 식별값 생성
        String owner = UUID.randomUUID().toString();
        // Redis 장애와 잠금 대기 초과 시 외부 비용 발생 차단
        if (!getTranslationLock(lockKey, owner)) {
            // "번역 기능을 사용할 수 없어요.\n잠시 후 다시 시도해주세요."
            return ResultData.fail(ResultEnum.REPORT_TRANSLATION_FAILED);
        }

        // 잠금 대기 동안 변경된 원문과 권한 및 캐시를 다시 확인
        try {
            // DB 트랜잭션 밖의 외부 번역과 짧은 저장 트랜잭션 수행
            return getTranslationResult(userNumb, reptNumb, true);
        }

        // 성공과 실패 및 접근 거절 모두 잠금 정리
        finally {
            // 현재 요청이 소유한 잠금만 제거
            delTranslationLock(lockKey, owner);
        }

    }

    /**
     * 현재 권한과 캐시를 확인하고 필요한 경우 트랜잭션 밖에서 번역 수행
     *
     * @author SeungHyeon.Kang
     * @param userNumb 요청 회원 번호
     * @param reptNumb 독후감 번호
     * @param translate 캐시 미적중 시 외부 번역 허용 여부
     * @return 처리 결과 또는 외부 번역이 필요한 사전 조회의 빈 결과
     */
    private ResultData getTranslationResult(Long userNumb, Long reptNumb, boolean translate) {
        // 서버에서 공개 범위와 차단 관계를 짧은 트랜잭션으로 재검증
        ReportTranslationDto source = getLockedSource(userNumb, reptNumb);

        // 공개 범위 안의 번역 가능한 독후감이 아니면 외부 호출 없이 접근을 거부
        if (StringUtil.isEmpty(source)) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 요청의 표시 언어를 서버가 지원하는 번역 대상 언어로 확정
        String targetLanguage = getTargetLanguage();
        // 작성 당시 앱 언어와 실제 본문 언어가 다른 기존 독후감의 번역 방향 보정
        String sourceLanguage = getSourceLanguage(source.getSourceLangCode(), source.getSourceContent());

        // 원문 언어와 표시 언어가 같거나 본문이 비어 있으면 번역 기능을 제공하지 않음
        if (targetLanguage.equals(sourceLanguage) || StringUtil.hasEmpty(sourceLanguage, source.getSourceContent())) {
            // "번역할 수 없는 독후감이에요."
            return ResultData.fail(ResultEnum.REPORT_TRANSLATION_UNAVAILABLE);
        }

        // 번역 대상 언어와 현재 원문 해시를 캐시 조회 조건에 설정
        source.setLangCode(targetLanguage);
        // 원문 변경 여부를 판정할 SHA-256 해시를 설정
        source.setOrigHash(getSha256(source.getSourceContent()));
        // 같은 짧은 트랜잭션에서 원문 권한과 해시가 검증된 캐시는 외부 호출 없이 반환
        if (source.isCached()) {
            // 현재 원문과 접근 범위에 맞는 번역 캐시 반환
            return ResultData.success(source);
        }

        // API 키가 없으면 외부 호출과 비용 발생 가능성을 모두 차단
        if (StringUtil.isEmpty(translationApiKey)) {
            // "번역 기능을 사용할 수 없어요.\n잠시 후 다시 시도해주세요."
            return ResultData.fail(ResultEnum.REPORT_TRANSLATION_FAILED);
        }

        // 사전 조회의 미적중은 잠금 획득 후 다시 검증할 대상으로 구분
        if (!translate) {
            // 외부 번역이 필요한 상태 반환
            return null;
        }

        // Google 과금 기준인 유니코드 코드 포인트 단위로 이번 번역 글자 수를 계산
        int requestChars = source.getSourceContent().codePointCount(0, source.getSourceContent().length());

        // 월간 무료 사용 범위 안에서 Redis 카운터를 원자적으로 예약한 요청만 외부 호출
        if (!reserveChars(requestChars)) {
            // "이번 달 번역 사용량을 모두 사용했어요."
            return ResultData.fail(ResultEnum.REPORT_TRANSLATION_LIMITED);
        }

        // Google 번역 응답 검증과 캐시 저장을 하나의 실패 경로로 관리
        try {
            // 원문과 작성 언어 및 표시 언어를 Google 번역 API에 전달
            String translatedContent = getGoogleTranslation(
                    source.getSourceContent(), sourceLanguage, targetLanguage);
            // 검증된 번역문을 캐시 저장값으로 설정
            source.setTrnsCntn(translatedContent);
            // 응답 대기 동안의 비공개 전환·차단·본문 변경을 잠금 조회로 확인한 뒤에만 저장
            return setTranslationResult(userNumb, source);
        }

        // 외부 API 또는 응답 검증 오류는 원문과 비밀값을 기록하지 않고 공통 실패로 전환
        catch (HttpMessageConversionException | RestClientException | IllegalStateException e) {
            // 독후감 번호만 포함해 번역 실패 원인을 운영 로그에 기록
            log.error("독후감 번역 처리에 실패했습니다. 독후감 번호={} failure={}", reptNumb, LogSafe.getFailure(e));
            // "번역 기능을 사용할 수 없어요.\n잠시 후 다시 시도해주세요."
            return ResultData.fail(ResultEnum.REPORT_TRANSLATION_FAILED);
        }
    }

    /**
     * 공개 범위와 차단 관계 및 캐시를 같은 짧은 트랜잭션에서 조회
     *
     * @author HanWon.Jang
     * @param userNumb 번역을 요청한 로그인 사용자 번호
     * @param reptNumb 번역할 독후감 번호
     * @return 번역 가능한 독후감 원문 또는 조회 실패값
     */
    private ReportTranslationDto getLockedSource(Long userNumb, Long reptNumb) {
        // 원문 잠금 조회에 인증 사용자와 대상 독후감 번호를 설정
        ReportTranslationDto request = new ReportTranslationDto();
        // 차단 관계와 활성 계정 확인에 사용할 로그인 사용자 번호를 설정
        request.setUserNumb(userNumb);
        // 번역 캐시와 연결할 독후감 번호를 설정
        request.setReptNumb(reptNumb);
        // 원문 조회가 외부 API 응답을 기다리는 동안 잠금을 유지하지 않도록 짧은 경계 적용
        TransactionTemplate transaction = getTranslationTx();
        // 공개 범위가 검증된 원문의 스냅샷 반환
        return transaction.execute(status -> {
            // 캐시 조회 동안에도 접근 범위와 원문이 바뀌지 않도록 잠금 조회
            ReportTranslationDto source = reportMapper.getReportTrnsSource(request);
            // 접근 제한과 빈 본문은 캐시 재사용 전에 원래 상태로 반환
            if (StringUtil.isEmpty(source) || StringUtil.isEmpty(source.getSourceContent())) {
                // 번역 대상이 아닌 현재 원문 상태 반환
                return source;
            }

            // 요청 표시 언어에 해당하는 캐시 조건 설정
            source.setLangCode(getTargetLanguage());
            // 현재 본문을 기준으로 캐시 원문 버전 설정
            source.setOrigHash(getSha256(source.getSourceContent()));
            // 원문 권한 확인과 같은 트랜잭션에서 캐시 조회
            ReportTranslationDto cached = reportMapper.getReportTrnsDtl(source);
            // 현재 본문과 일치하는 캐시만 스냅샷에 포함
            if (!StringUtil.isEmpty(cached) && source.getOrigHash().equals(cached.getOrigHash())) {
                // 유효 캐시의 번역문 설정
                source.setTrnsCntn(cached.getTrnsCntn());
                // 잠금과 외부 호출 없이 반환할 캐시 적중 상태 설정
                source.setCached(true);
            }

            // 조회 완료 즉시 커밋하여 DB 연결과 원문 잠금 반환
            return source;
        });
    }

    /**
     * 번역 완료 시점의 접근 권한과 원문을 검증하여 오래된 번역 저장과 노출 방지
     *
     * @author SeungHyeon.Kang
     * @param userNumb 요청 회원 번호
     * @param source 번역에 사용한 원문과 결과
     * @return 현재 원문 기준 번역 저장 결과
     */
    private ResultData setTranslationResult(Long userNumb, ReportTranslationDto source) {
        // 외부 통신 이후의 저장 전용 짧은 트랜잭션 생성
        TransactionTemplate transaction = getTranslationTx();
        // 권한과 원문 재확인부터 캐시 저장까지 동일 트랜잭션으로 처리
        return transaction.execute(status -> {
            // 최초 번역 요청과 동일한 회원의 현재 접근 범위 설정
            source.setUserNumb(userNumb);
            // 삭제·비공개·차단·비활성화와 경합하는 현재 원문 잠금 조회
            ReportTranslationDto current = reportMapper.getReportTrnsSource(source);
            // 접근할 수 없어진 원문은 번역문도 반환하지 않음
            if (StringUtil.isEmpty(current)) {
                // "접근 권한이 없습니다."
                return ResultData.fail(ResultEnum.FORBIDDEN);
            }

            // 대기 중 본문이 바뀌었으면 이전 본문 번역을 저장하거나 반환하지 않음
            if (StringUtil.isEmpty(current.getSourceContent()) || !source.getOrigHash().equals(getSha256(current.getSourceContent()))) {
                // "번역 기능을 사용할 수 없어요.\n잠시 후 다시 시도해주세요."
                return ResultData.fail(ResultEnum.REPORT_TRANSLATION_FAILED);
            }

            // 잠금 만료 후 다른 요청이 저장한 유효 번역은 덮어쓰지 않고 재사용
            ReportTranslationDto cached = reportMapper.getReportTrnsDtl(source);
            // 현재 원문과 같은 캐시가 있으면 먼저 완료된 결과 유지
            if (!StringUtil.isEmpty(cached) && source.getOrigHash().equals(cached.getOrigHash())) {
                // 기존 결과 재사용 여부 설정
                cached.setCached(true);
                // 먼저 저장된 현재 원문 번역 반환
                return ResultData.success(cached);
            }

            // 재검증한 원문의 번역 결과 저장
            reportMapper.setReportTrns(source);
            // 신규 외부 번역 결과임을 설정
            source.setCached(false);
            // 저장한 번역 결과 반환
            return ResultData.success(source);
        });
    }

    /**
     * 호출부 트랜잭션과 독립된 짧은 원문 조회와 번역 저장 경계 생성
     *
     * @author SeungHyeon.Kang
     * @return 새 트랜잭션 실행 객체
     */
    private TransactionTemplate getTranslationTx() {
        // 호출부 상태와 무관한 독립된 DB 경계 생성
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        // 외부 호출 구간에 호출부의 DB 잠금이 남지 않도록 분리
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        // 원문 조회와 결과 저장 전용 실행 경계 반환
        return transaction;
    }

    /**
     * DB 연결 없이 같은 독후감의 번역 순서를 Redis 잠금으로 직렬화
     *
     * @author SeungHyeon.Kang
     * @param key 독후감별 잠금 키
     * @param owner 현재 요청의 잠금 소유자
     * @return 대기 한도 안의 잠금 획득 여부
     */
    private boolean getTranslationLock(String key, String owner) {
        // 잘못된 유효시간 설정은 외부 호출 없이 차단
        if (lockSeconds <= 0) {
            // 잠금 유효시간이 없는 요청 거절
            return false;
        }

        // 시스템 시계 변경의 영향을 받지 않는 최대 대기 종료 시점 계산
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(lockSeconds);
        // Redis 장애와 요청 취소 시 외부 호출을 시작하지 않도록 격리
        try {
            // 기존 요청 완료 또는 잠금 만료까지 DB 연결 없이 대기
            do {
                // 장애로 작업자가 사라져도 잠금이 자동 만료되도록 유효시간과 함께 생성
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, owner, Duration.ofSeconds(lockSeconds));
                // 명시적인 Redis 성공 결과만 외부 번역 허용
                if (Boolean.TRUE.equals(acquired)) {
                    // 현재 요청의 번역 실행 권한 반환
                    return true;
                }

                // 캐시 경합 중 Redis 재시도 부하를 줄이기 위한 짧은 대기
                Thread.sleep(50L);
            } while (System.nanoTime() < deadline);
        }

        // 종료된 요청은 외부 번역 없이 중단
        catch (InterruptedException e) {
            // 호출 스레드의 취소 상태 보존
            Thread.currentThread().interrupt();
        }

        // Redis 장애 시 중복 과금 방지를 우회하지 않음
        catch (RuntimeException e) {
            // 원문과 잠금 식별값 없이 장애 원인 기록
            log.warn("번역 잠금 조회에 실패했습니다. failure={}", LogSafe.getFailure(e));
        }

        // 잠금 대기 초과와 요청 종료 및 저장소 장애의 거절 결과 반환
        return false;
    }

    /**
     * 현재 요청 소유의 번역 잠금만 제거
     *
     * @author SeungHyeon.Kang
     * @param key 독후감별 잠금 키
     * @param owner 현재 요청의 잠금 소유자
     */
    private void delTranslationLock(String key, String owner) {
        // 잠금 정리 장애가 이미 확정한 번역 결과를 변경하지 않도록 격리
        try {
            // 만료 후 재발급된 다른 요청의 잠금 삭제 방지
            redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(key), owner);
        }

        // 정리 실패 시에도 유효시간 이후 재시도 가능
        catch (RuntimeException e) {
            // 원문과 소유자 정보 없이 잠금 정리 실패 기록
            log.warn("번역 잠금 정리에 실패했습니다. failure={}", LogSafe.getFailure(e));
        }

    }

    /**
     * 독후감 원문을 Google Cloud Translation Basic API로 번역
     *
     * @author HanWon.Jang
     * @param sourceContent 번역할 독후감 원문
     * @param sourceLanguage 독후감 작성 언어 코드
     * @param targetLanguage 사용자 표시 언어 코드
     * @return Google이 반환한 번역문
     */
    private String getGoogleTranslation(String sourceContent, String sourceLanguage, String targetLanguage) {
        // API 키가 URL과 접근 로그에 노출되지 않도록 전용 HTTP 헤더에 설정
        HttpHeaders headers = new HttpHeaders();
        // Google REST 요청 본문이 UTF-8 JSON임을 설정
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 서버 전용 Google API 키를 인증 헤더에 설정
        headers.set("X-goog-api-key", translationApiKey);
        // 독후감은 서식 없는 사용자 문자열이므로 일반 텍스트 번역 요청 본문을 구성
        Map<String, String> requestBody = Map.of(
                "q", sourceContent,
                "source", sourceLanguage,
                "target", targetLanguage,
                "format", "text"
        );
        // 인증 헤더와 번역 요청 본문을 외부 호출 객체로 결합
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        // 공통 연결 및 읽기 제한시간이 적용된 클라이언트로 Google 번역 API를 호출
        Map<?, ?> responseBody = restTemplate.postForObject(translationUrl, requestEntity, Map.class);
        // 중첩된 Google 응답에서 첫 번째 번역문을 안전하게 조회
        String translatedContent = getTranslatedContent(responseBody);

        // 번역문이 없으면 성공 응답으로 캐시하지 않도록 외부 응답 검증 실패를 발생시킴
        if (StringUtil.isEmpty(translatedContent)) {
            // 유효한 번역문이 없는 외부 응답을 공통 실패 경로로 전달
            throw new IllegalStateException("Google translation response has no translated text.");
        }

        // JSON 역직렬화가 완료된 일반 텍스트 번역문을 반환
        return HtmlUtils.htmlUnescape(translatedContent);
    }

    /**
     * Google 번역 응답 구조에서 첫 번째 번역문만 안전하게 추출
     *
     * @author HanWon.Jang
     * @param responseBody Google 번역 JSON 응답을 변환한 맵
     * @return 번역문 또는 유효한 값이 없을 때 빈 문자열
     */
    private String getTranslatedContent(Map<?, ?> responseBody) {
        // 최상위 응답이나 data 객체가 없으면 외부 응답 검증 단계에서 실패하도록 빈 값을 반환
        if (StringUtil.isEmpty(responseBody) || !(responseBody.get("data") instanceof Map<?, ?> data)) {
            // Google 표준 응답 구조와 일치하지 않는 상태를 반환
            return StringUtil.EMPTY;
        }

        // 번역 결과 목록이 없거나 비어 있으면 외부 응답 검증 단계에서 실패하도록 빈 값을 반환
        Object translationsValue = data.get("translations");
        if (!(translationsValue instanceof List<?> translations) || StringUtil.isEmpty(translations)) {
            // 번역 결과가 없는 상태를 반환
            return StringUtil.EMPTY;
        }

        // 첫 번째 번역 결과가 객체가 아니면 유효한 응답으로 사용하지 않음
        if (!(translations.get(0) instanceof Map<?, ?> translation)) {
            // 번역문 필드를 확인할 수 없는 상태를 반환
            return StringUtil.EMPTY;
        }

        // translatedText가 문자열인 경우에만 캐시 가능한 번역문으로 반환
        Object translatedText = translation.get("translatedText");
        return translatedText instanceof String stringValue ? stringValue : StringUtil.EMPTY;
    }

    /**
     * 현재 요청의 표시 언어를 서비스가 지원하는 한국어 또는 영어 코드로 제한
     *
     * @author HanWon.Jang
     * @return ko 또는 en 언어 코드
     */
    private String getTargetLanguage() {
        // 영어 요청만 영어로 번역하고 그 외 요청은 한국어로 제한
        return LocaleUtil.getLanguageCode().startsWith("en") ? "en" : "ko";
    }

    /**
     * 원문 언어와 캐시 및 월간 잔여량으로 번역 버튼 표시 여부를 판정
     *
     * @author HanWon.Jang
     * @param sourceLanguage 독후감 작성 언어 코드
     * @param sourceContent 독후감 원문
     * @param cacheYsno 현재 원문과 대상 언어의 유효 캐시 여부
     * @param remainingChars 이번 달 남은 번역 글자 수
     * @return 버튼 표시 여부 Y 또는 N
     */
    private String getAvailability(String sourceLanguage, String sourceContent, String cacheYsno
                                 , long remainingChars) {
        // 저장 언어 누락 시에도 본문으로 판정 가능한 원문의 번역 방향 확인
        String contentLanguage = getSourceLanguage(sourceLanguage, sourceContent);
        // 저장된 앱 언어 대신 실제 본문 기준으로 동일 언어와 빈 본문 번역 제외
        if (StringUtil.hasEmpty(contentLanguage, sourceContent) || getTargetLanguage().equals(contentLanguage)) {
            // 원문과 대상 언어가 같거나 본문이 없는 카드의 번역 버튼 숨김 값을 반환
            return Constant.COMM_NO;
        }

        // 유효한 캐시는 월간 잔여량과 API 키 상태에 관계없이 다시 볼 수 있음
        if (Constant.COMM_YES.equals(cacheYsno)) {
            // 비용이 발생하지 않는 캐시 번역의 버튼 표시값을 반환
            return Constant.COMM_YES;
        }

        // 새 번역은 API 키와 Redis 잔여량이 모두 확보된 경우에만 버튼을 표시
        int sourceChars = sourceContent.codePointCount(0, sourceContent.length());
        boolean available = !StringUtil.isEmpty(translationApiKey) && remainingChars >= sourceChars;
        // 월간 50만자 한도 안에서 새 번역이 가능한지 Y 또는 N으로 반환
        return available ? Constant.COMM_YES : Constant.COMM_NO;
    }

    /**
     * 본문의 한글과 라틴 문자 수를 기준으로 번역 원문 언어 판정
     *
     * @author HanWon.Jang
     * @param storedLanguage 작성 당시 저장된 언어 코드
     * @param content 독후감 원문
     * @return 본문 언어 또는 판정 불가 시 저장된 언어
     */
    private String getSourceLanguage(String storedLanguage, String content) {
        // 빈 본문은 호출부에서 번역 제외 처리
        if (StringUtil.isEmpty(content)) {
            // 문자 정보가 없는 원문의 기존 언어 반환
            return storedLanguage;
        }

        // ponytail: 한영 혼합문은 문자 수 기준 판정, 다국어 지원 시 언어 감지기로 교체
        int koreanChars = 0;
        int latinChars = 0;
        // 공백과 숫자 및 이모지를 제외한 문자 체계 비교
        for (int codePoint : content.codePoints().toArray()) {
            // 조합형 자모와 완성형 한글을 같은 문자 체계로 집계
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HANGUL) {
                koreanChars++;
            }

            // 영어 본문의 라틴 문자 집계
            else if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.LATIN) {
                latinChars++;
            }

        }

        // 문자 수가 같거나 판정할 문자가 없으면 기존 언어 유지
        if (koreanChars == latinChars) {
            // 불확실한 본문의 저장된 언어 반환
            return storedLanguage;
        }

        // 본문에서 더 많이 사용한 지원 언어 반환
        return koreanChars > latinChars ? "ko" : "en";
    }

    /**
     * 현재 월간 번역 한도에서 남은 글자 수를 조회
     *
     * @author HanWon.Jang
     * @return 남은 글자 수 또는 조회 불가 시 음수
     */
    private long getRemainingChars() {
        // API 키가 없으면 번역 버튼을 처음부터 표시하지 않음
        if (StringUtil.isEmpty(translationApiKey)) {
            // 번역 기능 비활성화 상태를 나타내는 음수 잔여량을 반환
            return -1L;
        }

        // Redis 장애 시 50만자 보호 한도를 우회하지 않도록 버튼을 숨김
        try {
            // 현재 Google 월간 경계의 앱 전체 번역 사용 글자 수를 조회
            String usedValue = redisTemplate.opsForValue().get(getMonthlyUsageKey());
            // 아직 사용량이 없으면 전체 설정 한도를 남은 글자 수로 반환
            if (StringUtil.isEmpty(usedValue)) {
                // 이번 달 전체 번역 가능 글자 수를 반환
                return monthlyCharacterLimit;
            }

            // 저장된 사용량을 설정 한도에서 차감하고 음수로 내려가지 않게 보정
            return Math.max(0L, monthlyCharacterLimit - Long.parseLong(usedValue));
        }

        // 손상된 카운터나 Redis 장애는 새 번역을 허용하지 않고 운영 로그에 기록
        catch (RuntimeException e) {
            // 사용자 작성 내용과 API 키 없이 월간 사용량 조회 실패 원인을 기록
            log.error("독후감 번역 월간 사용량을 조회하지 못했습니다. failure={}", LogSafe.getFailure(e));
            // 한도 확인 불가 상태를 나타내는 음수 잔여량을 반환
            return -1L;
        }
    }

    /**
     * 이번 번역의 글자 수를 월간 한도 안에서 원자적으로 예약
     *
     * @author HanWon.Jang
     * @param requestChars Google에 전송할 원문 코드 포인트 수
     * @return 월간 한도 안에서 예약되었는지 여부
     */
    private boolean reserveChars(int requestChars) {
        // Redis 장애 시 Google 호출 비용 보호 한도를 우회하지 않도록 예약을 거부
        try {
            // 다음 월 경계 뒤까지 현재 월 카운터를 보존할 초 단위 유효시간을 계산
            long ttlSeconds = getUsageTtlSeconds();
            // 사용량 확인과 증가를 하나의 Redis 스크립트에서 처리해 동시 요청 초과를 방지
            Long result = redisTemplate.execute(
                    RESERVE_CHARS_SCRIPT,
                    List.of(getMonthlyUsageKey()),
                    String.valueOf(requestChars), String.valueOf(monthlyCharacterLimit), String.valueOf(ttlSeconds)
            );
            // 한도 초과가 아닌 명시적인 Redis 결과만 예약 성공으로 반환
            return !StringUtil.isEmpty(result) && result != MONTHLY_LIMIT_EXCEEDED;
        }

        // 월간 사용량 저장소 장애는 외부 호출 전에 차단하고 비밀값 없이 기록
        catch (RuntimeException e) {
            // 사용자 작성 내용 없이 Redis 예약 실패 원인을 기록
            log.error("독후감 번역 월간 사용량을 예약하지 못했습니다. failure={}", LogSafe.getFailure(e));
            // 한도 보호를 확인하지 못한 요청을 거절
            return false;
        }
    }

    /**
     * Google 월간 과금 경계와 같은 시간대의 Redis 사용량 키를 생성
     *
     * @author HanWon.Jang
     * @return yyyy-MM 접미사를 포함한 월간 사용량 키
     */
    private String getMonthlyUsageKey() {
        // 설정된 Google 쿼터 시간대의 현재 연월을 사용해 월별 카운터를 분리
        return MONTHLY_USAGE_KEY_PREFIX + YearMonth.now(ZoneId.of(quotaZone));
    }

    /**
     * 현재 월 카운터가 다음 월 경계 이후 자동 정리되도록 유효시간을 계산
     *
     * @author HanWon.Jang
     * @return 다음 월 시작 뒤 하루까지 남은 초
     */
    private long getUsageTtlSeconds() {
        // 설정된 Google 쿼터 시간대의 현재 시각을 조회
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(quotaZone));
        // 다음 월 시작 뒤 하루까지 카운터를 보존해 경계 시각의 중복 월 키 사용을 방지
        ZonedDateTime expiresAt = now.toLocalDate().withDayOfMonth(1).plusMonths(1).plusDays(1)
                .atStartOfDay(now.getZone());
        // Redis EXPIRE에 전달할 최소 1초 이상의 유효시간을 반환
        return Math.max(1L, Duration.between(now, expiresAt).getSeconds());
    }

    /**
     * 번역 원문을 SHA-256 소문자 16진수 해시로 변환
     *
     * @author HanWon.Jang
     * @param sourceContent 번역 캐시 유효성을 확인할 독후감 원문
     * @return 64자리 SHA-256 해시
     */
    private String getSha256(String sourceContent) {
        // 런타임 필수 알고리즘으로 원문을 고정 길이 해시로 변환
        try {
            // UTF-8 원문을 해시할 메시지 다이제스트 객체를 생성
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 원문을 64자리 소문자 16진수 값으로 변환해 반환
            return HexFormat.of().formatHex(messageDigest.digest(sourceContent.getBytes(StandardCharsets.UTF_8)));
        }

        // 필수 알고리즘이 없는 런타임에서는 원문 저장이나 약한 해시로 우회하지 않음
        catch (NoSuchAlgorithmException e) {
            // SHA-256 제공이 보장되지 않은 실행 환경 오류를 호출부에 전달
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", e);
        }
    }
}
