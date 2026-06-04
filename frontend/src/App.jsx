import { useState } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { login, register } from "./api/auth";
import RequireRole from "./components/RequireRole";
import {
  clearCurrentUser,
  inferRoleFromUserId,
  loadCurrentUser,
  saveAuthSession,
} from "./auth";
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

function LoginPage({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({
    userId: "",
    userName: "",
    password: "",
  });
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  function updateField(key, value) {
    setForm((current) => ({
      ...current,
      [key]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      if (mode === "register") {
        const registeredUser = await register({
          userId: Number(form.userId),
          userName: form.userName.trim(),
          password: form.password,
        });
        setMessage({
          type: "success",
          text: `注册成功，账号角色为${registeredUser.role === "DOCTOR" ? "医生" : "药师"}，请登录。`,
        });
        setMode("login");
        setForm({
          userId: String(registeredUser.userId),
          userName: "",
          password: "",
        });
        return;
      }

      const data = await login({
        userId: Number(form.userId),
        password: form.password,
      });
      onLogin(data);
    } catch (error) {
      setMessage({ type: "error", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  let roleHint = "用户号需以 1 或 2 开头";
  if (form.userId.trim()) {
    try {
      roleHint = `当前识别角色：${
        inferRoleFromUserId(form.userId) === "DOCTOR" ? "医生" : "药师"
      }`;
    } catch (error) {
      roleHint = error.message;
    }
  }

  return (
    <main className="login-shell">
      <section className="hero-card">
        <p className="eyebrow">Drug Management System</p>
        <h1>药物管理系统登录</h1>
        <p className="hero-copy">
          当前演示环境通过后端登录接口签发会话。用户号以 1 开头为药师，以 2 开头为医生。
        </p>
        <div className="auth-switch">
          <button
            className={mode === "login" ? "auth-tab active-tab" : "auth-tab"}
            onClick={() => setMode("login")}
            type="button"
          >
            登录
          </button>
          <button
            className={mode === "register" ? "auth-tab active-tab" : "auth-tab"}
            onClick={() => setMode("register")}
            type="button"
          >
            注册
          </button>
        </div>
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            用户号
            <input
              onChange={(event) => updateField("userId", event.target.value)}
              placeholder="例如 1001 / 2001"
              value={form.userId}
            />
          </label>
          {mode === "register" ? (
            <label>
              用户名
              <input
                onChange={(event) => updateField("userName", event.target.value)}
                placeholder="请输入姓名"
                value={form.userName}
              />
            </label>
          ) : null}
          <label>
            密码
            <input
              onChange={(event) => updateField("password", event.target.value)}
              placeholder="请输入密码"
              type={mode === "register" ? "text" : "password"}
              value={form.password}
            />
          </label>
          <p className="role-hint">{roleHint}</p>
          {message ? <p className={`message ${message.type}`}>{message.text}</p> : null}
          <button className="primary-action" disabled={loading} type="submit">
            {loading ? (mode === "login" ? "登录中..." : "注册中...") : mode === "login" ? "登录" : "注册"}
          </button>
        </form>
        <div className="demo-hint">
          <p>演示账号</p>
          <p>药师：1001 / pharm123</p>
          <p>医生：2001 / doctor123</p>
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
        onLogin={(session) => {
          saveAuthSession(session);
          setCurrentUser(session.user);
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
          <Route element={<RequireRole roles={["DOCTOR"]} />} path="prescriptions/new">
            <Route element={<PrescriptionCreatePage />} index />
          </Route>
          <Route element={<PrescriptionDetailPage />} path="prescriptions/:id" />
          <Route element={<Navigate replace to="/" />} path="dashboard" />
          <Route element={<NotFoundPage />} path="*" />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
