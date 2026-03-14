import { useParams } from "react-router-dom";
import { homeDummyData } from "../../assets/dummy";
import { useState } from "react";

function BookDetail() {
  const { id } = useParams();

  console.log(id);

  return <div>책 상세 {id}</div>;
}

export default BookDetail;
