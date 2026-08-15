# 목록·초기 번들·공통 캐시 성능 개선

## 개요

- 작업일: 2026-08-15
- 대상: 홈, 공개 독후감, 댓글, 팔로우 목록, 공통 레이아웃, 독서 통계
- 제외: 독서 모임 Service, Mapper 및 화면
- 목표: 최초 전송량과 무제한 목록 조회를 줄이고 중복 API 및 SQL 왕복을 통합한다.

계정 비활성화나 영구 탈퇴의 데이터 처리 범위를 변경하지 않는 조회 최적화와 공통화 작업이므로 계정 수명주기 정책 변경은 포함하지 않는다.

## 개선 전 문제

### 전체 목록 선조회

홈 독후감, ISBN별 공개 독후감, 댓글과 팔로우 목록은 서버에서 전체 결과를 받은 뒤 브라우저에서 일부만 순차 표시했다. 화면에 보이지 않는 행까지 DB 조회, JSON 직렬화, 네트워크 전송과 브라우저 메모리에 포함되는 구조였다.

### 초기 번들 집중

라우터가 모든 페이지를 정적 import하고 공통 헤더가 Firebase Messaging 모듈을 즉시 참조했다. 사용자가 방문하지 않은 페이지와 알림 기능 코드도 최초 JavaScript 청크에 포함됐다.

### 공통 데이터 중복 요청

헤더, 하단 내비게이션, 프로필 화면과 타이머 화면이 로그인 사용자 프로필과 타이머 요약을 각자 조회했다. 화면 전환 시 동일한 서버 상태를 공유하지 못해 불필요한 요청이 발생할 수 있었다.

### 독서 통계 SQL 왕복

독서 통계는 설정, 조회 연도, 일별 시간, 상태, 연속 기록 날짜, 상위 도서, 별점, 연도 비교를 별도 SQL로 조회해 전체 통계 한 번에 최대 8회의 SQL 왕복이 발생했다.

## 개선 내용

### 서버 페이지네이션

공통 `PageDto<T>` 응답에 `list`, `page`, `hasNext`를 정의했다. 다음 페이지 존재 여부는 페이지 크기보다 한 건 더 조회한 뒤 판정한다.

| 목록 | 페이지 단위 | 처리 기준 |
| --- | ---: | --- |
| 홈 독후감 | 12건 | 검색어와 정렬 조건 유지 |
| ISBN별 공개 독후감 | 12건 | 정렬과 독서 상태를 SQL 조건에 반영 |
| 댓글 | 부모 댓글 10개 | 선택한 부모 댓글의 대댓글을 함께 반환 |
| 팔로잉·팔로워 | 10명 | 본인 및 공개 프로필이 같은 계약 사용 |

프런트엔드는 React Query의 무한 조회를 사용해 스크롤 트리거가 동작할 때만 다음 페이지를 요청한다. 프로필 두 화면의 팔로우 모달 상태와 추가 조회 로직은 `useFollowListModal`로 통합했다.

### 공개 독후감 집계 범위 축소

공개 독후감의 좋아요와 댓글 집계 서브쿼리는 대상 ISBN에 해당하는 독후감만 먼저 제한한다. 상태명은 행마다 공통 함수로 조회하지 않고 `TB_CODEXD`를 조인해 가져온다. 상태 필터도 브라우저 후처리가 아니라 원본 독후감 조회 조건에 적용한다.

### 라우트와 Firebase 지연 로딩

각 페이지는 `React.lazy`와 `Suspense`를 통해 방문 시점에 로드한다. Firebase Messaging은 알림 권한이 있고 실제 초기화 또는 로그아웃 토큰 정리가 필요할 때 동적 import한다. 푸시 상태 이벤트는 Firebase SDK와 분리한 경량 모듈에서 공유한다.

### React Query 캐시 공통화

로그인 사용자 프로필과 타이머 요약의 Query Key와 옵션을 공통 모듈에 정의했다. 프로필은 60초, 타이머 요약은 10초의 `staleTime`을 사용하며 헤더, 내비게이션과 관련 화면이 같은 캐시를 재사용한다. 프로필 수정과 타이머 상태 변경은 공통 Query Cache를 갱신해 별도 전역 이벤트 또는 중복 조회를 줄였다.

### 독서 통계 집계 통합

조회 가능 연도와 선택 연도의 일별 시간은 `getHeatmapRowList` 한 번으로 반환한다. 상태, 현재·최장 연속 독서일, 정수 구간별 별점, 현재·이전 연도 비교는 `getStatsAggregate` 한 번으로 집계한다. 상위 도서와 공개 설정 조회는 독립적인 책임을 유지한다.

| 전체 통계 SQL | 개선 전 | 개선 후 |
| --- | ---: | ---: |
| 공개 설정 | 1회 | 1회 |
| 잔디 연도·일별 시간 | 2회 | 1회 |
| 상태·연속 기록·별점·연도 비교 | 4회 | 1회 |
| 상위 도서 | 1회 | 1회 |
| 합계 | 최대 8회 | 4회 |

SQL 호출 수는 소스 구조 기준으로 50% 감소했다. 실제 응답 시간 감소율은 데이터 건수, DB 실행 계획과 네트워크 지연에 따라 달라지므로 별도의 운영 유사 환경 측정이 필요하다.

## 빌드 측정 결과

같은 워크스페이스에서 `vite build`가 출력한 minified 자산 크기를 비교했다.

| 초기 JavaScript 청크 | 개선 전 | 개선 후 | 감소량 | 감소율 |
| --- | ---: | ---: | ---: | ---: |
| 원본 크기 | 725.35kB | 432.85kB | 292.50kB | 40.32% |
| gzip 크기 | 214.00kB | 135.25kB | 78.75kB | 36.80% |

Firebase Messaging은 50.80kB, gzip 15.45kB의 별도 청크로 분리됐다. 페이지별 JavaScript와 CSS도 별도 청크로 생성됐다. 이 값은 Vite 출력 파일 크기이며 실제 브라우저 전송량과 최초 렌더링 시간은 CDN 압축, 캐시, 네트워크와 방문 경로에 따라 달라진다.

## 공통화 결과

- 서버 목록 응답 구조를 `PageDto<T>`로 통일했다.
- 브라우저 페이지 응답 구조를 `PageData<T>`로 통일했다.
- 본인 및 상대 프로필의 팔로우 목록 상태와 추가 조회를 `useFollowListModal`로 통합했다.
- 프로필의 독서 종료일과 등급 표시 계산을 `profileReadingFormat`으로 통합했다.
- 프로필과 타이머 Query Key 및 조회 옵션을 공통화했다.
- 팔로잉과 팔로워 SQL의 공통 컬럼을 MyBatis SQL 조각으로 통합했다.

## 검증

- 백엔드 `compileJava` 성공
- 변경 관련 서비스 테스트 성공
- 프런트엔드 ESLint 성공
- TypeScript `tsc --noEmit` 성공
- Vite 프로덕션 빌드 성공

전체 백엔드 테스트에서는 변경 관련 테스트가 통과했으나 기존 `AuthServiceImplTest.kakaoLoginHandlesKakao4xx`가 메시지 리소스 조회 오류로 실패했다. 이번 변경 범위와 무관한 인증 테스트이며 별도 수정하지 않았다.

## 고려 사항

- 페이지 기반 `OFFSET` 조회는 뒤 페이지로 갈수록 스캔 비용이 늘 수 있다. 대규모 목록에서 문제가 확인되면 정렬 키를 포함한 커서 기반 페이지네이션을 검토한다.
- 댓글은 부모 댓글을 페이지 단위로 선택한 뒤 해당 대댓글을 함께 반환하므로 한 페이지의 실제 행 수는 대댓글 수에 따라 달라진다.
- 통계 통합 SQL은 왕복 횟수를 줄이는 대신 CTE와 조건부 집계가 복잡해졌다. 운영 데이터 규모에서 실행 계획과 인덱스 사용을 추가 확인해야 한다.
- React Query의 `staleTime` 동안은 캐시를 재사용한다. 서버 상태를 즉시 반영해야 하는 변경 작업은 현재처럼 캐시 갱신 또는 무효화를 함께 수행해야 한다.

## 관련 파일

- `src/main/java/org/our/sadari/global/common/dto/PageDto.java`
- `src/main/java/org/our/sadari/report/mapper/ReportMapper.xml`
- `src/main/java/org/our/sadari/reply/mapper/ReplyMapper.xml`
- `src/main/java/org/our/sadari/social/mapper/SocialMapper.xml`
- `src/main/java/org/our/sadari/myPage/mapper/ReadingStatisticsMapper.xml`
- `src/main/frontend/src/router/Router.tsx`
- `src/main/frontend/src/app/query/queryKeys.ts`
- `src/main/frontend/src/features/Social/hooks/useFollowListModal.ts`
- `src/main/frontend/src/features/User/utils/profileReadingFormat.ts`
