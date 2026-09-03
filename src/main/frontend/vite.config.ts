import { defineConfig, type HttpProxy, type ProxyOptions } from "vite";
import react from "@vitejs/plugin-react";
import { vanillaExtractPlugin } from "@vanilla-extract/vite-plugin";
import type { ClientRequest } from "node:http";
import { readFileSync } from "node:fs";
import * as path from "node:path";
import { parse } from "yaml";

type SpringProfileConfig = {
  domain?: {
    back?: string;
    front?: string;
    proxy?: string;
  };
};

type DevelopmentServerConfig = {
  allowedHost: string;
  proxyTarget: string;
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
function createDevProxyOptions(target: string): ProxyOptions {

  // 운영 CORS 허용 범위를 넓히지 않고 로컬 개발 프록시에서만 요청 출처를 보정한다
  return {
    target,
    changeOrigin: true,
    // Spring이 localhost와 Tailnet 요청을 구분하도록 브라우저가 사용한 원래 Host를 전달한다
    xfwd: true,
    configure: configureDevelopmentProxy,
  };
}

/**
 * Spring yml의 환경변수 placeholder를 현재 프로세스 환경값으로 해석한다
 *
 * @author HanWon.Jang
 * @param configuredValue yml의 내부 프록시 설정값
 * @return 실제 Vite proxy 대상 URL
 */
const resolveSpringValue = (configuredValue: string) => {

  const placeholder = SPRING_PLACEHOLDER_PATTERN.exec(configuredValue);

  // 환경변수 placeholder가 아니면 yml에 작성된 고정값을 그대로 사용한다
  if (!placeholder) {
    // yml에 작성된 고정 프록시 주소를 반환한다
    return configuredValue;
  }

  const [, environmentName, defaultValue] = placeholder;
  const resolvedValue = process.env[environmentName] ?? defaultValue;

  // 운영 프로필로 개발 서버를 실행하면서 필수 환경변수가 없으면 잘못된 proxy로 조용히 실행하지 않고 즉시 중단한다
  if (!resolvedValue) {
    throw new Error(
      `Spring yml placeholder ${environmentName} could not be resolved.`,
    );
  }

  // 현재 프로세스 환경에 맞게 해석한 프록시 주소를 반환한다
  return resolvedValue;
};

/**
 * 현재 Spring profile의 application yml에서 Vite 개발 서버 연결 설정을 읽는다
 * 외부 Tailscale 호스트와 내부 API 전달 주소를 분리하여 프록시 순환과 임의 Host 요청을 차단한다
 *
 * @author HanWon.Jang
 * @return 검증된 Vite 허용 호스트와 내부 프록시 대상
 */
const getDevServerConfig = (): DevelopmentServerConfig => {

  const activeProfile =
    process.env.SPRING_PROFILES_ACTIVE?.split(",")[0]?.trim() || "loc";

  // 환경변수를 파일명에 사용하므로 경로 문자가 포함된 profile 값은 yml 디렉터리 밖을 읽지 못하게 차단한다
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
  const configuredProxyTarget =
    springConfig.domain?.proxy ?? springConfig.domain?.back;
  const configuredPublicDomain =
    springConfig.domain?.front ?? springConfig.domain?.back;

  // 내부 프록시와 이전 호환용 백엔드 주소가 모두 없으면 잘못된 개발 서버 실행을 차단한다
  if (!configuredProxyTarget) {
    throw new Error(
      `domain.proxy and domain.back are missing from ${configPath}.`,
    );
  }

  // 외부 접속 주소가 없으면 Vite Host 허용 범위를 안전하게 확정할 수 없어 실행을 차단한다
  if (!configuredPublicDomain) {
    throw new Error(
      `domain.front and domain.back are missing from ${configPath}.`,
    );
  }

  const backendProxyTarget = resolveSpringValue(configuredProxyTarget);
  // 프록시 대상의 프로토콜을 검증할 URL 객체를 생성한다
  const parsedProxyTarget = new URL(backendProxyTarget);
  const publicDomain = resolveSpringValue(configuredPublicDomain);
  // 휴대폰의 Tailscale 요청 Host를 허용 목록에 등록할 URL 객체를 생성한다
  const parsedPublicDomain = new URL(publicDomain);

  // HTTP(S)가 아닌 scheme을 proxy 대상으로 허용하면 로컬 파일 등 의도하지 않은 자원에 접근할 수 있어 차단한다
  if (!["http:", "https:"].includes(parsedProxyTarget.protocol)) {
    throw new Error("domain.proxy or domain.back must use http or https.");
  }

  // HTTP(S)가 아닌 외부 주소는 Vite Host 허용 목록에 등록하지 않는다
  if (!["http:", "https:"].includes(parsedPublicDomain.protocol)) {
    throw new Error("domain.front or domain.back must use http or https.");
  }

  // 외부 Tailscale 호스트와 후행 슬래시를 제거한 내부 프록시 대상을 반환한다
  return {
    allowedHost: parsedPublicDomain.hostname,
    proxyTarget: backendProxyTarget.replace(/\/+$/, ""),
  };
};

export default defineConfig(({ command }) => {
  /*
   * proxy는 Vite 개발 서버에서만 필요하다.
   * 운영 build는 Spring이 정적 파일과 API를 같은 origin으로 제공하므로 로컬 yml 파일 없이도 생성할 수 있다.
   */
  const developmentServerConfig =
    command === "serve" ? getDevServerConfig() : null;

  // 개발 서버에서는 고정 포트와 내부 API 프록시를 적용하고 운영 빌드에서는 정적 자원 설정만 반환한다
  return {
    plugins: [react(), vanillaExtractPlugin()],
    build: {
      rollupOptions: {
        output: {
          // 화면 공통 프레임워크는 업무 코드와 분리하여 변경 시 브라우저 캐시를 재사용한다
          manualChunks: {
            "framework-vendor": ["react", "react-dom", "react-router-dom"],
            "data-vendor": ["@tanstack/react-query", "axios", "zustand"],
          },
        },
      },
    },
    server: developmentServerConfig
      ? {
          allowedHosts: [developmentServerConfig.allowedHost],
          host: "127.0.0.1",
          strictPort: true,
          proxy: {
            "/api": createDevProxyOptions(
              developmentServerConfig.proxyTarget,
            ),
            "/uploads": createDevProxyOptions(
              developmentServerConfig.proxyTarget,
            ),
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
