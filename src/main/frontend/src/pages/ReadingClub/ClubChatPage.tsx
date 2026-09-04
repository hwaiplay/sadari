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
import {formatDateValue} from "@/app/utils/dateUtil";
import {ActionButton} from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import Skeleton from "@/components/Skeleton/Skeleton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import {useClubChatPage} from "@/features/ReadingClub/hooks/useClubChatPage";
import {getReadingDeadline} from "@/features/ReadingClub/utils/readingClubDeadline";
import ProfileImage from "@/features/User/components/ProfileImage";
import type {ChangeEvent, FormEvent} from "react";
import {Fragment, useEffect, useRef} from "react";
import {Link} from "react-router-dom";
import * as styles from "./ClubChatPage.css";

const ClubChatPage = () => {
  const {
    club,
    content,
    isLoading,
    isSending,
    messages,
    pendingContent,
    handleSend,
    setContent,
  } = useClubChatPage();
  const messageEndRef = useRef<HTMLDivElement | null>(null);

  // 새 채팅이 표시되면 최신 메시지가 보이도록 목록 끝으로 이동함
  useEffect(() => {
    messageEndRef.current?.scrollIntoView({block: "end"});
  }, [messages, pendingContent]);

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

  const readingDeadline = getReadingDeadline(club.currentGoalEndt);
  const hasMessages = messages.length > 0 || pendingContent !== null;
  const todayValue = formatDateValue(new Date());
  const todayMessageIndex = messages.findIndex((chat) => chat.regiDate.slice(0, 10) === todayValue);

  return (
    <main className={styles.page}>
      {/* 현재 모임에서 읽는 책 요약 영역 */}
      {club.currentBookTitl ? (
        <Link className={styles.currentBookCard} to={`/reading-clubs/${club.clubNumb}`}>
          <img
            className={styles.currentBookImage}
            src={getBookCoverImageSource(club.currentBookCvim)}
            onError={handleBookCoverImageError}
            alt={club.currentBookTitl}
          />
          <span className={styles.currentBookInformation}>
            <span className={styles.currentBookLabel}>
              {/* "현재 읽는 책" */}
              {message("frontend.readingClub.chat.currentBook")}
            </span>
            <strong className={styles.currentBookTitle}>
              {club.currentBookTitl}
              {readingDeadline ? ` · ${readingDeadline.label}` : null}
            </strong>
          </span>
          <span className={styles.currentBookArrow} aria-hidden="true">›</span>
        </Link>
      ) : (
        <header className={styles.header}>
          <h1 className={styles.title}>
            {/* "{모임명} 채팅" */}
            {message("frontend.readingClub.chat.title", [club.clubName])}
          </h1>
          <p className={styles.description}>
            {/* "모임원들과 독서 이야기를 나눠보세요." */}
            {message("frontend.readingClub.chat.description")}
          </p>
        </header>
      )}

      {/* 모임 채팅 메시지 목록 영역 */}
      <section className={styles.messagePanel} aria-label={message("frontend.readingClub.chat.messageList")}>
        {hasMessages ? (
          <ol className={styles.messageList} aria-live="polite">
            {messages.map((chat, index) => (
              <Fragment key={chat.chatNumb}>
                {index === todayMessageIndex ? (
                  <li className={styles.dateDivider}>
                    {/* "오늘" */}
                    {message("frontend.readingClub.common.deadline.today")}
                  </li>
                ) : null}
                <li className={chat.mineYsno === "Y" ? styles.myMessageRow : styles.messageRow}>
                  {chat.mineYsno === "N" ? (
                    <ProfileImage
                      className={styles.avatar}
                      src={chat.porfPath}
                      alt={chat.userNick ?? message("frontend.readingClub.chat.anonymous")}
                    />
                  ) : null}
                  <div className={chat.mineYsno === "Y" ? styles.myMessageContent : styles.messageContent}>
                    {chat.mineYsno === "N" ? (
                      <span className={styles.sender}>
                        {chat.userNick ?? message("frontend.readingClub.chat.anonymous")}
                      </span>
                    ) : null}
                    <div className={chat.mineYsno === "Y" ? styles.myMessageLine : styles.messageLine}>
                      <div className={chat.mineYsno === "Y" ? styles.myBubble : styles.bubble}>
                        {chat.chatCntn}
                      </div>
                      <span className={chat.mineYsno === "Y" ? styles.myMessageMeta : styles.messageMeta}>
                        {chat.unreadCnt > 0 ? (
                          <>
                            {/* "{0}명 안 읽음" */}
                            <span
                              className={styles.unreadCount}
                              aria-label={message("frontend.readingClub.chat.unreadCount", [chat.unreadCnt])}
                            >
                              {chat.unreadCnt}
                            </span>
                          </>
                        ) : null}
                        <time className={styles.time} dateTime={chat.regiDate}>
                          {chat.regiDate.slice(11, 16)}
                        </time>
                      </span>
                    </div>
                  </div>
                </li>
              </Fragment>
            ))}
            {pendingContent ? (
              <>
                {todayMessageIndex === -1 ? (
                  <li className={styles.dateDivider}>
                    {/* "오늘" */}
                    {message("frontend.readingClub.common.deadline.today")}
                  </li>
                ) : null}
                <li className={styles.myMessageRow}>
                  <div className={styles.myMessageContent}>
                    <div className={styles.myMessageLine}>
                      <div className={styles.myBubble}>{pendingContent}</div>
                      {/* "전송 중" */}
                      <Loading title={message("frontend.readingClub.chat.sending")} isCompact isInline />
                    </div>
                  </div>
                </li>
              </>
            ) : null}
          </ol>
        ) : (
          <p className={styles.empty}>
            {/* "아직 채팅 메시지가 없어요." */}
            {message("frontend.readingClub.chat.empty")}
          </p>
        )}
        <div ref={messageEndRef}/>
      </section>

      {/* 새 채팅 메시지 입력과 전송 영역 */}
      <form className={styles.composer} onSubmit={handleSubmit}>
        <label className={styles.srOnly} htmlFor="club-chat-content">
          {/* "메시지를 입력해 주세요." */}
          {message("frontend.readingClub.chat.placeholder")}
        </label>
        <textarea
          className={styles.input}
          id="club-chat-content"
          value={content}
          maxLength={2000}
          rows={1}
          placeholder={message("frontend.readingClub.chat.placeholder")}
          disabled={isSending}
          onChange={handleContentChange}
        />
        {/* "보내기" */}
        <ActionButton
          className={styles.sendButton}
          type="submit"
          aria-label={message("frontend.readingClub.chat.send")}
          disabled={isSending || !content.trim()}
          icon={<span className={styles.sendIcon}>↑</span>}
        />
      </form>
    </main>
  );
};

export default ClubChatPage;
