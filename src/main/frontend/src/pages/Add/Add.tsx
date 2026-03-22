import React from "react";
import { Container } from "../../components/Layout/Container/Container";
import SearchBook from "../../features/Add/components/SearchBook";
import FormField from "../../features/Add/fields/FormField";
import { Button } from "../../components/Button/Button";
import { statusContainer } from "./Add.css";

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
    <Container>
      <form action="" method="post">
        <SearchBook />
        <FormField title="독서상태">
          <div className={statusContainer}>
            <Button>다 읽었어요</Button>
            <Button variant="disable">읽고 있어요</Button>
            <Button variant="disable">중단 했어요</Button>
          </div>
        </FormField>
      </form>
    </Container>
  );
}

export default Add;
