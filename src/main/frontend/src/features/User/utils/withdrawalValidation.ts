const textEncoder = new TextEncoder();

export const MAX_WITHDRAWAL_REASON_BYTES = 500;

/**
 * 탈퇴 사유 문자열이 UTF-8 저장 시 사용하는 바이트 수를 계산한다
 *
 * @author HanWon.Jang
 * @param value 바이트 수를 계산할 탈퇴 사유
 * @return UTF-8 기준 바이트 수
 */
export function getWithdrawReasonByteLen(value: string): number {

  // 브라우저의 UTF-8 인코더로 계산한 탈퇴 사유 바이트 수를 반환한다
  return textEncoder.encode(value).length;
}

/**
 * 탈퇴 사유를 UTF-8 최대 저장 바이트를 넘지 않는 문자열로 제한한다
 *
 * @author HanWon.Jang
 * @param value 제한할 탈퇴 사유
 * @param maxBytes 허용할 최대 UTF-8 바이트 수
 * @return 최대 바이트 안에 포함되는 탈퇴 사유
 */
export function truncateWithdrawalReason(
  value: string,
  maxBytes = MAX_WITHDRAWAL_REASON_BYTES,
): string {

  let byteLength = 0;
  let limitedValue = "";

  // 유니코드 문자를 순서대로 계산해 다중 바이트 문자 중간이 잘리지 않게 제한한다
  for (const character of value) {
    // 현재 문자 한 개가 차지하는 UTF-8 바이트 수를 계산한다
    const characterBytes = getWithdrawReasonByteLen(character);

    // 다음 문자를 추가했을 때 최대 바이트를 넘으면 입력 반영을 중단한다
    if (byteLength + characterBytes > maxBytes) {
      // 최대 바이트 안에 누적한 탈퇴 사유를 반환한다
      return limitedValue;
    }

    // 허용 범위의 문자를 탈퇴 사유에 추가한다
    limitedValue += character;
    // 화면에 표시할 누적 바이트 수를 갱신한다
    byteLength += characterBytes;
  }

  // 전체 입력이 허용 범위이면 원문과 같은 탈퇴 사유를 반환한다
  return limitedValue;
}
