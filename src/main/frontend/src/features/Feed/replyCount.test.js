import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { runInNewContext } from "node:vm";
import { transpileModule } from "typescript";

// 실제 닫기 처리 실행을 통한 대상별 집계와 실패 시 기존 상태 유지 검증
test("피드 댓글 시트 닫기 후 대상 댓글 수 동기화", async () => {

  const source = readFileSync(new URL("../../pages/Feed/FeedPage.tsx", import.meta.url), "utf8");
  const handler = source.match(/const closeReplySheet = async[\s\S]*?\n {2}};/)?.[0];
  assert.ok(handler);
  const compiled = transpileModule(`${handler}\ncloseReplySheet();`, {}).outputText;
  const target = { tagtType: "REPORT", tagtNumb: 10 };
  const original = [
    { ...target, replCnt: 1, likeCnt: 7 },
    { tagtType: "PROFILE_IMAGE", tagtNumb: 10, replCnt: 3 },
    { tagtType: "REPORT", tagtNumb: 11, replCnt: 4 },
  ];
  let items = original;
  let closed = false;
  let failed = false;
  const context = {
    replyItem: target,
    setReplyItem: (value) => { closed = value === null; },
    getFeedTargetApi: async (type, numb) => {
      assert.equal(closed, true);
      assert.equal(type, target.tagtType);
      assert.equal(numb, target.tagtNumb);
      return { replCnt: 2 };
    },
    setItems: (update) => { items = update(items); },
    sweetError: async () => { failed = true; },
    message: (key) => key,
    getApiErrorMessage: () => "조회 실패",
  };
  await runInNewContext(compiled, { ...context });
  assert.equal(items[0].replCnt, 2);
  assert.equal(items[0].likeCnt, 7);
  assert.equal(original[0].replCnt, 1);
  assert.equal(items[1], original[1]);
  assert.equal(items[2], original[2]);
  const refreshed = items;
  await runInNewContext(compiled, {
    ...context,
    getFeedTargetApi: async () => { throw new Error("조회 실패"); },
  });
  assert.equal(failed, true);
  assert.equal(items, refreshed);
  await runInNewContext(compiled, {
    ...context,
    replyItem: null,
    getFeedTargetApi: async () => assert.fail("닫힌 시트의 불필요한 재조회"),
  });
});
