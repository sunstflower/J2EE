import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import useApiAction from "../hooks/useApiAction";
import usePagedResource from "../hooks/usePagedResource";
import { loadCurrentUser } from "../auth";
import { queryDrugs } from "../api/drugs";
import {
  approvePrescriptionByDoctor,
  auditPrescription,
  cancelPrescription,
  createPrescription,
  dispensePrescription,
  getPrescription,
  queryPrescriptions,
  submitPrescription,
} from "../api/prescriptions";

const initialItem = {
  drugId: "",
  dosage: "",
  frequency: "",
  days: "1",
  quantity: "1",
};

function buildCreateForm(currentUser) {
  return {
    patientName: "",
    doctorId: currentUser?.role === "DOCTOR" ? String(currentUser.userId) : "",
    doctorName: currentUser?.role === "DOCTOR" ? currentUser.userName : "",
    items: [initialItem],
  };
}

function PrescriptionPage() {
  const currentUser = loadCurrentUser();
  const [query, setQuery] = useState({
    pageNum: 1,
    pageSize: 10,
    status: "",
    patientName: "",
  });
  const {
    data: prescriptions,
    loadResource: loadPrescriptionResource,
    loading: loadingList,
  } = usePagedResource();
  const [selectedPrescriptionId, setSelectedPrescriptionId] = useState(null);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [availableDrugs, setAvailableDrugs] = useState([]);
  const [createForm, setCreateForm] = useState(() => buildCreateForm(currentUser));
  const [rejectReason, setRejectReason] = useState("");
  const [loadingDetail, setLoadingDetail] = useState(false);
  const { errorMessage, message, runAction: executeAction, setErrorMessage, submitting } =
    useApiAction();

  async function loadPrescriptions(nextQuery = query) {
    try {
      const data = await loadPrescriptionResource(() => queryPrescriptions(nextQuery));
      if (!selectedPrescriptionId && data.records.length > 0) {
        setSelectedPrescriptionId(data.records[0].id);
      }
    } catch (error) {
      return null;
    }
  }

  async function loadPrescriptionDetail(id) {
    if (!id) {
      setSelectedPrescription(null);
      return;
    }

    setLoadingDetail(true);
    setErrorMessage("");

    try {
      const data = await getPrescription(id);
      setSelectedPrescription(data);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setLoadingDetail(false);
    }
  }

  async function loadDrugOptions() {
    try {
      const data = await queryDrugs({
        pageNum: 1,
        pageSize: 100,
        enabled: 1,
      });
      setAvailableDrugs(data.records);
    } catch (error) {
      setErrorMessage(error.message);
    }
  }

  useEffect(() => {
    loadPrescriptions(query);
    loadDrugOptions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (selectedPrescriptionId) {
      loadPrescriptionDetail(selectedPrescriptionId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedPrescriptionId]);

  function updateItem(index, field, value) {
    setCreateForm((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      ),
    }));
  }

  function addItem() {
    setCreateForm((current) => ({
      ...current,
      items: [...current.items, initialItem],
    }));
  }

  function removeItem(index) {
    setCreateForm((current) => ({
      ...current,
      items: current.items.filter((_, itemIndex) => itemIndex !== index),
    }));
  }

  async function handleCreate(event) {
    event.preventDefault();

    try {
      const payload = {
        patientName: createForm.patientName,
        createdByRole: currentUser.role,
        createdByUserId: currentUser.userId,
        createdByName: currentUser.userName,
        doctorId: Number(createForm.doctorId),
        doctorName: createForm.doctorName,
        items: createForm.items.map((item) => ({
          drugId: Number(item.drugId),
          dosage: item.dosage,
          frequency: item.frequency,
          days: Number(item.days),
          quantity: Number(item.quantity),
        })),
      };
      const id = await executeAction(
        "create",
        () => createPrescription(payload),
        "处方创建成功，列表和详情已刷新。"
      );
      setCreateForm(buildCreateForm(currentUser));
      await loadPrescriptions(query);
      setSelectedPrescriptionId(id);
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
    await loadPrescriptions(nextQuery);
  }

  async function handlePrescriptionPageChange(nextPageNum) {
    const nextQuery = {
      ...query,
      pageNum: nextPageNum,
    };
    setQuery(nextQuery);
    await loadPrescriptions(nextQuery);
  }

  async function handleWorkflowAction(type, executor) {
    if (!selectedPrescription) {
      return;
    }

    try {
      await executeAction(type, executor, "处方状态已更新，详情已刷新。");
      await loadPrescriptions(query);
      await loadPrescriptionDetail(selectedPrescription.id);
      setRejectReason("");
    } catch (error) {
      return null;
    }
  }

  const isDoctor = currentUser?.role === "DOCTOR";
  const isPharmacist = currentUser?.role === "PHARMACIST";
  const canDoctorApprove =
    isDoctor &&
    selectedPrescription?.status === "PENDING_DOCTOR_APPROVAL" &&
    selectedPrescription?.doctorId === currentUser.userId;
  const canDoctorSubmit =
    isDoctor &&
    selectedPrescription?.status === "DRAFT" &&
    selectedPrescription?.doctorId === currentUser.userId;
  const canPharmacistAudit = isPharmacist && selectedPrescription?.status === "SUBMITTED";
  const canPharmacistDispense = isPharmacist && selectedPrescription?.status === "APPROVED";
  const canCancel =
    selectedPrescription &&
    !["DISPENSED", "CANCELLED"].includes(selectedPrescription.status);

  return (
    <section className="module-panel">
      <div className="section-heading">
        <div>
          <p className="section-kicker">Prescription Module</p>
          <h2>处方模块联调</h2>
        </div>
        <button className="secondary-action" onClick={() => loadPrescriptions(query)} type="button">
          刷新处方列表
        </button>
      </div>

      <div className="prescription-layout">
        <section className="module-stack">
          <form className="module-card" onSubmit={handleSearch}>
            <h3>处方查询</h3>
            <div className="form-grid compact-grid">
              <label className="field">
                <span>状态</span>
                <select
                  name="status"
                  onChange={(event) =>
                    setQuery((current) => ({ ...current, status: event.target.value }))
                  }
                  value={query.status}
                >
                  <option value="">全部</option>
                  <option value="DRAFT">DRAFT</option>
                  <option value="PENDING_DOCTOR_APPROVAL">PENDING_DOCTOR_APPROVAL</option>
                  <option value="SUBMITTED">SUBMITTED</option>
                  <option value="APPROVED">APPROVED</option>
                  <option value="REJECTED">REJECTED</option>
                  <option value="DISPENSED">DISPENSED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </label>
              <label className="field">
                <span>患者姓名</span>
                <input
                  name="patientName"
                  onChange={(event) =>
                    setQuery((current) => ({ ...current, patientName: event.target.value }))
                  }
                  value={query.patientName}
                />
              </label>
            </div>
            <button className="primary-action" type="submit">
              查询处方
            </button>
          </form>

          <section className="module-card">
            <div className="list-header">
              <h3>处方列表</h3>
              <span>共 {prescriptions.total} 条</span>
            </div>
            {loadingList ? <p className="empty-state">正在加载处方列表...</p> : null}
            {!loadingList && prescriptions.records.length === 0 ? (
              <p className="empty-state">当前没有可展示的处方数据。</p>
            ) : null}
            <div className="prescription-list">
              {prescriptions.records.map((prescription) => (
                <button
                  className={
                    prescription.id === selectedPrescriptionId
                      ? "prescription-item active-item"
                      : "prescription-item"
                  }
                  key={prescription.id}
                  onClick={() => setSelectedPrescriptionId(prescription.id)}
                  type="button"
                >
                  <div>
                    <p className="drug-code">{prescription.prescriptionNo}</p>
                    <h4>{prescription.patientName}</h4>
                    <p className="drug-meta">
                      医生 {prescription.doctorName} / 状态 {prescription.status}
                    </p>
                  </div>
                  <span className="status-pill inactive">ID {prescription.id}</span>
                </button>
              ))}
            </div>
            <Pagination
              onPageChange={handlePrescriptionPageChange}
              pageNum={prescriptions.pageNum}
              pageSize={prescriptions.pageSize}
              total={prescriptions.total}
            />
          </section>
        </section>

        <section className="module-stack">
          <form className="module-card" onSubmit={handleCreate}>
            <h3>{isDoctor ? "医生直接建方" : "药师代开发起"}</h3>
            <div className="form-grid">
              <label className="field">
                <span>患者姓名</span>
                <input
                  name="patientName"
                  onChange={(event) =>
                    setCreateForm((current) => ({ ...current, patientName: event.target.value }))
                  }
                  required
                  value={createForm.patientName}
                />
              </label>
              <label className="field">
                <span>医生 ID</span>
                <input
                  disabled={isDoctor}
                  name="doctorId"
                  onChange={(event) =>
                    setCreateForm((current) => ({ ...current, doctorId: event.target.value }))
                  }
                  required
                  value={createForm.doctorId}
                />
              </label>
              <label className="field">
                <span>医生姓名</span>
                <input
                  disabled={isDoctor}
                  name="doctorName"
                  onChange={(event) =>
                    setCreateForm((current) => ({ ...current, doctorName: event.target.value }))
                  }
                  required
                  value={createForm.doctorName}
                />
              </label>
            </div>

            <div className="subsection-header">
              <h4>处方明细</h4>
              <button className="secondary-action" onClick={addItem} type="button">
                新增明细
              </button>
            </div>
            <div className="prescription-items">
              {createForm.items.map((item, index) => (
                <div className="prescription-item-form" key={`${index}-${item.drugId}`}>
                  <div className="form-grid">
                    <label className="field">
                      <span>药品</span>
                      <select
                        name={`drugId-${index}`}
                        onChange={(event) => updateItem(index, "drugId", event.target.value)}
                        required
                        value={item.drugId}
                      >
                        <option value="">选择药品</option>
                        {availableDrugs.map((drug) => (
                          <option key={drug.id} value={drug.id}>
                            {drug.drugName}（{drug.drugCode}）
                          </option>
                        ))}
                      </select>
                    </label>
                    <label className="field">
                      <span>剂量</span>
                      <input
                        name={`dosage-${index}`}
                        onChange={(event) => updateItem(index, "dosage", event.target.value)}
                        value={item.dosage}
                      />
                    </label>
                    <label className="field">
                      <span>频次</span>
                      <input
                        name={`frequency-${index}`}
                        onChange={(event) => updateItem(index, "frequency", event.target.value)}
                        value={item.frequency}
                      />
                    </label>
                    <label className="field">
                      <span>天数</span>
                      <input
                        min="1"
                        name={`days-${index}`}
                        onChange={(event) => updateItem(index, "days", event.target.value)}
                        required
                        type="number"
                        value={item.days}
                      />
                    </label>
                    <label className="field">
                      <span>数量</span>
                      <input
                        min="1"
                        name={`quantity-${index}`}
                        onChange={(event) => updateItem(index, "quantity", event.target.value)}
                        required
                        type="number"
                        value={item.quantity}
                      />
                    </label>
                  </div>
                  {createForm.items.length > 1 ? (
                    <button
                      className="danger-action"
                      onClick={() => removeItem(index)}
                      type="button"
                    >
                      删除明细
                    </button>
                  ) : null}
                </div>
              ))}
            </div>
            <button className="primary-action" disabled={submitting === "create"} type="submit">
              {submitting === "create" ? "提交中..." : "创建处方"}
            </button>
          </form>
        </section>
      </div>

      {message ? <p className="feedback success-text">{message}</p> : null}
      {errorMessage ? <p className="feedback error-text">{errorMessage}</p> : null}

      <section className="module-card">
        <div className="list-header">
          <h3>处方详情</h3>
          <span>{selectedPrescription ? selectedPrescription.prescriptionNo : "未选择处方"}</span>
        </div>
        {loadingDetail ? <p className="empty-state">正在加载处方详情...</p> : null}
        {!loadingDetail && !selectedPrescription ? (
          <p className="empty-state">请选择一条处方查看详情。</p>
        ) : null}
        {selectedPrescription ? (
          <div className="prescription-detail">
            <div className="detail-grid">
              <article className="detail-card">
                <h4>基础信息</h4>
                <p className="drug-meta">患者：{selectedPrescription.patientName}</p>
                <p className="drug-meta">医生：{selectedPrescription.doctorName}</p>
                <p className="drug-meta">创建角色：{selectedPrescription.createdByRole}</p>
                <p className="drug-meta">状态：{selectedPrescription.status}</p>
                <p className="drug-meta">
                  医生授权：{selectedPrescription.doctorApprovalStatus || "NONE"}
                </p>
              </article>
              <article className="detail-card">
                <h4>流程记录</h4>
                <p className="drug-meta">审核人：{selectedPrescription.auditBy || "-"}</p>
                <p className="drug-meta">发药人：{selectedPrescription.dispenseBy || "-"}</p>
                <p className="drug-meta">驳回原因：{selectedPrescription.rejectReason || "-"}</p>
              </article>
            </div>

            <section className="detail-card">
              <div className="list-header">
                <h4>处方明细</h4>
                <span>{selectedPrescription.items?.length || 0} 项</span>
              </div>
              <div className="record-list">
                {(selectedPrescription.items || []).map((item) => (
                  <article className="record-card" key={item.id || `${item.drugId}-${item.quantity}`}>
                    <div className="drug-card-main">
                      <p className="drug-code">{item.drugCode}</p>
                      <h4>{item.drugName}</h4>
                      <p className="drug-meta">
                        剂量 {item.dosage || "-"} / 频次 {item.frequency || "-"}
                      </p>
                      <p className="drug-meta">
                        天数 {item.days} / 数量 {item.quantity}
                      </p>
                    </div>
                  </article>
                ))}
              </div>
            </section>

            <section className="detail-card">
              <div className="subsection-header">
                <h4>状态动作</h4>
              </div>
              <label className="field">
                <span>驳回原因</span>
                <input
                  name="rejectReason"
                  onChange={(event) => setRejectReason(event.target.value)}
                  placeholder="驳回或拒绝代开时填写"
                  value={rejectReason}
                />
              </label>
              <div className="action-grid">
                {canDoctorApprove ? (
                  <>
                    <button
                      className="primary-action"
                      disabled={submitting === "doctor-approve"}
                      onClick={() =>
                        handleWorkflowAction("doctor-approve", () =>
                          approvePrescriptionByDoctor(selectedPrescription.id, {
                            action: "APPROVE",
                            doctorId: currentUser.userId,
                            doctorName: currentUser.userName,
                          })
                        )
                      }
                      type="button"
                    >
                      医生同意代开
                    </button>
                    <button
                      className="danger-action"
                      disabled={submitting === "doctor-reject"}
                      onClick={() =>
                        handleWorkflowAction("doctor-reject", () =>
                          approvePrescriptionByDoctor(selectedPrescription.id, {
                            action: "REJECT",
                            doctorId: currentUser.userId,
                            doctorName: currentUser.userName,
                          })
                        )
                      }
                      type="button"
                    >
                      医生拒绝代开
                    </button>
                  </>
                ) : null}

                {canDoctorSubmit ? (
                  <button
                    className="primary-action"
                    disabled={submitting === "submit"}
                    onClick={() =>
                      handleWorkflowAction("submit", () => submitPrescription(selectedPrescription.id))
                    }
                    type="button"
                  >
                    提交处方审核
                  </button>
                ) : null}

                {canPharmacistAudit ? (
                  <>
                    <button
                      className="primary-action"
                      disabled={submitting === "audit-approve"}
                      onClick={() =>
                        handleWorkflowAction("audit-approve", () =>
                          auditPrescription(selectedPrescription.id, {
                            action: "APPROVE",
                            operatorId: currentUser.userId,
                            operatorName: currentUser.userName,
                          })
                        )
                      }
                      type="button"
                    >
                      药师审核通过
                    </button>
                    <button
                      className="danger-action"
                      disabled={submitting === "audit-reject"}
                      onClick={() =>
                        handleWorkflowAction("audit-reject", () =>
                          auditPrescription(selectedPrescription.id, {
                            action: "REJECT",
                            operatorId: currentUser.userId,
                            operatorName: currentUser.userName,
                            rejectReason,
                          })
                        )
                      }
                      type="button"
                    >
                      药师驳回
                    </button>
                  </>
                ) : null}

                {canPharmacistDispense ? (
                  <button
                    className="primary-action"
                    disabled={submitting === "dispense"}
                    onClick={() =>
                      handleWorkflowAction("dispense", () =>
                        dispensePrescription(selectedPrescription.id, {
                          operatorId: currentUser.userId,
                          operatorName: currentUser.userName,
                        })
                      )
                    }
                    type="button"
                  >
                    确认发药
                  </button>
                ) : null}

                {canCancel ? (
                  <button
                    className="danger-action"
                    disabled={submitting === "cancel"}
                    onClick={() =>
                      handleWorkflowAction("cancel", () => cancelPrescription(selectedPrescription.id))
                    }
                    type="button"
                  >
                    取消处方
                  </button>
                ) : null}
              </div>
            </section>
          </div>
        ) : null}
      </section>
    </section>
  );
}

export default PrescriptionPage;
