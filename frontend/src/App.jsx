import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import List from "./pages/List";
import Detail from "./pages/Detail";
import Form from "./pages/Form";
import Layout from "./components/Layout";

export default function App() {
  const token = localStorage.getItem("token");
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={token ? <Layout><Dashboard /></Layout> : <Navigate to="/login" />} />
        <Route path="/changes" element={token ? <Layout><List /></Layout> : <Navigate to="/login" />} />
        <Route path="/changes/new" element={token ? <Layout><Form /></Layout> : <Navigate to="/login" />} />
        <Route path="/changes/:id" element={token ? <Layout><Detail /></Layout> : <Navigate to="/login" />} />
        <Route path="/changes/:id/edit" element={token ? <Layout><Form /></Layout> : <Navigate to="/login" />} />
        <Route path="*" element={<Navigate to={token ? "/dashboard" : "/login"} />} />
      </Routes>
    </BrowserRouter>
  );
}
