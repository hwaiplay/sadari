/**
 * fileName       : EachLine
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 기록하기 폼 내의 각 입력 영역
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

import React, { ReactNode } from "react";

interface EachLineProps {
  title: string;
  children: ReactNode;
}

const EachLine = ({ title, children }: EachLineProps) => {
  return (
    <div>
      <h1>{title}</h1>
      {children}
    </div>
  );
};

export default EachLine;
