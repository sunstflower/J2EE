import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import usePagedResource from "../hooks/usePagedResource";
import { queryExpiryWarnings, queryLowStockWarnings } from "../api/warnings";

function WarningPage() {
  const [lowStockQuery, setLowStockQuery] = useState({
    pageNum: 1,
    pageSize: 10,
  });
  const [expiryQuery, setExpiryQuery] = useState({
    pageNum: 1,
    pageSize: 10,
    expiryDays: 30,
  });
  const {
    data: lowStockResult,
    loadResource: loadLowStockResource,
    loading: loadingLowStock,
  } = usePagedResource();
  const {
    data: expiryResult,
    loadResource: loadExpiryResource,
    loading: loadingExpiry,
  } = usePagedResource();
  const [errorMessage, setErrorMessage] = useState("");

  async function loadLowStockWarnings(nextQuery = lowStockQuery) {
    setErrorMessage("");

    try {
      await loadLowStockResource(() => queryLowStockWarnings(nextQuery));
    } catch (error) {
      setErrorMessage(error.message);
    }
  }

  async function loadExpiryWarnings(nextQuery = expiryQuery) {
    setErrorMessage("");

    try {
      await loadExpiryResource(() => queryExpiryWarnings(nextQuery));
    } catch (error) {
      setErrorMessage(error.message);
    }
  }

  useEffect(() => {
    loadLowStockWarnings(lowStockQuery);
    loadExpiryWarnings(expiryQuery);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleExpirySearch(event) {
    event.preventDefault();
    const nextQuery = {
      ...expiryQuery,
      pageNum: 1,
      expiryDays: Number(expiryQuery.expiryDays),
    };
    setExpiryQuery(nextQuery);
    await loadExpiryWarnings(nextQuery);
  }

  async function handleLowStockPageChange(nextPageNum) {
    const nextQuery = {
      ...lowStockQuery,
      pageNum: nextPageNum,
    };
    setLowStockQuery(nextQuery);
    await loadLowStockWarnings(nextQuery);
  }

  async function handleExpiryPageChange(nextPageNum) {
    const nextQuery = {
      ...expiryQuery,
      pageNum: nextPageNum,
    };
    setExpiryQuery(nextQuery);
    await loadExpiryWarnings(nextQuery);
  }

  return (
    <section className="module-panel">
      <div className="section-heading">
        <div>
          <p className="section-kicker">Warning Module</p>
          <h2>预警模块联调</h2>
        </div>
        <div className="button-row">
          <button className="secondary-action" onClick={() => loadLowStockWarnings(lowStockQuery)} type="button">
            刷新低库存
          </button>
          <button className="secondary-action" onClick={() => loadExpiryWarnings(expiryQuery)} type="button">
            刷新效期预警
          </button>
        </div>
      </div>

      {errorMessage ? <p className="feedback error-text">{errorMessage}</p> : null}

      <div className="warning-layout">
        <section className="module-card">
          <div className="list-header">
            <h3>低库存预警</h3>
            <span>共 {lowStockResult.total} 条</span>
          </div>
          {loadingLowStock ? <p className="empty-state">正在加载低库存预警...</p> : null}
          {!loadingLowStock && lowStockResult.records.length === 0 ? (
            <p className="empty-state">当前没有低库存预警数据。</p>
          ) : null}
          <div className="warning-list">
            {lowStockResult.records.map((warning) => (
              <article className="warning-card low-stock-card" key={warning.drugId}>
                <div className="drug-card-main">
                  <p className="drug-code">{warning.drugCode}</p>
                  <h4>{warning.drugName}</h4>
                  <p className="drug-meta">
                    当前可用库存 {warning.availableQuantity} / 阈值 {warning.lowStockThreshold}
                  </p>
                </div>
                <div className="drug-card-actions">
                  <span className="status-pill warning-pill">低库存</span>
                </div>
              </article>
            ))}
          </div>
          <Pagination
            onPageChange={handleLowStockPageChange}
            pageNum={lowStockResult.pageNum}
            pageSize={lowStockResult.pageSize}
            total={lowStockResult.total}
          />
        </section>

        <section className="module-card">
          <div className="list-header">
            <h3>临期 / 过期预警</h3>
            <span>共 {expiryResult.total} 条</span>
          </div>
          <form className="inline-filter warning-filter" onSubmit={handleExpirySearch}>
            <label className="field inline-field">
              <span>临期天数</span>
              <input
                min="0"
                name="expiryDays"
                onChange={(event) =>
                  setExpiryQuery((current) => ({ ...current, expiryDays: event.target.value }))
                }
                type="number"
                value={expiryQuery.expiryDays}
              />
            </label>
            <button className="primary-action" type="submit">
              查询效期预警
            </button>
          </form>
          {loadingExpiry ? <p className="empty-state">正在加载效期预警...</p> : null}
          {!loadingExpiry && expiryResult.records.length === 0 ? (
            <p className="empty-state">当前没有临期或过期预警数据。</p>
          ) : null}
          <div className="warning-list">
            {expiryResult.records.map((warning) => (
              <article className="warning-card expiry-card" key={warning.inventoryId}>
                <div className="drug-card-main">
                  <p className="drug-code">{warning.drugCode}</p>
                  <h4>
                    {warning.drugName} / 批次 {warning.batchNo}
                  </h4>
                  <p className="drug-meta">
                    到期日 {warning.expiryDate} / 数量 {warning.quantity}
                  </p>
                  <p className="drug-meta">
                    剩余天数 {warning.daysToExpiry} / 库存ID {warning.inventoryId}
                  </p>
                </div>
                <div className="drug-card-actions">
                  <span
                    className={
                      warning.warningType === "EXPIRED"
                        ? "status-pill danger-pill"
                        : "status-pill expiry-pill"
                    }
                  >
                    {warning.warningType === "EXPIRED" ? "已过期" : "临期"}
                  </span>
                </div>
              </article>
            ))}
          </div>
          <Pagination
            onPageChange={handleExpiryPageChange}
            pageNum={expiryResult.pageNum}
            pageSize={expiryResult.pageSize}
            total={expiryResult.total}
          />
        </section>
      </div>
    </section>
  );
}

export default WarningPage;
