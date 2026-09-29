import http from 'k6/http';
import { fail } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const baseUrl = (__ENV.BASE_URL || '').replace(/\/+$/, '');
const testUserNumb = Number(__ENV.TEST_USER_NUMB);
const measuredVus = Number(__ENV.VUS || 1);

if (!/^http:\/\/localhost(:\d+)?$/.test(baseUrl)) {
  throw new Error('BASE_URL must point to localhost.');
}

if (!Number.isInteger(testUserNumb) || testUserNumb < 3) {
  throw new Error('TEST_USER_NUMB must be an integer greater than or equal to 3.');
}

if (!Number.isInteger(measuredVus) || measuredVus < 1 || measuredVus > 100) {
  throw new Error('VUS must be an integer from 1 to 100.');
}

const summaryDuration = new Trend('summary_duration', true);
const summaryErrors = new Rate('summary_errors');
let accessToken = '';

export const options = {
  scenarios: {
    measured: {
      executor: 'shared-iterations',
      exec: 'measured',
      vus: measuredVus,
      iterations: 100,
      maxDuration: '5m',
    },
  },
  thresholds: {
    summary_errors: ['rate==0'],
  },
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'min', 'max'],
};

/**
 * 현재 가상 사용자의 로컬 인증 Access Token을 한 번만 발급받는다.
 *
 * @param {boolean} recordError 인증 실패를 본 측정 오류에 포함할지 여부
 * @returns {string} Cookie 요청 헤더 값
 */
const getAccessTokenCookie = (recordError) => {
  if (accessToken) {
    return `accessToken=${accessToken}`;
  }

  const response = http.get(
    `${baseUrl}/api/oauth/local-login?userNumb=${testUserNumb}`,
    { redirects: 0, tags: { name: 'local-login' } },
  );
  const responseToken = response.cookies.accessToken && response.cookies.accessToken[0];
  const tokenValue = responseToken && responseToken.value;

  if (response.status !== 302 || !tokenValue) {
    if (recordError) {
      summaryErrors.add(true);
    }
    fail(`Local login failed with status ${response.status}.`);
  }

  accessToken = tokenValue;

  return `accessToken=${accessToken}`;
};

/**
 * 인증·Redis 세션 검증을 포함해 월별 독서 요약 API를 호출한다.
 *
 * @param {boolean} recordMetrics 본 측정 지표를 기록할지 여부
 * @returns {void}
 */
const requestSummary = (recordMetrics) => {
  const response = http.get(`${baseUrl}/api/user/monthly-reading-summary`, {
    headers: { Cookie: getAccessTokenCookie(recordMetrics) },
    tags: { name: 'monthly-reading-summary' },
  });
  const succeeded = response.status === 200;

  if (recordMetrics) {
    summaryDuration.add(response.timings.duration);
    summaryErrors.add(!succeeded);
  }
};

/**
 * 서버와 데이터베이스를 10회 예열한다.
 *
 * @returns {void}
 */
export const setup = () => {
  for (let index = 0; index < 10; index += 1) {
    requestSummary(false);
  }
};

/**
 * 지정한 동시 사용자 수로 총 100회 측정한다.
 *
 * @returns {void}
 */
export const measured = () => {
  requestSummary(true);
};
