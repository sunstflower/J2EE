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

function App() {
  const [currentUser, setCurrentUser] = useState(() => loadCurrentUser());

  if (!currentUser) {
    return (
      <main className="app-shell">
        <section className="hero">
          <p className="eyebrow">Drug Management System</p>
          <h1>药物管理系统联调入口</h1>
          <p className="lead">
            当前阶段先接入最小登录态，为医生与药师角色联调提供统一入口。
          </p>
          <div className="login-grid">
            {DEMO_USERS.map((user) => (
              <button
                key={user.userId}
                className="login-card"
                onClick={() => {
                  saveCurrentUser(user);
                  setCurrentUser(user);
                }}
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
          <Route element={<NotFoundPage />} path="*" />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
