import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import {
  checkInventory,
  inboundInventory,
  outboundInventory,
  queryInventories,
  queryInventoryRecords,
} from "../api/inventories";
import { loadCurrentUser } from "../auth";

function InventoryActionForm({
  title,
  submitText,
  initialState,
  onSubmit,
}) {
  const [form, setForm] = useState(initialState);

  return (
    <form
      className="panel form-panel"
      onSubmit={async (event) => {
        event.preventDefault();
        await onSubmit(form, () => setForm(initialState));
      }}
    >
      <h2>{title}</h2>
      <label>
        药品 ID
        <input
          onChange={(event) => setForm({ ...form, drugId: event.target.value })}
          value={form.drugId}
        />
      </label>
      {"batchNo" in form ? (
        <label>
          批次号
          <input
            onChange={(event) => setForm({ ...form, batchNo: event.target.value })}
            value={form.batchNo}
          />
        </label>
      ) : null}
      {"expiryDate" in form ? (
        <label>
          有效期
          <input
            onChange={(event) => setForm({ ...form, expiryDate: event.target.value })}
            value={form.expiryDate}
          />
        </label>
      ) : null}
      <label>
        数量
        <input
          onChange={(event) => setForm({ ...form, quantity: event.target.value })}
          type="number"
          value={form.quantity}
        />
      </label>
      {"locationCode" in form ? (
        <label>
          库位
          <input
            onChange={(event) => setForm({ ...form, locationCode: event.target.value })}
            value={form.locationCode}
          />
        </label>
      ) : null}
      <label>
        业务单号
        <input
          onChange={(event) => setForm({ ...form, bizNo: event.target.value })}
          value={form.bizNo}
        />
      </label>
      <label>
        备注
        <input
          onChange={(event) => setForm({ ...form, remark: event.target.value })}
          value={form.remark}
        />
      </label>
      <button type="submit">{submitText}</button>
    </form>
  );
}

function InventoryPage() {
  const [inventoryData, setInventoryData] = useState({ records: [], total: 0, pageSize: 10 });
  const [recordData, setRecordData] = useState({ records: [], total: 0, pageSize: 10 });
  const [inventoryPageNum, setInventoryPageNum] = useState(1);
  const [recordPageNum, setRecordPageNum] = useState(1);
  const [message, setMessage] = useState("");

  async function loadInventories(targetPage = inventoryPageNum) {
    const data = await queryInventories({ pageNum: targetPage, pageSize: 10 });
    setInventoryPageNum(data.pageNum);
    setInventoryData(data);
  }

  async function loadRecords(targetPage = recordPageNum) {
    const data = await queryInventoryRecords({ pageNum: targetPage, pageSize: 10 });
    setRecordPageNum(data.pageNum);
    setRecordData(data);
  }

  async function refreshAll() {
    await Promise.all([loadInventories(1), loadRecords(1)]);
  }

  useEffect(() => {
    refreshAll();
  }, []);

  const currentUser = loadCurrentUser();

  async function handleInventoryAction(action, form, reset) {
    const payload = {
      ...form,
      drugId: Number(form.drugId),
      quantity: Number(form.quantity),
      operatorName: currentUser?.userName || "",
    };

    await action(payload);
    reset();
    setMessage("入库成功，库存和流水已刷新。");
    await refreshAll();
  }

  return (
    <section className="page-stack">
      <article className="panel">
        <p className="eyebrow">Inventory</p>
        <h2>库存模块联调</h2>
        {message ? <p className="message success">{message}</p> : null}
        <div className="two-column">
          <div>
            <h3>库存列表</h3>
            <div className="table-list">
              {inventoryData.records.map((item) => (
                <article className="list-card" key={item.id}>
                  <div>
                    <strong>{item.drugName}</strong>
                    <p>
                      {item.batchNo} / 数量 {item.quantity} / 有效期 {item.expiryDate}
                    </p>
                  </div>
                </article>
              ))}
            </div>
            <Pagination
              onChange={(nextPage) => loadInventories(nextPage)}
              pageNum={inventoryPageNum}
              pageSize={inventoryData.pageSize || 10}
              total={inventoryData.total}
            />
          </div>
          <div>
            <h3>库存流水</h3>
            <div className="table-list">
              {recordData.records.map((item) => (
                <article className="list-card" key={item.id}>
                  <div>
                    <strong>{item.recordType}</strong>
                    <p>
                      {item.drugCode} / {item.batchNo} / 变更 {item.quantityChange}
                    </p>
                  </div>
                </article>
              ))}
            </div>
            <Pagination
              onChange={(nextPage) => loadRecords(nextPage)}
              pageNum={recordPageNum}
              pageSize={recordData.pageSize || 10}
              total={recordData.total}
            />
          </div>
        </div>
      </article>

      <section className="page-grid three-columns">
        <InventoryActionForm
          initialState={{
            drugId: "",
            batchNo: "",
            expiryDate: "",
            quantity: 0,
            locationCode: "",
            bizNo: "",
            remark: "",
          }}
          onSubmit={(form, reset) => handleInventoryAction(inboundInventory, form, reset)}
          submitText="提交入库"
          title="库存入库"
        />
        <InventoryActionForm
          initialState={{
            drugId: "",
            quantity: 0,
            bizNo: "",
            remark: "",
          }}
          onSubmit={(form, reset) => handleInventoryAction(outboundInventory, form, reset)}
          submitText="提交出库"
          title="库存出库"
        />
        <InventoryActionForm
          initialState={{
            drugId: "",
            quantity: 0,
            bizNo: "",
            remark: "",
          }}
          onSubmit={(form, reset) => handleInventoryAction(checkInventory, form, reset)}
          submitText="提交盘点"
          title="库存盘点"
        />
      </section>
    </section>
  );
}

export default InventoryPage;
