import { createPortal } from "react-dom";
import { useEffect, useRef, useState } from "react";
import type { PublicReportType } from "@/features/Book/types/book.type";
import { message } from "@/app/messages/message";
import * as styles from "./CommentSheet.css";

const CLOSE_DISTANCE = 96;
const CLOSE_VELOCITY = 0.55;
const CLOSE_ANIMATION_MS = 180;

type CommentSheetProps = {
  report: Pick<PublicReportType, "reptNumb" | "userNick">;
  comments: readonly string[];
  onClose: () => void;
  onSubmitComment: (comment: string) => void;
};

function CommentSheet({
  report,
  comments,
  onClose,
  onSubmitComment,
}: CommentSheetProps) {
  const sheetRef = useRef<HTMLElement>(null);
  const dragStartRef = useRef({ y: 0, time: 0, pointerId: -1 });
  const closeTimerRef = useRef<number | null>(null);
  const [commentInput, setCommentInput] = useState("");
  const [dragOffset, setDragOffset] = useState(0);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    const previousOverflow = document.body.style.overflow;
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    document.body.style.overflow = "hidden";
    window.addEventListener("keydown", handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);

      if (closeTimerRef.current !== null) {
        window.clearTimeout(closeTimerRef.current);
      }
    };
  }, [onClose]);

  const closeWithDragAnimation = () => {
    setIsDragging(false);
    setDragOffset(sheetRef.current?.offsetHeight ?? window.innerHeight);
    closeTimerRef.current = window.setTimeout(onClose, CLOSE_ANIMATION_MS);
  };

  const handlePointerDown = (
    event: React.PointerEvent<HTMLDivElement>,
  ) => {
    if (!event.isPrimary) {
      return;
    }

    dragStartRef.current = {
      y: event.clientY,
      time: performance.now(),
      pointerId: event.pointerId,
    };
    setIsDragging(true);
    event.currentTarget.setPointerCapture(event.pointerId);
  };

  const handlePointerMove = (
    event: React.PointerEvent<HTMLDivElement>,
  ) => {
    if (
      !isDragging ||
      dragStartRef.current.pointerId !== event.pointerId
    ) {
      return;
    }

    setDragOffset(Math.max(0, event.clientY - dragStartRef.current.y));
  };

  const handlePointerEnd = (
    event: React.PointerEvent<HTMLDivElement>,
  ) => {
    if (dragStartRef.current.pointerId !== event.pointerId) {
      return;
    }

    const distance = Math.max(0, event.clientY - dragStartRef.current.y);
    const elapsed = Math.max(1, performance.now() - dragStartRef.current.time);
    const velocity = distance / elapsed;
    dragStartRef.current.pointerId = -1;

    if (
      distance >= CLOSE_DISTANCE ||
      (distance >= 24 && velocity >= CLOSE_VELOCITY)
    ) {
      closeWithDragAnimation();
      return;
    }

    setIsDragging(false);
    setDragOffset(0);
  };

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const comment = commentInput.trim();

    if (!comment) {
      return;
    }

    onSubmitComment(comment);
    setCommentInput("");
  };

  const sheet = (
    <div className={styles.sheetLayer}>
      <button
        className={styles.sheetBackdrop}
        type="button"
        aria-label={message("frontend.common.close")}
        onClick={onClose}
      />
      <section
        className={styles.commentSheet}
        ref={sheetRef}
        role="dialog"
        aria-modal="true"
        aria-label={`${report.userNick}님의 독후감 댓글`}
        style={{
          transform: dragOffset > 0 ? `translateY(${dragOffset}px)` : undefined,
          transition: isDragging
            ? "none"
            : `transform ${CLOSE_ANIMATION_MS}ms ease-out`,
        }}
      >
        <div
          className={styles.sheetHandle}
          aria-label="아래로 당겨 닫기"
          role="button"
          tabIndex={0}
          onKeyDown={(event) => {
            if (event.key === "Enter" || event.key === " ") {
              event.preventDefault();
              onClose();
            }
          }}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerEnd}
          onPointerCancel={handlePointerEnd}
        />

        <div className={styles.commentSheetBody}>
          {comments.length > 0 ? (
            <ul className={styles.temporaryCommentList}>
              {comments.map((comment, index) => (
                <li
                  className={styles.temporaryComment}
                  key={`${report.reptNumb}-${index}`}
                >
                  {comment}
                </li>
              ))}
            </ul>
          ) : (
            <div className={styles.commentEmpty}>
              <img
                className={styles.commentEmptyIcon}
                src="/img/icons/noti-COMMENT.svg"
                alt=""
              />
              <p className={styles.commentEmptyTitle}>아직 댓글이 없어요.</p>
              <p className={styles.commentEmptyText}>
                첫 번째 댓글을 남겨보세요.
              </p>
            </div>
          )}
        </div>

        <form className={styles.commentForm} onSubmit={handleSubmit}>
          <input
            className={styles.commentInput}
            type="text"
            value={commentInput}
            maxLength={500}
            placeholder="댓글을 입력해주세요."
            aria-label="댓글 입력"
            onChange={(event) => setCommentInput(event.target.value)}
          />
          <button
            className={styles.commentSubmitButton}
            type="submit"
            disabled={!commentInput.trim()}
          >
            등록
          </button>
        </form>
      </section>
    </div>
  );

  return typeof document !== "undefined"
    ? createPortal(sheet, document.body)
    : null;
}

export default CommentSheet;
