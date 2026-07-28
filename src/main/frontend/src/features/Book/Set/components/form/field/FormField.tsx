/**
 * src/main/frontend/src/features/Book/Set/components/form/field/FormField.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */

import type { ReactNode } from "react";
import { field, fieldTitle } from "./FormField.css";

interface FormFieldProps {
  title: string;
  children: ReactNode;
}

/**
 * Form Field 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
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
