package org.our.sadari.global.file.exception;

/**
 * 업로드 파일이 서비스에서 허용한 이미지 형식이나 크기 조건을 충족하지 못했을 때 발생하는 예외입니다.
 *
 * @author Seunghyeon.Kang
 */
public class InvalidImageFileException extends RuntimeException {

    /**
     * 이미지 검증 실패 원인을 내부 로그와 예외 체인에 보존합니다.
     *
     * @author Seunghyeon.Kang
     * @param message 이미지 검증 실패 원인
     */
    public InvalidImageFileException(String message) {
        super(message);
    }

    /**
     * 이미지 디코딩 과정에서 발생한 원인 예외를 함께 보존합니다.
     *
     * @author Seunghyeon.Kang
     * @param message 이미지 검증 실패 원인
     * @param cause 이미지 디코딩 중 발생한 원인 예외
     */
    public InvalidImageFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
