/**
 * fileName       : ClubChatPage
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 활성 모임원이 대화하는 모임 채팅 화면을 구성함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
import {message} from "@/app/messages/message";
import {ActionButton} from "@/components/Button/ActionButton";
import Skeleton from "@/components/Skeleton/Skeleton";
import {useClubChatPage} from "@/features/ReadingClub/hooks/useClubChatPage";
import ProfileImage from "@/features/User/components/ProfileImage";
import type {ChangeEvent, FormEvent} from "react";
import {useEffect, useRef} from "react";
import * as styles from "./ClubChatPage.css";

const ClubChatPage = () => {
  const {club, content, isLoading, isSending, messages, handleSend, setContent} = useClubChatPage();
  const messageEndRef = useRef<HTMLDivElement | null>(null);

  // 새 채팅이 표시되면 최신 메시지가 보이도록 목록 끝으로 이동함
  useEffect(() => {
    messageEndRef.current?.scrollIntoView({block: "end"});
  }, [messages]);

  /** 채팅 입력값을 상태에 반영함. @author HanWon.Jang */
  const handleContentChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {
    setContent(event.currentTarget.value);
  };

  /** 폼 제출 시 현재 채팅을 전송함. @author HanWon.Jang */
  const handleSubmit = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();
    void handleSend();
  };

  if (isLoading || !club) {
    return (
      <main className={styles.page} aria-busy="true">
        <Skeleton width="100%" height={68} borderRadius={18}/>
        <Skeleton width="100%" height={360} borderRadius={18}/>
      </main>
    );
  }

  return (
    <main className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>{message("frontend.readingClub.chat.title", [club.clubName])}</h1>
        <p className={styles.description}>{message("frontend.readingClub.chat.description")}</p>
      </header>

      <section className={styles.messagePanel} aria-label={message("frontend.readingClub.chat.messageList")}>
        {messages.length ? (
          <ol className={styles.messageList} aria-live="polite">
            {messages.map((chat) => (
              <li className={chat.mineYsno === "Y" ? styles.myMessageRow : styles.messageRow} key={chat.chatNumb}>
                {chat.mineYsno === "N" ? (
                  <ProfileImage
                    className={styles.avatar}
                    src={chat.porfPath}
                    alt={chat.userNick ?? message("frontend.readingClub.chat.anonymous")}
                  />
                ) : null}
                <div className={styles.messageContent}>
                  <span className={styles.sender}>
                    {chat.mineYsno === "Y"
                      ? message("frontend.readingClub.chat.me")
                      : chat.userNick ?? message("frontend.readingClub.chat.anonymous")}
                  </span>
                  <div className={chat.mineYsno === "Y" ? styles.myBubble : styles.bubble}>
                    {chat.chatCntn}
                  </div>
                  <time className={styles.time} dateTime={chat.regiDate}>
                    {chat.regiDate.replace("T", " ").slice(0, 16)}
                  </time>
                </div>
              </li>
            ))}
          </ol>
        ) : (
          <p className={styles.empty}>{message("frontend.readingClub.chat.empty")}</p>
        )}
        <div ref={messageEndRef}/>
      </section>

      <form className={styles.composer} onSubmit={handleSubmit}>
        <label className={styles.srOnly} htmlFor="club-chat-content">
          {message("frontend.readingClub.chat.placeholder")}
        </label>
        <textarea
          className={styles.input}
          id="club-chat-content"
          value={content}
          maxLength={2000}
          rows={2}
          placeholder={message("frontend.readingClub.chat.placeholder")}
          disabled={isSending}
          onChange={handleContentChange}
        />
        <ActionButton type="submit" disabled={isSending || !content.trim()}>
          {isSending
            ? message("frontend.readingClub.chat.sending")
            : message("frontend.readingClub.chat.send")}
        </ActionButton>
      </form>
    </main>
  );
};

export default ClubChatPage;
