import { NavLink, Outlet } from "react-router-dom";

function buildNavigation(role) {
  if (role === "DOCTOR") {
    return [
      { to: "/", label: "首页总览", end: true },
      { to: "/drugs", label: "药品管理", badge: "维护" },
      { to: "/inventories", label: "库存管理", badge: "查看 / 操作" },
      { to: "/warnings", label: "预警查询", badge: "查看" },
      { to: "/prescriptions", label: "处方工作台", badge: "建方 / 授权" },
      { to: "/prescriptions/new", label: "新建处方", badge: "快捷入口" },
    ];
  }

  return [
    { to: "/", label: "首页总览", end: true },
    { to: "/prescriptions", label: "处方工作台", badge: "审核 / 发药" },
    { to: "/inventories", label: "库存管理", badge: "操作" },
    { to: "/inventories/records", label: "库存流水", badge: "审计" },
    { to: "/warnings", label: "预警查询", badge: "查看" },
    { to: "/drugs", label: "药品管理", badge: "只读演示" },
  ];
}

function HomePage({ currentUser, onLogout }) {
  const navigationItems = buildNavigation(currentUser.role);

  return (
    <main className="app-shell">
      <section className="workspace workspace-shell">
        <aside className="sidebar">
          <div className="sidebar-header">
            <p className="eyebrow">Workspace</p>
            <h1>药物管理系统</h1>
            <p className="sidebar-user">
              {currentUser.userName} / {currentUser.role}
            </p>
          </div>

          <nav className="sidebar-nav" aria-label="主导航">
            {navigationItems.map((item) => (
              <NavLink
                className={({ isActive }) =>
                  isActive ? "nav-link active-nav-link" : "nav-link"
                }
                end={item.end}
                key={item.to}
                to={item.to}
              >
                <span>{item.label}</span>
                {item.badge ? <small className="nav-badge">{item.badge}</small> : null}
              </NavLink>
            ))}
          </nav>

          <div className="sidebar-footer">
            <p className="sidebar-hint">
              {currentUser.role === "DOCTOR"
                ? "医生可直接建方、提交处方，并处理代开授权。"
                : "药师优先处理库存、预警、审核与发药。"}
            </p>
            <button className="secondary-action" onClick={onLogout} type="button">
              切换身份
            </button>
          </div>
        </aside>

        <section className="content-shell">
          <Outlet />
        </section>
      </section>
    </main>
  );
}

export default HomePage;
