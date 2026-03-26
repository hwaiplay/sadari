// src/api/axios.ts
import axios from "axios";

const api = axios.create({
  baseURL: "/api", // 백엔드 API 기본 URL
  withCredentials: true, // cookie 자동 포함
});

export default api;
