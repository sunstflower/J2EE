import { useState } from "react";
import { clearCurrentUser, DEMO_USERS, loadCurrentUser, saveCurrentUser } from "./auth";
import HomePage from "./pages/HomePage";

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
    <HomePage
      currentUser={currentUser}
      onLogout={() => {
        clearCurrentUser();
        setCurrentUser(null);
      }}
    />
  );
}

export default App;
