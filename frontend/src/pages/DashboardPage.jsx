function DashboardPage({ currentUser }) {
  return (
    <section className="panel">
      <p className="eyebrow">Dashboard</p>
      <h2>首页总览</h2>
      <p>当前登录身份：{currentUser.userName}</p>
      <div className="stat-grid">
        <article className="stat-card">
          <h3>药品管理</h3>
          <p>维护药品主数据，支撑库存与处方链路。</p>
        </article>
        <article className="stat-card">
          <h3>库存管理</h3>
          <p>执行入库、出库、盘点，并查看库存流水。</p>
        </article>
        <article className="stat-card">
          <h3>预警查询</h3>
          <p>跟踪低库存、临期和过期药品。</p>
        </article>
        <article className="stat-card">
          <h3>处方工作台</h3>
          <p>完成建方、授权、审核和发药。</p>
        </article>
      </div>
    </section>
  );
}

export default DashboardPage;
