import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import {
  getMyAlimListApi,
  readAllAlimApi,
  type AlimItem,
} from "@/features/Alim/api/alimApi";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./AlimPage.css";

/**
 * 로그인 사용자의 알림 목록을 보여주는 페이지입니다.
 * 목록 API는 조회된 알림을 읽음 처리하므로, 무한 스크롤로 실제 불러온 페이지까지만 읽음 처리됩니다.
 *
 * @author Hanwon.Jang
 * @return 알림 목록 화면
 */
function AlimPage() {
  const navigate = useNavigate();
  const [alimList, setAlimList] = useState<AlimItem[]>([]);
  const [nextPage, setNextPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isFetchingMore, setIsFetchingMore] = useState(false);
  const [isReadingAll, setIsReadingAll] = useState(false);
  const observerTargetRef = useRef<HTMLDivElement | null>(null);

  const loadAlimList = useCallback(
    async (page: number) => {
      const isFirstPage = page === 1;

      if (isFirstPage) {
        setIsLoading(true);
      } else {
        setIsFetchingMore(true);
      }

      try {
        const response = await getMyAlimListApi(page);
        const data = response.data;

        // 서버에서 조회된 알림은 이미 읽음 처리했으므로 프론트 목록도 같은 상태로 병합한다.
        setAlimList((prevList) => (
          isFirstPage ? data.list ?? [] : [...prevList, ...(data.list ?? [])]
        ));
        setHasNext(Boolean(data.hasNext));
        setNextPage(data.nextPage ?? page + 1);
      } catch (error) {
        void sweetError(
          message("frontend.alim.list.failedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      } finally {
        if (isFirstPage) {
          setIsLoading(false);
        } else {
          setIsFetchingMore(false);
        }
      }
    },
    [],
  );

  useEffect(() => {
    void loadAlimList(1);
  }, [loadAlimList]);

  useEffect(() => {
    const target = observerTargetRef.current;

    if (!target || !hasNext || isLoading || isFetchingMore) {
      return;
    }

    const observer = new IntersectionObserver((entries) => {
      const [entry] = entries;

      // 하단 감지 영역이 보이는 순간 다음 20개를 요청한다.
      // 이 요청이 성공한 페이지까지만 서버에서 읽음 처리되므로 스크롤하지 않은 알림은 미읽음 상태로 남는다.
      if (entry?.isIntersecting) {
        void loadAlimList(nextPage);
      }
    });

    observer.observe(target);

    return () => {
      observer.disconnect();
    };
  }, [hasNext, isFetchingMore, isLoading, loadAlimList, nextPage]);

  const handleReadAll = async () => {
    if (isReadingAll) {
      return;
    }

    setIsReadingAll(true);

    try {
      await readAllAlimApi();

      // 모두 읽음은 아직 불러오지 않은 알림도 처리하지만, 현재 화면에는 이미 로드된 목록만 있으므로 표시 목록만 즉시 보정한다.
      setAlimList((prevList) => prevList.map((alim) => ({ ...alim, readYsno: "Y" })));
    } catch (error) {
      void sweetError(
        message("frontend.alim.readAll.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsReadingAll(false);
    }
  };

  const handleAlimClick = (alim: AlimItem) => {
    // 링크가 없는 알림은 단순 안내 알림으로 취급해 현재 화면을 유지합니다.
    if (!alim.linkUrlx) {
      return;
    }

    navigate(alim.linkUrlx);
  };

  const renderAlimIcon = (alimIconName?: string) => {
    // 알림 상황 공통코드의 OPT1_NAME으로 아이콘을 분기한다.
    // 코드가 아직 등록되지 않은 상황은 기존 종 아이콘을 보여줘 알림 목록 자체는 깨지지 않게 한다.
    if (alimIconName === "HEART") {
      return (
        <svg className={styles.alimHeartIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z" />
        </svg>
      );
    }

    if (alimIconName === "FOLLOW") {
      return (
        <svg className={styles.alimIcon} viewBox="0 0 24 24" aria-hidden="true">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M19 8v6" />
          <path d="M22 11h-6" />
        </svg>
      );
    }

    return (
      <svg className={styles.alimIcon} viewBox="0 0 24 24" aria-hidden="true">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
    );
  };

  const getAlimIconWrapClass = (alimIconName?: string) => {
    // DB 공통코드의 OPT1_NAME을 화면 스타일로 매핑한다.
    // LIKE는 HEART, FOLLOW는 FOLLOW로 내려오며, 신규 상황 코드가 추가되면 기본 파란 종 아이콘 스타일을 사용한다.
    if (alimIconName === "HEART") {
      return styles.alimIconWrapLike;
    }

    if (alimIconName === "FOLLOW") {
      return styles.alimIconWrapFollow;
    }

    return styles.alimIconWrap;
  };

  if (isLoading) {
    return <Loading title={message("frontend.common.loadingList")} />;
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <h1 className={styles.title}>{message("frontend.alim.title")}</h1>
          <p className={styles.subtitle}>{message("frontend.alim.subtitle")}</p>
        </div>
        <button
          className={styles.readAllButton}
          type="button"
          disabled={isReadingAll}
          onClick={handleReadAll}
        >
          {message("frontend.alim.readAll")}
        </button>
      </section>

      {alimList.length === 0 ? (
        <div className={styles.empty}>{message("frontend.alim.empty")}</div>
      ) : (
        <section className={styles.list} aria-label={message("frontend.alim.title")}>
          {alimList.map((alim) => (
            <button
              className={styles.itemButton}
              type="button"
              onClick={() => handleAlimClick(alim)}
              key={`${alim.userNumb}-${alim.alimNumb}`}
            >
              <span className={getAlimIconWrapClass(alim.alimIconName)} aria-hidden="true">
                {renderAlimIcon(alim.alimIconName)}
              </span>
              <span className={styles.itemText}>
                <strong className={styles.itemTitle}>
                  {alim.alimTitl || message("frontend.alim.defaultTitle")}
                </strong>
                <span className={styles.itemContent}>{alim.alimCont}</span>
                <span className={styles.itemDate}>{alim.sendDate}</span>
              </span>
            </button>
          ))}
          <div className={styles.scrollTarget} ref={observerTargetRef}>
            {isFetchingMore ? message("frontend.alim.loadingMore") : null}
          </div>
        </section>
      )}
    </main>
  );
}

export default AlimPage;
