import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import FeedbackMessage from "../components/FeedbackMessage";
import { queryExpiryWarnings, queryLowStockWarnings } from "../api/warnings";
import useFlashMessage from "../hooks/useFlashMessage";

function WarningPage() {
  const [lowStockData, setLowStockData] = useState({ records: [], total: 0, pageSize: 10 });
  const [expiryData, setExpiryData] = useState({ records: [], total: 0, pageSize: 10 });
  const [lowStockPageNum, setLowStockPageNum] = useState(1);
  const [expiryPageNum, setExpiryPageNum] = useState(1);
  const [expiryDays, setExpiryDays] = useState("30");
  const [loading, setLoading] = useState(false);
  const { message, showError } = useFlashMessage();

  async function loadLowStock(targetPage = lowStockPageNum) {
    setLoading(true);
    try {
      const data = await queryLowStockWarnings({ pageNum: targetPage, pageSize: 10 });
      setLowStockPageNum(data.pageNum);
      setLowStockData(data);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadExpiry(targetPage = expiryPageNum, targetDays = expiryDays) {
    setLoading(true);
    try {
      const data = await queryExpiryWarnings({
        pageNum: targetPage,
        pageSize: 10,
        expiryDays: targetDays,
      });
      setExpiryPageNum(data.pageNum);
      setExpiryData(data);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadLowStock(1);
    loadExpiry(1, "30");
  }, []);

  return (
    <section className="page-grid">
      <article className="panel">
        <p className="eyebrow">Warning</p>
        <h2>低库存预警</h2>
        <FeedbackMessage message={message} />
        {loading ? <p>加载中...</p> : null}
        <div className="table-list">
          {lowStockData.records.map((item) => (
            <article className="list-card" key={item.drugId}>
              <div>
                <strong>{item.drugName}</strong>
                <p>
                  {item.drugCode} / 可用库存 {item.availableQuantity} / 阈值 {item.lowStockThreshold}
                </p>
              </div>
            </article>
          ))}
        </div>
        <Pagination
          onChange={(nextPage) => loadLowStock(nextPage)}
          pageNum={lowStockPageNum}
          pageSize={lowStockData.pageSize || 10}
          total={lowStockData.total}
        />
      </article>

      <article className="panel">
        <p className="eyebrow">Expiry</p>
        <h2>效期预警</h2>
        <form
          className="inline-form"
          onSubmit={async (event) => {
            event.preventDefault();
            await loadExpiry(1, expiryDays);
          }}
        >
          <label>
            临期天数
            <input
              onChange={(event) => setExpiryDays(event.target.value)}
              type="number"
              value={expiryDays}
            />
          </label>
          <button type="submit">查询效期预警</button>
        </form>
        <div className="table-list">
          {expiryData.records.map((item) => (
            <article className="list-card" key={item.inventoryId}>
              <h3>{item.drugName}</h3>
              <p>
                {item.batchNo} / {item.warningType} / {item.expiryDate} / 剩余 {item.daysToExpiry} 天
              </p>
            </article>
          ))}
        </div>
        <Pagination
          onChange={(nextPage) => loadExpiry(nextPage, expiryDays)}
          pageNum={expiryPageNum}
          pageSize={expiryData.pageSize || 10}
          total={expiryData.total}
        />
      </article>
    </section>
  );
}

export default WarningPage;
