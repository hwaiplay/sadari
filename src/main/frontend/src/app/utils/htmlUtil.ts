/**
 * HTML tag markup is removed from API text before the value is rendered or stored.
 * @Author Hanwon.Jang
 * @param value HTML tag markup may be included in this text.
 * @return Text with HTML tags removed.
 */
export function stripHtmlTags(value?: string) {
  return value?.replace(/<[^>]*>/g, "") ?? "";
}
