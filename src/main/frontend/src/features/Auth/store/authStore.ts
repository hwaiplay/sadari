/**
 * fileName       : authStore
 * author         : hanwon.Jang
 * date           : 2026-03-23
 * description    : 로그인 상태 관리 스토어
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23       hanwon.Jang       최초 생성
 * 2026-03-24       hanwon.Jang       로그인 상태 초기값 null로 변경
 * 2026-03-25       hanwon.Jang       커스텀 훅 맞춰 수정
 */

import { create } from "zustand";

interface AuthState {
  user: any | null;
  isAuthenticated: boolean;
  setAuth: (user: any) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setAuth: (user) => set({ user, isAuthenticated: true }),
  clearAuth: () => set({ user: null, isAuthenticated: false }),
}));
