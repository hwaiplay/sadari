export type SafetyReportTargetType = "REPORT" | "REPLY";

export type SafetyReportTarget = {
  targetType: SafetyReportTargetType;
  targetNumb: number;
  reportNumb: number;
  userNumb: number;
  userNick: string;
  content: string;
};

export type UserReportLocationState = {
  target: SafetyReportTarget;
};
