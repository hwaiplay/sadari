/**
 * fileName       : useClubChatPage
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 활성 모임원의 채팅 조회와 전송 상태를 관리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
import {getApiErrorMessage} from "@/app/api/resultData";
import {sweetError} from "@/app/lib/sweetAlert/sweetAlert";
import {message} from "@/app/messages/message";
import {
  createClubChatApi,
  getClubChatListApi,
  getClubDtlApi,
  type ClubChatMessage,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import {useCallback, useEffect, useRef, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";

// 새 채팅을 확인하는 간격
const CHAT_POLL_INTERVAL_MS = 3000;

/** 모임 채팅 화면의 조회와 전송 상태를 제공함. @author HanWon.Jang */
export const useClubChatPage = () => {
  const navigate = useNavigate();
  const {clubNumb: clubNumbParam} = useParams<{clubNumb: string}>();
  const clubNumb = Number(clubNumbParam);
  const [club, setClub] = useState<ReadingClub | null>(null);
  const [messages, setMessages] = useState<ClubChatMessage[]>([]);
  const [content, setContent] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const lastChatNumbRef = useRef<number | undefined>(undefined);
  const pollingRef = useRef(false);

  /** 새 채팅을 기존 목록에 중복 없이 추가함. @author HanWon.Jang */
  const appendMessages = useCallback((nextMessages: ClubChatMessage[]): void => {
    if (!nextMessages.length) {
      return;
    }

    setMessages((currentMessages) => {
      const knownChatNumbs = new Set(currentMessages.map((item) => item.chatNumb));
      return [
        ...currentMessages,
        ...nextMessages.filter((item) => !knownChatNumbs.has(item.chatNumb)),
      ].sort((left, right) => left.chatNumb - right.chatNumb);
    });
    lastChatNumbRef.current = Math.max(...nextMessages.map((item) => item.chatNumb));
  }, []);

  /** 마지막 조회 다음 채팅을 불러옴. @author HanWon.Jang */
  const loadNewMessages = useCallback(async (): Promise<void> => {
    if (pollingRef.current || !Number.isFinite(clubNumb) || clubNumb <= 0) {
      return;
    }

    pollingRef.current = true;
    try {
      appendMessages(await getClubChatListApi(clubNumb, lastChatNumbRef.current));
    } finally {
      pollingRef.current = false;
    }
  }, [appendMessages, clubNumb]);

  // 활성 모임원 권한과 최초 채팅 목록을 확인한 뒤 주기 조회를 시작함
  useEffect(() => {
    if (!Number.isFinite(clubNumb) || clubNumb <= 0) {
      navigate("/reading-clubs/mine", {replace: true});
      return;
    }

    let active = true;
    setIsLoading(true);
    void Promise.all([getClubDtlApi(clubNumb), getClubChatListApi(clubNumb)])
      .then(([nextClub, nextMessages]) => {
        if (!active) {
          return;
        }
        setClub(nextClub);
        appendMessages(nextMessages);
      })
      .catch((error: unknown) => {
        void sweetError(
          message("frontend.readingClub.chat.loadErrorTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        ).then(() => navigate(`/reading-clubs/${clubNumb}`, {replace: true}));
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    const pollTimer = window.setInterval(() => {
      void loadNewMessages().catch(() => undefined);
    }, CHAT_POLL_INTERVAL_MS);

    return () => {
      active = false;
      window.clearInterval(pollTimer);
    };
  }, [appendMessages, clubNumb, loadNewMessages, navigate]);

  /** 입력한 채팅을 한 번만 전송함. @author HanWon.Jang */
  const handleSend = async (): Promise<void> => {
    const normalizedContent = content.trim();
    if (!normalizedContent || isSending) {
      return;
    }

    setIsSending(true);
    try {
      const savedMessage = await createClubChatApi(
        clubNumb,
        normalizedContent,
        crypto.randomUUID(),
      );
      appendMessages([savedMessage]);
      setContent("");
    } catch (error) {
      void sweetError(
        message("frontend.readingClub.chat.sendErrorTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsSending(false);
    }
  };

  return {
    club,
    content,
    isLoading,
    isSending,
    messages,
    handleSend,
    setContent,
  };
};
