# Mac mini GitHub Actions 운영 배포

이 문서는 2026-09-16 기준 Mac mini 단일 서버 배포 절차를 설명합니다. MySQL과 Redis,
Spring Boot, 영구 파일 저장소는 같은 Mac mini에서 실행하고 외부 접근은 Tailnet으로 제한합니다.
`master` 브랜치에 push되면 다음 순서로 배포됩니다.

1. Java 17과 Node.js 24 환경에서 WAR 빌드를 검증합니다.
2. AMD64와 ARM64 Docker 이미지를 빌드해 `ghcr.io/<owner>/<repository>`에 커밋 SHA와 `latest` 태그로 올립니다.
3. Mac mini 자체 실행기가 공개 운영 변수와 `mac-production` 비밀값으로 배포 파일을 설치합니다.
4. Mac mini가 새 이미지를 pull하고 기존 MySQL·Redis Docker 네트워크에 애플리케이션을 연결합니다.
5. `http://127.0.0.1:<APP_PORT>/` 응답을 최대 2분 동안 확인하고 실패하면 직전 이미지로 복구합니다.

공개 운영 값과 애플리케이션 비밀값은 GitHub 저장소의 `mac-production` Environment에서 관리합니다.
MySQL과 Redis 비밀번호는 Mac mini의 권한이 제한된 파일에만 저장합니다.

## Actions Secrets

`GITHUB_TOKEN`은 Actions 실행 시 GitHub가 자동 발급하므로 직접 등록하지 않습니다. 이 토큰은
워크플로의 GHCR 이미지 push와 Mac mini의 동일 저장소 이미지 pull에 사용됩니다.

## mac-production Environment Secrets

다음 값은 `Settings > Environments > mac-production > Environment secrets`에서 관리합니다. 배포할 때
자체 실행기가 Mac mini의 `secrets/app.env`와 Firebase 서비스 계정 파일을 소유자 전용 권한으로 갱신합니다.

| 이름 | 용도 |
| --- | --- |
| `JWT_SECRET` | JWT 서명 비밀키 |
| `KAKAO_REST_API_KEY` | Kakao 로그인과 도서 검색 서버 키 |
| `KAKAO_JAVASCRIPT_KEY` | Kakao 브라우저 SDK 키 |
| `KAKAO_NATIVE_APP_KEY` | Kakao 네이티브 앱 키 |
| `GOOGLE_TRANSLATION_API_KEY` | Cloud Translation API 서버 키 |
| `GOOGLE_BOOKS_API_KEY` | Google Books API 서버 키 |
| `FIREBASE_WEB_API_KEY` | Firebase Web App API 키 |
| `FIREBASE_WEB_AUTH_DOMAIN` | Firebase Web App 인증 도메인 |
| `FIREBASE_WEB_PROJECT_ID` | Firebase 프로젝트 식별자 |
| `FIREBASE_WEB_STORAGE_BUCKET` | Firebase Storage 버킷 식별자 |
| `FIREBASE_WEB_MESSAGING_SENDER_ID` | Firebase Messaging 발신자 식별자 |
| `FIREBASE_WEB_APP_ID` | Firebase Web App 식별자 |
| `FIREBASE_VAPID_PUBLIC_KEY` | Firebase Web Push 공개키 |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Admin 서비스 계정 JSON 원문 |

`FIREBASE_CREDENTIALS_PATH`는 워크플로가 컨테이너 내부 경로로 고정합니다. 로컬 파일 저장소를 사용하므로
S3 접근키는 배포 환경에 전달하지 않습니다.

## Mac mini 인프라 환경변수

다음 값은 Actions Secret이 아니라 Mac mini 배포 폴더의 `secrets/infra.env`에 저장합니다. 파일 권한은
소유자 읽기·쓰기만 허용하고 실제 값은 저장소와 배포 로그에 기록하지 않습니다.

| 이름 | 기본값 | 용도 |
| --- | --- | --- |
| `DB_URL` | Docker 내부 MySQL 주소 | 애플리케이션의 MySQL JDBC 연결 주소 |
| `DB_USERNAME` | `sadari_app` | 운영 스키마 전용 MySQL 계정 |
| `DB_PASSWORD` | 필수 | 운영 스키마 전용 MySQL 비밀번호 |
| `REDIS_HOST` | `sadari-redis` | Docker 내부 Redis 서비스 이름 |
| `REDIS_PORT` | `6379` | Docker 내부 Redis 포트 |
| `REDIS_PASSWORD` | 필수 | Redis 기본 사용자 인증 비밀번호 |

## Actions Variables

아래 값은 비밀정보가 아니며 등록하지 않으면 표의 기본값이 사용됩니다.

| 이름 | 기본값 | 용도 |
| --- | --- | --- |
| `FRONT_DOMAIN` | 필수 | Tailnet 또는 Cloudflare에서 접속하는 프론트 HTTPS Origin |
| `BACK_DOMAIN` | 필수 | Tailnet 또는 Cloudflare에서 접속하는 백엔드 HTTPS Origin |
| `DEPLOY_ROOT` | `/Users/<username>/sadari` | 자체 실행기가 배포 파일과 영구 업로드를 관리하는 폴더 |
| `INFRA_NETWORK` | `sadari-mac-infra_default` | MySQL과 Redis가 연결된 외부 Docker 네트워크 |
| `APP_PORT` | `8080` | Tailscale Serve가 전달할 Mac mini 로컬 애플리케이션 포트 |
| `DB_CONNECTION_TIMEOUT` | `60000` | DB 커넥션 획득 제한시간(ms) |
| `DB_MINIMUM_IDLE` | `2` | Hikari 최소 유휴 커넥션 수 |
| `DB_MAXIMUM_POOL_SIZE` | `10` | Hikari 최대 커넥션 수 |
| `HTTP_CONNECT_TIMEOUT_MILLIS` | `3000` | 외부 HTTP 서버 연결 제한시간(ms) |
| `HTTP_READ_TIMEOUT_MILLIS` | `5000` | 외부 HTTP 서버 응답 제한시간(ms) |
| `JWT_ACCESS_TOKEN_SECONDS` | `1800` | Access Token 유효시간(초) |
| `JWT_REFRESH_TOKEN_SECONDS` | `86400` | Refresh Token 유효시간(초) |
| `JWT_REFRESH_ROTATION_GRACE_SECONDS` | `10` | 다중 탭 동시 재발급을 동일 회전 결과로 처리하는 유예시간(초) |
| `WITHDRAWAL_HARD_DELETE_WAIT_DAYS` | `30` | 영구 탈퇴 신청 후 회원 데이터를 물리 삭제하기까지의 유예기간(일) |
| `TIMER_ATTENDANCE_MIN_SECONDS` | `600` | 하루 독서 출석 인정에 필요한 최소 누적 시간(초) |
| `TIMER_MAX_SESSION_SECONDS` | `28800` | 단일 독서 타이머 세션과 목표시간 알림에 적용하는 최대 시간(초) |
| `TIMER_ZONE_ID` | `Asia/Seoul` | 일별 독서 시간과 주간 출석 경계를 계산하는 시간대 |
| `TIMER_DETAIL_RETENTION_DAYS` | `365` | 완료된 독서 타이머 세션 상세 보존기간(일) |
| `BOOK_SEARCH_CACHE_HIT_RATE_LIMIT_PER_MINUTE` | `300` | 회원별 60초 캐시 적중 도서 검색 요청 한도 |
| `BOOK_SEARCH_CACHE_MISS_RATE_LIMIT_PER_MINUTE` | `60` | 회원별 60초 캐시 미적중 도서 검색 요청 한도 |
| `BOOK_SEARCH_RATE_LIMIT_PER_DAY` | `200` | 캐시 미적중 시 차감하는 회원별 24시간 외부 도서 검색 실제 호출 한도 |
| `BOOK_SEARCH_PROVIDER_CALL_LIMIT_PER_DAY` | `27000` | 공급자별 앱 전체 24시간 외부 도서 검색 실제 호출 보호 한도 |
| `BOOK_SEARCH_CACHE_TTL_SECONDS` | `600` | 사용자와 연결하지 않은 도서 검색 결과 Redis 캐시 유효시간(초) |
| `BOOK_SEARCH_POPULAR_KEYWORD_WINDOW_DAYS` | `7` | 인기 검색어 점수 합산과 회원별 동일 검색어 중복 제한 기간(일) |
| `BOOK_SEARCH_POPULAR_KEYWORD_MIN_USER_COUNT` | `3` | 인기 검색어 공용 화면 노출에 필요한 최소 고유 회원 수 |
| `BOOK_SEARCH_POPULAR_KEYWORD_MAX_SIZE` | `10` | 검색 화면에 전달할 인기 검색어 최대 건수 |
| `MULTIPART_MAX_FILE_SIZE` | `10MB` | 단일 업로드 파일 제한 |
| `MULTIPART_MAX_REQUEST_SIZE` | `21MB` | 전체 multipart 요청 제한 |
| `UPLOAD_MAX_IMAGE_BYTES` | `10485760` | 디코딩 전 이미지 최대 바이트 수 |
| `UPLOAD_MAX_IMAGE_PIXELS` | `20000000` | 이미지 최대 전체 픽셀 수 |
| `UPLOAD_MAX_IMAGE_DIMENSION` | `8192` | 이미지 한 변의 최대 픽셀 수 |
| `COOKIE_SECURE` | `true` | HTTPS 쿠키 전용 여부 |
| `COOKIE_SAME_SITE` | `None` | 인증 쿠키 SameSite 정책 |
| `FIREBASE_CREDENTIALS_PATH` | 필수 | Firebase 서비스 계정 자격증명 파일 경로 |
| `SCHEDULER_ENABLED` | `true` | 운영 스케줄러 실행 여부 |
| `SCHEDULER_MAX_SIZE` | `100` | 한 번의 스케줄 실행 최대 처리 건수 |
| `ALIM_DELETE_RETENTION_DAYS` | `30` | 사용자가 삭제한 알림을 물리 삭제하기 전 보존기간(일) |
| `SERVER_PORT` | `8080` | Spring 서버 포트 |
| `LOGGING_LEVEL_ROOT` | `info` | 루트 로그 레벨 |
| `LOGGING_LEVEL_APP` | `info` | 프로젝트 패키지 로그 레벨 |
| `COMPLAINT_RESULT_MAX_SIZE` | `5` | 한 번의 팝업에 표시할 미확인 신고 조치 결과 최대 건수 |

## 프로필 고정 설정

- 운영 애플리케이션은 외부 Docker 네트워크의 `sadari-mysql:3306`과 `sadari-redis:6379`에 직접 연결합니다.
  비밀번호와 Redis 인증값은 Mac mini의 `secrets/infra.env`에서만 주입합니다.
- MySQL과 Redis 포트는 Mac mini의 루프백에만 게시합니다. 개발자 DB 접속과 서비스 화면 접근은
  Tailscale TCP 전달과 Tailscale Serve를 사용하며 공유기 포트포워딩을 사용하지 않습니다.
- 운영 파일 저장소는 컨테이너의 `/app/uploads`이고 Mac mini 배포 폴더의 `uploads`와 연결됩니다.
  `application-prod.yml`이나 GitHub Actions에는 Mac 사용자별 절대 경로를 하드코딩하지 않습니다.
- Tailnet 장치에서 로컬 OAuth를 검증할 때는 `application-loc.yml`의 `domain.front`와
  `domain.back` 기본값을 같은 `https://<tailscale-device>.<tailnet>.ts.net` 주소로 설정하고
  `app.cookie.secure=true`, `app.cookie.same-site=Lax`를 사용합니다.
- `application-loc.yml`의 `domain.local-front`는 localhost 로그인 완료 후 이동할 Vite 주소인
  `http://localhost:5173`으로 고정합니다.
- `loc` 프로필은 `/api/oauth/local-login?userNumb=<test-user-number>` 간편 로그인 URL을 제공합니다.
  활성 회원만 DB 원본 권한으로 로그인시키며 비활성화, 영구 탈퇴 대기 및 이용정지 회원은 상태 변경 없이
  차단합니다. 해당 Controller와 Service는 `loc` 프로필이 활성화되고 운영 프로필은 비활성화된 경우에만
  등록되므로 두 프로필이 잘못 함께 활성화되더라도 운영 환경에는 Endpoint가 생성되지 않습니다.
- Vite 개발 서버는 `application-loc.yml`의 `domain.proxy=http://127.0.0.1:8080`을 읽어 `/api` 요청을
  로컬 Spring 서버로 전달합니다. Vite는 Tailscale Serve 대상과 동일한 `127.0.0.1:5173`에 고정되며,
  포트가 이미 사용 중이면 다른 포트로 이동하지 않고 시작에 실패하여 잘못된 프록시 연결을 차단합니다.
- Vite 개발 프록시는 원래 브라우저 Host를 `X-Forwarded-Host`로 전달합니다. 로컬 로그인 Controller는
  이 값을 허용 주소 선택에만 사용하여 localhost 요청은 `domain.local-front`, 그 외 loc 요청은
  설정된 `domain.front` 호스트와 일치할 때만 Tailnet 주소로 이동시킵니다. 그 외 Host에서는 회원 조회와
  세션 발급을 시작하지 않으며 요청값으로 임의 리다이렉트 주소를 받지 않습니다.
- Vite Host 허용 목록에는 `domain.front`의 Tailnet 호스트만 추가하여 휴대폰 요청을 허용하고 임의 Host
  헤더 요청은 차단합니다.
- Vite의 내부 전달에서는 브라우저의 개발 Origin을 제거하여 POST와 PUT 요청이 외부 CORS 요청으로
  오인되지 않게 하며, Spring은 `domain.back`의 Tailscale 기본값으로 OAuth 콜백 URI를 생성합니다.
- 프런트엔드는 HTTPS를 사용하는 `*.ts.net` 개발 주소에서도 서비스워커를 등록하여 Tailnet 장치의
  PWA 설치와 오프라인 앱 셸 검증을 허용합니다.
- 카카오 개발자 콘솔의 Redirect URI에는
  `https://<tailscale-device>.<tailnet>.ts.net/api/oauth/callback/kakao`를 등록해야 합니다.
- `application-prod.yml`의 기본 `DB_URL`은 Docker 내부 MySQL 주소를 사용하며 `secrets/infra.env`로
  환경별 값을 덮어쓸 수 있습니다.
- 로컬과 운영의 `book.search.url`은 종료된 네이버 도서 API의 대체 공급자인 카카오 도서 검색
  `https://dapi.kakao.com/v3/search/book`으로 고정하며 인증에는 `secrets/app.env`의
  `KAKAO_REST_API_KEY`를 사용합니다.
- Google 번역은 `GOOGLE_TRANSLATION_API_KEY`가 있을 때 활성화하고, 키가 없으면 기존 번역 캐시만 표시하며 신규 번역 버튼은 숨깁니다. 영어 설정의 도서 검색에는 `GOOGLE_BOOKS_API_KEY`가 필요하며 키가 없으면 외부 요청 없이 검색 실패 응답을 반환합니다.
- Google Cloud Console의 `API 및 서비스 > 사용자 인증 정보`에서 `sadari-translation-server` 값은 `GOOGLE_TRANSLATION_API_KEY`, `sadari-books-server` 값은 `GOOGLE_BOOKS_API_KEY`에 각각 등록합니다.
- Google 번역은 Cloud Translation Basic v2 서버 주소를 사용하고 앱 전체 월간 신규 번역을 500,000 유니코드 코드 포인트로 고정합니다. 월간 경계는 Google 쿼터 기준 시간대와 맞추며, Redis에서 사용량을 확인할 수 없으면 신규 Google 호출을 중단합니다.
- 운영 도서 검색은 한국어 Kakao에서 최대 50권, 영어 Google Books에서 최대 40권을 조회하며 캐시 적중 300회·미적중 60회의 회원별 60초 제한, 회원별 일간 제한, 공급자별 앱 전체 실제 호출 제한과 10분 공용 캐시를 Redis에서 관리합니다.
- 도서 인기 검색어는 최근 7일의 일별 Redis 점수를 합산하고 동일 회원의 같은 검색어를 기간 내 한 번만 반영하며 최소 3명 이상인 상위 10건을 제공합니다.
- 운영의 `book.search.popular-keyword-user-dedup-enabled`는 순위 조작 방지를 위해 `true`로 고정하며 환경변수로 노출하지 않습니다.
- 로컬의 `book.search.popular-keyword-user-dedup-enabled`는 한 계정의 반복 검색으로 화면을 검증할 수 있도록 `false`를 사용하고 최소 노출 인원은 `1`로 설정합니다.
- `BOOK_SEARCH_PROVIDER_CALL_LIMIT_PER_DAY` 기본값은 카카오 도서 검색 일일 30,000건 중 3,000건을 장애 대응과 운영 확인용으로 남기는 `27,000`입니다.
- 도서 검색 제한값, 캐시 유효시간과 인기 검색어 집계 기준은 공개 가능한 운영 정책이므로 Actions Variables로 관리합니다. Redis가 검색 제한을 확인할 수 없으면 외부 호출을 중단하고 인기 검색어 집계나 조회만 실패하면 일반 도서 검색은 유지합니다.
- `application-loc.yml`은 운영과 동일하게 `withdrawal.hard-delete-wait-days`를 `30`으로 설정하고
  `withdrawal.hard-delete-test-enabled`를 `false`로 설정합니다.
- `application-loc.yml`은 Git에서 제외되므로 각 개발 환경의 로컬 파일에 위 두 값을 직접 유지해야 합니다.
- Tailnet OAuth 검증용 `application-loc.yml`은 공유 DB의 삭제 위험을 차단하도록
  `scheduler.enabled=false`를 사용합니다.
- 영구 탈퇴 테스트 스케줄러가 필요한 경우에는 격리된 로컬 DB를 연결한 뒤에만 일시적으로
  `scheduler.enabled=true`, `withdrawal.hard-delete-wait-days=0`, `withdrawal.hard-delete-test-enabled=true`를 사용하고 검증 후 기본값으로 복구해야 합니다.
- `application-prod.yml`은 `withdrawal.hard-delete-test-enabled`를 `false`로 고정합니다.
  이 값은 GitHub Actions 환경변수로 노출하지 않으므로 운영 배포에서 로컬 테스트 스케줄러를
  활성화할 수 없습니다.
- 독서 모임의 종료 회차 확정 스케줄은 로컬과 운영에서 매분 실행하도록 고정합니다.
  `scheduler.round-completion-cron`은 서비스 내부 일자 경계 처리를 빠르게 확정하기 위한 값이며
  운영 중 임의 변경 대상이 아니므로 Actions Variable이나 Secret으로 노출하지 않습니다.
- 운영 유예기간은 `영구 탈퇴 데이터 삭제_처리 대기 일수` Actions Variable로 조정할 수 있으며,
  등록하지 않으면 30일을 사용합니다.
- 사용자 앱과 로컬 전용 관리자 앱의 `application-loc.yml`에 있는 `complaint.auto-action` 임계치는 기능 검증을 위해 독후감, 댓글, 프로필 사진, 배경사진 및 한줄소개 모두 `1`건으로 고정합니다.
- 운영 배포하는 사용자 앱의 `application-prod.yml`에는 같은 다섯 대상의 임계치를 모두 `5`건으로 고정합니다.
  이 값은 운영 중 임의 변경으로 조치 기준이 달라지지 않도록 Actions Variable이나 Secret으로 노출하지 않습니다.
- 로컬과 운영의 `complaint.evidence.retention-days`는 `180`, `cleanup-batch-size`는 `100`,
  증거 정리 스케줄은 매일 `04:20`으로 고정합니다. 미처리 신고와 연결된 증거는 보존하고,
  연결된 신고가 모두 종결된 뒤 최근 처리일로부터 180일이 지난 증거만 물리 삭제합니다.
  이 값들은 신고 감사 정책의 일부이므로 Actions Variable이나 Secret으로 노출하지 않습니다.
- 자동 조치 및 증거 보관 기능을 배포하기 전에 `scripts/db/mysql/01-create.sql`의
  신고 이력, 자동 조치 이력, 관리자 전용 이미지 증거 테이블과
  `승인된 비공개 기준정보 패키지`의 신고 조치 결과, 신고 처리 결과, 신고 대상 유형 공통코드를 먼저 반영합니다.

## Mac mini 사전 조건

- Docker Desktop과 Docker Compose v2가 실행 중이어야 합니다.
- MySQL과 Redis 인프라 Compose 프로젝트가 먼저 실행되고 MySQL 상태가 `healthy`여야 합니다.
- GitHub Actions 자체 실행기를 `self-hosted`, `macOS`, `ARM64`, `sadari-prod` 라벨로 등록해야 합니다.
- 공개 저장소의 Pull Request는 GitHub 제공 실행기에서만 검증합니다. 자체 실행기 배포 작업은 기본 브랜치
  push와 `master`를 선택한 저장소 권한 보유자의 수동 실행에만 반응하도록 유지하고 외부 기여자의 워크플로 실행 승인을
  저장소 설정에서 요구합니다.
- Mac mini의 시스템 잠자기는 비활성화하고 정전 후 자동 재시작을 활성화합니다.
- Tailscale은 무인 실행 상태를 유지하고 HTTPS 서비스 주소는 Tailnet 구성원만 접근하도록 설정합니다.
- PWA와 Secure Cookie, Firebase Web Push는 Tailscale Serve가 제공하는 HTTPS 주소를 사용합니다.

## 독서 타이머 8시간 및 목표 알림 배포

- 애플리케이션 배포 전에 `scripts/db/mysql/01-create.sql`의 중요도 순서대로 독서 타이머 세션을 재구성해야 합니다. 기존 테이블 끝에 컬럼을 단순 추가하지 않습니다.
- 유지보수 창에서 애플리케이션을 중지하고 DB 스냅샷을 만든 뒤, 교체 테이블을 기준 DDL로 생성해 기존 10개 컬럼을 명시적으로 복사합니다. 신규 알림 목표 독서 시간 초, 목표시간 알림 예정 일시, 알림 발송 일시는 기존 세션에 `NULL`로 둡니다.
- 원본과 교체 테이블의 전체 행 수, 사용자별 활성 세션 수, 확정 독서 시간 초 합계, FK 및 인덱스를 대조한 뒤 원자적 이름 교환으로 전환합니다. 검증 전 원본 테이블을 삭제하지 않습니다.
- 재구성된 테이블에는 업무 조회 최적화 인덱스 (독서 타이머 상태 코드, 알림 발송 일시, 목표시간 알림 예정 일시, 독서 타이머 세션 번호)`가 있어야 합니다.
- `승인된 비공개 기준정보 패키지`를 적용해 독서 타이머 기간 초과 스케줄러 분류값과 알림 템플릿을 등록합니다. 기존 동일 코드의 관리자 문구와 사용 여부는 덮어쓰지 않습니다.
- GitHub Actions Variable `TIMER_MAX_SESSION_SECONDS`를 별도로 등록했다면 `28800`으로 변경합니다. 기존 `14400` 값이 남아 있으면 화면과 서버가 8시간 설정을 거부합니다.

## 기기별 인증 세션 전환

`sid` 기반 기기별 세션을 처음 배포할 때는 기존 사용자별 단일 Refresh Token 키와 새 세션 키가 일시적으로 함께 존재할 수 있습니다.

1. 모든 애플리케이션 인스턴스를 새 버전으로 교체하고 구버전 인스턴스가 요청을 처리하지 않는지 확인합니다.
2. `sid`가 없는 기존 JWT는 새 버전에서 인증할 수 없으므로 사용자가 한 번 다시 로그인할 수 있음을 배포 공지와 점검 항목에 포함합니다.
3. Redis에서 `SCAN`을 사용해 구형 `auth:refresh:*` 키의 존재와 TTL을 확인합니다. 운영 Redis에서 전체 키를 한 번에 조회하는 `KEYS` 명령은 사용하지 않습니다.
4. 구형 키의 TTL이 남아 있으면 자연 만료를 기다릴 수 있습니다. 즉시 정리해야 하면 구버전 인스턴스 종료를 확인한 뒤 `UNLINK`를 사용해 비동기로 삭제합니다.
5. 새 로그인 후 `auth:session:{sid}`, `auth:user:sessions:{userNumb}`, `auth:user:nick:{userNumb}`와 `auth:user:status:{userNumb}`가 생성되는지 확인합니다.
6. 현재 디바이스 로그아웃은 현재 `auth:session:{sid}`만 제거하고, 전체 디바이스 로그아웃은 회원별 Set에 연결된 모든 세션을 제거하는지 확인합니다.

현재 버전은 구형 `auth:refresh:{userNumb}`를 읽거나 생성하지 않으며 새 로그아웃 처리에서도 해당 키를 삭제하지 않습니다. 구버전과 새 버전을 동시에 운영하는 동안 구형 키를 먼저 삭제하면 구버전 사용자의 재발급이 실패하므로 배포 순서를 지켜야 합니다.

## 최초 설정 순서

1. Mac mini에 저장소 전용 자체 실행기를 설치하고 `sadari-prod` 라벨을 추가합니다.
2. `mac-production` Environment에 애플리케이션 비밀값과 Tailnet HTTPS Origin을 등록합니다.
3. `secrets/infra.env`에 MySQL과 Redis 연결값을 기록합니다.
4. Firebase 서비스 계정 파일을 `secrets/firebase-service-account.json`에 저장합니다.
5. 세 비밀 파일의 권한을 소유자 읽기·쓰기만 허용합니다.
6. 필요한 공개 Actions Variables를 등록합니다.
7. Tailscale Serve에서 HTTPS 요청을 `127.0.0.1:8080`으로 전달합니다.
8. `master` 브랜치에 push하거나 Actions 화면에서 `Sadari CI/CD`를 수동 실행합니다.

현재 `SadariApplicationTests`는 Git에서 제외된 로컬 설정과 실제 DB/Redis를 요구하므로 CI에서
자동 실행하지 않습니다. 추후 Testcontainers나 독립 `application-test.yml`을 추가하면 워크플로의
`-x test`를 제거해 통합 테스트까지 배포 차단 조건으로 사용할 수 있습니다.

## 로컬 파일 저장소 설정

운영 환경의 영구 이미지는 배포 서버의 일반 디렉터리에 저장합니다. 애플리케이션은 서버 운영체제와 관계없이 컨테이너 내부의 `/app/uploads`를 사용하고, Docker Compose가 서버의 실제 디렉터리를 이 경로에 연결합니다. 브라우저에서 사용하는 `/uploads/{type}/{yyMMdd}/{uuid}.{ext}` 주소와 데이터베이스의 상대 객체 키는 변경하지 않습니다.

Mac mini의 기본 호스트 경로는 배포 폴더 아래의 `uploads`입니다. 컨테이너에는 `/app/uploads`로
연결되며 배포 이미지 교체나 컨테이너 재생성 후에도 파일이 유지됩니다.

Actions Variables에는 다음 값을 등록합니다.

| 이름 | 기본값 | 용도 |
| --- | --- | --- |
| `STORAGE_PROVIDER` | `local` | 운영 파일 저장소 구현 |
| `STORAGE_LOCAL_ROOT` | `/app/uploads` | 애플리케이션 컨테이너 내부의 공통 저장 경로 |
| `STORAGE_HOST_ROOT` | `./uploads` | Docker Compose 파일을 기준으로 한 서버의 실제 저장 경로 |
| `STORAGE_S3_BUCKET` | 없음 | S3를 선택할 때 사용하는 비공개 버킷 이름 |
| `STORAGE_S3_REGION` | `ap-northeast-2` | S3 버킷 리전 |
| `STORAGE_S3_ENDPOINT` | 빈 값 | S3 호환 저장소를 선택할 때 사용하는 API 주소 |
| `STORAGE_S3_PATH_STYLE_ACCESS` | `false` | S3 호환 저장소의 경로형 접근 사용 여부 |

워크플로는 Mac mini 배포에서 `STORAGE_PROVIDER=local`, `STORAGE_LOCAL_ROOT=/app/uploads`를 고정합니다.
`STORAGE_HOST_ROOT` 기본값 `./uploads`는 배포 폴더 아래의 영구 디렉터리로 해석됩니다.

Windows에서 실행 중인 애플리케이션은 macOS의 `/Users/...` 경로를 직접 사용할 수 없습니다. Windows 로컬 실행은 Windows에서 접근 가능한 경로를 사용하고, Mac mini의 실제 디스크를 사용하려면 애플리케이션 컨테이너도 Mac mini에서 실행합니다.

사용자 애플리케이션과 관리자 애플리케이션을 같은 서버에서 실행하면 두 컨테이너가 동일한 `STORAGE_HOST_ROOT`를 연결해야 합니다. 서로 다른 장비의 로컬 디스크는 같은 경로 문자열만으로 파일을 공유하지 못합니다.

EC2에서 Mac mini로 전환할 때는 먼저 파일 쓰기를 중지하고 `~/sadari/uploads`의 내용을 Mac mini 업로드 디렉터리로 복사합니다. 파일 수와 전체 크기를 대조한 뒤 Mac mini 애플리케이션을 시작하고 기존 이미지 조회와 신규 업로드를 확인합니다. 검증이 끝나기 전에는 EC2 원본 폴더를 삭제하지 않습니다.

Garage나 S3 호환 저장소의 내부 데이터 디렉터리는 일반 파일 구조가 아니므로 직접 복사하지 않습니다. 기존 객체는 저장소 API를 통해 상대 객체 키를 보존하며 내보낸 뒤 파일 수와 크기를 검증합니다. 프로필 편집 중 생성되는 30분 임시 이미지는 공개 경로와 분리된 컨테이너 임시 디렉터리에 계속 저장하며 재배포 시 소실될 수 있습니다.

Mac mini 자동 배포는 로컬 저장소만 사용합니다. S3 호환 구현은 소스에 남아 있지만 다시 선택하려면
워크플로와 비밀값 전달 정책을 별도로 검토한 뒤 배포해야 합니다.

### 채팅 열람 상태와 알림 읽음 처리

채팅 열람 유효 시간은 로컬과 운영 설정의 `reading-club.chat-view-ttl-seconds`에서 초 단위로 관리하며 기본값은 15입니다. 화면은 3초마다 열람 상태를 갱신하므로 유효 시간은 갱신 주기보다 충분히 길게 유지합니다. Redis를 공유하는 서버들은 동일한 시각 기준과 설정을 사용해야 합니다. Redis 장애 시 알림 생략을 중단하고 일반 수신 조건으로 발송합니다.

배포 전 [초기 테이블 정의](../scripts/db/mysql/01-create.sql)의 알림 원본 채팅 번호 필드를 기존 데이터베이스에 먼저 반영합니다. 기존 알림은 원본 연결이 비어 있어도 조회할 수 있고, 해당 채팅방의 최신 메시지까지 읽으면 함께 읽음 처리합니다. 앱을 이전 버전으로 되돌려도 추가 필드는 유지할 수 있습니다.
