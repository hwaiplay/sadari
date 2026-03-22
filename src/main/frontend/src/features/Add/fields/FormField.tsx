/**
 * fileName       : FormField
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 기록하기 폼 내의 각 입력 영역
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

import React, { ReactNode } from "react";
import { fieldTitle } from "./FormField.css";

interface FormFieldProps {
  title: string;
  children: ReactNode;
}

const FormField = ({ title, children }: FormFieldProps) => {
  return (
    <div>
      <h1 className={fieldTitle}>{title}</h1>
      {children}
    </div>
  );
};

export default FormField;
