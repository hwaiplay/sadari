import assert from "node:assert/strict";
import {readFileSync} from "node:fs";
import test from "node:test";
import {runInNewContext} from "node:vm";
import {ModuleKind, transpileModule} from "typescript";
import type {ClubChatMessage} from "@/features/ReadingClub/api/readingClubApi";
import type {mergeChatMessages as mergeType} from "./chatSync";

// 런타임 API 의존성 없이 실제 병합 함수 실행
const compiled = transpileModule(readFileSync(new URL("./chatSync.ts", import.meta.url), "utf8"), {
  compilerOptions: {module: ModuleKind.CommonJS},
}).outputText;
const exported = {} as {mergeChatMessages: typeof mergeType};
// 타입 전용 API 참조가 런타임 네트워크 호출로 이어지지 않음 확인
runInNewContext(compiled, {exports: exported});
const merge = exported.mergeChatMessages;

/**
 * 테스트 메시지의 필수 표시 필드 구성
 *
 * @author SeungHyeon.Kang
 * @param chatNumb 채팅 번호
 * @param unreadCnt 미읽음 수
 * @return 테스트용 원본 메시지
 */
const getMessage = (chatNumb: number, unreadCnt: number): ClubChatMessage => {
  // 병합과 무관한 API 필드는 동일한 테스트 값 사용
  return {chatNumb, unreadCnt, chatCntn: "local verification"} as ClubChatMessage;
};

/**
 * 같은 읽음 수의 반복 응답이 표시 상태를 교체하지 않는지 검증
 *
 * @author SeungHyeon.Kang
 * @return 검증 결과
 */
const testUnchanged = (): void => {
  // 기존 화면 상태 구성
  const current = [getMessage(10, 1)];
  // 새 본문도 숫자 변경도 없는 응답 병합
  const result = merge(current, [], [{chatNumb: 10, unreadCnt: 1}]);
  // 불필요한 React 재렌더링 없는 원본 참조 확인
  assert.equal(result, current);
};

/**
 * 새 메시지 없이도 기존 읽음 수가 갱신되고 본문은 보존되는지 검증
 *
 * @author SeungHyeon.Kang
 * @return 검증 결과
 */
const testReadOnly = (): void => {
  // 다른 회원이 아직 읽지 않은 본문 구성
  const current = [getMessage(10, 1)];
  // 기존 메시지와 화면에 없는 메시지의 숫자 응답 구성
  const result = merge(current, [], [{chatNumb: 10, unreadCnt: 0}, {chatNumb: 9, unreadCnt: 0}]);
  // 숫자만 있는 응답으로 유령 메시지가 생기지 않음 확인
  assert.equal(result.length, 1);
  // 다른 회원의 읽음 완료 반영 확인
  assert.equal(result[0].unreadCnt, 0);
  // 기존 본문이 비어 있는 숫자 응답으로 교체되지 않음 확인
  assert.equal(result[0].chatCntn, current[0].chatCntn);
  // 이전 React 상태의 원본 불변 확인
  assert.equal(current[0].unreadCnt, 1);
};

/**
 * 발신 응답과 폴링 응답이 겹쳐도 중복 없이 시간순 표시 검증
 *
 * @author SeungHyeon.Kang
 * @return 검증 결과
 */
const testDuplicate = (): void => {
  // 먼저 도착한 발신 응답 구성
  const current = [getMessage(11, 1)];
  // 늦게 도착한 이전 메시지와 중복 발신 응답 병합
  const result = merge(current, [getMessage(10, 1), getMessage(11, 0)], []);
  // 중복 메시지 생성 제외 확인
  assert.equal(result.length, 2);
  // 번호 시간순 표시 확인
  assert.equal(result[0].chatNumb, 10);
  // 최신 서버 미읽음 수 확인
  assert.equal(result[1].unreadCnt, 0);
};

// 실제 표시 상태 보존 계약을 독립적으로 검증
test("unchanged polling preserves state", testUnchanged);
test("read counts update without new messages", testReadOnly);
test("overlapping replies preserve order without duplicates", testDuplicate);
