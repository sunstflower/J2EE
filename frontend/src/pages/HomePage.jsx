import DrugPage from "./DrugPage";
import InventoryPage from "./InventoryPage";

function HomePage({ currentUser, onLogout }) {
  return (
    <main className="app-shell">
      <section className="workspace">
        <section className="hero">
          <p className="eyebrow">Drug Management System</p>
          <h1>药物管理系统主功能联调准备已就绪</h1>
          <p className="lead">
            当前身份：{currentUser.userName}（{currentUser.role}）
          </p>
          <div className="action-row">
            <button className="secondary-action" onClick={onLogout} type="button">
              切换身份
            </button>
          </div>
          <div className="status-grid">
            <article className="status-card">
              <h2>认证接入</h2>
              <p>当前用户身份已建立，请求头会通过统一请求层自动注入。</p>
            </article>
            <article className="status-card">
              <h2>联调顺序</h2>
              <p>当前已进入库存联调，后续依次推进预警和处方。</p>
            </article>
            <article className="status-card">
              <h2>演示目标</h2>
              <p>最终目标是打通建档、入库、预警、处方、发药完整链路。</p>
            </article>
          </div>
        </section>
        <DrugPage />
        <InventoryPage />
      </section>
    </main>
  );
}

export default HomePage;
