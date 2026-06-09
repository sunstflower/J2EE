import { useEffect, useState } from "react";
import { queryDrugs } from "../api/drugs";
import { inboundInventory, queryInventories } from "../api/inventories";
import FeedbackMessage from "../components/FeedbackMessage";
import useFlashMessage from "../hooks/useFlashMessage";

function buildInitialForm() {
  return {
    drugId: "",
    batchNo: "",
    expiryDate: "",
    quantity: 1,
    locationCode: "",
    bizNo: "",
    remark: "",
  };
}

function InboundPage() {
  const [drugData, setDrugData] = useState({ records: [] });
  const [recentData, setRecentData] = useState({ records: [] });
  const [form, setForm] = useState(buildInitialForm());
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const { message, showError, showSuccess } = useFlashMessage();

  async function loadPageData() {
    setLoading(true);
    try {
      const [drugs, inventories] = await Promise.all([
        queryDrugs({ pageNum: 1, pageSize: 50, enabled: 1 }),
        queryInventories({ pageNum: 1, pageSize: 5 }),
      ]);
      setDrugData(drugs);
      setRecentData(inventories);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadPageData();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    try {
      await inboundInventory({
        ...form,
        drugId: Number(form.drugId),
        quantity: Number(form.quantity),
      });
      showSuccess("入库成功，库存预览可查看最新数量。");
      setForm(buildInitialForm());
      await loadPageData();
    } catch (error) {
      showError(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page-grid">
      <form className="panel form-panel" onSubmit={handleSubmit}>
        <p className="eyebrow">Inbound</p>
        <h2>药物入库</h2>
        <p>演示补货流程。入库完成后，右侧会展示最新库存记录。</p>
        <FeedbackMessage message={message} />
        <label>
          药品
          <select
            onChange={(event) => setForm({ ...form, drugId: event.target.value })}
            value={form.drugId}
          >
            <option value="">请选择药品</option>
            {drugData.records.map((drug) => (
              <option key={drug.id} value={drug.id}>
                {drug.drugName} ({drug.drugCode})
              </option>
            ))}
          </select>
        </label>
        <label>
          批次号
          <input
            onChange={(event) => setForm({ ...form, batchNo: event.target.value })}
            value={form.batchNo}
          />
        </label>
        <label>
          有效期
          <input
            onChange={(event) => setForm({ ...form, expiryDate: event.target.value })}
            type="date"
            value={form.expiryDate}
          />
        </label>
        <label>
          数量
          <input
            min="1"
            onChange={(event) => setForm({ ...form, quantity: event.target.value })}
            type="number"
            value={form.quantity}
          />
        </label>
        <label>
          库位
          <input
            onChange={(event) => setForm({ ...form, locationCode: event.target.value })}
            value={form.locationCode}
          />
        </label>
        <label>
          业务单号
          <input
            onChange={(event) => setForm({ ...form, bizNo: event.target.value })}
            value={form.bizNo}
          />
        </label>
        <label>
          备注
          <textarea
            onChange={(event) => setForm({ ...form, remark: event.target.value })}
            rows="3"
            value={form.remark}
          />
        </label>
        <button disabled={submitting} type="submit">
          {submitting ? "提交中..." : "提交入库"}
        </button>
      </form>

      <article className="panel">
        <p className="eyebrow">Recent Inventory</p>
        <h2>最近库存概览</h2>
        {loading ? <p>加载中...</p> : null}
        <div className="table-list">
          {recentData.records.map((item) => (
            <article className="list-card" key={item.id}>
              <div>
                <h3>{item.drugName}</h3>
                <p>
                  批次 {item.batchNo} / 数量 {item.quantity}
                </p>
                <p>
                  库位 {item.locationCode || "未设置"} / 有效期 {item.expiryDate}
                </p>
              </div>
            </article>
          ))}
        </div>
      </article>
    </section>
  );
}

export default InboundPage;
