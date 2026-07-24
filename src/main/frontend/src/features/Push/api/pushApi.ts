import api from "@/app/api/axios";
import { assertResultDataSuccess } from "@/app/api/resultData";

export type FirebaseWebConfig = {
  apiKey: string;
  authDomain?: string;
  projectId: string;
  storageBucket?: string;
  messagingSenderId: string;
  appId: string;
  vapidPublicKey: string;
};

export type PushSubRequest = {
  endpUrlx: string;
  p256Keyx?: string;
  authKeyx?: string;
};

/**
 * FCM token 발급에 필요한 Firebase Web 공개 설정을 조회합니다.
 * service account 같은 비밀값은 서버가 내려주지 않습니다.
 *
 * @author Hanwon.Jang
 * @return Firebase Web 설정 API 응답
 */
export const getPushConfigApi = async () => {
  const res = await api.get<{ data: FirebaseWebConfig }>("/push/config");
  return assertResultDataSuccess(res.data);
};

/**
 * 현재 브라우저의 FCM token을 서버 구독 테이블에 저장합니다.
 * 서버는 로그인 사용자 번호를 인증 정보에서 채우므로 프론트는 token만 전달합니다.
 *
 * @author Hanwon.Jang
 * @param data FCM token 요청
 * @return 저장 API 응답
 */
export const setPushSubApi = async (data: PushSubRequest) => {
  const res = await api.post("/push/subscribe", data);
  return assertResultDataSuccess(res.data);
};
