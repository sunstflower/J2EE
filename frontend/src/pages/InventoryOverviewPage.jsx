import { useEffect, useMemo, useState } from "react";
import { queryInventories } from "../api/inventories";
import { queryLowStockWarnings } from "../api/warnings";
import FeedbackMessage from "../components/FeedbackMessage";
import Pagination from "../components/Pagination";
import useFlashMessage from "../hooks/useFlashMessage";

function InventoryOverviewPage() {
  const [keyword, setKeyword] = useState("");
  const [pageNum, setPageNum] = useState(1);
  const [inventoryData, setInventoryData] = useState({ records: [], total: 0, pageSize: 10 });
  const [warningData, setWarningData] = useState({ records: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const { message, showError } = useFlashMessage();

  async function loadData(targetPage = 1, targetKeyword = keyword) {
    setLoading(true);
    try {
      const [inventories, warnings] = await Promise.all([
        queryInventories({ pageNum: targetPage, pageSize: 10, keyword: targetKeyword }),
        queryLowStockWarnings({ pageNum: 1, pageSize: 50 }),
      ]);
      setPageNum(inventories.pageNum);
      setInventoryData(inventories);
      setWarningData(warnings);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData(1, "");
  }, []);

  const warningMap = useMemo(() => {
    const map = new Map();
    warningData.records.forEach((item) => {
      map.set(item.drugId, item);
    });
    return map;
  }, [warningData]);

  return (
    <section className="page-grid">
      <article className="panel">
        <p className="eyebrow">Inventory</p>
        <h2>库存预览</h2>
        <p>查看当前库存数量、批次和有效期，并直接识别低库存药品。</p>
        <FeedbackMessage message={message} />
        <form
          className="inline-form"
          onSubmit={(event) => {
            event.preventDefault();
            loadData(1, keyword);
          }}
        >
          <label>
            药品检索
            <input
              onChange={(event) => setKeyword(event.target.value)}
              placeholder="药品名称 / 编码 / 批次号"
              value={keyword}
            />
          </label>
          <button type="submit">查询库存</button>
        </form>
        {loading ? <p>加载中...</p> : null}
        <div className="table-list">
          {inventoryData.records.map((item) => {
            const warning = warningMap.get(item.drugId);
            return (
              <article className="list-card" key={item.id}>
                <div>
                  <h3>{item.drugName}</h3>
                  <p>
                    {item.drugCode} / 批次 {item.batchNo} / 库位 {item.locationCode || "未设置"}
                  </p>
                  <p>
                    库存 {item.quantity} / 锁定 {item.lockedQuantity} / 有效期 {item.expiryDate}
                  </p>
                </div>
                <div className="status-block">
                  {warning ? (
                    <span className="badge warning">
                      低库存：可用 {warning.availableQuantity} / 阈值 {warning.lowStockThreshold}
                    </span>
                  ) : (
                    <span className="badge normal">库存正常</span>
                  )}
                </div>
              </article>
            );
          })}
        </div>
        <Pagination
          onChange={(nextPage) => loadData(nextPage, keyword)}
          pageNum={pageNum}
          pageSize={inventoryData.pageSize || 10}
          total={inventoryData.total}
        />
      </article>

      <article className="panel">
        <p className="eyebrow">Low Stock</p>
        <h2>低库存提醒</h2>
        <div className="table-list">
          {warningData.records.length === 0 ? <p>当前没有低库存预警。</p> : null}
          {warningData.records.map((item) => (
            <article className="list-card" key={item.drugId}>
              <div>
                <h3>{item.drugName}</h3>
                <p>
                  {item.drugCode} / 可用库存 {item.availableQuantity}
                </p>
              </div>
              <span className="badge warning">阈值 {item.lowStockThreshold}</span>
            </article>
          ))}
        </div>
      </article>
    </section>
  );
}

export default InventoryOverviewPage;
