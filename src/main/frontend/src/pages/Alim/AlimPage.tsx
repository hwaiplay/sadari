import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import Loading from "@/components/Loading/Loading";
import { getMyAlimListApi, type AlimItem } from "@/features/Alim/api/alimApi";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./AlimPage.css";

/**
 * 로그인 사용자의 알림 목록을 보여주는 페이지입니다.
 * 알림 저장 시점에 TB_ALIMXX에 제목/내용/링크가 스냅샷으로 들어가므로 화면은 별도 템플릿 치환 없이 저장된 값을 그대로 표시합니다.
 *
 * @author Hanwon.Jang
 * @return 알림 목록 화면
 */
function AlimPage() {
  const navigate = useNavigate();
  const [alimList, setAlimList] = useState<AlimItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    getMyAlimListApi()
      .then((response) => {
        // ResultData 공통 검증을 통과한 알림만 화면 상태에 반영합니다.
        // 실패 응답은 catch로 떨어지므로 빈 목록과 실패 상태가 섞이지 않습니다.
        if (!ignore) {
          setAlimList(response.data ?? []);
        }
      })
      .catch((error) => {
        // 알림 조회 실패는 사용자가 현재 화면에서 바로 인지해야 하므로 공통 오류 알림으로 표시합니다.
        if (!ignore) {
          void sweetError(
            message("frontend.alim.list.failedTitle"),
            getApiErrorMessage(error, message("frontend.common.tryAgain")),
          );
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

  const handleAlimClick = (alim: AlimItem) => {
    // 링크가 없는 알림은 단순 안내 알림으로 취급해 현재 화면을 유지합니다.
    if (!alim.linkUrlx) {
      return;
    }

    // 서버가 TB_ALTEMP.LINK_URLX와 대상 번호를 조합해 저장한 링크를 그대로 사용합니다.
    navigate(alim.linkUrlx);
  };

  // 목록을 받아오기 전에는 다른 빈 상태 문구가 먼저 보이지 않도록 로딩 화면을 우선 표시합니다.
  if (isLoading) {
    return <Loading />;
  }

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <h1 className={styles.title}>{message("frontend.alim.title")}</h1>
        <p className={styles.subtitle}>{message("frontend.alim.subtitle")}</p>
      </section>

      {alimList.length === 0 ? (
        // 조회는 성공했지만 표시할 알림이 없는 경우의 빈 상태입니다.
        <div className={styles.empty}>{message("frontend.alim.empty")}</div>
      ) : (
        // 알림이 있는 경우에는 TB_ALIMXX에 저장된 발송 당시 제목/내용/링크를 그대로 렌더링합니다.
        <section className={styles.list} aria-label={message("frontend.alim.title")}>
          {alimList.map((alim) => (
            <button
              className={styles.itemButton}
              type="button"
              onClick={() => handleAlimClick(alim)}
              key={`${alim.userNumb}-${alim.alimNumb}`}
            >
              <span className={styles.bellWrap} aria-hidden="true">
                <svg className={styles.bellIcon} viewBox="0 0 24 24">
                  <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
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
        </section>
      )}
    </main>
  );
}

export default AlimPage;
