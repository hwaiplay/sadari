-- 세부코드가 같은 공통코드 안에서 재귀 계층을 구성할 수 있도록 상위 코드를 추가한다
ALTER TABLE TB_CODEXD
    ADD COLUMN UPPR_CODE varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '상위 세부코드' AFTER CODE_EXPL,
    ADD KEY IX_TB_CODEXD_TREE (COMM_CODE, UPPR_CODE, SORT_ORDR, COMD_CODE),
    ADD CONSTRAINT FK_TB_CODEXD_UPPR FOREIGN KEY (COMM_CODE, UPPR_CODE) REFERENCES TB_CODEXD (COMM_CODE, COMD_CODE);

START TRANSACTION;

-- 독서 관심분야를 담을 단일 공통코드를 등록한다
INSERT INTO TM_CODEXM (
            COMM_CODE
          , CODE_NAME
          , CODE_EXPL
          , USEE_YSNO
          , REGI_ADMN
          , REGI_DATE
          , UPDT_ADMN
          , UPDT_DATE
)
SELECT 'CATE_CODE'
     , '독서 관심분야'
     , '독서 관심분야 대분류와 세부 카테고리 계층'
     , 'Y'
     , 'SYSTEM'
     , CURRENT_TIMESTAMP
     , NULL
     , NULL;

-- 기존 CATE_ 공통코드를 CATE_CODE의 최상위 대분류 세부코드로 옮긴다
INSERT INTO TB_CODEXD (
            COMM_CODE
          , COMD_CODE
          , COMD_NAME
          , CODE_EXPL
          , UPPR_CODE
          , USEE_YSNO
          , REGI_ADMN
          , REGI_DATE
          , SORT_ORDR
          , UPDT_ADMN
          , UPDT_DATE
)
SELECT 'CATE_CODE'
     , COMM_CODE
     , CODE_NAME
     , CODE_EXPL
     , NULL
     , USEE_YSNO
     , REGI_ADMN
     , REGI_DATE
     , SORT_ORDR
     , UPDT_ADMN
     , UPDT_DATE
  FROM TM_CODEXM
 WHERE COMM_CODE LIKE 'CATE\_%'
   AND COMM_CODE != 'CATE_CODE';

-- 기존 관심분야 상세코드를 대분류 세부코드의 자식으로 옮긴다
UPDATE TB_CODEXD
   SET UPPR_CODE = COMM_CODE
     , COMM_CODE = 'CATE_CODE'
 WHERE COMM_CODE LIKE 'CATE\_%'
   AND COMM_CODE != 'CATE_CODE';

-- 계층으로 전환한 기존 CATE_ 공통코드를 제거한다
DELETE
  FROM TM_CODEXM
 WHERE COMM_CODE LIKE 'CATE\_%'
   AND COMM_CODE != 'CATE_CODE';

COMMIT;

-- 공통코드 그룹은 계층 정렬 대상이 아니므로 마스터 정렬 컬럼을 제거한다
ALTER TABLE TM_CODEXM
    DROP COLUMN SORT_ORDR;
