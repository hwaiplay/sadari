import React from "react";
import FormField from "../../features/Book/Add/fields/FormField";
import { Button } from "../../components/Button/Button";
import { statusContainer } from "./Add.css";
import SearchBookButton from "../../features/Book/Add/components/SearchBookButton";

/**
 * fileName       : Add
 * author         : Hanwon.Jang
 * date           : 2026-03-21
 * description    : 기록하기 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21       Hanwon.Jang       최초 생성
 */

function Add() {
  return (
    <form action="" method="post">
      <SearchBookButton />
      <FormField title="독서상태">
        <div className={statusContainer}>
          <Button>다 읽었어요</Button>
          <Button variant="disable">읽고 있어요</Button>
          <Button variant="disable">중단 했어요</Button>
        </div>
      </FormField>
    </form>
  );
}

export default Add;
