# 사용자 서비스 운영 로그

작성자: SeungHyeon.Kang

기준일은 2026-09-19입니다. 사용자 서비스의 API, 인증 거절, 외부 HTTP 연동, 푸시, 비동기 알림 및 정기 작업을 대상으로 합니다. 이 문서는 소스에 반영한 관측 범위와 배포 후 확인 절차를 설명합니다. 운영 서버 적용 여부와 실제 로그 수집 상태는 별도로 확인해야 합니다.

## 적용 내용

| 영역 | 이벤트 및 확인 항목 |
| --- | --- |
| 전체 `/api` 요청 | `http_request`: HTTP 메서드, 서버 매핑 패턴, HTTP 상태, 업무 결과 코드, 소요 시간, 지연 여부 |
| 인증·권한 거절 | HTTP 401·403과 가능한 경우 서버가 지정한 `reason`; MVC 이전 거절의 경로는 `unmapped` |
| OAuth·탈퇴 콜백 | 리다이렉트 HTTP 302와 별도로 업무 결과 코드 기록; 로그인 상태값 검증 실패 원인도 기록 |
| DB 예외 변환 | `database_failure`: 원인 예외 유형과 코드 위치; 응답으로 변환되는 DB 오류도 기록 |
| 공통 외부 HTTP 호출 | `outbound_http`: 공급자 호스트, HTTP 상태, 응답 헤더 수신까지 걸린 시간; OAuth·도서 검색·번역·프로필 다운로드 포함 |
| Firebase 푸시 | `push_send`: 성공 또는 토큰 부재·공급자 미초기화에 따른 생략; 발송 예외는 별도 경고 |
| 비동기 좋아요 알림 | `like_notification`: 입력 누락·발신자 정보 부재·업무 거절·완료; 완료는 정책에 따른 정상 생략을 포함 |
| 전체 스케줄 진입점 | `scheduler_started`, `scheduler_returned`, `scheduler_failed`: 작업명, 소요 시간, 실행 식별자 |
| 기존 스케줄러 집계 | `scheduler_summary`, `scheduler_item_failed`: 대상·성공·실패 건수와 개별 실패 진단; DB 로그 저장 실패와 독립적으로 콘솔에 기록 |
| Redis·파일·번역 등 기존 오류 | 원시 예외 대신 제한된 원인 유형과 코드 위치를 기록하도록 보강 |

로그 레벨은 정상 API 완료 INFO, 업무 거절·클라이언트 오류·지연 WARN, 서버 장애 ERROR입니다. 스케줄러 시작과 고빈도 타이머 호출 완료는 DEBUG입니다. 실제 처리 대상이 있는 기존 스케줄러의 집계는 INFO 또는 WARN입니다. `scheduler_returned`는 호출이 반환되었다는 뜻이며 내부에서 격리된 부분 실패까지 성공했다는 뜻은 아닙니다.

HTTP 200이라도 `resultCode`가 200이 아니면 업무 실패입니다. 바이너리 응답과 보안 필터에서 끝난 요청은 공통 본문이 없으므로 `resultCode`가 비어 있을 수 있습니다. 예외가 응답 완료 전에 전파되면 기록된 HTTP 상태와 별개로 `failure`를 확인해야 합니다.

## 사전 조건과 설정

운영 이미지를 새로 빌드하여 배포해야 코드 변경이 반영됩니다. Docker 사용 권한과 배포 디렉터리 접근 권한이 필요합니다. 일반 환경변수는 [배포 설정 문서](../github-actions-deployment.md)의 GitHub Actions Variables에 등록합니다. 이 작업에서 새 Secret은 추가하지 않습니다.

| 설정 | 기본값 | 용도 |
| --- | --- | --- |
| `LOGGING_LEVEL_ROOT` | `info` | 운영 루트 로그 레벨 |
| `LOGGING_LEVEL_APP` | `info` | 사용자 서비스 로그 레벨 |
| `APP_LOG_SLOW_REQUEST_MILLIS` | `1000` | API 경고 기준 밀리초; 0 이하 입력은 1ms로 보정 |

지연 기준은 loc와 prod 모두 적용합니다. 두 프로필의 로그 레벨 패턴에는 `requestId`와 `jobId`가 들어갑니다. 이 패턴은 요청과 주기 작업을 연결하기 위한 고정 설정입니다. loc는 기존 앱 DEBUG 설정을 유지합니다. 운영에서 앱 전체를 DEBUG로 올리면 MyBatis 등 기존 상세 로그가 함께 출력될 수 있으므로 필요한 로거만 일시 조정하고 종료 후 복원합니다.

GitHub Actions는 새 지연 기준을 배포 환경 파일에 전달하며 Docker Compose는 기존 `env_file`로 읽습니다. 별도의 로그 파일이나 외부 수집기는 추가하지 않았습니다. 기존 Docker `json-file`의 파일당 20MB, 최대 5개 순환 설정을 사용합니다. 이는 시간 기준 보존을 보장하지 않으며 트래픽 증가에 따라 조회 가능한 기간이 줄어들 수 있습니다.

## 조회와 확인 순서

1. 기존 배포 절차로 새 이미지를 적용하고 `/api/oauth/csrf`의 HTTP 200 응답을 확인합니다. 이 확인은 웹·보안 경로의 기동 확인이며 DB와 외부 연동 전체의 정상 상태를 보장하지 않습니다.
2. 정상 API 응답의 `X-Request-ID`를 확보하고 같은 `requestId`의 완료·외부 연동·오류 로그를 조회합니다. 식별자는 서버가 발급하며 클라이언트 입력값을 재사용하지 않습니다.
3. WARN·ERROR의 HTTP 상태, 업무 코드, `reason`, `failure`를 확인합니다. 느린 요청은 `slow=true`와 `durationMs`로 찾습니다.
4. 정기 작업은 같은 `jobId`의 호출 완료, 실패, 집계 로그를 함께 확인합니다. 로그 DB에 저장하지 못한 경우에도 콘솔 실패를 확인합니다.

배포 디렉터리에서 Docker 권한으로 다음 명령을 실행합니다. 예시 식별자는 실제 값으로 바꾸되 조회 결과를 외부에 공유하기 전에 민감정보를 확인합니다.

```bash
docker compose logs --since=30m --tail=500 app
docker compose logs --since=30m app | grep 'requestId=<request-id>'
docker compose logs --since=30m app | grep 'slow=true'
docker compose logs --since=30m app | grep 'jobId=<job-id>'
```

응답 헤더는 브라우저 개발자 도구에서 확인할 수 있습니다. 프론트엔드 화면에 식별자를 표시하는 기능은 추가하지 않았습니다. 비동기 좋아요 이벤트는 생성 시점의 요청 ID를 작업 스레드에 전달하고 종료 시 이전 문맥을 복원합니다. HTTP 요청 밖에서 생성한 이벤트에는 요청 ID가 없을 수 있습니다.

## 보안과 진단 한계

새 공통 로그는 사용자 식별값, IP, 요청·응답 본문, 쿼리, 인증 헤더, Cookie, 토큰을 수집하지 않습니다. 서버 매핑 전 거절·알 수 없는 경로는 원문 대신 `unmapped`로 기록합니다. API 입력을 별도 저장하거나 계정 수명주기별 보존 범위를 확장하지 않습니다.

애플리케이션의 기존 예외 출력은 원인별 최대 12개 코드 위치, 최대 8개 원인으로 제한합니다. 원시 예외 메시지와 suppressed 예외는 제외합니다. 따라서 공급자의 상세 오류 본문이나 SQL 원문은 확인할 수 없습니다. 필요한 경우 코드 위치와 상태를 근거로 재현합니다. 프레임워크·서블릿 컨테이너·외부 라이브러리 자체 로그는 별도이므로 이 변경만으로 모든 출력의 민감정보 제외를 보장하지 않습니다.

현재 API는 동기 MVC 처리입니다. 추후 비동기 서블릿·스트리밍 API를 추가하면 최종 완료 시점 로깅을 별도로 검토해야 합니다. 외부 HTTP 시간은 응답 본문 전체 소비 시간이 아닙니다. SDK 기반 스토리지 성공 호출과 표지 분석 내부 단계는 API 완료 로그로 관측하며 개별 SDK 호출의 모든 단계를 기록하지 않습니다.

## 실패 시 복구

1. 배포 후 기동 또는 주요 API에 문제가 있으면 기존 절차로 직전 정상 이미지를 다시 배포합니다. 데이터 마이그레이션은 없습니다.
2. 지연 경고가 과도하면 실제 응답 시간을 확인한 뒤 `APP_LOG_SLOW_REQUEST_MILLIS`를 조정하고 컨테이너를 재생성합니다.
3. 로그량 때문에 장애 확인이 어려우면 필요한 로거의 레벨을 제한적으로 조정합니다. 앱 전체를 ERROR로 낮추면 정상 요청과 업무 거절 관측이 사라지므로 정상 상태를 확인한 뒤 INFO로 복구합니다.

## 구현 근거와 검증 범위

- API 관측: `src/main/java/org/our/sadari/global/common/logging/RequestLogFilter.java`, `ResultLogAdvice.java`
- 외부 호출 관측과 안전한 진단: 같은 디렉터리의 `OutboundLogInterceptor.java`, `LogSafe.java` 및 `global/common/config/RestTemplateConfig.java`
- DB 예외와 인증 거절: `global/common/exception/CommonExceptionHandler.java`, `global/security/config/SecurityConfig.java`, `global/security/jwt/JwtFilter.java`
- 정기 작업 관측: `global/scheduler/Scheduler.java`, `global/scheduler/common/SchedulerLogSupport.java`
- 비동기 알림과 푸시: `alim/event/LikeAlimWorker.java`, `push/service/FirebaseMessagingProvider.java`
- 자동 검증: `src/test/java/org/our/sadari/global/common/logging`의 MVC 응답·보안 거절·예외 전파·문맥 정리·본문 비노출·외부 응답 보존 테스트

2026-09-19 전체 검사에서 백엔드 384개 테스트, 프론트엔드 lint·TypeScript·빌드가 통과했습니다. 전체 규칙 검사는 작업 전부터 변경된 `src/main/resources/static/index.html`의 줄바꿈·공백 오류로 실패했습니다. 해당 기존 변경은 그대로 보존했으며 이번 작업 파일의 공백 검사는 통과했습니다.

운영 트래픽에서의 로그 발생량, 저장기간, 실제 외부 공급자 지연은 측정하지 않았습니다. 관측용 내부 변경은 자동 테스트로 검증했으며 Tailnet 실제 화면 확인과 운영 배포는 수행하지 않았습니다.
