import { NavLink, Outlet } from "react-router-dom";

function buildNavigation(role) {
  if (role === "DOCTOR") {
    return [
      { to: "/", label: "首页总览", end: true },
      { to: "/drugs", label: "药品管理" },
      { to: "/inventories", label: "库存管理" },
      { to: "/warnings", label: "预警查询" },
      { to: "/prescriptions", label: "处方工作台" },
      { to: "/prescriptions/new", label: "新建处方" },
    ];
  }

  return [
    { to: "/", label: "首页总览", end: true },
    { to: "/prescriptions", label: "处方工作台" },
    { to: "/inventories", label: "库存管理" },
    { to: "/inventories/records", label: "库存流水" },
    { to: "/warnings", label: "预警查询" },
    { to: "/drugs", label: "药品管理" },
  ];
}

function HomePage({ currentUser, onLogout }) {
  const items = buildNavigation(currentUser.role);

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">Workspace</p>
          <h1>药物管理系统</h1>
          <p className="sidebar-user">
            {currentUser.userName} / {currentUser.role}
          </p>
        </div>
        <nav className="sidebar-nav">
          {items.map((item) => (
            <NavLink
              className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
              end={item.end}
              key={item.to}
              to={item.to}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="secondary-action" onClick={onLogout} type="button">
          切换身份
        </button>
      </aside>
      <section className="content-shell">
        <Outlet />
      </section>
    </main>
  );
}

export default HomePage;
