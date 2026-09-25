-- 기존 알림 테이블에 개인 알림 발신자와 차단 정리용 인덱스를 한 번만 추가
ALTER TABLE `TB_ALIMXX`
    ADD COLUMN `SEND_NUMB` bigint DEFAULT NULL COMMENT '내부 알림 발신 사용자 번호' AFTER `USER_NUMB`,
    ADD INDEX `IX_TB_ALIMXX_SEND` (`USER_NUMB`, `SEND_NUMB`, `DELT_YSNO`);
