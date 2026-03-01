import { useEffect, useState } from "react";
import axios from "axios";

function App() {
  const [hello, setHello] = useState([]);

  useEffect(() => {
    axios.get("/api/test").then((res) => {
      setHello(res.data);
    });
  }, []);
  return (
    <div className="App">
      <h3>백엔드 데이터</h3>
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

export default App;
