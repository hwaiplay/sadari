import { message } from "@/app/messages/message";
import { sweetError, sweetSuccess, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import Loading from "@/components/Loading/Loading";
import {
  getMyProfileApi,
  updateMyProfileApi,
  type UserProfile,
} from "@/features/User/api/userApi";
import { notifyUserProfileUpdated } from "@/features/User/lib/profileEvents";
import type { FormEvent, MouseEvent } from "react";
import { useEffect, useState } from "react";
import * as styles from "./ProfileEditPage.css";

const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";
const USER_NICK_MAX_LENGTH = 10;
const PROFILE_INTRO_MAX_LENGTH = 50;
const KOREAN_NICK_REGEX = /^[ㄱ-ㅎㅏ-ㅣ가-힣]+$/;

/**
 * 닉네임 입력값에서 한글이 아닌 문자를 제거하고 최대 입력 길이를 제한한다.
 * 사용자가 영문, 숫자, 특수문자를 붙여넣어도 저장 가능한 한글 닉네임 형식만 상태에 남긴다.
 * @Author Hanwon.Jang
 * @param value 사용자가 입력한 닉네임 원문
 * @return 한글 10자 이하로 정리된 닉네임
 */
const normalizeKoreanNick = (value: string) =>
  value.replace(/[^ㄱ-ㅎㅏ-ㅣ가-힣]/g, "").slice(0, USER_NICK_MAX_LENGTH);

/**
 * 한줄 소개 입력값을 허용 길이 이하로 제한한다.
 * textarea의 maxLength와 별개로 붙여넣기나 브라우저별 입력 차이를 한 번 더 방어한다.
 * @Author Hanwon.Jang
 * @param value 사용자가 입력한 한줄 소개 원문
 * @return 50자 이하로 제한된 한줄 소개
 */
const normalizeProfileIntro = (value: string) =>
  value.slice(0, PROFILE_INTRO_MAX_LENGTH);

/**
 * 로그인 사용자의 프로필 사진, 배경 사진, 닉네임, 한줄 소개를 조회하고 수정한다.
 * 수정 모드에서는 화면을 전환하지 않고 기존 요소 위치에서 텍스트와 이미지만 편집할 수 있게 제공한다.
 * @Author Hanwon.Jang
 * @return 프로필 상세 및 수정 페이지 컴포넌트
 */
function ProfileEditPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isEditMode, setIsEditMode] = useState(false);
  const [userNick, setUserNick] = useState("");
  const [intrCntn, setIntrCntn] = useState("");
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [backgroundImage, setBackgroundImage] = useState<File | null>(null);
  const [previewImage, setPreviewImage] = useState(DEFAULT_PROFILE_IMAGE);
  const [previewBackground, setPreviewBackground] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  /**
   * 서버에서 받은 프로필 값을 화면 상태와 이미지 미리보기 상태에 함께 반영한다.
   * 저장 완료 후 파일 선택 상태를 비워 같은 파일이 다시 선택될 때도 정상적으로 반응하게 만든다.
   * @Author Hanwon.Jang
   * @param nextProfile 서버에서 조회하거나 저장 후 반환된 사용자 프로필 정보
   * @return
   */
  const syncProfileState = (nextProfile: UserProfile) => {
    setProfile(nextProfile);
    setUserNick(nextProfile?.userNick ?? "");
    setIntrCntn(nextProfile?.intrCntn ?? "");
    setPreviewImage(nextProfile?.porfPath || DEFAULT_PROFILE_IMAGE);
    setPreviewBackground(nextProfile?.bgimPath || "");
    setProfileImage(null);
    setBackgroundImage(null);
  };

  useEffect(() => {
    let ignore = false;

    getMyProfileApi()
      .then((response) => {
        if (!ignore) {
          syncProfileState(response.data?.data as UserProfile);
        }
      })
      .finally(() => {
        if (!ignore) {
          setIsLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  /**
   * 사용자가 선택한 이미지 파일을 프로필 또는 배경 대상에 맞춰 미리보기로 반영한다.
   * 이미지가 아닌 파일은 서버 저장 대상에서 제외하고 경고 알림만 표시한다.
   * @Author Hanwon.Jang
   * @param file 사용자가 선택한 이미지 파일
   * @param target 이미지가 적용될 영역 구분값
   * @return
   */
  const applyImagePreview = (file: File | undefined, target: "profile" | "background") => {
    if (!file) {
      return;
    }

    if (!file.type.startsWith("image/")) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.imageOnly"),
      );
      return;
    }

    const previewUrl = URL.createObjectURL(file);

    if (target === "profile") {
      setProfileImage(file);
      setPreviewImage(previewUrl);
      return;
    }

    setBackgroundImage(file);
    setPreviewBackground(previewUrl);
  };

  /**
   * 프로필 수정 버튼 클릭 시 폼 기본 동작과 상위 영역 이벤트 전파를 막고 수정 모드로 전환한다.
   * 배경 영역 안의 버튼이 다른 요소에 가려지거나 폼 이벤트와 섞이지 않도록 클릭 흐름을 고정한다.
   * @Author Hanwon.Jang
   * @param event 프로필 수정 버튼 클릭 이벤트
   * @return
   */
  const handleEditModeClick = (event: MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
    event.stopPropagation();
    setIsEditMode(true);
  };

  /**
   * 닉네임 필수값을 확인한 뒤 프로필 수정 API를 호출해 텍스트와 이미지 파일을 함께 저장한다.
   * 저장에 성공하면 서버가 반환한 최신 프로필 정보로 화면을 갱신하고 조회 모드로 되돌린다.
   * @Author Hanwon.Jang
   * @param event 프로필 수정 폼 제출 이벤트
   * @return
   */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!userNick.trim()) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.nickRequired"),
      );
      return;
    }

    if (
      userNick.trim().length > USER_NICK_MAX_LENGTH ||
      !KOREAN_NICK_REGEX.test(userNick.trim())
    ) {
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        message("frontend.profile.nickKoreanOnly"),
      );
      return;
    }

    try {
      setIsSaving(true);
      const response = await updateMyProfileApi({
        userNick: userNick.trim(),
        intrCntn: intrCntn.trim(),
        profileImage,
        backgroundImage,
      });
      const nextProfile = response.data?.data as UserProfile;
      syncProfileState(nextProfile);
      notifyUserProfileUpdated(nextProfile);
      setIsEditMode(false);
      await sweetSuccess(
        message("frontend.profile.savedTitle"),
        message("frontend.profile.saved"),
      );
    } catch {
      void sweetError(
        message("frontend.alert.updateFailedTitle"),
        message("frontend.common.tryAgain"),
      );
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  return (
    <main className={styles.page}>
      <form className={styles.profileShell} onSubmit={handleSubmit}>
        <section
          className={styles.cover}
          style={
            previewBackground
              ? { backgroundImage: `url("${previewBackground}")` }
              : undefined
          }
        >
          {!previewBackground && (
            <p className={styles.coverEmptyText}>
              {message("frontend.profile.background.empty")}
            </p>
          )}

          <div className={styles.coverActionGroup}>
            {isEditMode && (
              <label className={styles.coverImageButton}>
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M9 4 7.2 6H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-3.2L15 4H9Zm3 14a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                </svg>
                {message("frontend.profile.backgroundChange")}
                <input
                  className={styles.hiddenInput}
                  type="file"
                  accept="image/*"
                  onChange={(event) =>
                    applyImagePreview(event.currentTarget.files?.[0], "background")
                  }
                />
              </label>
            )}

            {isEditMode ? (
              <button className={styles.coverSaveButton} type="submit" disabled={isSaving}>
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M5 3h12.6L21 6.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Zm2 2v5h9V5H7Zm0 14h10v-6H7v6Z" />
                </svg>
                {message("frontend.report.save")}
              </button>
            ) : (
              <button
                className={styles.coverProfileEditButton}
                type="button"
                onClick={handleEditModeClick}
              >
                <svg
                  className={styles.actionIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                  focusable="false"
                >
                  <path d="M4 20h4.7L19.4 9.3a2.1 2.1 0 0 0 0-3L17.7 4.6a2.1 2.1 0 0 0-3 0L4 15.3V20Zm2-2v-1.9L16.1 6l1.9 1.9L7.9 18H6Z" />
                </svg>
                {message("frontend.profile.edit")}
              </button>
            )}
          </div>
        </section>

        <section className={styles.profileBody}>
          <div className={styles.profileHeaderRow}>
            <div className={styles.avatarWrap}>
              <img
                className={styles.profileImage}
                src={previewImage}
                alt={profile?.userNick ?? message("frontend.profile.edit")}
              />
              {isEditMode && (
                <label className={styles.avatarCameraButton}>
                  <svg
                    className={styles.cameraIcon}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                    focusable="false"
                  >
                    <path d="M9 4 7.2 6H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-3.2L15 4H9Zm3 14a5 5 0 1 1 0-10 5 5 0 0 1 0 10Zm0-2a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
                  </svg>
                  <input
                    className={styles.hiddenInput}
                    type="file"
                    accept="image/*"
                    onChange={(event) =>
                      applyImagePreview(event.currentTarget.files?.[0], "profile")
                    }
                  />
                </label>
              )}
            </div>

            <div className={styles.profileText}>
              {isEditMode ? (
                <input
                  className={styles.profileNameInput}
                  value={userNick}
                  maxLength={USER_NICK_MAX_LENGTH}
                  aria-label={message("frontend.profile.nick")}
                  onChange={(event) =>
                    setUserNick(normalizeKoreanNick(event.currentTarget.value))
                  }
                />
              ) : (
                <h1 className={styles.profileName}>{profile?.userNick || "사용자"}</h1>
              )}

              {isEditMode ? (
                <textarea
                  className={styles.profileIntroInput}
                  value={intrCntn}
                  maxLength={PROFILE_INTRO_MAX_LENGTH}
                  aria-label={message("frontend.profile.intro")}
                  onChange={(event) =>
                    setIntrCntn(normalizeProfileIntro(event.currentTarget.value))
                  }
                />
              ) : (
                <p className={styles.profileIntro}>
                  {profile?.intrCntn || message("frontend.profile.intro.empty")}
                </p>
              )}
            </div>
          </div>
        </section>
      </form>
    </main>
  );
}

export default ProfileEditPage;
