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
  uptClubChatReadApi,
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
  const [pendingContent, setPendingContent] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const readChatNumbRef = useRef(0);
  const pollingRef = useRef(false);

  /** 새 채팅과 변경된 안 읽은 수를 기존 목록에 병합함. @author HanWon.Jang */
  const mergeMessages = useCallback((nextMessages: ClubChatMessage[]): void => {
    if (!nextMessages.length) {
      return;
    }

    setMessages((currentMessages) => {
      const messageMap = new Map<number, ClubChatMessage>();
      for (const chat of currentMessages) {
        messageMap.set(chat.chatNumb, chat);
      }

      for (const chat of nextMessages) {
        messageMap.set(chat.chatNumb, chat);
      }

      return Array.from(messageMap.values()).sort((left, right) => left.chatNumb - right.chatNumb);
    });
  }, []);

  /** 화면에 표시한 최신 채팅까지 읽음 위치를 한 번만 갱신함. @author HanWon.Jang */
  const uptLatestRead = useCallback(async (nextMessages: ClubChatMessage[]): Promise<void> => {
    const latestMessage = nextMessages[nextMessages.length - 1];
    if (!latestMessage || document.visibilityState !== "visible"
        || latestMessage.chatNumb <= readChatNumbRef.current) {
      return;
    }

    await uptClubChatReadApi(clubNumb, latestMessage.chatNumb);
    readChatNumbRef.current = latestMessage.chatNumb;
  }, [clubNumb]);

  /** 마지막 조회 다음 채팅을 불러옴. @author HanWon.Jang */
  const loadNewMessages = useCallback(async (): Promise<void> => {
    if (pollingRef.current || !Number.isFinite(clubNumb) || clubNumb <= 0) {
      return;
    }

    pollingRef.current = true;
    try {
      const nextMessages = await getClubChatListApi(clubNumb);
      mergeMessages(nextMessages);
      await uptLatestRead(nextMessages);
    } finally {
      pollingRef.current = false;
    }
  }, [clubNumb, mergeMessages, uptLatestRead]);

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
        mergeMessages(nextMessages);
        void uptLatestRead(nextMessages).catch(() => undefined);
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

    /** 숨겨졌던 채팅 화면이 다시 보이면 최신 메시지와 읽음 수를 즉시 동기화함 */
    const handleVisibilityChange = (): void => {
      if (document.visibilityState === "visible") {
        void loadNewMessages().catch(() => undefined);
      }
    };
    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      active = false;
      window.clearInterval(pollTimer);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [clubNumb, loadNewMessages, mergeMessages, navigate, uptLatestRead]);

  /** 입력한 채팅을 한 번만 전송함. @author HanWon.Jang */
  const handleSend = async (): Promise<void> => {
    const normalizedContent = content.trim();
    if (!normalizedContent || isSending) {
      return;
    }

    setIsSending(true);
    setPendingContent(normalizedContent);
    setContent("");
    try {
      const savedMessage = await createClubChatApi(
        clubNumb,
        normalizedContent,
        crypto.randomUUID(),
      );
      setPendingContent(null);
      mergeMessages([savedMessage]);
    } catch (error) {
      setPendingContent(null);
      setContent(normalizedContent);
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
    pendingContent,
    handleSend,
    setContent,
  };
};
