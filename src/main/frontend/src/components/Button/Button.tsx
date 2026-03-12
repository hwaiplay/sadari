import * as s from "./button.css";

type ButtonProps = {
  variant?: keyof typeof s.buttonVariant;
  children: React.ReactNode;
};

export function Button({ variant = "primary", children }: ButtonProps) {
  return (
    <button className={`${s.buttonBase} ${s.buttonVariant[variant]}`}>
      {children}
    </button>
  );
}
