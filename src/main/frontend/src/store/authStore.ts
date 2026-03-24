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
 */

import { create } from "zustand";

interface AuthState {
  isLogin: boolean | null; // 초기 로딩 상태
  accessToken: string | null;
  setLogin: (token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isLogin: null,
  accessToken: null,

  setLogin: (token: string) =>
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
