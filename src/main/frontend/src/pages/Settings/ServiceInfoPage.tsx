import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import {
  getPrivacyPolicyApi,
  getServiceInfoListApi,
  type ServiceInfo,
} from "@/features/ServiceInfo/api/serviceInfoApi";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./ServiceInfoPage.css";

type ServiceInfoPageProps = {
  privacyOnly?: boolean;
};

/**
 * 서비스 정보 아코디언 또는 로그인 전 개인정보처리방침 본문을 표시함
 *
 * @author SeungHyeon.Kang
 * @param privacyOnly 개인정보처리방침만 공개할지 여부
 * @return 서비스 정보 또는 공개 개인정보처리방침 화면
 */
const ServiceInfoPage = ({ privacyOnly = false }: ServiceInfoPageProps) => {

  const navigate = useNavigate();
  const [serviceInfoList, setServiceInfoList] = useState<ServiceInfo[]>([]);
  const [openCategoryCode, setOpenCategoryCode] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {

    let ignore = false;
    // 로그인 전에는 개인정보처리방침만 조회하고 인증 화면에서는 전체 서비스 정보를 조회함
    const request = privacyOnly
      ? getPrivacyPolicyApi().then((privacyPolicy) => privacyPolicy ? [privacyPolicy] : [])
      : getServiceInfoListApi();

    // 선택한 공개 범위의 서비스 정보 배포본을 조회함
    request
      .then((result) => {
        // 화면이 유지되는 동안에만 목록 상태를 갱신함
        if (!ignore) {
          setServiceInfoList(result);
        }
      })
      .catch(() => {
        // 조회 실패 상태를 사용자 안내 문구로 전환함
        if (!ignore) {
          setHasError(true);
        }
      })
      .finally(() => {
        // 화면이 유지되는 동안 로딩 상태를 해제함
        if (!ignore) {
          setIsLoading(false);
        }
      });

    // 화면 이탈 뒤 비동기 응답이 상태를 변경하지 않게 정리함
    return () => {
      ignore = true;
    };
  }, [privacyOnly]);

  /** 선택한 서비스 정보 카테고리의 아코디언 상태를 전환함 */
  const handleToggle = (cateCode: string): void => {
    // 열린 카테고리를 다시 누르면 접고 다른 카테고리를 누르면 해당 항목만 펼침
    setOpenCategoryCode((currentCode) => currentCode === cateCode ? "" : cateCode);
  };

  /**
   * 공개 개인정보처리방침 확인 후 이전 화면으로 이동함
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleConfirm = (): void => {
    // 개인정보처리방침을 열기 전 화면으로 이동함
    navigate(-1);
  };

  // 목록 조회 중에는 공통 로딩 화면을 반환함
  if (isLoading) {
    return <Loading />;
  }

  // 서비스 정보 카테고리 또는 공개 개인정보처리방침 화면을 반환함
  return (
    /* 서비스 정보 카테고리와 배포 정책 전체 영역 */
    <main className={privacyOnly ? `${styles.page} ${styles.policyPage}` : styles.page}>
      {/* 서비스 정보 카테고리 아코디언 목록 영역 */}
      <section className={styles.list} aria-label={message("frontend.serviceInfo.label")}>
        {hasError ? (
          <p className={styles.empty}>
            {/* "다시 시도해주세요." */}
            {message("frontend.common.tryAgain")}
          </p>
        ) : serviceInfoList.length === 0 ? (
          <p className={styles.empty}>
            {/* "배포된 내용이 없습니다." */}
            {message("frontend.serviceInfo.empty")}
          </p>
        ) : serviceInfoList.map((serviceInfo) => {
          const isOpen = openCategoryCode === serviceInfo.cateCode;
          const contentId = `service-info-${serviceInfo.cateCode}`;
          // 수정일이 없는 최초 배포본은 배포일을 기준으로 최근 수정일을 표시함
          const modifiedDate = serviceInfo.updtDate ?? serviceInfo.dplyDate;
          // 최근 수정일의 날짜 부분을 사용자 화면의 공통 점 표기로 변환함
          const displayModifiedDate = formatDashedDateToDot(modifiedDate?.slice(0, 10));
          // 서비스 정보 카테고리 한 항목과 현재 배포 본문을 반환함
          return (
            <article className={privacyOnly ? styles.policyItem : styles.item} key={serviceInfo.cateCode}>
              {/* 공개 개인정보처리방침 제목 또는 서비스 정보 카테고리 펼침 버튼 영역 */}
              {privacyOnly ? (
                <h1 className={styles.policyTitle}>{serviceInfo.cateName}</h1>
              ) : (
                <button
                  className={styles.button}
                  type="button"
                  aria-expanded={isOpen}
                  aria-controls={contentId}
                  onClick={() => handleToggle(serviceInfo.cateCode)}
                >
                  <span>{serviceInfo.cateName}</span>
                  <svg className={`${styles.chevron} ${isOpen ? styles.chevronOpen : ""}`} viewBox="0 0 24 24" aria-hidden="true"><path d="m9 18 6-6-6-6" /></svg>
                </button>
              )}
              {/* 선택 카테고리의 현재 배포 서비스 정보 본문 영역 */}
              <div id={contentId} className={privacyOnly ? styles.policyContent : `${styles.contentWrap} ${isOpen ? styles.contentWrapOpen : ""}`}>
                <div className={styles.contentClip}>
                  {serviceInfo.svciCntn ? (
                    <div className={styles.content} dangerouslySetInnerHTML={{ __html: serviceInfo.svciCntn }} />
                  ) : (
                    <p className={`${styles.content} ${styles.empty}`}>
                      {/* "배포된 내용이 없습니다." */}
                      {message("frontend.serviceInfo.empty")}
                    </p>
                  )}
                  {displayModifiedDate && (
                    /* 현재 배포 서비스 정보의 최근 수정일 영역 */
                    <footer className={styles.modifiedDate}>
                      {/* "최근 수정일" */}
                      <span>{message("frontend.serviceInfo.modifiedDate")}</span>
                      <time dateTime={modifiedDate ?? undefined}>{displayModifiedDate}</time>
                    </footer>
                  )}
                </div>
              </div>
            </article>
          );
        })}
      </section>
      {privacyOnly && (
        /* 공개 개인정보처리방침 확인과 이전 화면 이동 영역 */
        <footer className={styles.confirmArea}>
          <ActionButton variant="primary" size="lg" width="full" onClick={handleConfirm}>
            {/* "확인" */}
            {message("frontend.common.confirm")}
          </ActionButton>
        </footer>
      )}
    </main>
  );
};

export default ServiceInfoPage;
