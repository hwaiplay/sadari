import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { runInNewContext } from "node:vm";
import { ModuleKind, transpileModule } from "typescript";

class MockResultDataError extends Error {
  result: { code: number; message?: string };

  /**
   * 인증 API 실패 응답을 실제 공통 오류와 같은 형태로 구성함
   *
   * @author SeungHyeon.Kang
   * @param result 인증 실패 코드와 메시지
   */
  constructor(result: { code: number; message?: string }) {
    super(result.message);
    this.result = result;
  }
}

type AuthQueryOptions = {
  queryFn: () => Promise<unknown>;
  retry?: boolean;
  retryOnMount?: boolean;
  refetchOnMount?: boolean;
};

/**
 * 실제 인증 Hook의 API 대역으로 제한 계정 설정 조회 차단과 활성 계정 언어 조회 검증
 *
 * @author HanWon.Jang
 * @return 인증 결과와 API 호출 검증 완료 Promise
 * @throws 인증 상태 또는 설정 호출 횟수 불일치 시 발생
 */
const checkRestrictedAuth = async () => {
  // Hook 소스를 읽어 실제 구현을 외부 API 없이 실행할 CommonJS로 변환
  const source = readFileSync(new URL("./useAuthQuery.tsx", import.meta.url), "utf8");
  // 이미 설치된 TypeScript로 Hook 모듈 변환
  const compiled = transpileModule(source, { compilerOptions: { module: ModuleKind.CommonJS } });
  // 제한 계정과 정상 계정 모두 동일한 인증 경로 검증
  for (const userStat of ["DELETE_PENDING", "SUSPENDED", "WITHDRAWN", "ACTIVE"]) {
    const authState = { code: 200, data: { userStat, onbdYsno: "Y" } };
    let settingCalls = 0;
    let localeCalls = 0;
    const hookExports: { useAuthQuery?: () => AuthQueryOptions } = {};
    // 실제 Hook이 참조하는 모듈에 한정한 API 대역
    const dependencies = {
      "@tanstack/react-query": { useQuery: (options: unknown) => options },
      "@/app/api/resultData": { ResultDataError: MockResultDataError },
      "../api/authApi": {
        checkAuthApi: async () => authState,
        // 정상 인증 뒤 불필요한 재발급 시도 검증
        refreshTokenApi: async () => assert.fail("unexpected refresh"),
      },
      "@/features/User/api/userApi": {
        // 제한 계정에서 실제 서버와 동일하게 설정 조회 거절
        getUserSettingApi: async () => {
          settingCalls++;
          // 제한 상태의 설정 조회는 인증 실패로 이어지면 안 되는 경로
          if (userStat !== "ACTIVE") {
            throw new Error("Forbidden");
          }

          // 활성 계정의 언어 설정 반환
          return { englishYsno: "N" };
        },
      },
      "@/app/messages/message": {
        // 활성 회원의 언어 동기화 호출 수 기록
        setMessageLocale: () => { localeCalls++; },
      },
    };
    // 실제 컴파일된 Hook의 모듈 로딩을 API 대역으로 연결
    const requireDependency = (id: keyof typeof dependencies) => dependencies[id];
    // 실제 Hook 소스를 별도 컨텍스트에서 실행
    runInNewContext(compiled.outputText, { exports: hookExports, require: requireDependency });
    // 실제 Query 인증 함수 호출
    const result = await hookExports.useAuthQuery!().queryFn();
    // 제한 상태도 서버 인증 성공 결과를 그대로 유지하는지 검증
    assert.equal(result, authState);
    // 활성 상태 외에는 사용자 설정 API를 호출하지 않는지 검증
    assert.equal(settingCalls, Number(userStat === "ACTIVE"));
    // 제한 상태에서는 브라우저 언어를 변경하지 않는지 검증
    assert.equal(localeCalls, Number(userStat === "ACTIVE"));
  }
};

// 탈퇴 후 재로그인과 정지 상태 로그인 회귀 검증 등록
test("제한 계정 인증은 설정 API 없이 성공하고 활성 계정만 언어를 조회", checkRestrictedAuth);

/**
 * 만료된 Refresh Token을 로그아웃 상태로 확정하고 Route 재마운트 재시도를 차단하는지 검증
 *
 * @author SeungHyeon.Kang
 * @return 인증 복구 종료와 Query 설정 검증 완료 Promise
 */
const checkExpiredSession = async () => {
  // Hook 소스를 실제 운영 코드와 같은 형태로 실행할 CommonJS로 변환함
  const source = readFileSync(new URL("./useAuthQuery.tsx", import.meta.url), "utf8");
  const compiled = transpileModule(source, { compilerOptions: { module: ModuleKind.CommonJS } });
  const expiredResult = { code: 1003, message: "expired" };
  let tokenCheckCalls = 0;
  let refreshCalls = 0;
  const hookExports: { useAuthQuery?: () => AuthQueryOptions } = {};
  const dependencies = {
    "@tanstack/react-query": { useQuery: (options: AuthQueryOptions) => options },
    "@/app/api/resultData": { ResultDataError: MockResultDataError },
    "../api/authApi": {
      checkAuthApi: async () => {
        tokenCheckCalls++;
        throw new MockResultDataError({ code: 1001 });
      },
      refreshTokenApi: async () => {
        refreshCalls++;
        throw new MockResultDataError(expiredResult);
      },
    },
    "@/features/User/api/userApi": {
      getUserSettingApi: async () => assert.fail("unexpected setting request"),
    },
    "@/app/messages/message": {
      setMessageLocale: () => assert.fail("unexpected locale update"),
    },
  };
  const requireDependency = (id: keyof typeof dependencies) => dependencies[id];

  // 실제 Hook을 실행해 만료 세션을 추가 인증 조회 없이 종료하는지 확인함
  runInNewContext(compiled.outputText, { exports: hookExports, require: requireDependency });
  const query = hookExports.useAuthQuery!();
  const result = await query.queryFn();

  // Refresh 실패 결과가 오류 상태 대신 확정된 비로그인 응답으로 반환되어야 함
  assert.deepEqual(result, expiredResult);
  assert.equal(tokenCheckCalls, 1);
  assert.equal(refreshCalls, 1);
  // Route 구독자가 다시 붙어도 실패 Query와 오래된 인증 상태를 자동 조회하지 않아야 함
  assert.equal(query.retry, false);
  assert.equal(query.retryOnMount, false);
  assert.equal(query.refetchOnMount, false);
};

test("만료 세션은 한 번만 복구하고 Route 재마운트에서 다시 조회하지 않음", checkExpiredSession);
