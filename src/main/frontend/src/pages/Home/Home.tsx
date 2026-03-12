import { useEffect, useState } from "react";
import axios from "axios";
import { Button } from "../../components/Button/Button";

interface DataProps {
  ename: string;
  hiredate: string;
}

function Home() {
  const [hello, setHello] = useState<DataProps[]>([]);

  useEffect(() => {
    axios.get<DataProps[]>("/api/test").then((res) => {
      setHello(res.data);
    });
  }, []);

  return (
    <div className="App">
      <h3>백엔드 데이터</h3>
      <Button variant="primary">기록하기</Button>
      {hello.map((h, index) => {
        return (
          <div key={index}>
            <span>{h.ename}: </span>
            <span>{h.hiredate}</span>
          </div>
        );
      })}
    </div>
  );
}

export default Home;
