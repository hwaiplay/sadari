/* 관리자 독서 모임 메뉴를 기존 RDS에도 반복 적용할 수 있게 등록함 */
INSERT INTO TM_ADMENU (
       MENU_NUMB, SUBX_NUMB, MENU_NAME, MENU_URLX, SORT_ORDR, USEE_YSNO
     , REGI_ADMN, REGI_DATE, UPDT_ADMN, UPDT_DATE
) VALUES (
       7, 5, '독서 모임 관리', '/sadari/adm/reading-club/list', 5, 'Y'
     , 1, CURRENT_TIMESTAMP(6), 1, CURRENT_TIMESTAMP(6)
)
ON DUPLICATE KEY UPDATE MENU_NAME = VALUES(MENU_NAME)
                      , MENU_URLX = VALUES(MENU_URLX)
                      , SORT_ORDR = VALUES(SORT_ORDR)
                      , USEE_YSNO = VALUES(USEE_YSNO)
                      , UPDT_ADMN = VALUES(UPDT_ADMN)
                      , UPDT_DATE = VALUES(UPDT_DATE);

/* 일반 관리자와 슈퍼 관리자에게 독서 모임 조회·수정 권한을 등록함 */
INSERT INTO TB_AUTHMN (
       AUTH_CODE, MENU_NUMB, SUBX_NUMB, READ_YSNO, WRIT_YSNO, DELT_YSNO
     , REGI_ADMN, REGI_DATE, UPDT_ADMN, UPDT_DATE
) VALUES
       ('ADMIN', 7, 5, 'Y', 'Y', 'N', 1, CURRENT_TIMESTAMP(6), 1, CURRENT_TIMESTAMP(6))
     , ('SUPER', 7, 5, 'Y', 'Y', 'N', 1, CURRENT_TIMESTAMP(6), 1, CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE READ_YSNO = VALUES(READ_YSNO)
                      , WRIT_YSNO = VALUES(WRIT_YSNO)
                      , DELT_YSNO = VALUES(DELT_YSNO)
                      , UPDT_ADMN = VALUES(UPDT_ADMN)
                      , UPDT_DATE = VALUES(UPDT_DATE);
