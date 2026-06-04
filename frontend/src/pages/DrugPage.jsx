import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import useApiAction from "../hooks/useApiAction";
import usePagedResource from "../hooks/usePagedResource";
import { createDrug, deleteDrug, queryDrugs } from "../api/drugs";

const initialForm = {
  drugCode: "",
  drugName: "",
  category: "",
  unit: "盒",
  purchasePrice: "0",
  salePrice: "0",
  lowStockThreshold: "0",
  enabled: "1",
};

function DrugPage() {
  const [query, setQuery] = useState({
    pageNum: 1,
    pageSize: 10,
    keyword: "",
  });
  const { data: result, loadResource: loadDrugResource, loading } = usePagedResource();
  const [form, setForm] = useState(initialForm);
  const { errorMessage, message, resetFeedback, runAction, submitting } = useApiAction();

  async function loadDrugs(nextQuery = query, options = {}) {
    if (options.resetFeedback) {
      resetFeedback();
    }

    try {
      await loadDrugResource(() => queryDrugs(nextQuery));
    } catch (error) {
      return null;
    }
  }

  useEffect(() => {
    loadDrugs(query);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleCreate(event) {
    event.preventDefault();

    try {
      await runAction(
        "create",
        () =>
          createDrug({
            ...form,
            purchasePrice: Number(form.purchasePrice),
            salePrice: Number(form.salePrice),
            lowStockThreshold: Number(form.lowStockThreshold),
            enabled: Number(form.enabled),
          }),
        "药品新增成功，列表已刷新。"
      );
      setForm(initialForm);
      await loadDrugs(query);
    } catch (error) {
      return null;
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("确认删除该药品吗？")) {
      return;
    }

    resetFeedback();

    try {
      await runAction("delete", () => deleteDrug(id), "药品已删除。");
      await loadDrugs(query);
    } catch (error) {
      return null;
    }
  }

  async function handleSearch(event) {
    event.preventDefault();
    const nextQuery = {
      ...query,
      pageNum: 1,
    };
    setQuery(nextQuery);
    await loadDrugs(nextQuery);
  }

  async function handlePageChange(nextPageNum) {
    const nextQuery = {
      ...query,
      pageNum: nextPageNum,
    };
    setQuery(nextQuery);
    await loadDrugs(nextQuery);
  }

  return (
    <section className="module-panel">
      <div className="section-heading">
        <div>
          <p className="section-kicker">Drug Module</p>
          <h2>药品页面联调</h2>
        </div>
        <button
          className="secondary-action"
          onClick={() => loadDrugs(query, { resetFeedback: true })}
          type="button"
        >
          刷新列表
        </button>
      </div>

      <div className="module-layout">
        <form className="module-card" onSubmit={handleSearch}>
          <h3>查询条件</h3>
          <label className="field">
            <span>关键字</span>
            <input
              name="keyword"
              onChange={(event) =>
                setQuery((current) => ({ ...current, keyword: event.target.value }))
              }
              placeholder="按药品名称或编码筛选"
              value={query.keyword}
            />
          </label>
          <button className="primary-action" type="submit">
            查询药品
          </button>
        </form>

        <form className="module-card" onSubmit={handleCreate}>
          <h3>新增药品</h3>
          <div className="form-grid">
            <label className="field">
              <span>药品编码</span>
              <input
                name="drugCode"
                onChange={(event) =>
                  setForm((current) => ({ ...current, drugCode: event.target.value }))
                }
                required
                value={form.drugCode}
              />
            </label>
            <label className="field">
              <span>药品名称</span>
              <input
                name="drugName"
                onChange={(event) =>
                  setForm((current) => ({ ...current, drugName: event.target.value }))
                }
                required
                value={form.drugName}
              />
            </label>
            <label className="field">
              <span>分类</span>
              <input
                name="category"
                onChange={(event) =>
                  setForm((current) => ({ ...current, category: event.target.value }))
                }
                value={form.category}
              />
            </label>
            <label className="field">
              <span>单位</span>
              <input
                name="unit"
                onChange={(event) =>
                  setForm((current) => ({ ...current, unit: event.target.value }))
                }
                required
                value={form.unit}
              />
            </label>
            <label className="field">
              <span>采购价</span>
              <input
                min="0"
                name="purchasePrice"
                onChange={(event) =>
                  setForm((current) => ({ ...current, purchasePrice: event.target.value }))
                }
                required
                step="0.01"
                type="number"
                value={form.purchasePrice}
              />
            </label>
            <label className="field">
              <span>销售价</span>
              <input
                min="0"
                name="salePrice"
                onChange={(event) =>
                  setForm((current) => ({ ...current, salePrice: event.target.value }))
                }
                required
                step="0.01"
                type="number"
                value={form.salePrice}
              />
            </label>
            <label className="field">
              <span>最低库存阈值</span>
              <input
                min="0"
                name="lowStockThreshold"
                onChange={(event) =>
                  setForm((current) => ({ ...current, lowStockThreshold: event.target.value }))
                }
                required
                step="1"
                type="number"
                value={form.lowStockThreshold}
              />
            </label>
            <label className="field">
              <span>启用状态</span>
              <select
                name="enabled"
                onChange={(event) =>
                  setForm((current) => ({ ...current, enabled: event.target.value }))
                }
                value={form.enabled}
              >
                <option value="1">启用</option>
                <option value="0">停用</option>
              </select>
            </label>
          </div>
          <button className="primary-action" disabled={submitting === "create"} type="submit">
            {submitting === "create" ? "提交中..." : "新增药品"}
          </button>
        </form>
      </div>

      {message ? <p className="feedback success-text">{message}</p> : null}
      {errorMessage ? <p className="feedback error-text">{errorMessage}</p> : null}

      <section className="module-card">
        <div className="list-header">
          <h3>药品列表</h3>
          <span>共 {result.total} 条</span>
        </div>
        {loading ? <p className="empty-state">正在加载药品数据...</p> : null}
        {!loading && result.records.length === 0 ? (
          <p className="empty-state">当前没有可展示的药品数据。</p>
        ) : null}
        <div className="drug-list">
          {result.records.map((drug) => (
            <article className="drug-card" key={drug.id}>
              <div className="drug-card-main">
                <p className="drug-code">{drug.drugCode}</p>
                <h4>{drug.drugName}</h4>
                <p className="drug-meta">
                  {drug.category || "未分类"} / {drug.unit} / 阈值 {drug.lowStockThreshold}
                </p>
                <p className="drug-meta">
                  采购价 {drug.purchasePrice} / 销售价 {drug.salePrice}
                </p>
              </div>
              <div className="drug-card-actions">
                <span className={drug.enabled === 1 ? "status-pill active" : "status-pill inactive"}>
                  {drug.enabled === 1 ? "启用中" : "已停用"}
                </span>
                <button
                  className="danger-action"
                  onClick={() => handleDelete(drug.id)}
                  type="button"
                >
                  删除
                </button>
              </div>
            </article>
          ))}
        </div>
        <Pagination
          onPageChange={handlePageChange}
          pageNum={result.pageNum}
          pageSize={result.pageSize}
          total={result.total}
        />
      </section>
    </section>
  );
}

export default DrugPage;
