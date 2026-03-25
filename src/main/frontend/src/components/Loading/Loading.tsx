// src/components/common/Loading.tsx
import React from "react";
import * as styles from "./Loading.css";

const Loading: React.FC = () => {
  return (
    <div className={styles.container}>
      <div className={styles.spinner} />
      <p className={styles.text}>로딩중...</p>
    </div>
  );
};

export default Loading;
