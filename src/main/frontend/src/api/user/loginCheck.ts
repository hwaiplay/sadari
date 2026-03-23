import api from "../axios";

export const checkLogin = async () => {
  try {
    await api.get("/oauth/me");
    return true;
  } catch (e) {
    return false;
  }
};
