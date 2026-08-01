import { defineConfig, type HttpProxy, type ProxyOptions } from "vite";
import react from "@vitejs/plugin-react";
import { vanillaExtractPlugin } from "@vanilla-extract/vite-plugin";
import type { ClientRequest } from "node:http";
import { readFileSync } from "node:fs";
import path from "node:path";
import { parse } from "yaml";

type SpringProfileConfig = {
  domain?: {
    back?: string;
  };
};

const SPRING_PLACEHOLDER_PATTERN =
  /^\$\{([A-Z][A-Z0-9_]*)(?::([^}]*))?\}$/;
const SPRING_PROFILE_PATTERN = /^[A-Za-z0-9_-]+$/;

/**
 * 브라우저가 로컬 개발 서버에 보낸 Origin을 내부 백엔드 프록시 요청에서 제거한다
 *
 * @author HanWon.Jang
 * @param proxyRequest 백엔드로 전달할 프록시 요청
 * @return 반환값이 없다
 */
function removeForwardedOrigin(proxyRequest: ClientRequest): void {

  // 브라우저와 Vite 사이에서는 동일 출처 요청이므로 내부 전달 단계에서 외부 CORS 요청으로 오인되지 않게 한다
  proxyRequest.removeHeader("origin");
}

/**
 * 로컬 개발 프록시에 동일 출처 요청 헤더 보정 처리를 등록한다
 *
 * @author HanWon.Jang
 * @param proxy Vite 개발 서버의 HTTP 프록시
 * @return 반환값이 없다
 */
function configureDevelopmentProxy(proxy: HttpProxy.ProxyServer): void {

  // POST와 PUT 저장 요청도 조회 요청과 같은 내부 프록시 흐름으로 처리되도록 Origin 보정을 등록한다
  proxy.on("proxyReq", removeForwardedOrigin);
}

/**
 * API와 업로드 경로에 공통으로 적용할 로컬 개발 프록시 옵션을 생성한다
 *
 * @author HanWon.Jang
 * @param target 프록시 요청을 전달할 백엔드 주소
 * @return 동일 출처 헤더 보정이 포함된 Vite 프록시 옵션
 */
function createDevelopmentProxyOptions(target: string): ProxyOptions {

  // 운영 CORS 허용 범위를 넓히지 않고 로컬 개발 프록시에서만 요청 출처를 보정한다
  return {
    target,
    changeOrigin: true,
    configure: configureDevelopmentProxy,
  };
}

/**
 * Spring yml의 환경변수 placeholder를 현재 프로세스 환경값으로 해석합니다.
 *
 * @author HanWon.Jang
 * @param configuredValue yml의 domain.back 설정값
 * @return 실제 Vite proxy 대상 URL
 */
const resolveSpringValue = (configuredValue: string) => {
  const placeholder = SPRING_PLACEHOLDER_PATTERN.exec(configuredValue);

  if (!placeholder) {
    return configuredValue;
  }

  const [, environmentName, defaultValue] = placeholder;
  const resolvedValue = process.env[environmentName] ?? defaultValue;

  // 운영 프로필로 개발 서버를 실행하면서 필수 환경변수가 없으면 잘못된 proxy로 조용히 실행하지 않고 즉시 중단합니다.
  if (!resolvedValue) {
    throw new Error(
      `Spring yml placeholder ${environmentName} could not be resolved.`,
    );
  }

  return resolvedValue;
};

/**
 * 현재 Spring profile의 application yml에서 백엔드 도메인을 읽습니다.
 * Vite 개발 서버와 Spring 서버가 서로 다른 주소를 사용하지 않도록 yml을 단일 설정 원본으로 사용합니다.
 *
 * @author HanWon.Jang
 * @return 검증된 백엔드 도메인 URL
 */
const getBackendDomain = () => {
  const activeProfile =
    process.env.SPRING_PROFILES_ACTIVE?.split(",")[0]?.trim() || "loc";

  // 환경변수를 파일명에 사용하므로 경로 문자가 포함된 profile 값은 yml 디렉터리 밖을 읽지 못하게 차단합니다.
  if (!SPRING_PROFILE_PATTERN.test(activeProfile)) {
    throw new Error("SPRING_PROFILES_ACTIVE contains invalid characters.");
  }

  const configPath = path.resolve(
    __dirname,
    `../resources/application-${activeProfile}.yml`,
  );
  const springConfig = parse(
    readFileSync(configPath, "utf8"),
  ) as SpringProfileConfig;
  const configuredBackDomain = springConfig.domain?.back;

  if (!configuredBackDomain) {
    throw new Error(`domain.back is missing from ${configPath}.`);
  }

  const backendDomain = resolveSpringValue(configuredBackDomain);
  const parsedDomain = new URL(backendDomain);

  // HTTP(S)가 아닌 scheme을 proxy 대상으로 허용하면 로컬 파일 등 의도하지 않은 자원에 접근할 수 있어 차단합니다.
  if (!["http:", "https:"].includes(parsedDomain.protocol)) {
    throw new Error("domain.back must use http or https.");
  }

  return backendDomain.replace(/\/+$/, "");
};

export default defineConfig(({ command }) => {
  /*
   * proxy는 Vite 개발 서버에서만 필요합니다.
   * 운영 build는 Spring이 정적 파일과 API를 같은 origin으로 제공하므로 로컬 yml 파일 없이도 생성할 수 있습니다.
   */
  const backendDomain = command === "serve" ? getBackendDomain() : null;

  return {
    plugins: [react(), vanillaExtractPlugin()],
    server: backendDomain
      ? {
          proxy: {
            "/api": createDevelopmentProxyOptions(backendDomain),
            "/uploads": createDevelopmentProxyOptions(backendDomain),
          },
        }
      : undefined,
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  };
});
