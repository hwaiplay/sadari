import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { runInNewContext } from "node:vm";
import { ModuleKind, transpileModule } from "typescript";

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
    const hookExports: { useAuthQuery?: () => { queryFn: () => Promise<unknown> } } = {};
    // 실제 Hook이 참조하는 모듈에 한정한 API 대역
    const dependencies = {
      "@tanstack/react-query": { useQuery: (options: unknown) => options },
      "@/app/api/resultData": { ResultDataError: Error },
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
