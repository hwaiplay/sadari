package org.our.sadari.global.file.exception;

/**
 * fileName       : InvalidImageFileException
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 이미지 파일 예외를 표현하고 공통 응답으로 변환함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
public class InvalidImageFileException extends RuntimeException {

    /**
     * 이미지 검증 실패 원인을 내부 로그와 예외 체인에 보존함
     *
     * @author SeungHyeon.Kang
     * @param message 이미지 검증 실패 원인
     */
    public InvalidImageFileException(String message) {
        // 예외 메시지와 원인을 상위 예외 객체에 전달함
        super(message);
    }

    /**
     * 이미지 디코딩 과정에서 발생한 원인 예외를 함께 보존함
     *
     * @author SeungHyeon.Kang
     * @param message 이미지 검증 실패 원인
     * @param cause 이미지 디코딩 중 발생한 원인 예외
     */
    public InvalidImageFileException(String message, Throwable cause) {
        // 예외 메시지와 원인을 상위 예외 객체에 전달함
        super(message, cause);
    }
}
