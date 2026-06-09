import { useMemo, useState } from "react";
import { BrowserRouter, Navigate, NavLink, Route, Routes } from "react-router-dom";
import { postPublic } from "./api/client";
import { clearCurrentUser, inferRoleFromUserId, loadCurrentUser, saveAuthSession } from "./auth";
import RequireRole from "./components/RequireRole";
import DashboardPage from "./pages/DashboardPage";
import InboundPage from "./pages/InboundPage";
import InventoryOverviewPage from "./pages/InventoryOverviewPage";
import NotFoundPage from "./pages/NotFoundPage";
import PrescribePage from "./pages/PrescribePage";

function LoginPage({ onLogin }) {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  const roleHint = useMemo(() => {
    if (!userId.trim()) {
      return "用户号需以 1 或 2 开头";
    }

    try {
      return inferRoleFromUserId(userId) === "DOCTOR" ? "当前识别角色：医生" : "当前识别角色：药师";
    } catch (error) {
      return error.message;
    }
  }, [userId]);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const data = await postPublic("/api/auth/login", {
        userId: Number(userId),
        password,
      });
      onLogin(data);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-shell">
      <form className="auth-card" onSubmit={handleSubmit}>
        <p className="eyebrow">Drug Management Demo</p>
        <h1>药物管理系统</h1>
        <p className="muted">当前演示版本只保留库存预览、开药、药物入库三项功能。</p>
        <label>
          用户号
          <input onChange={(event) => setUserId(event.target.value)} value={userId} />
        </label>
        <label>
          密码
          <input
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            value={password}
          />
        </label>
        <p className="muted">{roleHint}</p>
        {message ? <p className="message error">{message}</p> : null}
        <button disabled={loading} type="submit">
          {loading ? "登录中..." : "登录"}
        </button>
        <div className="demo-block">
          <p>默认账号</p>
          <p>药师：1001 / pharm123</p>
          <p>医生：2001 / doctor123</p>
        </div>
      </form>
    </main>
  );
}

function AppLayout({ currentUser, onLogout }) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Minimal Demo</p>
          <h1>药物管理系统</h1>
          <p className="muted">{currentUser.userName}</p>
        </div>
        <nav className="top-nav">
          <NavLink to="/">主页</NavLink>
          <NavLink to="/inventory-overview">库存预览</NavLink>
          {currentUser.role === "DOCTOR" ? <NavLink to="/prescribe">开药</NavLink> : null}
          <NavLink to="/inbound">药物入库</NavLink>
        </nav>
        <button className="ghost-button" onClick={onLogout} type="button">
          退出登录
        </button>
      </header>
      <main className="content-shell">
        <Routes>
          <Route element={<DashboardPage currentUser={currentUser} />} path="/" />
          <Route element={<InventoryOverviewPage />} path="/inventory-overview" />
          <Route element={<InboundPage />} path="/inbound" />
          <Route element={<RequireRole roles={["DOCTOR"]} />} path="/prescribe">
            <Route element={<PrescribePage />} index />
          </Route>
          <Route element={<NotFoundPage />} path="*" />
        </Routes>
      </main>
    </div>
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
      <AppLayout
        currentUser={currentUser}
        onLogout={() => {
          clearCurrentUser();
          setCurrentUser(null);
        }}
      />
    </BrowserRouter>
  );
}

export default App;
