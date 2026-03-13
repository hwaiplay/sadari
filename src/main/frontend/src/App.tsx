// App.tsx
import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home/Home";
import MainLayout from "./components/Layout/MainLayout/MainLayout";
export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
      </Route>
    </Routes>
  );
}
