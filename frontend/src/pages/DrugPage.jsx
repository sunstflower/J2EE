import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import FeedbackMessage from "../components/FeedbackMessage";
import { createDrug, deleteDrug, queryDrugs } from "../api/drugs";
import useFlashMessage from "../hooks/useFlashMessage";

function buildInitialForm() {
  return {
    drugCode: "",
    drugName: "",
    genericName: "",
    category: "",
    specification: "",
    unit: "",
    manufacturer: "",
    approvalNumber: "",
    purchasePrice: 0,
    salePrice: 0,
    lowStockThreshold: 0,
    enabled: true,
  };
}

function DrugPage() {
  const [pageNum, setPageNum] = useState(1);
  const [pageData, setPageData] = useState({ records: [], total: 0, pageSize: 10 });
  const [form, setForm] = useState(buildInitialForm());
  const [loading, setLoading] = useState(false);
  const { message, showError, showSuccess } = useFlashMessage();

  async function loadData(targetPage = pageNum) {
    setLoading(true);
    try {
      const data = await queryDrugs({ pageNum: targetPage, pageSize: 10 });
      setPageNum(data.pageNum);
      setPageData(data);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData(1);
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    try {
      await createDrug({
        ...form,
        purchasePrice: Number(form.purchasePrice),
        salePrice: Number(form.salePrice),
        lowStockThreshold: Number(form.lowStockThreshold),
      });
      showSuccess("药品创建成功，列表已刷新。");
      setForm(buildInitialForm());
      await loadData(1);
    } catch (error) {
      showError(error.message);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("确认删除该药品吗？")) {
      return;
    }
    try {
      await deleteDrug(id);
      showSuccess("药品删除成功，列表已刷新。");
      await loadData(pageNum);
    } catch (error) {
      showError(error.message);
    }
  }

  return (
    <section className="page-grid">
      <article className="panel">
        <p className="eyebrow">Drug</p>
        <h2>药品模块联调</h2>
        <FeedbackMessage message={message} />
        {loading ? <p>加载中...</p> : null}
        <div className="table-list">
          {pageData.records.map((drug) => (
            <article className="list-card" key={drug.id}>
              <div>
                <h3>{drug.drugName}</h3>
                <p>
                  {drug.drugCode} / {drug.category || "未分类"} / 阈值 {drug.lowStockThreshold}
                </p>
              </div>
              <button onClick={() => handleDelete(drug.id)} type="button">
                删除
              </button>
            </article>
          ))}
        </div>
        <Pagination
          onChange={(nextPage) => loadData(nextPage)}
          pageNum={pageNum}
          pageSize={pageData.pageSize || 10}
          total={pageData.total}
        />
      </article>

      <form className="panel form-panel" onSubmit={handleSubmit}>
        <p className="eyebrow">Create</p>
        <h2>新增药品</h2>
        <label>
          药品编码
          <input
            onChange={(event) => setForm({ ...form, drugCode: event.target.value })}
            value={form.drugCode}
          />
        </label>
        <label>
          药品名称
          <input
            onChange={(event) => setForm({ ...form, drugName: event.target.value })}
            value={form.drugName}
          />
        </label>
        <label>
          分类
          <input
            onChange={(event) => setForm({ ...form, category: event.target.value })}
            value={form.category}
          />
        </label>
        <label>
          单位
          <input
            onChange={(event) => setForm({ ...form, unit: event.target.value })}
            value={form.unit}
          />
        </label>
        <label>
          采购价
          <input
            onChange={(event) => setForm({ ...form, purchasePrice: event.target.value })}
            type="number"
            value={form.purchasePrice}
          />
        </label>
        <label>
          销售价
          <input
            onChange={(event) => setForm({ ...form, salePrice: event.target.value })}
            type="number"
            value={form.salePrice}
          />
        </label>
        <label>
          最低库存阈值
          <input
            onChange={(event) => setForm({ ...form, lowStockThreshold: event.target.value })}
            type="number"
            value={form.lowStockThreshold}
          />
        </label>
        <button type="submit">提交药品</button>
      </form>
    </section>
  );
}

export default DrugPage;
