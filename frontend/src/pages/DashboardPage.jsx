function DashboardPage({ currentUser }) {
  return (
    <section className="hero dashboard-hero">
      <p className="eyebrow">Drug Management System</p>
      <h1>药物管理系统主功能联调准备已就绪</h1>
      <p className="lead">
        当前身份：{currentUser.userName}（{currentUser.role}）
      </p>
      <div className="status-grid">
        <article className="status-card">
          <h2>认证接入</h2>
          <p>当前用户身份已建立，请求头会通过统一请求层自动注入。</p>
        </article>
        <article className="status-card">
          <h2>结构整改</h2>
          <p>当前已切换为独立模块路由，后续继续补齐分页、确认弹窗和统一状态管理。</p>
        </article>
        <article className="status-card">
          <h2>演示目标</h2>
          <p>最终目标是打通建档、入库、预警、处方、发药完整链路，并让医生与药师入口分层可见。</p>
        </article>
      </div>
    </section>
  );
}

export default DashboardPage;
