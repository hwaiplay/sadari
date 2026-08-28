import { vars } from "@/app/styles/tokens.css";

/**
 * 목표 달성률 구간에 맞는 마이페이지 공통 진행 색상을 반환한다
 *
 * @author Hanwon.Jang
 * @param rate 현재 목표 달성률
 * @return 목표 달성률 구간의 디자인 토큰
 */
export const getGoalProgressColor = (rate: number) => {

  if (rate >= 100) {
    // 목표를 모두 달성한 상태는 브랜드 색상을 반환한다
    return vars.color.brand;
  }

  if (rate >= 25) {
    // 백분율이 중간 구간 이상인 상태는 노란색을 반환한다
    return vars.color.yellow;
  }

  // 백분율이 25퍼센트 미만인 상태는 분홍색을 반환한다
  return vars.color.negative;
};
