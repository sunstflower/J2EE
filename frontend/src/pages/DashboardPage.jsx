import { useNavigate } from "react-router-dom";

function DashboardPage({ currentUser }) {
  const navigate = useNavigate();
  const modules = [
    {
      title: "药物管理",
      description: "维护药品主数据，支撑库存与处方链路。",
      path: "/drugs",
    },
    {
      title: "库存管理",
      description: "执行入库、出库、盘点，并查看库存流水。",
      path: "/inventories",
    },
    {
      title: "预警查询",
      description: "跟踪低库存、临期和过期药品。",
      path: "/warnings",
    },
    {
      title: "处方工作台",
      description: "完成建方、授权、审核和发药。",
      path: "/prescriptions",
    },
  ];

  return (
    <section className="panel">
      <p className="eyebrow">Main Route</p>
      <h2>主路由</h2>
      <p>当前登录身份：{currentUser.userName}，可从这里进入业务分路由。</p>
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
