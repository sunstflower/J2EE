import { useState } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { clearCurrentUser, DEMO_USERS, loadCurrentUser, saveCurrentUser } from "./auth";
import DashboardPage from "./pages/DashboardPage";
import DrugPage from "./pages/DrugPage";
import HomePage from "./pages/HomePage";
import InventoryPage from "./pages/InventoryPage";
import InventoryRecordsPage from "./pages/InventoryRecordsPage";
import NotFoundPage from "./pages/NotFoundPage";
import PrescriptionCreatePage from "./pages/PrescriptionCreatePage";
import PrescriptionDetailPage from "./pages/PrescriptionDetailPage";
import PrescriptionPage from "./pages/PrescriptionPage";
import WarningPage from "./pages/WarningPage";

function LoginPage({ onSelectUser }) {
  return (
    <main className="login-shell">
      <section className="hero-card">
        <p className="eyebrow">Drug Management System</p>
        <h1>药物管理系统联调入口</h1>
        <p className="hero-copy">当前阶段使用最小登录态切换医生与药师身份，便于前后端联调与容器演示。</p>
        <div className="login-grid">
          {DEMO_USERS.map((user) => (
            <button
              className="login-card"
              key={user.userId}
              onClick={() => onSelectUser(user)}
              type="button"
            >
              <strong>{user.userName}</strong>
              <span>{user.role === "DOCTOR" ? "医生身份" : "药师身份"}</span>
            </button>
          ))}
        </div>
      </section>
    </main>
  );
}

function App() {
  const [currentUser, setCurrentUser] = useState(() => loadCurrentUser());

  if (!currentUser) {
    return (
      <LoginPage
        onSelectUser={(user) => {
          saveCurrentUser(user);
          setCurrentUser(user);
        }}
      />
    );
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          element={
            <HomePage
              currentUser={currentUser}
              onLogout={() => {
                clearCurrentUser();
                setCurrentUser(null);
              }}
            />
          }
          path="/"
        >
          <Route element={<DashboardPage currentUser={currentUser} />} index />
          <Route element={<DrugPage />} path="drugs" />
          <Route element={<InventoryPage />} path="inventories" />
          <Route element={<InventoryRecordsPage />} path="inventories/records" />
          <Route element={<WarningPage />} path="warnings" />
          <Route element={<PrescriptionPage />} path="prescriptions" />
          <Route element={<PrescriptionCreatePage />} path="prescriptions/new" />
          <Route element={<PrescriptionDetailPage />} path="prescriptions/:id" />
          <Route element={<Navigate replace to="/" />} path="dashboard" />
          <Route element={<NotFoundPage />} path="*" />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
