// src/api/axios.ts
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true, // cookie 자동 포함
});

export default api;
