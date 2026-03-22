import React from "react";
import { Outlet } from "react-router-dom";
import { vars } from "../../styles/tokens.css";
import FormHeader from "../../components/Layout/Header/FormHeader";

function AddLayout() {
  return (
    <div style={{ backgroundColor: vars.color.background }}>
      <FormHeader />
      <main style={{ marginTop: vars.headerHeight }}>
        <Outlet />
      </main>
    </div>
  );
}

export default AddLayout;
