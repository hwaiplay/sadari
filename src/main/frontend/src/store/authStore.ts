/**
 * fileName       : authStore
 * author         : hanwon.Jang
 * date           : 2026-03-23
 * description    : 로그인 상태 관리 스토어
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23       hanwon.Jang       최초 생성
 */

import { create } from "zustand";

interface AuthState {
  isLogin: boolean;
  accessToken: string | null;
  setLogin: (token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isLogin: false,
  accessToken: null,

  setLogin: (token) =>
    set({
      isLogin: true,
      accessToken: token,
    }),

  logout: () =>
    set({
      isLogin: false,
      accessToken: null,
    }),
}));
