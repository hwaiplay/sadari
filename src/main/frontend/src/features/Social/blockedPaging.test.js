import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { runInNewContext } from "node:vm";
import { JsxEmit, ModuleKind, transpileModule } from "typescript";

/**
 * 실제 차단 페이지의 추가 조회 로딩과 중복 요청 차단 검증
 * @author HanWon.Jang
 * @return 조회 시작과 완료 상태 검증 Promise
 */
async function checkBlockedPaging() {
  // 실제 페이지를 설치된 TypeScript로 변환하여 조회 경로 실행
  const source = readFileSync(new URL("../../pages/Settings/BlockedUsersPage.tsx", import.meta.url), "utf8");
  // JSX를 단순 객체로 반환하는 런타임과 연결
  const compiled = transpileModule(source, {
    compilerOptions: { module: ModuleKind.CommonJS, jsx: JsxEmit.ReactJSX },
  });
  const states = [];
  const refs = [];
  let stateIndex = 0;
  let refIndex = 0;
  let needsRender = false;
  let loadPage;
  let complete;
  const requestedPages = [];
  const exports = {};
  /**
   * 페이지 상태를 다음 렌더링까지 보관
   * @author HanWon.Jang
   * @param initial 최초 상태
   * @return 현재 상태와 갱신 함수
   */
  function useState(initial) {
    const index = stateIndex++;
    states[index] ??= initial;
    // 실제 페이지의 함수형 상태 갱신도 동일하게 반영
    return [states[index], (value) => {
      const next = typeof value === "function" ? value(states[index]) : value;
      needsRender ||= !Object.is(states[index], next);
      states[index] = next;
    }];
  }
  /**
   * 페이지에서 참조하는 모듈만 조회 대역과 연결
   * @author HanWon.Jang
   * @param id 모듈 경로
   * @return 페이지 실행용 모듈
   */
  function requireModule(id) {
    // React 상태와 요청 잠금을 렌더링 간 유지
    if (id === "react") return {
      useState,
      useRef: (initial) => refs[refIndex++] ??= { current: initial },
      useEffect: () => {},
      useCallback: (callback) => { loadPage = callback; return callback; },
    };
    // JSX 출력에서 하단 감지기에 전달된 실제 속성 확인
    if (id === "react/jsx-runtime") return { jsx: (type, props) => ({ type, props }), jsxs: (type, props) => ({ type, props }) };
    // 요청 완료를 수동 제어하여 진행 중 렌더링과 중복 호출 검증
    if (id.endsWith("socialApi")) return {
      getBlockUserPageApi: (page) => {
        requestedPages.push(page);
        return new Promise((resolve) => { complete = resolve; });
      },
    };
    // 사용자 문구와 화면 구성은 조회 검증에서 외부 의존성 제외
    return { default: id, message: (key) => key };
  }
  // 실제 페이지 모듈을 조회 대역에 연결
  runInNewContext(compiled.outputText, { exports, require: requireModule });
  /**
   * 현재 상태로 실제 페이지를 다시 렌더링
   * @author HanWon.Jang
   * @return 페이지 JSX 객체
   */
  function renderPage() {
    stateIndex = 0;
    refIndex = 0;
    needsRender = false;
    return exports.default();
  }
  // 첫 페이지 요청은 한 번만 실행
  const initialPage = renderPage();
  const first = loadPage(1);
  await loadPage(1);
  assert.deepEqual(requestedPages, [1]);
  complete({ list: [], page: 1, hasNext: true });
  await first;
  const loadedPage = needsRender ? renderPage() : initialPage;
  // 추가 조회 중 상태가 감지기 속성으로 전달되는지 검증
  const next = loadPage(2);
  const pendingPage = needsRender ? renderPage() : loadedPage;
  const pending = pendingPage.props.children.find((child) => child?.type?.endsWith?.("InfiniteScrollTrigger"));
  assert.equal(pending.props.isLoading, true);
  await loadPage(2);
  assert.deepEqual(requestedPages, [1, 2]);
  complete({ list: [], page: 2, hasNext: false });
  await next;
  // 마지막 페이지 완료 뒤 로딩과 추가 감지 종료 검증
  const finished = renderPage().props.children.find((child) => child?.type?.endsWith?.("InfiniteScrollTrigger"));
  assert.equal(finished.props.isLoading, false);
  assert.equal(finished.props.hasNext, false);
}

// 실제 페이지의 추가 조회 상태 회귀 검사 등록
test("차단 목록 추가 조회는 로딩 상태를 렌더링하고 중복 요청을 차단", checkBlockedPaging);
