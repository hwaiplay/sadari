import koMessages from "./messages.properties?raw";
import enMessages from "./messages_en.properties?raw";

type MessageParams = Array<string | number>;

const MESSAGE_LOCALE_STORAGE_KEY = "sadari:message-locale";

/**
 * properties 형식 메시지 파일을 key-value 객체로 변환함
 *
 * @author HanWon.Jang
 * @param source raw 문자열로 읽은 properties 파일 내용
 * @return 메시지 key-value 객체
 */
const parseProperties = (source: string) => {

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
};

const MESSAGE_SOURCES = {
  ko: parseProperties(koMessages),
  en: parseProperties(enMessages),
};

/**
 * 브라우저 언어를 기준으로 사용할 메시지 locale을 결정함
 *
 * @author HanWon.Jang
 * @return 지원 locale 코드
 */
export const getDeviceEnglishYsno = (): "Y" | "N" =>
  navigator.language.toLowerCase().startsWith("en") ? "Y" : "N";

/** 저장된 계정 언어가 없으면 현재 기기 언어를 메시지 언어로 사용함 */
export const getMessageLocale = (): "en" | "ko" => {
  const savedLocale = window.localStorage.getItem(MESSAGE_LOCALE_STORAGE_KEY);
  if (savedLocale === "en" || savedLocale === "ko") {
    return savedLocale;
  }

  return getDeviceEnglishYsno() === "Y" ? "en" : "ko";
};

/** 서버에서 확정한 계정 언어를 현재 브라우저 메시지 언어로 저장함 */
export const setMessageLocale = (englishYsno: "Y" | "N"): void => {
  window.localStorage.setItem(MESSAGE_LOCALE_STORAGE_KEY, englishYsno === "Y" ? "en" : "ko");
};

/**
 * 현재 locale에 맞는 메시지를 조회하고 파라미터를 치환함
 *
 * @author HanWon.Jang
 * @param key 조회할 메시지 key
 * @param params 메시지 템플릿의 {0}, {1} 자리에 치환할 값 목록
 * @return 치환이 완료된 메시지 문자열
 */
export const message = (key: string, params: MessageParams = []) => {

  const localeMessages = MESSAGE_SOURCES[getMessageLocale()];
  const fallbackMessages = MESSAGE_SOURCES.ko;
  const template = localeMessages[key] ?? fallbackMessages[key] ?? key;

  return params.reduce<string>(
    (result, param, index) => result.split(`{${index}}`).join(String(param)),
    template,
  );
};
