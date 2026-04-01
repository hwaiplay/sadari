import React from "react";
import { Outlet } from "react-router-dom";
import { Container } from "../../../components/Layout/Container/Container";
import FormHeader from "../../../components/Layout/Header/FormHeader";
import { vars } from "../../../app/styles/tokens.css";

function AddLayout() {
  return (
    <Container>
      <FormHeader />
      <main style={{ marginTop: vars.headerHeight }}>
        <Outlet />
      </main>
    </Container>
  );
}

export default AddLayout;
