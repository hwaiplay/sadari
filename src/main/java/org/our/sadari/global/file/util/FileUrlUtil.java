package org.our.sadari.global.file.util;

import org.our.sadari.global.common.util.StringUtil;

/**
 * fileName       : FileUrlUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-08-26
 * description    : 저장된 원본 이미지 경로를 화면 용도별 공개 URL로 변환함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        SeungHyeon.Kang         배경사진 화면용 파생 URL 생성
 */
public final class FileUrlUtil {

    // 서버가 관리하는 배경사진 공개 경로 접두사
    private static final String BACKGROUND_ACCESS_PREFIX = "/uploads/background/";
    // 배경사진 화면용 파생본을 요청하는 쿼리값
    private static final String DISPLAY_VARIANT_QUERY = "?variant=display";

    /** 공통 URL 변환 유틸리티의 인스턴스 생성을 차단함 */
    private FileUrlUtil() {

    }

    /**
     * 원본 배경사진 경로를 일반 화면용 파생본 경로로 변환함
     *
     * @author SeungHyeon.Kang
     * @param filePath DB에서 조회한 원본 배경사진 경로
     * @return 화면용 파생본 경로, 내부 배경사진 경로가 아니면 원본 경로
     */
    public static String getBgDisplayPath(String filePath) {
        // 외부 URL이나 지원하지 않는 이전 경로는 기존 표시 계약을 유지함
        if (StringUtil.isEmpty(filePath) || !filePath.startsWith(BACKGROUND_ACCESS_PREFIX)) {
            // 변환할 수 없는 경로를 변경 없이 반환함
            return filePath;
        }

        // 서버의 화면용 파생 이미지 조회 계약을 원본 경로에 추가함
        return filePath + DISPLAY_VARIANT_QUERY;
    }
}
