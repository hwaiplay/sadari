import api from "../axios";
import { useAuthStore } from "../../store/authStore";

export const refresh = async () => {
  try {
    const res = await api.post("/oauth/refresh");

    const accessToken = res.data.accessToken;

    // Zustand에 저장
    useAuthStore.getState().setLogin(accessToken);

    return true;
  } catch (e) {
    useAuthStore.getState().logout();
    return false;
  }
};
