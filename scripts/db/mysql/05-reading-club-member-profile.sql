-- 기존 독서 모임 회원 관계에 프로필 노출 여부를 추가한다.
ALTER TABLE `TB_CLMEMX`
    ADD COLUMN `PROF_YSNO` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
        NOT NULL DEFAULT 'Y' COMMENT '모임원 프로필 노출 여부' AFTER `BLOC_YSNO`;
