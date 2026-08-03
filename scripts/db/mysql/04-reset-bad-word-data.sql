-- BADX_WORD 공통코드 세부 데이터를 핵심 욕설 사전으로 재구성한다.
SET NAMES utf8mb4;
USE sadari;
SET @bad_word_code = 'BADX_WORD';
SET @word_code_prefix = 'WORD_';
SET @yes_code = 'Y';
SET @system_admin = 'SYSTEM';
START TRANSACTION;

-- 기존 비속어 세부코드는 새 연속 코드와 정렬 순서를 보장하기 위해 전체 교체한다.
DELETE /* delBadWordDictionary */
  FROM TB_CODEXD
 WHERE COMM_CODE = @bad_word_code;

-- 소스 정규화로 복원할 수 없는 욕설 철자 변형만 순서대로 정의한다.
SET @bad_word_json = JSON_ARRAY(
    '18놈',
    '18새끼',
    '개가튼',
    '개같',
    '개걸레',
    '개넘',
    '개년',
    '개놈',
    '개늠',
    '개라슥',
    '개새끼',
    'ㄱㅐㅅㅐㄲl',
    '개세끼',
    '개쓰래기',
    '개쓰레기',
    '개씁년',
    '개씁블',
    'ㄱㅐㅈㅏ',
    '개자식',
    '개후라',
    '걸래년',
    '걸레같은년',
    '걸레년',
    '걸레핀년',
    '게늠',
    '게새끼',
    '게세끼',
    '게자식',
    '너거애비',
    '느금마',
    '니기미',
    '니미',
    '니씨브랄',
    '니애미',
    '니애비',
    '닌기미',
    '닝기미',
    '띠발',
    '띠벌',
    '띠벨',
    '띠빌',
    '띠팔',
    '띠펄',
    '띠풀',
    '맛간년',
    '미친넘',
    '미친년',
    '미친놈',
    '미친눔',
    '미친새끼',
    '미친쇄리',
    '미친쇠리',
    '미친쉐이',
    '미튄',
    '미티넘',
    '미틴것',
    '미틴넘',
    '미틴년',
    '미틴놈',
    'ㅂㅅ',
    '벵신',
    '병닥',
    '병딱',
    '병신',
    '뷩딱',
    '븅쉰',
    '븅신',
    '빙띤',
    '빙신',
    '빠가씹새',
    '뻑큐',
    '뼝신',
    'ㅅㅂ',
    '상년',
    '새꺄',
    '새뀌',
    '색갸',
    '색끼',
    '색키',
    '샤발',
    '세꺄',
    '쉐끼',
    '쉑갸',
    '쉬발',
    '쉬방',
    '쉬밸',
    '쉬벌',
    '쉬불',
    '쉬붕',
    '쉬빨',
    '쉬이발',
    '쉬이방',
    '쉬이벌',
    '쉬이불',
    '쉬이붕',
    '쉬이빨',
    '쉬이팔',
    '쉬이펄',
    '쉬이풀',
    '쉬팔',
    '쉬펄',
    '쉬풀',
    '쉽쌔',
    '시댕이',
    '시발',
    '시밸',
    '시벌',
    '시불',
    '시붕',
    '시이발',
    '시이벌',
    '시이불',
    '시이붕',
    '시이팔',
    '시이펄',
    '시이풀',
    '시팔',
    '시펄',
    '십8',
    '십때끼',
    '십떼끼',
    '십새',
    '십세이',
    '십셰리',
    '십쉐',
    '십자석',
    '십자슥',
    '십창',
    '십탱',
    '십팔새끼',
    'ㅆㅂ',
    '쌍넘',
    '쌍년',
    '쌍놈',
    '쌍눔',
    '쌔끼',
    '쌔리',
    '썅년',
    '썅놈',
    '썅뇬',
    '썅늠',
    '써글년',
    '쓉새',
    '쓰바새끼',
    '쓰브랄쉽세',
    '씌발',
    '씌팔',
    '씨가랭넘',
    '씨가랭년',
    '씨가랭놈',
    '씨바',
    '씨발',
    '씨방새',
    '씨방세',
    '씨밸',
    '씨뱅가리',
    '씨벌',
    '씨부랄',
    '씨부럴',
    '씨불',
    '씨붕',
    '씨브럴',
    '씨블',
    '씨빨',
    '씨이발',
    '씨이벌',
    '씨이불',
    '씨이붕',
    '씨이팔',
    '씨파넘',
    '씨팔',
    '씨펄',
    '씨퐁넘',
    '씨퐁뇬',
    '씹년',
    '씹미랄',
    '씹새끼',
    '씹세',
    '씹자석',
    '씹자슥',
    '씹창',
    '씹탱',
    '씹팔',
    '아가리',
    '애미랄',
    '애미씨뱅',
    '양아치',
    '엄창',
    '염병',
    '염뵹',
    '엿먹어라',
    '육갑',
    'ㅈㄹ',
    '잡년',
    '잡놈',
    '젓가튼',
    '젓같',
    '젓까',
    '젓나',
    '젓밥',
    '젖같은',
    '젖까',
    '젖밥',
    '조까',
    '조또',
    '족같',
    '족까',
    '존나',
    '존니',
    '졸라',
    '좃',
    '좆',
    '쥐랄',
    '지랄',
    '지랼',
    '지럴',
    '지뢀',
    '찌랄',
    '촌씨브라리',
    '촌씨브랑이',
    '촌씨브랭이',
    '호냥년',
    '호로새끼',
    '호로자슥',
    '호로자식',
    '호로짜식',
    '호루자슥',
    '후라덜넘',
    'bitch',
    'fuck'
);

-- 배열 순서를 세부코드와 정렬 순서에 동일하게 반영한다.
/* setBadWordDictionary */
INSERT INTO TB_CODEXD (
            COMM_CODE
          , COMD_CODE
          , COMD_NAME
          , CODE_EXPL
          , OPT1_CODE
          , OPT1_NAME
          , OPT2_CODE
          , OPT2_NAME
          , OPT3_CODE
          , OPT3_NAME
          , OPT4_CODE
          , OPT4_NAME
          , USEE_YSNO
          , REGI_ADMN
          , REGI_DATE
          , SORT_ORDR
          , UPDT_ADMN
          , UPDT_DATE
)
     SELECT @bad_word_code AS COMM_CODE
          , CONCAT(@word_code_prefix, LPAD(WORDS.SORT_ORDR, 4, '0')) AS COMD_CODE
          , WORDS.COMD_NAME
          , '비속어 필터 차단 대상 욕설' AS CODE_EXPL
          , NULL AS OPT1_CODE
          , NULL AS OPT1_NAME
          , NULL AS OPT2_CODE
          , NULL AS OPT2_NAME
          , NULL AS OPT3_CODE
          , NULL AS OPT3_NAME
          , NULL AS OPT4_CODE
          , NULL AS OPT4_NAME
          , @yes_code AS USEE_YSNO
          , @system_admin AS REGI_ADMN
          , CURRENT_TIMESTAMP AS REGI_DATE
          , WORDS.SORT_ORDR
          , NULL AS UPDT_ADMN
          , NULL AS UPDT_DATE
       FROM JSON_TABLE(
                @bad_word_json
              , '$[*]' COLUMNS (
                    SORT_ORDR FOR ORDINALITY
                  , COMD_NAME VARCHAR(100) PATH '$'
                )
            ) WORDS
   ORDER BY WORDS.SORT_ORDR;

COMMIT;
