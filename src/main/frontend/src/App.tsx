// App.tsx
import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home/Home";
import Header from "./components/Layout/Header/Header";
import Navigation from "./components/Layout/Navigation/Navigation";
export default function App() {
  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<Home />} />
      </Routes>
      <Navigation />
    </>
  );
}
