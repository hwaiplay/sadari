import { readFile, writeFile } from "node:fs/promises";
import { instance } from "@viz-js/viz";

const [dotPath, svgPath] = process.argv.slice(2);

// ERD Graphviz 정의 로드
const dot = await readFile(dotPath, "utf8");
// WebAssembly 기반 Graphviz 렌더러 초기화
const viz = await instance();
// 확대 가능한 ERD SVG 생성
const svg = viz.renderString(dot, { format: "svg", engine: "dot" });
// Git 관리 대상 SVG 저장
await writeFile(svgPath, svg, "utf8");
