import React from "react";
import Header from "../Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "../Navigation/Navigation";
import { Container } from "../Container/Container";

function MainLayout() {
  return (
    <div>
      <Header />
      <main>
        <Outlet />
      </main>
      <Navigation />
    </div>
  );
}

export default MainLayout;
