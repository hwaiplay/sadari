import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import {
  getUserInterestCatalogApi,
  getMyProfileApi,
  updateUserInterestsApi,
  updateOnboardingApi,
  type UserInterest,
  type UserProfile,
} from "@/features/User/api/userApi";
import { useQueryClient } from "@tanstack/react-query";
import type {
  ChangeEvent,
  FormEvent,
  MouseEvent,
  TouchEvent,
} from "react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./WelcomePage.css";

const LAST_SLIDE_INDEX = 3;
const SLIDE_COUNT = 4;
const USER_NICK_MAX_LENGTH = 25;
const SWIPE_THRESHOLD_PX = 48;
const USER_NICK_REGEX = /^[A-Za-z0-9\uAC00-\uD7A3]+(?:[ _-][A-Za-z0-9\uAC00-\uD7A3]+)*$/;
const USER_NICK_INPUT_REGEX = /[^A-Za-z0-9\uAC00-\uD7A3\u3131-\u318E\u1100-\u11FF\uA960-\uA97F\uD7B0-\uD7FF _-]/g;

/**
 * 최초 로그인 사용자에게 서비스 특징과 닉네임 설정 흐름을 슬라이드로 제공한다
 *
 * @author HanWon.Jang
 * @return 최초 로그인 웰컴 페이지
 */
function WelcomePage() {

  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const touchStartXRef = useRef<number | null>(null);
  const [activeSlide, setActiveSlide] = useState(0);
  const [userNick, setUserNick] = useState("");
  const [interestCatalog, setInterestCatalog] = useState<UserInterest[]>([]);
  const [selectedInterestKeys, setSelectedInterestKeys] = useState<Set<string>>(new Set());
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {

    let ignore = false;

    // 가입 시 서버가 발급한 고유 랜덤 닉네임을 마지막 슬라이드의 기본값으로 조회한다
    getMyProfileApi()
      .then((response) => {

        // 화면을 떠난 뒤 도착한 프로필 응답은 입력 상태에 반영하지 않는다
        if (!ignore) {
          const profile = response.data as UserProfile;
          // 서버가 발급한 닉네임을 사용자가 바로 확정하거나 수정할 수 있도록 입력값에 설정한다
          setUserNick(profile.userNick ?? "");
        }
      })
      .catch((error) => {

        // 화면이 유지되는 동안 발생한 조회 실패만 사용자에게 안내한다
        if (!ignore) {
          void sweetError(
            /* "조회에 실패했습니다." */ message("frontend.alert.loadFailedTitle"),
            getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
          );
        }
      })
      .finally(() => {

        // 화면이 유지되는 동안에만 프로필 로딩 상태를 종료한다
        if (!ignore) {
          // 닉네임 입력 화면을 사용할 수 있도록 로딩 상태를 해제한다
          setIsLoading(false);
        }
      });

    // 관심분야 조회 실패가 웰컴 완료를 막지 않도록 프로필과 별도 흐름으로 조회한다
    getUserInterestCatalogApi()
      .then((catalog) => {
        // 화면을 떠난 뒤 도착한 관심분야 응답은 선택 상태에 반영하지 않는다
        if (!ignore) {
          // 활성 공통코드로 구성된 관심분야를 선택 목록에 설정한다
          setInterestCatalog(catalog);
        }
      })
      .catch(() => {
        // 공통코드 조회 실패 시 관심분야 단계를 건너뛰고 기존 웰컴 흐름을 유지한다
        if (!ignore) {
          // 선택할 수 없는 빈 관심분야 목록을 명시적으로 설정한다
          setInterestCatalog([]);
        }
      });

    // 화면 전환 뒤 도착한 프로필 응답이 상태를 변경하지 않도록 정리한다
    return () => {

      ignore = true;
    };
  }, []);

  /**
   * 허용하지 않은 닉네임 문자를 제거하고 최대 입력 길이를 제한한다
   *
   * @author HanWon.Jang
   * @param event 닉네임 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleUserNickChange = (event: ChangeEvent<HTMLInputElement>): void => {

    // 한글 조합 중간값은 유지하면서 서버와 같은 허용 문자 범위로 입력값을 제한한다
    const normalizedValue = event.target.value
      .replace(USER_NICK_INPUT_REGEX, "")
      .slice(0, USER_NICK_MAX_LENGTH);
    // 정규화한 닉네임을 최종 슬라이드 입력 상태에 반영한다
    setUserNick(normalizedValue);
  };

  /**
   * 현재 슬라이드에서 다음 소개 또는 닉네임 설정 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleNext = (): void => {

    // 마지막 슬라이드 이전에서만 오른쪽 슬라이드로 이동한다
    if (activeSlide < LAST_SLIDE_INDEX) {
      // 현재 위치보다 한 단계 뒤의 소개 화면을 선택한다
      setActiveSlide((currentSlide) => currentSlide + 1);
    }
  };

  /**
   * 현재 슬라이드에서 이전 소개 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handlePrevious = (): void => {

    // 첫 슬라이드 이후에서만 왼쪽 슬라이드로 이동한다
    if (activeSlide > 0) {
      // 현재 위치보다 한 단계 앞의 소개 화면을 선택한다
      setActiveSlide((currentSlide) => currentSlide - 1);
    }
  };

  /**
   * 진행 표시 버튼의 데이터 값으로 사용자가 선택한 슬라이드로 이동한다
   *
   * @author HanWon.Jang
   * @param event 슬라이드 진행 표시 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleSlideSelect = (event: MouseEvent<HTMLButtonElement>): void => {

    // 버튼에 지정한 슬라이드 번호를 숫자로 변환한다
    const nextSlide = Number(event.currentTarget.dataset.slide);

    // 정의된 슬라이드 범위의 값만 화면 상태로 사용한다
    if (Number.isInteger(nextSlide) && nextSlide >= 0 && nextSlide < SLIDE_COUNT) {
      // 사용자가 누른 진행 표시 위치로 슬라이드를 이동한다
      setActiveSlide(nextSlide);
    }
  };

  /**
   * 모바일 좌우 넘김 방향을 계산할 수 있도록 터치 시작 위치를 저장한다
   *
   * @author HanWon.Jang
   * @param event 슬라이드 터치 시작 이벤트
   * @return 반환값이 없다
   */
  const handleTouchStart = (event: TouchEvent<HTMLDivElement>): void => {

    // 첫 번째 터치 지점을 좌우 넘김 시작 좌표로 저장한다
    touchStartXRef.current = event.touches[0]?.clientX ?? null;
  };

  /**
   * 터치 이동 거리가 기준을 넘으면 좌우 방향에 맞는 슬라이드로 이동한다
   *
   * @author HanWon.Jang
   * @param event 슬라이드 터치 종료 이벤트
   * @return 반환값이 없다
   */
  const handleTouchEnd = (event: TouchEvent<HTMLDivElement>): void => {

    const touchStartX = touchStartXRef.current;
    const touchEndX = event.changedTouches[0]?.clientX;
    // 다음 터치가 이전 좌표를 재사용하지 않도록 시작 지점을 비운다
    touchStartXRef.current = null;

    // 시작점이나 종료점이 없으면 넘김 방향을 계산하지 않는다
    if (touchStartX === null || touchEndX === undefined) {
      // 좌표가 불완전한 터치 이벤트 처리를 종료한다
      return;
    }

    const distance = touchStartX - touchEndX;

    // 왼쪽으로 충분히 밀었으면 다음 소개 화면을 연다
    if (distance > SWIPE_THRESHOLD_PX) {
      // 오른쪽에 있는 다음 슬라이드로 이동한다
      handleNext();
      // 한 번의 터치가 두 방향으로 처리되지 않도록 종료한다
      return;
    }

    // 오른쪽으로 충분히 밀었으면 이전 소개 화면을 연다
    if (distance < -SWIPE_THRESHOLD_PX) {
      // 왼쪽에 있는 이전 슬라이드로 이동한다
      handlePrevious();
    }
  };

  /**
   * 최초 로그인 사용자가 누른 독서 관심분야의 선택 상태를 전환한다
   *
   * @author SeungHyeon.Kang
   * @param event 관심분야 선택 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleInterestToggle = (event: MouseEvent<HTMLButtonElement>): void => {
    const interestKey = event.currentTarget.dataset.interestKey;

    // 화면에 존재하는 관심분야 키만 선택 상태에 반영한다
    if (!interestKey) {
      // 유효한 관심분야가 없는 클릭 처리를 종료한다
      return;
    }

    // 기존 Set을 직접 변경하지 않도록 새 선택 집합을 생성한다
    setSelectedInterestKeys((currentKeys) => {
      const nextKeys = new Set(currentKeys);
      // 이미 선택한 관심분야는 다시 눌렀을 때 해제한다
      if (nextKeys.has(interestKey)) {
        // 선택 집합에서 현재 관심분야를 제거한다
        nextKeys.delete(interestKey);
      } else {
        // 새 관심분야를 개수 제한 없이 선택 집합에 추가한다
        nextKeys.add(interestKey);
      }

      // 변경된 관심분야 선택 집합을 반환한다
      return nextKeys;
    });
  };

  /**
   * 사용자가 확정한 닉네임을 저장하고 최초 로그인 웰컴 흐름을 완료한다
   *
   * @author HanWon.Jang
   * @param event 닉네임 설정 폼 제출 이벤트
   * @return 온보딩 완료 처리 Promise
   */
  const handleStart = async (event: FormEvent<HTMLFormElement>): Promise<void> => {

    event.preventDefault();
    const normalizedUserNick = userNick.trim();

    // 닉네임이 비어 있으면 서버 요청 전에 입력 위치를 안내한다
    if (!normalizedUserNick) {
      void sweetWarning(
        /* "입력이 필요합니다." */ message("frontend.alert.inputRequired"),
        /* "닉네임을 입력해주세요." */ message("frontend.profile.nickRequired"),
      );
      // 비어 있는 닉네임 저장 요청을 중단한다
      return;
    }

    // 서버 정책과 다른 길이 또는 문자 조합이면 저장 전에 입력 규칙을 안내한다
    if (normalizedUserNick.length > USER_NICK_MAX_LENGTH || !USER_NICK_REGEX.test(normalizedUserNick)) {
      void sweetWarning(
        /* "입력이 필요합니다." */ message("frontend.alert.inputRequired"),
        /* "닉네임은 한글, 영문, 숫자와 문자 사이의 공백, 언더바, 하이픈을 한 칸씩 사용해 25자 이하로 입력해주세요." */ message("frontend.profile.nickFormat"),
      );
      // 형식이 맞지 않는 닉네임 저장 요청을 중단한다
      return;
    }

    // 저장 중 버튼을 다시 눌러 동일 요청이 중복되지 않도록 차단한다
    if (isSaving) {
      // 진행 중인 저장 요청이 끝날 때까지 추가 제출을 중단한다
      return;
    }

    // 시작하기 버튼과 닉네임 입력을 저장 완료까지 비활성화한다
    setIsSaving(true);

    // 닉네임 저장과 인증 상태 갱신 실패를 하나의 사용자 오류 흐름으로 처리한다
    try {
      // 관심분야를 선택했다면 온보딩 완료 전에 별도 API로 저장을 시도한다
      if (selectedInterestKeys.size > 0) {
        const interestList = interestCatalog
          .filter((interest) => selectedInterestKeys.has(`${interest.intrCgrp}:${interest.intrCode}`))
          .map((interest) => ({ intrCgrp: interest.intrCgrp, intrCode: interest.intrCode }));

        // 관심분야 저장 실패와 ONBD_YSNO 완료 처리를 분리하기 위한 블록이다
        try {
          // 유효한 공통코드 조합으로 사용자의 관심분야를 전체 교체한다
          await updateUserInterestsApi({ interestList });
        }

        // 관심분야 저장 실패는 웰컴 화면을 본 사용자의 온보딩 완료를 막지 않는다
        catch {
          // 저장 실패 시 별도 상태를 만들지 않고 닉네임과 온보딩 완료 처리를 계속한다
        }
      }

      // 닉네임과 최초 로그인 완료 상태를 서버에 함께 저장한다
      await updateOnboardingApi({ userNick: normalizedUserNick });
      // 보호 라우트가 완료된 온보딩 상태를 사용하도록 인증 정보를 즉시 다시 조회한다
      await queryClient.refetchQueries({ queryKey: ["auth"], type: "active" });
      // 웰컴 화면을 방문 이력에 남기지 않고 서비스 홈으로 이동한다
      navigate("/home", { replace: true });
    }

    // 중복 닉네임이나 비속어를 포함한 서버 검증 메시지를 사용자에게 안내한다
    catch (error) {
      void sweetError(
        /* "수정에 실패했습니다." */ message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    }

    // 성공과 실패 모두에서 사용자가 다시 입력할 수 있도록 저장 상태를 해제한다
    finally {
      // 온보딩 저장 요청이 끝났음을 버튼 상태에 반영한다
      setIsSaving(false);
    }
  };

  // 가입 시 발급된 닉네임을 조회하는 동안 공통 로딩 화면을 반환한다
  if (isLoading) {
    return <Loading title={/* "로그인 중" */ message("frontend.common.loginLoading")} />;
  }

  // 서비스 소개와 닉네임 설정을 한 화면에서 넘겨보는 웰컴 페이지를 반환한다
  return (
    <main className={styles.page}>
      {/* 웰컴 화면 상단 브랜드와 현재 진행 상태 영역 */}
      <header className={styles.header}>
        <img
          className={styles.logo}
          src="/img/common/logo-upper.svg"
          alt={/* "사다리 로고" */ message("frontend.common.logoAlt")}
        />
        <p className={styles.progressText} aria-live="polite">
          {/* "{0} / {1}" */}
          {message("frontend.welcome.progress", [activeSlide + 1, SLIDE_COUNT])}
        </p>
      </header>

      {/* 좌우 터치와 버튼으로 전환하는 서비스 소개 슬라이드 영역 */}
      <div
        className={styles.viewport}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
      >
        <div
          className={styles.track}
          style={{ transform: `translate3d(-${activeSlide * 25}%, 0, 0)` }}
        >
          {/* 도서 표지 대표색과 자동 책장 색상 소개 영역 */}
          <section className={styles.slide} aria-hidden={activeSlide !== 0}>
            <div className={styles.copy}>
              <p className={styles.eyebrow}>
                {/* "표지에서 시작하는 책장" */}
                {message("frontend.welcome.cover.eyebrow")}
              </p>
              <h1 className={styles.title}>
                {/* "책 표지의 분위기를\n내 책장 색으로" */}
                {message("frontend.welcome.cover.title")}
              </h1>
              <p className={styles.description}>
                {/* "표지의 대표색을 분석해 가장 가까운 책장 색을 자동으로 추천해요." */}
                {message("frontend.welcome.cover.description")}
              </p>
            </div>
            {/* 표지 색상 분석과 책장 팔레트 시각화 영역 */}
            <div className={styles.coverVisual} aria-hidden="true">
              <div className={styles.coverBookBack} />
              <div className={styles.coverBookMain}>
                <span className={styles.coverBookLine} />
                <span className={styles.coverBookLineShort} />
              </div>
              <div className={styles.paletteCard}>
                <span className={styles.paletteLabel}>
                  {/* "책장 색 자동 추천" */}
                  {message("frontend.welcome.cover.palette")}
                </span>
                <span className={styles.swatchCoral} />
                <span className={styles.swatchGold} />
                <span className={styles.swatchTeal} />
                <span className={styles.swatchNavy} />
              </div>
            </div>
          </section>

          {/* 독서 목표와 달력 기록 소개 영역 */}
          <section className={styles.slide} aria-hidden={activeSlide !== 1}>
            <div className={styles.copy}>
              <p className={styles.eyebrow}>
                {/* "기록이 쌓이는 즐거움" */}
                {message("frontend.welcome.goal.eyebrow")}
              </p>
              <h1 className={styles.title}>
                {/* "오늘의 한 페이지가\n한 달의 흐름이 되도록" */}
                {message("frontend.welcome.goal.title")}
              </h1>
              <p className={styles.description}>
                {/* "주간·월간·연간 목표와 독서 달력으로 나만의 읽는 속도를 확인해요." */}
                {message("frontend.welcome.goal.description")}
              </p>
            </div>
            {/* 독서 달력과 목표 달성률 시각화 영역 */}
            <div className={styles.goalVisual} aria-hidden="true">
              <div className={styles.calendarCard}>
                <div className={styles.calendarHeader}>
                  <span />
                  <span />
                </div>
                <div className={styles.calendarGrid}>
                  <i />
                  <i />
                  <i className={styles.calendarActiveCoral} />
                  <i />
                  <i className={styles.calendarActiveTeal} />
                  <i />
                  <i />
                  <i className={styles.calendarActiveGold} />
                  <i />
                  <i />
                  <i />
                  <i className={styles.calendarActiveNavy} />
                  <i />
                  <i />
                </div>
              </div>
              <div className={styles.goalCard}>
                <span className={styles.goalCaption}>
                  {/* "이번 달 독서 목표" */}
                  {message("frontend.welcome.goal.caption")}
                </span>
                <strong className={styles.goalRate}>
                  {/* "67%" */}
                  {message("frontend.welcome.goal.rate")}
                </strong>
                <span className={styles.goalBar}>
                  <span />
                </span>
              </div>
            </div>
          </section>

          {/* 공개 독후감과 소셜 알림 소개 영역 */}
          <section className={styles.slide} aria-hidden={activeSlide !== 2}>
            <div className={styles.copy}>
              <p className={styles.eyebrow}>
                {/* "혼자 읽고, 함께 발견하기" */}
                {message("frontend.welcome.social.eyebrow")}
              </p>
              <h1 className={styles.title}>
                {/* "좋았던 기록은 나누고\n새로운 책은 발견하고" */}
                {message("frontend.welcome.social.title")}
              </h1>
              <p className={styles.description}>
                {/* "공개 독후감에서 취향이 맞는 사람을 팔로우하고 좋아요와 알림으로 이어져요." */}
                {message("frontend.welcome.social.description")}
              </p>
            </div>
            {/* 공개 기록 카드와 팔로우 알림 시각화 영역 */}
            <div className={styles.socialVisual} aria-hidden="true">
              <div className={styles.reportCard}>
                <span className={styles.reportCover} />
                <span className={styles.reportLines}>
                  <i />
                  <i />
                  <i />
                </span>
                <span className={styles.reportHeart}>♡</span>
              </div>
              <div className={styles.profileBubbleLeft}>S</div>
              <div className={styles.profileBubbleRight}>R</div>
              <div className={styles.notificationCard}>
                <span className={styles.notificationDot} />
                <span>
                  {/* "좋아요·팔로우 알림" */}
                  {message("frontend.welcome.social.notification")}
                </span>
              </div>
            </div>
          </section>

          {/* 랜덤 추천 닉네임 확인과 수정 영역 */}
          <section className={styles.slide} aria-hidden={activeSlide !== LAST_SLIDE_INDEX}>
            <div className={styles.copy}>
              <p className={styles.eyebrow}>
                {/* "마지막으로, 당신의 이름" */}
                {message("frontend.welcome.nickname.eyebrow")}
              </p>
              <h1 className={styles.title}>
                {/* "이 닉네임으로\n첫 장을 시작할까요?" */}
                {message("frontend.welcome.nickname.title")}
              </h1>
              <p className={styles.description}>
                {/* "Sadari가 만든 랜덤 닉네임이에요. 마음에 드는 이름으로 자유롭게 바꿀 수 있어요." */}
                {message("frontend.welcome.nickname.description")}
              </p>
            </div>
            {/* 추천 닉네임 입력과 글자 수 안내 영역 */}
            <form className={styles.nicknameCard} onSubmit={handleStart}>
              {/* 최초 로그인 독서 관심분야 선택 영역 */}
              {interestCatalog.length > 0 ? (
                <section className={styles.interestSection}>
                  <h2 className={styles.interestTitle}>
                    {/* "관심분야 선택" */}
                    {message("frontend.welcome.interest.title")}
                  </h2>
                  <p className={styles.interestHint}>
                    {/* "좋아하는 분야를 골라주세요. 선택하지 않고 시작할 수도 있어요." */}
                    {message("frontend.welcome.interest.hint")}
                  </p>
                  {/* 독서 관심분야 선택 항목 목록 */}
                  <div className={styles.interestList}>
                    {interestCatalog.map((interest) => {
                      const interestKey = `${interest.intrCgrp}:${interest.intrCode}`;
                      const isSelected = selectedInterestKeys.has(interestKey);
                      // 대분류와 세부코드를 구분할 수 있는 관심분야 버튼을 반환한다
                      return (
                        <button
                          className={isSelected ? styles.interestButtonSelected : styles.interestButton}
                          type="button"
                          data-interest-key={interestKey}
                          aria-pressed={isSelected}
                          onClick={handleInterestToggle}
                          key={interestKey}
                        >
                          {interest.intrCnam} · {interest.intrName}
                        </button>
                      );
                    })}
                  </div>
                </section>
              ) : null}
              {/* 최초 로그인 닉네임 입력 영역 */}
              <label className={styles.nicknameLabel} htmlFor="welcome-user-nick">
                {/* "닉네임" */}
                {message("frontend.profile.nick")}
              </label>
              <div className={styles.nicknameInputWrap}>
                <input
                  id="welcome-user-nick"
                  className={styles.nicknameInput}
                  type="text"
                  value={userNick}
                  maxLength={USER_NICK_MAX_LENGTH}
                  autoComplete="nickname"
                  disabled={isSaving}
                  onChange={handleUserNickChange}
                />
                <span className={styles.nickLength}>
                  {userNick.length}/{USER_NICK_MAX_LENGTH}
                </span>
              </div>
              <p className={styles.nicknameHint}>
                {/* "한글, 영문, 숫자와 한 칸의 공백·언더바·하이픈을 사용할 수 있어요." */}
                {message("frontend.welcome.nickname.hint")}
              </p>
              <button className={styles.startButton} type="submit" disabled={isSaving}>
                {/* "시작하기" / "저장 중" */}
                {isSaving
                  ? message("frontend.common.saving")
                  : message("frontend.welcome.start")}
              </button>
            </form>
          </section>
        </div>
      </div>

      {/* 슬라이드 진행 위치와 이전 및 다음 이동 버튼 영역 */}
      <footer className={styles.footer}>
        <div
          className={styles.dots}
          role="group"
          aria-label={/* "웰컴 화면 이동" */ message("frontend.welcome.navigation")}
        >
          <button
            className={activeSlide === 0 ? styles.dotActive : styles.dot}
            type="button"
            data-slide="0"
            aria-label={message("frontend.welcome.slideButton", [1])}
            aria-current={activeSlide === 0 ? "step" : undefined}
            onClick={handleSlideSelect}
          />
          <button
            className={activeSlide === 1 ? styles.dotActive : styles.dot}
            type="button"
            data-slide="1"
            aria-label={message("frontend.welcome.slideButton", [2])}
            aria-current={activeSlide === 1 ? "step" : undefined}
            onClick={handleSlideSelect}
          />
          <button
            className={activeSlide === 2 ? styles.dotActive : styles.dot}
            type="button"
            data-slide="2"
            aria-label={message("frontend.welcome.slideButton", [3])}
            aria-current={activeSlide === 2 ? "step" : undefined}
            onClick={handleSlideSelect}
          />
          <button
            className={activeSlide === LAST_SLIDE_INDEX ? styles.dotActive : styles.dot}
            type="button"
            data-slide="3"
            aria-label={message("frontend.welcome.slideButton", [4])}
            aria-current={activeSlide === LAST_SLIDE_INDEX ? "step" : undefined}
            onClick={handleSlideSelect}
          />
        </div>
        <div className={styles.navigationButtons}>
          <button
            className={styles.previousButton}
            type="button"
            disabled={activeSlide === 0}
            onClick={handlePrevious}
          >
            {/* "이전" */}
            {message("frontend.welcome.previous")}
          </button>
          <button
            className={styles.nextButton}
            type="button"
            disabled={activeSlide === LAST_SLIDE_INDEX}
            onClick={handleNext}
          >
            {/* "다음" */}
            {message("frontend.welcome.next")}
          </button>
        </div>
      </footer>
    </main>
  );
}

export default WelcomePage;
