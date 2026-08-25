import { useLayoutEffect, useRef, useState } from "react";
import * as styles from "./ReportListView.css";

type AnimatedReportContentProps = {
  content: string;
  expanded: boolean;
};

/** 독후감 미리보기 높이와 실제 본문 높이 사이를 부드럽게 전환한다. */
export default function AnimatedReportContent({ content, expanded }: AnimatedReportContentProps) {
  const contentRef = useRef<HTMLParagraphElement>(null);
  const [contentHeight, setContentHeight] = useState(70);

  useLayoutEffect(() => {
    const contentElement = contentRef.current;
    if (!contentElement) return undefined;
    const updateHeight = (): void => setContentHeight(Math.max(contentElement.scrollHeight, 70));
    updateHeight();
    const observer = new ResizeObserver(updateHeight);
    observer.observe(contentElement);
    // 본문 크기 감시가 더 이상 필요하지 않으면 관찰을 종료한다
    return () => observer.disconnect();
  }, [content]);

  // 측정한 실제 높이를 사용해 짧은 미리보기와 전체 본문 사이를 전환한다
  return (
    <div
      className={styles.reportContentWrap}
      style={{ maxHeight: expanded ? `${contentHeight}px` : "70px" }}
    >
      <p className={styles.reportContent} ref={contentRef}>{content}</p>
    </div>
  );
}
