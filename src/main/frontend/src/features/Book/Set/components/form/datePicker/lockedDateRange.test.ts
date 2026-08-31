import assert from "node:assert/strict";
import test from "node:test";
import { getLockedDateRange } from "./lockedDateRange.ts";

test("잠긴 독서 시작일은 유지하고 시작일보다 이른 종료일은 거부한다", () => {

  assert.deepEqual(
    getLockedDateRange("2026-08-20", "2026-08-31"),
    ["2026-08-20", "2026-08-31"],
  );
  assert.equal(getLockedDateRange("2026-08-20", "2026-08-19"), null);
});
