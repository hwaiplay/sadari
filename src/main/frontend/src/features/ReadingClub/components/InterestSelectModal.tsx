import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import type { UserInterest } from "@/features/User/api/userApi";
import { useMemo, useState } from "react";
import { createPortal } from "react-dom";
import * as styles from "./InterestSelectModal.css.ts";
import {style} from "@vanilla-extract/css";

type InterestSelectModalProps = {
  catalog: UserInterest[];
  initialCodes: string[];
  minimum?: number;
  maximum?: number;
  onSave: (codes: string[]) => void;
  onClose?: () => void;
};

/** 관심분야를 대분류별로 선택하는 공통 팝업을 구성한다. @author SeungHyeon.Kang @param props 관심분야 선택 속성 @return 관심분야 선택 팝업 */
export default function InterestSelectModal({
  catalog,
  initialCodes,
  minimum = 1,
  maximum,
  onSave,
  onClose,
}: InterestSelectModalProps) {

  const [selectedCodes, setSelectedCodes] = useState(() => new Set(initialCodes));
  const groups = useMemo(() => {
    // 대분류명이 같은 관심분야를 한 묶음으로 구성한다
    const grouped = new Map<string, UserInterest[]>();
    catalog.forEach((interest) => grouped.set(interest.intrCnam, [...(grouped.get(interest.intrCnam) ?? []), interest]));
    // 원래 서버 정렬을 유지한 대분류 목록을 반환한다
    return Array.from(grouped.entries());
  }, [catalog]);

  /** 관심분야 한 항목을 선택하거나 해제한다. @author SeungHyeon.Kang @param intrCode 관심분야 코드 @return 반환값이 없다 */
  const toggleInterest = (intrCode: string): void => {
    // 기존 Set을 직접 변경하지 않고 새 선택 상태를 만든다
    setSelectedCodes((current) => {
      const next = new Set(current);
      // 이미 선택한 항목은 해제한다
      if (next.has(intrCode)) {
        next.delete(intrCode);
        // 선택 해제 결과를 반환한다
        return next;
      }
      // 최대 선택 수를 넘는 추가 선택은 무시한다
      if (maximum && next.size >= maximum) {
        // 기존 선택 상태를 유지한다
        return current;
      }
      // 허용 범위 안의 새 항목을 추가한다
      next.add(intrCode);
      // 새 선택 상태를 반환한다
      return next;
    });
  };

  // 페이지 전환 stacking context 밖에서 최상위 관심분야 선택 팝업을 반환한다
  return createPortal(
    <div className={styles.overlay} role="presentation">
      <section className={styles.modal} role="dialog" aria-modal="true" aria-labelledby="interest-modal-title">
        {/* 팝업 제목과 선택 조건 영역 */}
        <header className={styles.modalHeader}>
          <div>
            <h2 id="interest-modal-title" className={styles.modalTitle}>
              {/* 관심 카테고리 선택 */}
              {message("frontend.readingClub.interest.title")}
            </h2>
            <p className={styles.modalDescription}>
              {/* 좋아하는 독서 분야를 선택해 주세요 */}
              {message("frontend.readingClub.interest.description")}
              {maximum ? `\n${message("frontend.readingClub.interest.maximum", [maximum])}` : ""}
            </p>
          </div>
          {onClose && (
            <button className={styles.closeButton} type="button" onClick={onClose}>
              <img src="/img/icons/icon-close.svg" alt={/* "닫기" */ message("frontend.common.close")} />
            </button>
          )}
        </header>
        {/* 대분류별 관심분야 선택 칩 영역 */}
        <div className={styles.modalBody}>
          {groups.map(([categoryName, interests]) => (
            <section className={styles.interestGroup} key={categoryName}>
              <h3 className={styles.interestTitle}>{categoryName}</h3>
              <div className={styles.interestList}>
                {interests.map((interest) => (
                  <button className={styles.interest} type="button" data-selected={selectedCodes.has(interest.intrCode)} onClick={() => toggleInterest(interest.intrCode)} key={interest.intrCode}>
                    {interest.intrName}
                  </button>
                ))}
              </div>
            </section>
          ))}
        </div>
        {/* 선택 저장 영역 */}
        <footer className={styles.modalActions}>
          <ActionButton
            type="button"
            variant="primary"
            size="md"
            width="full"
            disabled={selectedCodes.size < minimum}
            onClick={() => onSave(Array.from(selectedCodes))}
          >
            {/* 선택 완료 */}
            {message("frontend.readingClub.interest.save")}
          </ActionButton>
        </footer>
      </section>
    </div>,
    document.body,
  );
}
