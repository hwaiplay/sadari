# 도서 검색 및 외부 API 쿼터 보호 정책

## 개요

- 기준일은 2026년 8월 16일입니다.
- 로그인 회원이 Kakao 도서 검색 API를 사용하는 화면과 서버의 호출 제한 및 캐시 처리에 적용합니다.
- 외부 API 일일 쿼터를 효율적으로 사용하고 한 회원의 반복 요청이 전체 서비스 검색 기능을 소진하지 않게 하는 것이 목적입니다.

## 접근 범위

- 도서 검색 화면과 `GET /api/book/search`는 로그인한 `ACTIVE` 회원만 사용할 수 있습니다.
- 비로그인 요청은 Spring Security 인증 단계에서 차단합니다.
- `WITHDRAWN`, `DELETE_PENDING` 및 `SUSPENDED` 상태는 기존 계정 상태 접근 제한을 적용하므로 도서 검색을 사용할 수 없습니다.
- 클라이언트가 보낸 회원 번호를 신뢰하지 않고 인증된 회원 번호를 요청 제한 기준으로 사용합니다.

## 검색 결과 조회

1. 서버는 카카오 도서 검색 API 한 번에 최대 50권을 요청합니다.
2. 화면은 받아둔 결과 중 처음 10권만 표시합니다.
3. 사용자가 목록 하단에 도달하면 이미 받은 결과에서 10권씩 추가로 표시합니다.
4. 받아둔 50권을 모두 표시한 뒤에만 다음 카카오 검색 페이지를 호출합니다.
5. 카카오 응답의 `meta.is_end`가 마지막 페이지이면 빈 다음 페이지를 호출하지 않습니다.

10권 단위로 카카오 API를 직접 호출하던 구조와 비교하면 50권을 확인하는 데 필요한 외부 호출 수는 최대 5회에서 1회로 감소합니다. 이는 소스 호출 구조에서 계산한 값이며 네트워크 응답 시간 개선을 측정한 결과는 아닙니다.

## 기간별 인기 도서

- 검색 화면에 처음 진입하고 복원할 직접 검색 결과가 없으면 `GET /api/book/popular?period=monthly`로 월간 인기 도서를 최대 10권 조회합니다.
- 사용자는 검색 입력창 아래 오른쪽의 선택 목록에서 `weekly`, `monthly`, `yearly`에 해당하는 주간·월간·연간 인기 도서를 변경할 수 있습니다.
- `Asia/Seoul` 기준으로 주간은 현재 주 월요일부터 다음 월요일 전까지, 월간은 현재 달 1일부터 다음 달 1일 전까지, 연간은 현재 연도 1월 1일부터 다음 연도 1월 1일 전까지 `TM_REPORT.REGI_DATE`를 집계합니다.
- 회원 상태, 독서 상태 및 독후감 공개 여부와 관계없이 `TM_REPORT`에 남아 있는 모든 독후감을 집계합니다.
- 작성자 수가 많은 도서부터 정렬하고, 동률이면 가장 최근 독후감 등록 시각과 도서 번호 순으로 정렬해 1위부터 10위까지 순위를 부여합니다.
- 인기 도서 평균 평점은 도서 정보 화면과 동일하게 읽는 중인 독후감을 제외하고 공개 여부와 관계없이 해당 도서의 전체 완료·중단 독후감 평점으로 계산합니다.
- 인기 도서 카드는 도서 소개를 표시하지 않고 출판사 위치에 도서 정보 화면과 같은 별 아이콘 및 평균 평점을 표시합니다. 순위와 작성자 수는 도서 표지 아래와 제목 위에 표시합니다.
- 직접 검색 결과는 기존 출판사와 세 줄 도서 소개를 유지하며, 검색 결과가 반환되면 인기 도서 기간 선택, 순위, 작성자 수 및 평균 평점을 숨깁니다.

## 회원별 요청 제한

| 제한 | 기본값 | 집계 대상 | 만료 |
| --- | ---: | --- | --- |
| 단기 반복 요청 | 60초당 20회 | 캐시 적중을 포함한 회원의 모든 도서 검색 요청 | 첫 허용 요청부터 60초 |
| 일간 반복 요청 | 24시간당 200회 | 캐시 적중을 포함한 회원의 모든 도서 검색 요청 | 첫 허용 요청부터 24시간 |

- 분간 및 일간 카운터는 Redis Lua Script에서 한 번에 검사하고 허용된 요청만 원자적으로 증가시킵니다.
- 제한을 초과하면 카카오 API를 호출하지 않고 “검색 요청이 너무 많아요. 잠시 후 다시 시도해주세요.” 메시지를 반환합니다.
- Redis 장애로 제한을 확인할 수 없으면 외부 쿼터 보호를 우선하여 도서 검색을 일시적으로 차단합니다.
- 제한값은 운영 환경변수로 조정할 수 있으며 변경된 값은 새 요청부터 적용합니다.

## 앱 전체 쿼터 보호

- 공용 캐시에 없는 검색으로 카카오 API를 실제 호출하기 직전에 앱 전체 카운터를 예약합니다.
- 기본 24시간 실제 호출 한도는 27,000회입니다.
- 카카오 도서 검색 일일 제공량 30,000회 중 3,000회는 운영 확인과 비상 대응을 위해 남깁니다.
- 카카오 요청이 오류 응답을 반환해도 공급자 쿼터가 사용됐을 가능성이 있으므로 예약한 횟수를 되돌리지 않습니다.
- 공용 캐시에 적중한 요청은 카카오 API를 호출하지 않으므로 앱 전체 실제 호출 카운터를 증가시키지 않습니다.

## 공용 검색 결과 캐시

- 같은 검색어와 카카오 페이지의 응답은 Redis에 기본 10분간 저장합니다.
- 캐시 키에는 검색어 원문을 기록하지 않고 정규화한 검색어와 페이지를 SHA-256으로 변환한 값을 사용합니다.
- 캐시 값에는 카카오가 반환한 공개 도서 정보와 페이지 메타정보만 저장하며 회원 번호, 검색 회원, 선택한 도서 및 독후감 정보를 포함하지 않습니다.
- 캐시 저장에 실패해도 이미 받은 카카오 응답은 사용자에게 반환하지만, 다음 동일 검색은 새 외부 호출 한도 예약이 필요합니다.
- 캐시 조회값이 없거나 복원할 수 없으면 앱 전체 호출 한도를 다시 검사한 뒤에만 카카오 API를 호출합니다.

## 계정 수명주기

- 계정 비활성화와 영구 탈퇴 신청 시 회원별 분간 및 일간 제한 카운터는 기존 TTL까지 유지합니다.
- 재로그인으로 계정을 활성화하거나 영구 탈퇴를 취소해도 남은 제한 횟수와 만료시간을 초기화하지 않습니다.
- 유예기간이 지나 회원이 물리 삭제되면 회원별 제한 키를 즉시 삭제합니다.
- 공용 검색 결과 캐시는 사용자와 연결되지 않으므로 계정 비활성화, 영구 탈퇴 신청·취소 및 물리 삭제로 변경하지 않고 TTL 만료까지 유지합니다.
- 도서 검색 제한과 캐시는 독후감 공개 범위, 댓글, 알림, 푸시 구독 및 소셜 관계를 변경하거나 복원하지 않습니다.
- 주간·월간·연간 인기 도서 집계는 `ACTIVE`, `WITHDRAWN`, `DELETE_PENDING`, `SUSPENDED` 등 회원 상태를 조건으로 사용하지 않습니다.
- 계정 상태가 변경되어도 `TM_REPORT`에 독후감이 남아 있는 동안에는 모든 기간의 인기 도서 작성자 수와 평균 평점에 포함합니다.
- 유예기간이 지나 회원과 `TM_REPORT`가 물리 삭제되면 해당 독후감은 모든 기간 집계에서 영구 제외되며 별도 인기 순위 또는 평균 평점 이력으로 복원하지 않습니다.
- 기간별 인기 도서 조회는 독후감 공개 설정, 댓글, 알림, 푸시 구독 및 소셜 관계를 변경하지 않습니다.

## 실패 및 운영 확인

- 회원 제한 또는 앱 전체 제한에 도달하면 업무 코드 `BOOK_SEARCH_RATE_LIMITED`를 반환합니다.
- 카카오 인증, 통신 또는 응답 계약 오류는 기존 공통 검색 실패 응답으로 처리하고 원시 외부 응답과 API 키를 노출하지 않습니다.
- 운영 Redis에서는 `KEYS` 명령을 사용하지 않고 제한 키의 TTL과 개별 카운터 또는 `SCAN`으로 상태를 확인합니다.
- 쿼터 제한값을 변경하면 `application-prod.yml`, `.env.example`, GitHub Actions Variables와 배포 문서를 함께 갱신합니다.

## 구현 근거

- `src/main/java/org/our/sadari/book/controller/BookController.java`
- `src/main/java/org/our/sadari/book/service/BookSearchService.java`
- `src/main/java/org/our/sadari/book/service/BookSearchProtectionService.java`
- `src/main/java/org/our/sadari/book/service/BookPopularService.java`
- `src/main/java/org/our/sadari/book/mapper/BookMapper.xml`의 `getPopularBookList`
- `src/main/java/org/our/sadari/book/dto/PopularBookDto.java`
- `src/main/java/org/our/sadari/book/dto/BookSearchResponseDto.java`
- `src/main/java/org/our/sadari/book/dto/KakaoBookJsonDto.java`
- `src/main/java/org/our/sadari/global/security/config/SecurityConfig.java`
- `src/main/java/org/our/sadari/global/scheduler/service/UserHardDeleteServiceImpl.java`
- `src/main/frontend/src/features/Book/Search/hook/useSearchBookPage.ts`
- `src/main/frontend/src/pages/Book/Search/SearchBookPage.tsx`
- `src/main/resources/application-prod.yml`
- `BOOK_SEARCH_RATE_LIMITED`
