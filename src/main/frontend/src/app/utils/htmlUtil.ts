/**
 * src/main/frontend/src/app/utils/htmlUtil.ts 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
export function stripHtmlTags(value?: string) {
  return value?.replace(/<[^>]*>/g, "") ?? "";
}