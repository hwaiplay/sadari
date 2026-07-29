# 성능과 알고리즘

## 문서 목적

- 목적: Sadari에서 실제 문제 해결에 적용한 성능 개선과 알고리즘을 설명
- 적용 범위: SQL 왕복 축소, 비속어 탐지, 표지 색상 매칭, 코드 캐시
- 기준일: 2026-07-30

## 마이페이지 독서 요약 SQL 최적화

### 문제

마이페이지는 현재 읽는 책, 주간·월간·연간 독서량, 이전 기간 비교, 목표와 달성 횟수를 함께 보여준다. 기존 구현은 조건만 다른 SQL을 반복 호출해 한 화면 요청에서 최대 19회의 DB 왕복이 발생했다.

### 개선

두 종류의 조회로 통합했다.

1. `getReadingSummary`: CTE와 조건부 집계로 기간별 독서량, 목표, 달성 횟수를 한 행으로 조회
2. `getReadingSummaryReportList`: 현재 읽는 책과 현재 연도 완료 책을 한 번 조회

서비스는 목록을 한 번 순회하며 현재 주, 월, 연도 목록으로 분류한다. 전체 이력이 아니라 현재 읽는 책과 현재 연도 완료 데이터로 범위를 제한해 애플리케이션 메모리 사용도 제어했다.

### 결과

| 구분 | 개선 전 | 개선 후 |
| --- | ---: | ---: |
| 집계 SQL | 15회 | 1회 |
| 목록 SQL | 4회 | 1회 |
| 전체 SQL 왕복 | 최대 19회 | 2회 |

소스 구조 기준 SQL 왕복 횟수는 약 89.5% 감소했다. 이는 실제 응답 시간 개선율이 아니며 운영 데이터 기준 평균·P95 응답 시간 측정이 추가로 필요하다.

구현 근거:

- `docs/performance/my-page-reading-summary-optimization.md`
- `src/main/java/org/our/sadari/myPage/dto/ReadingSummaryQueryDto.java`
- `src/main/java/org/our/sadari/report/mapper/ReportMapper.xml`
- `src/main/java/org/our/sadari/report/service/ReportServiceImpl.java`

### 면접에서 설명할 트레이드오프

- SQL 왕복은 줄었지만 CTE와 조건부 집계로 SQL 복잡도가 증가했다.
- 기간별 목록 분류를 Java로 옮겨 DB 반복 조회 대신 애플리케이션 메모리를 사용한다.
- 목록 조회 범위를 현재 연도로 제한해 메모리 증가를 통제했다.
- 인덱스와 실행 계획은 운영 데이터 분포를 기준으로 별도 검증해야 한다.

## Aho-Corasick 비속어 탐지

### 문제

비속어 사전의 각 단어에 대해 사용자 문장을 반복 `contains`로 검사하면 사전 크기와 입력 길이가 커질수록 비교 비용이 증가한다. 사용자 입력마다 DB에서 사전을 조회하는 방식도 비효율적이다.

또한 다음 요구를 함께 만족해야 한다.

- 특수문자 또는 숫자를 끼운 우회 표현 탐지
- `시발점`처럼 비속어 문자열이 포함된 정상 단어 허용
- `보이지`처럼 일반 글자가 포함된 정상 문장을 과잉 차단하지 않음

### 구현

`BadWordDetectionService`는 다음 구조를 사용한다.

1. `BADX_WORD`와 `EXCP_WORD` 공통코드를 한 번 조회한다.
2. 두 사전을 같은 10분 캐시 생명주기로 관리한다.
3. 일반 탐지와 숫자 우회 탐지용 Aho-Corasick 자동자를 생성한다.
4. Trie와 Failure Link를 미리 구성한다.
5. 사용자 입력을 Unicode Code Point 단위로 한 번 순회해 모든 후보를 찾는다.
6. 차단 단어 범위를 더 넓게 감싸는 예외 단어가 있으면 정상 표현으로 허용한다.
7. 숫자·특수문자 우회는 별도 정규화 문자열로 검사하고 일반 한글·영문 삽입은 제거하지 않는다.

구현 근거:

- `src/main/java/org/our/sadari/global/common/service/BadWordDetectionService.java`
- `src/main/java/org/our/sadari/global/common/constant/Constant.java`

### 복잡도 관점

사전 자동자 구성은 캐시 갱신 시 수행하고, 일반 요청은 입력 길이와 매칭 결과 수에 비례해 처리한다. 사전 단어를 매 요청마다 순차 비교하는 구조보다 입력이 길거나 사전이 클 때 유리하다.

### 포트폴리오 포인트

알고리즘을 단순 학습 예제로 구현한 것이 아니라 DB 관리 사전, 캐시, 우회 표현, 예외 범위 정책을 결합해 실제 입력 검증에 적용했다.

## 도서 표지 대표색과 CIELAB 매칭

### 문제

독서 달력의 색상을 사용자가 매번 선택하게 하면 입력 부담이 생긴다. 표지 대표색을 그대로 사용하면 화면 팔레트와 어울리지 않거나 가독성이 낮아질 수 있다.

### 구현

`BookCoverColorService`는 NAVER 표지를 분석해 `BOOK_COLR` 공통코드 중 가장 가까운 색상을 선택한다.

1. 신뢰한 NAVER 이미지 URL만 다운로드한다.
2. 최대 5MB, 4096픽셀 크기 제한을 적용한다.
3. 최대 4096개 픽셀을 표본 추출한다.
4. 투명하거나 흰색에 가까운 배경 픽셀을 제외한다.
5. RGB를 버킷화하고 빈도와 채도를 고려해 대표색을 계산한다.
6. 대표 RGB와 공통코드 HEX를 D65 기준 CIELAB로 변환한다.
7. CIELAB 유클리드 거리 제곱이 가장 작은 활성 색상 코드를 선택한다.
8. 분석 실패 시 정렬상 첫 공통코드를 안전한 기본값으로 반환한다.

구현 근거:

- `src/main/java/org/our/sadari/book/service/BookCoverColorService.java`
- `src/test/java/org/our/sadari/book/service/BookCoverColorServiceTest.java`
- `src/main/frontend/src/pages/Book/Set/SetReportPage.tsx`

### 선택 이유

RGB 축의 단순 거리는 사람의 색상 인지 차이와 일치하지 않는다. CIELAB를 사용하면 밝기와 색상 축을 분리해 시각적으로 더 가까운 팔레트 색을 선택할 수 있다.

## 공통코드 일괄 조회와 캐시

독후감 등록·수정 화면은 상태, 공개 여부, 책장 색상 등 여러 코드 그룹이 필요하다. 그룹마다 API를 호출하지 않고 `commCodes` 쿼리로 여러 그룹을 한 번에 요청한다.

- 백엔드: 중복 제거, 대문자 정규화, 최대 20개 그룹 제한
- 프론트: 정렬·중복 제거한 Query Key 사용
- 캐시: TanStack Query `staleTime` 10분

구현 근거:

- `src/main/java/org/our/sadari/global/common/code/util/CodeUtil.java`
- `src/main/frontend/src/features/Common/utils/codeUtil.ts`

## 추가 측정 과제

| 항목 | 현재 상태 | 필요한 검증 |
| --- | --- | --- |
| 마이페이지 응답 시간 | SQL 횟수만 산정 | 동일 데이터 기준 평균·P95·P99 |
| 통합 SQL | 소스 구현 완료 | Oracle 실행 계획과 논리 읽기 |
| 비속어 탐지 | 알고리즘 구현 완료 | 사전 크기별 JMH 비교 |
| 표지 색상 | 단위 테스트 존재 | 실제 표지 샘플의 사용자 평가 |
| 코드 캐시 | 10분 적용 | 코드 변경 전파 지연 모니터링 |

