import koMessages from "./messages.properties?raw";
import enMessages from "./messages_en.properties?raw";

type MessageParams = Array<string | number>;

const MESSAGE_SOURCES = {
  ko: parseProperties(koMessages),
  en: parseProperties(enMessages),
};

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

function getLocale() {
  return navigator.language.toLowerCase().startsWith("en") ? "en" : "ko";
}

export function message(key: string, params: MessageParams = []) {
  const localeMessages = MESSAGE_SOURCES[getLocale()];
  const fallbackMessages = MESSAGE_SOURCES.ko;
  const template = localeMessages[key] ?? fallbackMessages[key] ?? key;

  return params.reduce<string>(
    (result, param, index) => result.split(`{${index}}`).join(String(param)),
    template,
  );
}
