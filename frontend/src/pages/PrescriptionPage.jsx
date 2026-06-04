import { useEffect, useState } from "react";
import FeedbackMessage from "../components/FeedbackMessage";
import Pagination from "../components/Pagination";
import {
  auditPrescription,
  createPrescription,
  getPrescription,
  queryPrescriptions,
} from "../api/prescriptions";
import { queryDrugs } from "../api/drugs";
import { loadCurrentUser } from "../auth";
import useFlashMessage from "../hooks/useFlashMessage";

function buildCreateForm(currentUser) {
  return {
    patientName: "",
    doctorId: currentUser?.role === "DOCTOR" ? currentUser.userId : "",
    doctorName: currentUser?.role === "DOCTOR" ? currentUser.userName : "",
    createdByUserId: currentUser?.userId || "",
    createdByUserName: currentUser?.userName || "",
    createdByRole: currentUser?.role || "",
    items: [
      {
        drugId: "",
        dosage: "",
        frequency: "",
        days: 3,
        quantity: 1,
      },
    ],
  };
}

function PrescriptionPage() {
  const currentUser = loadCurrentUser();
  const [pageNum, setPageNum] = useState(1);
  const [pageData, setPageData] = useState({ records: [], total: 0, pageSize: 10 });
  const [drugs, setDrugs] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState(() => buildCreateForm(currentUser));
  const { message, showError, showSuccess } = useFlashMessage();

  async function loadList(targetPage = pageNum) {
    const data = await queryPrescriptions({ pageNum: targetPage, pageSize: 10 });
    const nextRecords = data?.records || [];
    setPageNum(data.pageNum);
    setPageData({
      records: nextRecords,
      total: data?.total || 0,
      pageNum: data?.pageNum || targetPage,
      pageSize: data?.pageSize || 10,
    });
    if (nextRecords.length > 0) {
      setSelectedId((currentSelectedId) => currentSelectedId || nextRecords[0].id);
    }
    return {
      ...data,
      records: nextRecords,
    };
  }

  async function loadDrugs() {
    const data = await queryDrugs({ pageNum: 1, pageSize: 100 });
    setDrugs(data.records || []);
  }

  async function loadDetail(id) {
    if (!id) {
      return;
    }
    const data = await getPrescription(id);
    setDetail(data);
  }

  useEffect(() => {
    async function initialize() {
      setLoading(true);
      try {
        const listData = await loadList(1);
        await loadDrugs();
        if (listData.records.length > 0) {
          await loadDetail(listData.records[0].id);
        }
      } catch (error) {
        showError(error.message);
      } finally {
        setLoading(false);
      }
    }

    initialize();
  }, []);

  async function handleCreate(event) {
    event.preventDefault();
    try {
      const payload = {
        ...form,
        doctorId: Number(form.doctorId),
        createdByUserId: Number(form.createdByUserId),
        items: form.items.map((item) => ({
          ...item,
          drugId: Number(item.drugId),
          days: Number(item.days),
          quantity: Number(item.quantity),
        })),
      };
      const newId = await createPrescription(payload);
      showSuccess("处方创建成功，列表和详情已刷新。");
      setForm(buildCreateForm(currentUser));
      const listData = await loadList(1);
      setSelectedId(newId || listData.records[0]?.id || null);
      if (newId) {
        await loadDetail(newId);
      }
    } catch (error) {
      showError(error.message);
    }
  }

  async function handleAudit(action) {
    if (!selectedId) {
      return;
    }
    try {
      await auditPrescription(selectedId, {
        action,
        operatorId: currentUser.userId,
        operatorName: currentUser.userName,
      });
      showSuccess("处方状态已更新，详情已刷新。");
      await loadList(1);
      await loadDetail(selectedId);
    } catch (error) {
      showError(error.message);
    }
  }

  const currentItem = form.items[0];

  return (
    <section className="page-stack">
      <article className="panel">
        <p className="eyebrow">Prescription</p>
        <h2>处方工作台</h2>
        <FeedbackMessage message={message} />
        {loading ? <p>加载中...</p> : null}
        <div className="two-column">
          <div>
            <h3>处方列表</h3>
            <div className="table-list">
              {pageData.records.map((item) => (
                <button
                  className={`list-card selectable ${selectedId === item.id ? "selected" : ""}`}
                  key={item.id}
                  onClick={async () => {
                    setSelectedId(item.id);
                    await loadDetail(item.id);
                  }}
                  type="button"
                >
                  <div>
                    <strong>{item.prescriptionNo}</strong>
                    <p>
                      {item.patientName} / {item.status} / {item.createdByRole}
                    </p>
                  </div>
                </button>
              ))}
            </div>
            <Pagination
              onChange={(nextPage) => loadList(nextPage)}
              pageNum={pageNum}
              pageSize={pageData.pageSize || 10}
              total={pageData.total}
            />
          </div>
          <div>
            <h3>处方详情</h3>
            {detail ? (
              <article className="detail-card">
                <p>编号：{detail.prescriptionNo}</p>
                <p>患者：{detail.patientName}</p>
                <p>状态：{detail.status}</p>
                <p>医生：{detail.doctorName}</p>
                <ul className="detail-list">
                  {(detail.items || []).map((item) => (
                    <li key={item.id || item.drugId}>
                      {item.drugName} / {item.dosage} / {item.frequency} / {item.quantity}
                    </li>
                  ))}
                </ul>
                {currentUser?.role === "PHARMACIST" ? (
                  <div className="inline-actions">
                    <button onClick={() => handleAudit("APPROVE")} type="button">
                      药师审核通过
                    </button>
                  </div>
                ) : null}
              </article>
            ) : (
              <p>暂无详情</p>
            )}
          </div>
        </div>
      </article>

      {currentUser?.role === "DOCTOR" ? (
        <form className="panel form-panel" onSubmit={handleCreate}>
          <h2>医生直接建方</h2>
          <label>
            患者姓名
            <input
              onChange={(event) => setForm({ ...form, patientName: event.target.value })}
              value={form.patientName}
            />
          </label>
          <label>
            药品
            <select
              onChange={(event) =>
                setForm({
                  ...form,
                  items: [{ ...currentItem, drugId: event.target.value }],
                })
              }
              value={currentItem.drugId}
            >
              <option value="">请选择药品</option>
              {drugs.map((drug) => (
                <option key={drug.id} value={drug.id}>
                  {drug.drugName}
                </option>
              ))}
            </select>
          </label>
          <label>
            剂量
            <input
              onChange={(event) =>
                setForm({
                  ...form,
                  items: [{ ...currentItem, dosage: event.target.value }],
                })
              }
              value={currentItem.dosage}
            />
          </label>
          <label>
            频次
            <input
              onChange={(event) =>
                setForm({
                  ...form,
                  items: [{ ...currentItem, frequency: event.target.value }],
                })
              }
              value={currentItem.frequency}
            />
          </label>
          <label>
            天数
            <input
              onChange={(event) =>
                setForm({
                  ...form,
                  items: [{ ...currentItem, days: event.target.value }],
                })
              }
              type="number"
              value={currentItem.days}
            />
          </label>
          <label>
            数量
            <input
              onChange={(event) =>
                setForm({
                  ...form,
                  items: [{ ...currentItem, quantity: event.target.value }],
                })
              }
              type="number"
              value={currentItem.quantity}
            />
          </label>
          <button type="submit">创建处方</button>
        </form>
      ) : (
        <article className="panel">
          <h2>药师工作说明</h2>
          <p>当前页面已开放审核入口，后续可继续补充代开申请、发药与退回动作。</p>
        </article>
      )}
    </section>
  );
}

export default PrescriptionPage;
