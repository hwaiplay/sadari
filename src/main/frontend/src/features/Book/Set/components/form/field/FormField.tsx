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
import { field, fieldTitle } from "./FormField.css";

interface FormFieldProps {
  title: string;
  children: ReactNode;
}

/**
 * 독후감 입력 폼의 제목과 입력 영역을 하나의 필드 블록으로 감싼다.
 * @Author Hanwon.Jang
 * @param title 필드 제목
 * @param children 필드 안에 렌더링할 입력 UI
 * @return 폼 필드 레이아웃 컴포넌트
 */
const FormField = ({ title, children }: FormFieldProps) => {
  return (
    <div className={field}>
      <h1 className={fieldTitle}>{title}</h1>
      {children}
    </div>
  );
};

export default FormField;
