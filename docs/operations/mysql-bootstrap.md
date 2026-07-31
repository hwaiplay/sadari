# MySQL 8.4 초기 구축 절차

## 결과물

자동 실행용 PowerShell 파일이나 DB 변환 스크립트는 사용하지 않습니다. 다음 정적 SQL만 사용합니다.

| 실행 순서 | 파일 | 역할 |
| --- | --- | --- |
| 1 | `scripts/db/mysql/01-create.sql` | 26개 테이블, PK, FK, 인덱스, 코멘트 생성 |
| 2 | `scripts/db/mysql/output/02-admin-insert.sql` | 관리자 기준정보 9개 테이블의 현재 데이터 입력 |
| 3 | `scripts/db/mysql/routines.sql` | MySQL 함수 4개와 프로시저 1개 생성 |
| 선택 | `scripts/db/mysql/03-reset-user-data.sql` | 사용자 데이터 17개 테이블 삭제 및 자동증가 값 초기화 |

`02-admin-insert.sql`에는 관리자 계정 정보가 들어 있으므로 `output` 디렉터리는 Git에 커밋하지 않고 안전한 경로로 직접 전달합니다.

## 이관하는 관리자 기준정보

다음 9개 테이블만 데이터가 포함됩니다.

| 테이블 | 용도 |
| --- | --- |
| `TM_ADMINX` | 관리자 계정 |
| `TM_AUTHXM` | 관리자 권한 그룹 |
| `TM_ADMENU` | 관리자 메뉴 |
| `TM_URMENU` | 사용자 메뉴 |
| `TM_CODEXM` | 공통코드 그룹 |
| `TB_CODEXD` | 공통코드 상세 |
| `TB_ALTEMP` | 알림 템플릿 |
| `CT_POPUPX` | 사용자 안내 팝업 |
| `TB_AUTHMN` | 권한별 관리자 메뉴 권한 |

다음 17개 테이블은 구조만 만들고 데이터는 이관하지 않습니다.

`TB_ALIMXX`, `TB_EVTBOX`, `TB_FOLLOW`, `TB_LIKEXX`, `TB_LOGHIS`, `TB_NKSEQX`, `TB_PSHSUB`, `TB_REPLXX`, `TH_USSPND`, `TH_USWTHD`, `TL_SCFAIL`, `TL_SCLOGX`, `TM_BKINFO`, `TM_FILEXM`, `TM_GOALXM`, `TM_REPORT`, `TM_USERXM`

## PK 발급 방식

- MySQL 8.4에는 Oracle식 `CREATE SEQUENCE`를 사용하지 않습니다.
- 사용자 서버가 새 행을 생성하는 숫자 PK 9개는 `AUTO_INCREMENT`로 만들며 시작값은 1입니다.
- 관리자 서버가 생성하는 `TH_USSPND.SPND_NUMB`는 INSERT 직전에 `MAX(SPND_NUMB) + 1`로 계산합니다.
- 공유 테이블 `TB_EVTBOX.EVNT_NUMB`는 사용자 서버에서는 `AUTO_INCREMENT`, 관리자 서버에서는 명시적인 `MAX(EVNT_NUMB) + 1` 값을 사용합니다.
- 관리자 계정 초기 데이터의 `TM_ADMINX.ADMN_NUMB`는 `02-admin-insert.sql`에 1부터 명시합니다.
- 관리자·사용자 메뉴의 문자열 번호는 각 Mapper에서 `MAX(...) + 1`로 계산합니다.

## 빈 데이터베이스 준비

MySQL 관리자 계정에서 다음 SQL을 한 문장씩 실행합니다. 비밀번호는 저장소나 문서에 기록하지 않습니다.

```sql
CREATE DATABASE sadari
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'sadari'@'localhost'
    IDENTIFIED BY '<password>';

GRANT ALL PRIVILEGES ON sadari.*
    TO 'sadari'@'localhost';
```

계정이나 데이터베이스가 이미 존재하면 해당 `CREATE` 문은 생략합니다.

## DBeaver 실행 순서

1. 대상 MySQL 연결에서 `sadari` 데이터베이스를 선택합니다.
2. SQL 편집기로 `01-create.sql`을 열어 전체 실행합니다.
3. `02-admin-insert.sql`을 열어 전체 실행합니다.
4. 바이너리 로그가 활성화된 MySQL에서는 관리자 권한 연결로 다음 SQL을 먼저 실행합니다.

```sql
SET GLOBAL log_bin_trust_function_creators = 1;
```

5. `sadari` 연결로 돌아와 애플리케이션이 사용하는 `routines.sql`을 전체 실행합니다.
6. 관리자 권한 연결에서 `SET GLOBAL log_bin_trust_function_creators = 0;`을 실행해 임시 허용값을 복원합니다.
7. 기존 개발 DB의 사용자 데이터를 지워야 할 때만 `03-reset-user-data.sql`을 실행합니다. 이 파일은 되돌릴 수 없는 `TRUNCATE`를 수행합니다.

4번은 함수 생성 시 `ERROR 1419`가 발생하는 서버에서만 필요하며 `SYSTEM_VARIABLES_ADMIN` 또는 동등한 관리자 권한이 필요합니다. 서버 재시작 후 루틴을 다시 생성할 때는 전역값을 다시 확인합니다.

## 검증

```sql
SELECT COUNT(*) AS TABLE_COUNT
  FROM INFORMATION_SCHEMA.TABLES
 WHERE TABLE_SCHEMA = 'sadari'
   AND TABLE_TYPE = 'BASE TABLE';

SELECT TABLE_NAME, COLUMN_NAME
  FROM INFORMATION_SCHEMA.COLUMNS
 WHERE TABLE_SCHEMA = 'sadari'
   AND EXTRA LIKE '%auto_increment%'
 ORDER BY TABLE_NAME;

SELECT ROUTINE_TYPE, ROUTINE_NAME
  FROM INFORMATION_SCHEMA.ROUTINES
 WHERE ROUTINE_SCHEMA = 'sadari'
 ORDER BY ROUTINE_TYPE, ROUTINE_NAME;
```

성공 기준은 테이블 26개, `AUTO_INCREMENT` 컬럼 9개, 함수 4개, 프로시저 1개입니다. 신규 DB에서는 제외 대상 17개 테이블의 행 수가 모두 0이어야 합니다.

## 구현 근거

- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/output/02-admin-insert.sql`
- `scripts/db/mysql/03-reset-user-data.sql`
- `scripts/db/mysql/routines.sql`
- `sadari-admin` 저장소 `src/main/java/org/sadari/admin/sadariadmin/currentuser/mapper/CurrentUserMapper.xml`
