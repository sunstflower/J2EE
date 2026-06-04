import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import useApiAction from "../hooks/useApiAction";
import usePagedResource from "../hooks/usePagedResource";
import { loadCurrentUser } from "../auth";
import {
  checkInventory,
  inboundInventory,
  outboundInventory,
  queryInventories,
  queryInventoryRecords,
} from "../api/inventories";

const initialInboundForm = {
  drugId: "",
  batchNo: "",
  expiryDate: "",
  quantity: "1",
  locationCode: "",
  bizNo: "",
  remark: "",
};

const initialOutboundForm = {
  drugId: "",
  quantity: "1",
  bizNo: "",
  remark: "",
};

const initialCheckForm = {
  inventoryId: "",
  actualQuantity: "0",
  bizNo: "",
  remark: "",
};

function InventoryPage() {
  const [inventoryQuery, setInventoryQuery] = useState({
    pageNum: 1,
    pageSize: 10,
    keyword: "",
    drugId: "",
  });
  const [recordQuery, setRecordQuery] = useState({
    pageNum: 1,
    pageSize: 10,
    drugId: "",
    recordType: "",
    bizNo: "",
  });
  const {
    data: inventories,
    loadResource: loadInventoryResource,
    loading: loadingInventories,
  } = usePagedResource();
  const {
    data: records,
    loadResource: loadRecordResource,
    loading: loadingRecords,
  } = usePagedResource();
  const [inboundForm, setInboundForm] = useState(initialInboundForm);
  const [outboundForm, setOutboundForm] = useState(initialOutboundForm);
  const [checkForm, setCheckForm] = useState(initialCheckForm);
  const { errorMessage, message, runAction, submitting } = useApiAction();

  async function loadInventories(nextQuery = inventoryQuery) {
    try {
      await loadInventoryResource(() => queryInventories(nextQuery));
    } catch (error) {
      return null;
    }
  }

  async function loadRecords(nextQuery = recordQuery) {
    try {
      await loadRecordResource(() => queryInventoryRecords(nextQuery));
    } catch (error) {
      return null;
    }
  }

  useEffect(() => {
    loadInventories(inventoryQuery);
    loadRecords(recordQuery);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function getOperatorName() {
    const currentUser = loadCurrentUser();
    return currentUser?.userName || "";
  }

  async function handleInbound(event) {
    event.preventDefault();

    try {
      await runAction(
        "inbound",
        () =>
          inboundInventory({
            ...inboundForm,
            drugId: Number(inboundForm.drugId),
            quantity: Number(inboundForm.quantity),
            operatorName: getOperatorName(),
          }),
        "入库成功，库存和流水已刷新。"
      );
      setInboundForm(initialInboundForm);
      await Promise.all([loadInventories(inventoryQuery), loadRecords(recordQuery)]);
    } catch (error) {
      return null;
    }
  }

  async function handleOutbound(event) {
    event.preventDefault();

    try {
      await runAction(
        "outbound",
        () =>
          outboundInventory({
            ...outboundForm,
            drugId: Number(outboundForm.drugId),
            quantity: Number(outboundForm.quantity),
            operatorName: getOperatorName(),
          }),
        "出库成功，库存和流水已刷新。"
      );
      setOutboundForm(initialOutboundForm);
      await Promise.all([loadInventories(inventoryQuery), loadRecords(recordQuery)]);
    } catch (error) {
      return null;
    }
  }

  async function handleCheck(event) {
    event.preventDefault();

    try {
      await runAction(
        "check",
        () =>
          checkInventory({
            ...checkForm,
            inventoryId: Number(checkForm.inventoryId),
            actualQuantity: Number(checkForm.actualQuantity),
            operatorName: getOperatorName(),
          }),
        "盘点成功，库存和流水已刷新。"
      );
      setCheckForm(initialCheckForm);
      await Promise.all([loadInventories(inventoryQuery), loadRecords(recordQuery)]);
    } catch (error) {
      return null;
    }
  }

  async function handleInventorySearch(event) {
    event.preventDefault();
    const nextQuery = {
      ...inventoryQuery,
      pageNum: 1,
    };
    setInventoryQuery(nextQuery);
    await loadInventories(nextQuery);
  }

  async function handleRecordSearch(event) {
    event.preventDefault();
    const nextQuery = {
      ...recordQuery,
      pageNum: 1,
    };
    setRecordQuery(nextQuery);
    await loadRecords(nextQuery);
  }

  async function handleInventoryPageChange(nextPageNum) {
    const nextQuery = {
      ...inventoryQuery,
      pageNum: nextPageNum,
    };
    setInventoryQuery(nextQuery);
    await loadInventories(nextQuery);
  }

  async function handleRecordPageChange(nextPageNum) {
    const nextQuery = {
      ...recordQuery,
      pageNum: nextPageNum,
    };
    setRecordQuery(nextQuery);
    await loadRecords(nextQuery);
  }

  return (
    <section className="module-panel">
      <div className="section-heading">
        <div>
          <p className="section-kicker">Inventory Module</p>
          <h2>库存模块联调</h2>
        </div>
        <div className="button-row">
          <button className="secondary-action" onClick={() => loadInventories(inventoryQuery)} type="button">
            刷新库存
          </button>
          <button className="secondary-action" onClick={() => loadRecords(recordQuery)} type="button">
            刷新流水
          </button>
        </div>
      </div>

      <div className="inventory-layout">
        <section className="module-stack">
          <form className="module-card" onSubmit={handleInventorySearch}>
            <h3>库存查询</h3>
            <div className="form-grid compact-grid">
              <label className="field">
                <span>药品 ID</span>
                <input
                  name="drugId"
                  onChange={(event) =>
                    setInventoryQuery((current) => ({ ...current, drugId: event.target.value }))
                  }
                  placeholder="按药品 ID 筛选"
                  value={inventoryQuery.drugId}
                />
              </label>
              <label className="field">
                <span>关键字</span>
                <input
                  name="keyword"
                  onChange={(event) =>
                    setInventoryQuery((current) => ({ ...current, keyword: event.target.value }))
                  }
                  placeholder="按药品名称或编码筛选"
                  value={inventoryQuery.keyword}
                />
              </label>
            </div>
            <button className="primary-action" type="submit">
              查询库存
            </button>
          </form>

          <section className="module-card">
            <div className="list-header">
              <h3>库存列表</h3>
              <span>共 {inventories.total} 条</span>
            </div>
            {loadingInventories ? <p className="empty-state">正在加载库存数据...</p> : null}
            {!loadingInventories && inventories.records.length === 0 ? (
              <p className="empty-state">当前没有可展示的库存数据。</p>
            ) : null}
            <div className="inventory-list">
              {inventories.records.map((inventory) => (
                <article className="inventory-card" key={inventory.id}>
                  <div className="drug-card-main">
                    <p className="drug-code">{inventory.drugCode}</p>
                    <h4>{inventory.drugName}</h4>
                    <p className="drug-meta">
                      批次 {inventory.batchNo} / 库位 {inventory.locationCode || "未设置"}
                    </p>
                    <p className="drug-meta">
                      到期日 {inventory.expiryDate} / 可用 {inventory.quantity} / 锁定 {inventory.lockedQuantity}
                    </p>
                  </div>
                  <div className="drug-card-actions">
                    <span className="status-pill active">库存ID {inventory.id}</span>
                  </div>
                </article>
              ))}
            </div>
            <Pagination
              onPageChange={handleInventoryPageChange}
              pageNum={inventories.pageNum}
              pageSize={inventories.pageSize}
              total={inventories.total}
            />
          </section>
        </section>

        <section className="module-stack">
          <form className="module-card" onSubmit={handleInbound}>
            <h3>库存入库</h3>
            <div className="form-grid">
              <label className="field">
                <span>药品 ID</span>
                <input
                  name="drugId"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, drugId: event.target.value }))
                  }
                  required
                  value={inboundForm.drugId}
                />
              </label>
              <label className="field">
                <span>批次号</span>
                <input
                  name="batchNo"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, batchNo: event.target.value }))
                  }
                  required
                  value={inboundForm.batchNo}
                />
              </label>
              <label className="field">
                <span>有效期</span>
                <input
                  name="expiryDate"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, expiryDate: event.target.value }))
                  }
                  required
                  type="date"
                  value={inboundForm.expiryDate}
                />
              </label>
              <label className="field">
                <span>数量</span>
                <input
                  min="1"
                  name="quantity"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, quantity: event.target.value }))
                  }
                  required
                  type="number"
                  value={inboundForm.quantity}
                />
              </label>
              <label className="field">
                <span>库位</span>
                <input
                  name="locationCode"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, locationCode: event.target.value }))
                  }
                  value={inboundForm.locationCode}
                />
              </label>
              <label className="field">
                <span>业务单号</span>
                <input
                  name="bizNo"
                  onChange={(event) =>
                    setInboundForm((current) => ({ ...current, bizNo: event.target.value }))
                  }
                  required
                  value={inboundForm.bizNo}
                />
              </label>
            </div>
            <label className="field">
              <span>备注</span>
              <input
                name="remark"
                onChange={(event) =>
                  setInboundForm((current) => ({ ...current, remark: event.target.value }))
                }
                value={inboundForm.remark}
              />
            </label>
            <button className="primary-action" disabled={submitting === "inbound"} type="submit">
              {submitting === "inbound" ? "提交中..." : "提交入库"}
            </button>
          </form>

          <div className="form-grid two-column-grid">
            <form className="module-card" onSubmit={handleOutbound}>
              <h3>库存出库</h3>
              <label className="field">
                <span>药品 ID</span>
                <input
                  name="drugId"
                  onChange={(event) =>
                    setOutboundForm((current) => ({ ...current, drugId: event.target.value }))
                  }
                  required
                  value={outboundForm.drugId}
                />
              </label>
              <label className="field">
                <span>出库数量</span>
                <input
                  min="1"
                  name="quantity"
                  onChange={(event) =>
                    setOutboundForm((current) => ({ ...current, quantity: event.target.value }))
                  }
                  required
                  type="number"
                  value={outboundForm.quantity}
                />
              </label>
              <label className="field">
                <span>业务单号</span>
                <input
                  name="bizNo"
                  onChange={(event) =>
                    setOutboundForm((current) => ({ ...current, bizNo: event.target.value }))
                  }
                  required
                  value={outboundForm.bizNo}
                />
              </label>
              <label className="field">
                <span>备注</span>
                <input
                  name="remark"
                  onChange={(event) =>
                    setOutboundForm((current) => ({ ...current, remark: event.target.value }))
                  }
                  value={outboundForm.remark}
                />
              </label>
              <button className="primary-action" disabled={submitting === "outbound"} type="submit">
                {submitting === "outbound" ? "提交中..." : "提交出库"}
              </button>
            </form>

            <form className="module-card" onSubmit={handleCheck}>
              <h3>库存盘点</h3>
              <label className="field">
                <span>库存 ID</span>
                <input
                  name="inventoryId"
                  onChange={(event) =>
                    setCheckForm((current) => ({ ...current, inventoryId: event.target.value }))
                  }
                  required
                  value={checkForm.inventoryId}
                />
              </label>
              <label className="field">
                <span>实盘数量</span>
                <input
                  min="0"
                  name="actualQuantity"
                  onChange={(event) =>
                    setCheckForm((current) => ({ ...current, actualQuantity: event.target.value }))
                  }
                  required
                  type="number"
                  value={checkForm.actualQuantity}
                />
              </label>
              <label className="field">
                <span>业务单号</span>
                <input
                  name="bizNo"
                  onChange={(event) =>
                    setCheckForm((current) => ({ ...current, bizNo: event.target.value }))
                  }
                  required
                  value={checkForm.bizNo}
                />
              </label>
              <label className="field">
                <span>备注</span>
                <input
                  name="remark"
                  onChange={(event) =>
                    setCheckForm((current) => ({ ...current, remark: event.target.value }))
                  }
                  value={checkForm.remark}
                />
              </label>
              <button className="primary-action" disabled={submitting === "check"} type="submit">
                {submitting === "check" ? "提交中..." : "提交盘点"}
              </button>
            </form>
          </div>
        </section>
      </div>

      {message ? <p className="feedback success-text">{message}</p> : null}
      {errorMessage ? <p className="feedback error-text">{errorMessage}</p> : null}

      <section className="module-card">
        <div className="list-header">
          <h3>库存流水</h3>
          <span>共 {records.total} 条</span>
        </div>
        <form className="inline-filter" onSubmit={handleRecordSearch}>
          <label className="field inline-field">
            <span>药品 ID</span>
            <input
              name="drugId"
              onChange={(event) =>
                setRecordQuery((current) => ({ ...current, drugId: event.target.value }))
              }
              value={recordQuery.drugId}
            />
          </label>
          <label className="field inline-field">
            <span>流水类型</span>
            <input
              name="recordType"
              onChange={(event) =>
                setRecordQuery((current) => ({ ...current, recordType: event.target.value }))
              }
              placeholder="INBOUND / OUTBOUND / CHECK"
              value={recordQuery.recordType}
            />
          </label>
          <label className="field inline-field">
            <span>业务单号</span>
            <input
              name="bizNo"
              onChange={(event) =>
                setRecordQuery((current) => ({ ...current, bizNo: event.target.value }))
              }
              value={recordQuery.bizNo}
            />
          </label>
          <button className="primary-action" type="submit">
            查询流水
          </button>
        </form>
        {loadingRecords ? <p className="empty-state">正在加载库存流水...</p> : null}
        {!loadingRecords && records.records.length === 0 ? (
          <p className="empty-state">当前没有可展示的库存流水。</p>
        ) : null}
        <div className="record-list">
          {records.records.map((record) => (
            <article className="record-card" key={record.id}>
              <div className="drug-card-main">
                <p className="drug-code">{record.recordType}</p>
                <h4>
                  {record.drugName} / 批次 {record.batchNo}
                </h4>
                <p className="drug-meta">
                  变更 {record.quantityChange}，库存 {record.beforeQuantity} → {record.afterQuantity}
                </p>
                <p className="drug-meta">
                  业务单号 {record.bizNo} / 操作人 {record.operatorName}
                </p>
              </div>
              <div className="drug-card-actions">
                <span className="status-pill inactive">库存ID {record.inventoryId}</span>
              </div>
            </article>
          ))}
        </div>
        <Pagination
          onPageChange={handleRecordPageChange}
          pageNum={records.pageNum}
          pageSize={records.pageSize}
          total={records.total}
        />
      </section>
    </section>
  );
}

export default InventoryPage;
