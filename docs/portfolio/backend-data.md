# 백엔드와 데이터 설계

## 문서 목적

- 목적: Sadari 백엔드의 데이터 정합성, 도메인 모델링과 트랜잭션 설계를 설명
- 적용 범위: 도서·독후감, 공통코드, 소셜, 닉네임, 예외 처리
- 기준일: 2026-07-30

## 도서와 독후감의 원자적 등록

### 문제

카카오 도서 API 검색 결과는 외부 데이터이므로 내부 DB에 항상 존재하지 않는다. 독후감을 먼저 저장하거나 도서 저장과 독후감 저장을 별개 요청으로 처리하면 도서 없는 독후감이나 중복 도서가 생길 수 있다.

### 구현

`ReportServiceImpl.setReport`는 다음 작업을 하나의 트랜잭션으로 처리한다.

1. 요청값과 도서 정보를 정규화한다.
2. ISBN 기준으로 `TM_BKINFO`의 기존 도서를 확인한다.
3. 도서가 없을 때만 `BookMapper.setBook`으로 도서 마스터를 생성한다.
4. 기존 또는 신규 도서 번호를 `ReportDto`에 설정한다.
5. `ReportMapper.setReport`로 `TM_REPORT`에 독후감을 저장한다.
6. 예상한 등록 건수가 아니면 실패 응답으로 처리하고 트랜잭션을 롤백한다.

구현 근거:

- `src/main/java/org/our/sadari/report/service/ReportServiceImpl.java`
- `src/main/java/org/our/sadari/book/mapper/BookMapper.xml`
- `src/main/java/org/our/sadari/report/mapper/ReportMapper.xml`

### 포트폴리오 포인트

외부 API 결과를 그대로 화면에만 사용하지 않고 내부 마스터 데이터와 사용자 생성 데이터를 일관된 트랜잭션으로 연결했다. 이 경험은 주문·상품, 예약·시설처럼 마스터와 이력 데이터가 함께 생성되는 업무에도 적용할 수 있다.

## 독후감 상태 검증의 단일화

독서 상태에 따라 유효한 기간과 평점 규칙이 다르다.

- `READ`: 목표 독서 기간을 사용하고 기록은 선택값으로 처리
- `DONE`: 종료일과 평점을 저장할 수 있음
- `STOP`: 중단일을 종료일로 처리

등록, 전체 수정, 상태·평점 수정이 서로 다른 검증을 사용하면 동일한 데이터가 API에 따라 허용되거나 거부될 수 있다. `ReportServiceImpl`은 공통 검증 흐름을 통해 상태 코드 존재 여부, 기간, 평점, 기록 길이와 비속어를 일관되게 검사한다.

관련 정책:

- `docs/policies/reading-report-policy.md`
- `src/main/java/org/our/sadari/report/service/ReportServiceImpl.java`

## 공통코드 중심 설계

`TM_CODEXM`과 `TB_CODEXD`는 단순 SelectBox 데이터 이상으로 사용된다.

| 코드 그룹 | 활용 |
| --- | --- |
| `READ_STAT` | 독서 상태와 서비스 검증 |
| `BOOK_COLR` | 달력 색상과 표지 대표색 매칭 |
| `FOLW_STAT` | 팔로우 버튼 상태 |
| `ALIM_SITU` | 알림 상황과 화면 아이콘 |
| `SCHD_CODE` | 스케줄러 실행 여부 |
| `BADX_WORD` | 차단 비속어 사전 |
| `EXCP_WORD` | 정상 표현 예외 사전 |
| `NICK_SUBJ`, `NICK_PRED`, `NICK_ANML` | 신규 회원 닉네임 조합 |

백엔드 `CodeUtil.getCodeGroupList`는 최대 20개 그룹을 한 번에 조회하고, 프론트 `useCodeGroupList`는 정규화된 키로 결과를 10분간 캐시한다.

구현 근거:

- `src/main/java/org/our/sadari/global/common/code/util/CodeUtil.java`
- `src/main/frontend/src/features/Common/utils/codeUtil.ts`

### 트레이드오프

공통코드로 운영 변경 범위를 줄였지만 모든 규칙을 코드 테이블로 옮기면 의미 추적이 어려워질 수 있다. 서비스 불변식은 Java 검증에 두고, 운영자가 변경할 값과 화면 표시값만 공통코드로 관리하는 경계가 필요하다.

### 관리자 연동

`sadari-admin`의 공통코드 관리 기능은 `TM_CODEXM`, `TB_CODEXD`를 직접 관리한다. 사용자 서비스는 같은 테이블의 활성 코드를 상태 검증, 선택지, 비속어 사전, 색상 후보, 자동 닉네임과 스케줄러 실행 조건으로 사용한다.

관리자 감사 컬럼에는 등록·수정 관리자와 일시가 남는다. 사용자 프론트 공통코드와 백엔드 비속어 사전은 각각 10분 캐시를 사용하므로 변경이 항상 즉시 반영되는 것은 아니다.

관리자 구현 근거:

- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/service/CodeManageService.java`
- `../sadari-admin/src/main/java/org/sadari/admin/sadariadmin/common/code/mapper/CodeMapper.xml`

전체 영향 범위는 [관리자와 사용자 서비스 연동](admin-user-integration.md)에 정리했다.

## 운영 테이블의 소유와 소비

| 테이블 | 관리자 서비스 | 사용자 서비스 |
| --- | --- | --- |
| `TM_CODEXM`, `TB_CODEXD` | 코드 등록·수정·삭제 | 상태·정책·표시값 조회 |
| `TB_ALTEMP` | 알림 템플릿 등록·수정 | 템플릿 조회·치환·알림 생성 |
| `TM_URMENU` | 사용자 메뉴 계층·노출 관리 | Header 제목과 햄버거 목록 조회 |
| `TL_SCLOGX`, `TL_SCFAIL` | 실행·실패 로그 조회 | 스케줄러 실행 결과 기록 |

이 구분은 동일 테이블을 두 애플리케이션이 무작정 수정하는 구조가 아니다. 운영 설정은 관리자가 작성하고 사용자 백엔드는 주로 소비하며, 스케줄러 로그는 사용자 백엔드가 작성하고 관리자가 읽는 방향성을 갖는다.

## 소셜 데이터 모델

### 범용 좋아요

`TB_LIKEXX`는 독후감 전용 번호 대신 다음 키를 사용한다.

- 사용자 번호
- 대상 유형 `TAGT_TYPE`
- 대상 번호 `TAGT_NUMB`

현재 서비스는 `REPORT` 유형만 허용하지만 DTO와 테이블은 댓글 등 다른 대상 유형을 확장할 수 있는 구조다. 서비스는 범용 테이블이라는 이유로 모든 문자열을 허용하지 않고 현재 지원하는 대상 유형을 검증한다.

구현 근거:

- `src/main/java/org/our/sadari/social/dto/SocialDto.java`
- `src/main/java/org/our/sadari/social/service/SocialServiceImpl.java`
- `src/main/java/org/our/sadari/social/mapper/SocialMapper.xml`

### 팔로우 상태의 중앙화

팔로우 버튼은 양방향 관계에 따라 `팔로우`, `맞팔로우`, `팔로잉`으로 달라진다. 이 판단을 화면마다 중복하지 않고 MySQL 함수 `FN_GET_FOLW_STAT`에 로그인 사용자와 대상 사용자 번호를 전달해 결정한다.

`SocialServiceImpl.getFollowStatus`와 `SocialMapper.getFollowStatus`가 이 함수를 사용한다. 관계 판단 기준이 SQL, 서비스, 화면에 흩어지지 않는다는 점이 핵심이다.

## 신규 회원 닉네임 발급

### 요구사항

Kakao 닉네임을 그대로 사용하면 중복 정책을 보장할 수 없다. 신규 가입 시 서비스 고유 닉네임을 발급한다.

형식 예시:

```text
마음이 따뜻한 코끼리_26070001
```

### 구현 방식

1. 주어, 서술어, 동물 명사를 공통코드에서 한 번에 조회한다.
2. 주어의 `OPT1_CODE`부터 `OPT4_CODE`에 등록된 네 개의 서술어 코드로 자연스러운 조합을 제한한다.
3. 조합을 무작위로 선택한다.
4. `TB_NKSEQX`에서 주어·서술어·동물 세부코드 조합과 연월별 순번을 원자적으로 증가시킨다.
5. 최초 동시 등록 충돌은 `DuplicateKeyException`을 처리한 뒤 기존 행 증가로 재시도한다.
6. 최대 100개 조합을 시도하고 25자 정책과 최종 중복 여부를 검사한다.

`TB_NKSEQX`의 물리 컬럼은 복합키인 `SUBJ_CODE`, `PRED_CODE`, `ANML_CODE`, `ISSU_YEAM`을 먼저 배치하고 `LAST_NUMB`, `REGI_DATE`, `UPDT_DATE`를 이어서 배치한다.

구현 근거:

- `src/main/java/org/our/sadari/user/service/NicknameGenerationServiceImpl.java`
- `src/main/java/org/our/sadari/user/mapper/NicknameSequenceMapper.xml`
- `src/test/java/org/our/sadari/user/service/NicknameGenerationServiceImplTest.java`

### 포트폴리오 포인트

문자열 조합 기능에서 끝내지 않고 동시 가입 시 순번 충돌까지 고려해 별도 시퀀스 테이블과 재시도 정책을 설계했다.

## 공통 오류 응답

백엔드는 `ResultData`와 `ResultEnum`으로 업무 성공·실패 응답을 통일한다. `CommonExceptionHandler`는 MyBatis 예외 내부 원인을 추적해 DB 연결 실패를 별도 응답으로 변환한다.

구현 근거:

- `src/main/java/org/our/sadari/global/common/result/ResultData.java`
- `src/main/java/org/our/sadari/global/common/exception/CommonExceptionHandler.java`

## 향후 개선

- 범용 좋아요 대상이 추가되면 대상 유형별 존재 여부 검증 전략을 인터페이스로 분리할 수 있다.
- 닉네임 발급의 최종 중복 방지는 DB Unique Key를 기준으로 보장하고 서비스 재시도를 함께 운영해야 한다.
- 주요 집계 SQL은 운영 데이터 기준 실행 계획과 인덱스 효과를 별도로 기록해야 한다.
- 관리 데이터 변경 이력을 별도 감사 로그로 남기고 승인·롤백 절차를 추가할 수 있다.
