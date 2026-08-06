-- 독서 모임 1차 개발용 신규 관계 테이블과 공통코드를 현행 DB에 반영한다.

CREATE TABLE IF NOT EXISTS `TB_CLCATE` (
  `CLUB_NUMB` bigint NOT NULL COMMENT '모임 번호',
  `INTR_CODE` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'CATE_CODE 하위 관심분야 세부코드',
  `SORT_ORDR` tinyint NOT NULL COMMENT '모임 카테고리 노출 순서와 대표 카테고리 구분',
  `REGI_DATE` datetime(6) NOT NULL COMMENT '등록 일시',
  PRIMARY KEY (`CLUB_NUMB`,`INTR_CODE`),
  UNIQUE KEY `UK_TB_CLCATE_SORT` (`CLUB_NUMB`,`SORT_ORDR`),
  KEY `IX_TB_CLCATE_RECM` (`INTR_CODE`,`CLUB_NUMB`),
  CONSTRAINT `FK_TB_CLCATE_01` FOREIGN KEY (`CLUB_NUMB`) REFERENCES `TM_CLUBXM` (`CLUB_NUMB`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='독서 모임 카테고리';

CREATE TABLE IF NOT EXISTS `TB_CLMEMX` (
  `CLUB_NUMB` bigint NOT NULL COMMENT '모임 번호',
  `USER_NUMB` bigint NOT NULL COMMENT '사용자 번호',
  `MEMB_ROLE` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '모임원 역할',
  `MEMB_STAT` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '모임원 또는 초대 상태',
  `SEND_NUMB` bigint DEFAULT NULL COMMENT '초대를 발송한 모임장 사용자 번호',
  `INVT_DATE` datetime(6) DEFAULT NULL COMMENT '초대 발송 일시',
  `EXPR_DATE` datetime(6) DEFAULT NULL COMMENT '초대 만료 일시',
  `JOIN_DATE` datetime(6) DEFAULT NULL COMMENT '가입 확정 일시',
  `EXIT_DATE` datetime(6) DEFAULT NULL COMMENT '모임 탈퇴 또는 퇴장 일시',
  `BLOC_YSNO` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'N' COMMENT '재가입 차단 여부',
  `REGI_DATE` datetime(6) NOT NULL COMMENT '등록 일시',
  `UPDT_DATE` datetime(6) NOT NULL COMMENT '수정 일시',
  PRIMARY KEY (`CLUB_NUMB`,`USER_NUMB`),
  KEY `IX_TB_CLMEMX_USER` (`USER_NUMB`,`MEMB_STAT`,`CLUB_NUMB`),
  KEY `IX_TB_CLMEMX_CLUB` (`CLUB_NUMB`,`MEMB_STAT`,`USER_NUMB`),
  KEY `IX_TB_CLMEMX_SEAT` (`CLUB_NUMB`,`MEMB_STAT`,`EXPR_DATE`),
  KEY `FK_TB_CLMEMX_SEND` (`SEND_NUMB`),
  CONSTRAINT `FK_TB_CLMEMX_01` FOREIGN KEY (`CLUB_NUMB`) REFERENCES `TM_CLUBXM` (`CLUB_NUMB`) ON DELETE CASCADE,
  CONSTRAINT `FK_TB_CLMEMX_02` FOREIGN KEY (`USER_NUMB`) REFERENCES `TM_USERXM` (`USER_NUMB`) ON DELETE CASCADE,
  CONSTRAINT `FK_TB_CLMEMX_03` FOREIGN KEY (`SEND_NUMB`) REFERENCES `TM_USERXM` (`USER_NUMB`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='독서 모임 회원과 초대 예약석';

CREATE TABLE IF NOT EXISTS `TB_CLJOIN` (
  `CLUB_NUMB` bigint NOT NULL COMMENT '모임 번호',
  `APPL_NUMB` bigint NOT NULL COMMENT '모임별 가입 신청 번호',
  `USER_NUMB` bigint NOT NULL COMMENT '가입 신청 사용자 번호',
  `QUES_FIRS` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '첫 번째 가입 질문 사본',
  `QUES_SECO` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '두 번째 가입 질문 사본',
  `QUES_THIR` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '세 번째 가입 질문 사본',
  `QUES_FOUR` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '네 번째 가입 질문 사본',
  `QUES_FIFT` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '다섯 번째 가입 질문 사본',
  `ANSR_FIRS` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '처리 전 첫 번째 가입 답변',
  `ANSR_SECO` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '처리 전 두 번째 가입 답변',
  `ANSR_THIR` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '처리 전 세 번째 가입 답변',
  `ANSR_FOUR` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '처리 전 네 번째 가입 답변',
  `ANSR_FIFT` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '처리 전 다섯 번째 가입 답변',
  `JOIN_STAT` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '가입 신청 상태',
  `APPL_DATE` datetime(6) NOT NULL COMMENT '가입 신청 일시',
  `PROC_USER` bigint DEFAULT NULL COMMENT '가입 신청 처리 사용자 번호',
  `PROC_DATE` datetime(6) DEFAULT NULL COMMENT '가입 신청 처리 일시',
  PRIMARY KEY (`CLUB_NUMB`,`APPL_NUMB`),
  KEY `IX_TB_CLJOIN_USER` (`CLUB_NUMB`,`USER_NUMB`,`JOIN_STAT`),
  KEY `IX_TB_CLJOIN_MINE` (`USER_NUMB`,`JOIN_STAT`,`APPL_DATE`),
  KEY `FK_TB_CLJOIN_PROC` (`PROC_USER`),
  CONSTRAINT `FK_TB_CLJOIN_01` FOREIGN KEY (`CLUB_NUMB`) REFERENCES `TM_CLUBXM` (`CLUB_NUMB`) ON DELETE CASCADE,
  CONSTRAINT `FK_TB_CLJOIN_02` FOREIGN KEY (`USER_NUMB`) REFERENCES `TM_USERXM` (`USER_NUMB`) ON DELETE CASCADE,
  CONSTRAINT `FK_TB_CLJOIN_03` FOREIGN KEY (`PROC_USER`) REFERENCES `TM_USERXM` (`USER_NUMB`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='독서 모임 승인 가입 신청';

DELETE FROM `TB_CODEXD`
 WHERE `COMM_CODE` IN ('JOIN_PATH', 'INVT_STAT');

DELETE FROM `TM_CODEXM`
 WHERE `COMM_CODE` IN ('JOIN_PATH', 'INVT_STAT');

INSERT INTO `TB_CODEXD` (
            `COMM_CODE`
          , `COMD_CODE`
          , `COMD_NAME`
          , `CODE_EXPL`
          , `OPT1_CODE`
          , `OPT1_NAME`
          , `OPT2_CODE`
          , `OPT2_NAME`
          , `OPT3_CODE`
          , `OPT3_NAME`
          , `OPT4_CODE`
          , `OPT4_NAME`
          , `USEE_YSNO`
          , `REGI_ADMN`
          , `REGI_DATE`
          , `SORT_ORDR`
          , `UPDT_ADMN`
          , `UPDT_DATE`
) VALUES (  'MEMB_STAT'
          , 'INVITED'
          , '초대 대기'
          , '좌석을 예약한 맞팔 초대'
          , NULL
          , NULL
          , NULL
          , NULL
          , NULL
          , NULL
          , NULL
          , NULL
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , 1
          , NULL
          , NULL
)
ON DUPLICATE KEY UPDATE
       `COMD_NAME` = VALUES(`COMD_NAME`)
     , `CODE_EXPL` = VALUES(`CODE_EXPL`)
     , `USEE_YSNO` = VALUES(`USEE_YSNO`)
     , `SORT_ORDR` = VALUES(`SORT_ORDR`)
     , `UPDT_ADMN` = 'SYSTEM'
     , `UPDT_DATE` = CURRENT_TIMESTAMP;

UPDATE `TB_CODEXD`
   SET `SORT_ORDR` = CASE WHEN `COMD_CODE` = 'ACTIVE'
                               THEN 2
                          WHEN `COMD_CODE` = 'LEFT'
                               THEN 3
                          WHEN `COMD_CODE` = 'KICKED'
                               THEN 4
                          ELSE `SORT_ORDR`
                      END
     , `UPDT_ADMN` = 'SYSTEM'
     , `UPDT_DATE` = CURRENT_TIMESTAMP
 WHERE `COMM_CODE` = 'MEMB_STAT';

INSERT INTO `TM_URMENU` (
            `MENU_NUMB`
          , `SUBX_NUMB`
          , `MENU_NAME`
          , `MENU_URLX`
          , `SORT_ORDR`
          , `SHOW_YSNO`
          , `USEE_YSNO`
          , `REGI_ADMN`
          , `REGI_DATE`
          , `UPDT_ADMN`
          , `UPDT_DATE`
) VALUES (  '12'
          , '0'
          , '독서 모임'
          , ''
          , 2
          , 'Y'
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , NULL
          , NULL
)
       , (  '12'
          , '1'
          , '내 모임'
          , '/reading-clubs/mine'
          , 1
          , 'Y'
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , NULL
          , NULL
)
       , (  '12'
          , '2'
          , '모임 찾기'
          , '/reading-clubs/find'
          , 2
          , 'Y'
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , NULL
          , NULL
)
       , (  '12'
          , '3'
          , '새 모임 만들기'
          , '/reading-clubs/new'
          , NULL
          , 'N'
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , NULL
          , NULL
)
       , (  '12'
          , '4'
          , '독서 모임 상세'
          , '/reading-clubs/'
          , NULL
          , 'N'
          , 'Y'
          , 'SYSTEM'
          , CURRENT_TIMESTAMP
          , NULL
          , NULL
)
ON DUPLICATE KEY UPDATE
       `MENU_NAME` = VALUES(`MENU_NAME`)
     , `MENU_URLX` = VALUES(`MENU_URLX`)
     , `SORT_ORDR` = VALUES(`SORT_ORDR`)
     , `SHOW_YSNO` = VALUES(`SHOW_YSNO`)
     , `USEE_YSNO` = VALUES(`USEE_YSNO`)
     , `UPDT_ADMN` = 'SYSTEM'
     , `UPDT_DATE` = CURRENT_TIMESTAMP;
