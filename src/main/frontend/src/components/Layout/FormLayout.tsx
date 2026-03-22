import React from "react";
import Header from "./Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { Container } from "./Container/Container";
import FormHeader from "./Header/FormHeader";

function FormLayout() {
  return (
    <div>
      <FormHeader />
      <main>
        <Outlet />
      </main>
    </div>
  );
}

export default FormLayout;
