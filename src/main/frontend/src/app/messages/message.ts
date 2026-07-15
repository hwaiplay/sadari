import koMessages from "./messages.properties?raw";
import enMessages from "./messages_en.properties?raw";

type MessageParams = Array<string | number>;

const MESSAGE_SOURCES = {
  ko: parseProperties(koMessages),
  en: parseProperties(enMessages),
};

/**
 * properties 형식 메시지 파일을 key-value 객체로 변환합니다.
 *
 * @author Hanwon.Jang
 * @param source raw 문자열로 읽은 properties 파일 내용
 * @return 메시지 key-value 객체
 */
function parseProperties(source: string) {
  return source
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith("#"))
    .reduce<Record<string, string>>((messages, line) => {
      const separatorIndex = line.indexOf("=");

      if (separatorIndex === -1) {
        return messages;
      }

      const key = line.slice(0, separatorIndex).trim();
      const value = line
        .slice(separatorIndex + 1)
        .trim()
        .replace(/\\n/g, "\n")
        .replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) =>
          String.fromCharCode(parseInt(hex, 16)),
        );

      messages[key] = value;
      return messages;
    }, {});
}

/**
 * 브라우저 언어를 기준으로 사용할 메시지 locale을 결정합니다.
 *
 * @author Hanwon.Jang
 * @return 지원 locale 코드
 */
function getLocale() {
  return navigator.language.toLowerCase().startsWith("en") ? "en" : "ko";
}

/**
 * 현재 locale에 맞는 메시지를 조회하고 파라미터를 치환합니다.
 *
 * @author Hanwon.Jang
 * @param key 조회할 메시지 key
 * @param params 메시지 템플릿의 {0}, {1} 자리에 치환할 값 목록
 * @return 치환이 완료된 메시지 문자열
 */
export function message(key: string, params: MessageParams = []) {
  const localeMessages = MESSAGE_SOURCES[getLocale()];
  const fallbackMessages = MESSAGE_SOURCES.ko;
  const template = localeMessages[key] ?? fallbackMessages[key] ?? key;

  return params.reduce<string>(
    (result, param, index) => result.split(`{${index}}`).join(String(param)),
    template,
  );
}
