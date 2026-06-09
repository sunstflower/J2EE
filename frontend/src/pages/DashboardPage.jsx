import { useNavigate } from "react-router-dom";

function DashboardPage({ currentUser }) {
  const navigate = useNavigate();
  const modules = [
    {
      title: "库存预览",
      description: "查看库存中已有药量，并同时查看低库存提醒。",
      path: "/inventory-overview",
    },
    ...(currentUser.role === "DOCTOR"
      ? [
          {
            title: "开药",
            description: "仅医生账号可开出处方药。",
            path: "/prescribe",
          },
        ]
      : []),
    {
      title: "药物入库",
      description: "执行入库并用于补充库存。",
      path: "/inbound",
    },
  ];

  return (
    <section className="panel">
      <p className="eyebrow">Main Route</p>
      <h2>主页</h2>
      <p>当前登录身份：{currentUser.userName}。当前演示版本只保留三个核心入口。</p>
      <div className="stat-grid">
        {modules.map((module) => (
          <article className="stat-card module-card" key={module.path}>
            <h3>{module.title}</h3>
            <p>{module.description}</p>
            <button onClick={() => navigate(module.path)} type="button">
              进入{module.title}
            </button>
          </article>
        ))}
      </div>
    </section>
  );
}

export default DashboardPage;
