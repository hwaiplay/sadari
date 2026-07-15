/**
 * src/main/frontend/src/features/Book/Set/components/form/field/FormField.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */

import React, { ReactNode } from "react";
import { field, fieldTitle } from "./FormField.css";

interface FormFieldProps {
  title: string;
  children: ReactNode;
}

const FormField = ({ title, children }: FormFieldProps) => {
  return (
    <div className={field}>
      <h1 className={fieldTitle}>{title}</h1>
      {children}
    </div>
  );
};

export default FormField;