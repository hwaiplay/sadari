export type SafetyReportTargetType = "USER" | "REPORT" | "REPLY" | "PROFILE" | "INTRO";

export type SafetyReportTarget = {
  targetType: SafetyReportTargetType;
  targetNumb: number;
  reportNumb?: number;
  userNumb: number;
  userNick: string;
  content: string;
};

export type SafetyReportOption = {
  label: string;
  target: SafetyReportTarget;
};

export type UserReportLocationState = {
  target: SafetyReportTarget;
};
