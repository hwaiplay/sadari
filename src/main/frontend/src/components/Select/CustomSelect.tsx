import { useEffect, useId, useRef, useState, type ReactNode } from "react";
import { clsx } from "clsx";
import * as styles from "./CustomSelect.css";

export type CustomSelectOption<T extends string> = {
  value: T;
  label: string;
  className?: string;
  disabled?: boolean;
};

type CustomSelectProps<T extends string> = {
  value: T;
  options: readonly CustomSelectOption<T>[];
  onChange: (value: T) => void;
  ariaLabel: string;
  className?: string;
  triggerClassName?: string;
  optionListClassName?: string;
  optionClassName?: string;
  triggerContent?: ReactNode;
  showArrow?: boolean;
};

/**
 * Custom Select 화면 또는 컴포넌트를 구성함
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
 */
function CustomSelect<T extends string>({
  value,
  options,
  onChange,
  ariaLabel,
  className,
  triggerClassName,
  optionListClassName,
  optionClassName,
  triggerContent,
  showArrow = true,
}: CustomSelectProps<T>) {

  const listboxId = useId();
  const rootRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const optionRefs = useRef<Array<HTMLButtonElement | null>>([]);
  const selectedIndex = Math.max(
    0,
    options.findIndex((option) => option.value === value),
  );
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(selectedIndex);
  const selectedOption = options[selectedIndex];

  useEffect(() => {

    if (!isOpen) {
      return;
    }

    optionRefs.current[activeIndex]?.focus();
  }, [activeIndex, isOpen]);

  useEffect(() => {
    /**
     * handle Pointer Down 사용자 동작을 처리함
     *
     * @author HanWon.Jang
     * @param event event 입력값
     * @return 반환값이 없음
     */
    const handlePointerDown = (event: PointerEvent) => {

      if (!rootRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("pointerdown", handlePointerDown);

    return () => {

      document.removeEventListener("pointerdown", handlePointerDown);
    };
  }, []);

  /**
   * handle Select 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @param option option 입력값
   * @return 반환값이 없음
   */
  const handleSelect = (option: CustomSelectOption<T>) => {

    if (option.disabled) {
      return;
    }

    onChange(option.value);
    setActiveIndex(
      Math.max(
        0,
        options.findIndex((item) => item.value === option.value),
      ),
    );
    setIsOpen(false);
    triggerRef.current?.focus();
  };

  /**
   * handle Key Down 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @param event event 입력값
   * @return 반환값이 없음
   */
  const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {

    if (event.key === "Escape") {
      setIsOpen(false);
      triggerRef.current?.focus();
      return;
    }

    if (event.key === "ArrowDown" || event.key === "ArrowUp") {
      event.preventDefault();

      const direction = event.key === "ArrowDown" ? 1 : -1;
      setIsOpen(true);
      setActiveIndex((currentIndex) => {

        const nextIndex = currentIndex + direction;

        if (nextIndex < 0) return options.length - 1;
        if (nextIndex >= options.length) return 0;
        return nextIndex;
      });
    }
  };

  return (
    <div
      className={clsx(styles.root, className)}
      ref={rootRef}
      onKeyDown={handleKeyDown}
      onBlur={(event) => {

        if (!event.currentTarget.contains(event.relatedTarget)) {
          setIsOpen(false);
        }
      }}
    >
      <button
        className={clsx(styles.trigger, triggerClassName)}
        ref={triggerRef}
        type="button"
        aria-label={ariaLabel}
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-controls={listboxId}
        onClick={() => {

          setActiveIndex(selectedIndex);
          setIsOpen((prev) => !prev);
        }}
      >
        <span className={styles.triggerValue}>
          {triggerContent ?? selectedOption?.label ?? ""}
        </span>
        {showArrow ? (
          <svg
            className={clsx(styles.arrow, isOpen && styles.arrowOpen)}
            viewBox="0 0 12 12"
            aria-hidden="true"
          >
            <path d="m2.5 4.25 3.5 3.5 3.5-3.5" />
          </svg>
        ) : null}
      </button>

      <div
        className={clsx(
          styles.optionList,
          isOpen && styles.optionListOpen,
          optionListClassName,
        )}
        id={listboxId}
        role="listbox"
        aria-hidden={!isOpen}
      >
        {options.map((option, index) => {

          const isSelected = option.value === value;

          return (
            <button
              className={clsx(
                styles.option,
                optionClassName,
                option.className,
                isSelected && styles.optionSelected,
              )}
              ref={(element) => {

                optionRefs.current[index] = element;
              }}
              type="button"
              role="option"
              aria-selected={isSelected}
              disabled={option.disabled}
              tabIndex={isOpen ? 0 : -1}
              key={option.value}
              onMouseEnter={() => setActiveIndex(index)}
              onClick={() => handleSelect(option)}
            >
              {option.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default CustomSelect;
