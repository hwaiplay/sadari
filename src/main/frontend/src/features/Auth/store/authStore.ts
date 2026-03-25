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
import api from "../../../app/api/axios";

interface AuthState {
  user: any | null;
  isAuthenticated: boolean; // 로그인 인증 여부
  isLoading: boolean;
  checkAuth: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  // 초기값 설정
  user: null,
  isAuthenticated: false,
  isLoading: true,

  // 로그인 상태 체크 함수
  checkAuth: async () => {
    try {
      const res = await api.get("/oauth/tokenCheck", {});

      console.log("토큰 체크 응답:", res.data);
      set({
        user: res.data.data,
        isAuthenticated: true,
        isLoading: false,
      });
    } catch {
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
      });
      throw new Error("auth failed");
    }
  },
}));
