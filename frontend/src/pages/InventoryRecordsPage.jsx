import InventoryPage from "./InventoryPage";

function InventoryRecordsPage() {
  return (
    <section className="route-wrapper">
      <div className="route-header">
        <p className="section-kicker">Inventory Records</p>
        <h2>库存流水页</h2>
        <p className="lead">当前版本先复用库存模块页面承载库存流水查询，后续可继续拆成独立查询页。</p>
      </div>
      <InventoryPage />
    </section>
  );
}

export default InventoryRecordsPage;
