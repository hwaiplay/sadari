package org.our.sadari.global.common.logging;

import org.our.sadari.global.common.util.StringUtil;

/**
 * fileName       : LogSafe
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : 민감한 예외 메시지 없이 원인 유형과 코드 위치를 제공
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
public final class LogSafe {

    // 비정상 원인 체인과 대형 스택의 로그 증폭 방지 한도
    private static final int MAX_CAUSES = 8;
    // 원인별 코드 위치 출력 한도
    private static final int MAX_FRAMES = 12;

    private LogSafe() {

    }

    /**
     * 예외 메시지와 suppressed 예외를 제외한 원인별 코드 위치 생성
     *
     * @author SeungHyeon.Kang
     * @param error 진단할 예외
     * @return 메시지와 요청 데이터가 제외된 진단 문자열
     */
    public static String getFailure(Throwable error) {
        // 예외가 없는 업무 실패의 진단값
        if (StringUtil.isEmpty(error)) {
            // 예외 미발생 표시
            return "none";
        }

        // 원문 메시지 호출 없이 진단 정보를 조립할 버퍼
        StringBuilder result = new StringBuilder();
        Throwable current = error;
        // 순환 원인 체인도 고정된 횟수 안에서 종료
        for (int depth = 0; depth < MAX_CAUSES && !StringUtil.isEmpty(current); depth++) {
            // 원인 클래스만 기록하여 SQL·토큰·본문 노출 방지
            result.append(current.getClass().getName());
            // 코드 위치만 추출하여 예외의 toString 호출 회피
            StackTraceElement[] frames = current.getStackTrace();
            // 진단에 필요한 상위 스택만 제한적으로 기록
            for (int index = 0; index < Math.min(frames.length, MAX_FRAMES); index++) {
                // 파일 시스템 경로와 예외 메시지 없이 클래스·메서드·행 기록
                result.append(" at ").append(frames[index].getClassName()).append('.').append(frames[index].getMethodName()).append(':').append(frames[index].getLineNumber());
            }

            // 다음 원인 구분
            result.append("; ");
            // 중첩 예외 원인 조회
            current = current.getCause();
        }

        // 제한된 예외 진단 문자열
        return result.toString();
    }
}
