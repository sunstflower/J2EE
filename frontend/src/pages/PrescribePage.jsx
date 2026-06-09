import { useEffect, useState } from "react";
import { queryDrugs } from "../api/drugs";
import { createPrescription } from "../api/prescriptions";
import FeedbackMessage from "../components/FeedbackMessage";
import useFlashMessage from "../hooks/useFlashMessage";

function buildInitialForm() {
  return {
    patientName: "",
    item: {
      drugId: "",
      dosage: "",
      frequency: "",
      days: 1,
      quantity: 1,
    },
    remark: "",
  };
}

function PrescribePage() {
  const [drugData, setDrugData] = useState({ records: [] });
  const [form, setForm] = useState(buildInitialForm());
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(false);
  const { message, showError, showSuccess } = useFlashMessage();

  useEffect(() => {
    async function loadDrugs() {
      setLoading(true);
      try {
        const drugs = await queryDrugs({ pageNum: 1, pageSize: 50, enabled: 1 });
        setDrugData(drugs);
      } catch (error) {
        showError(error.message);
      } finally {
        setLoading(false);
      }
    }

    loadDrugs();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    try {
      const prescriptionId = await createPrescription({
        patientName: form.patientName,
        remark: form.remark,
        items: [
          {
            drugId: Number(form.item.drugId),
            dosage: form.item.dosage,
            frequency: form.item.frequency,
            days: Number(form.item.days),
            quantity: Number(form.item.quantity),
          },
        ],
      });
      showSuccess(`处方已开出，单号ID：${prescriptionId}`);
      setForm(buildInitialForm());
    } catch (error) {
      showError(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page-grid single-column">
      <form className="panel form-panel" onSubmit={handleSubmit}>
        <p className="eyebrow">Prescription</p>
        <h2>开药</h2>
        <p>该页面只提供医生账号使用，提交后会直接扣减库存。</p>
        <FeedbackMessage message={message} />
        {loading ? <p>加载中...</p> : null}
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
              setForm({ ...form, item: { ...form.item, drugId: event.target.value } })
            }
            value={form.item.drugId}
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
          剂量
          <input
            onChange={(event) =>
              setForm({ ...form, item: { ...form.item, dosage: event.target.value } })
            }
            placeholder="如：1片"
            value={form.item.dosage}
          />
        </label>
        <label>
          频次
          <input
            onChange={(event) =>
              setForm({ ...form, item: { ...form.item, frequency: event.target.value } })
            }
            placeholder="如：bid"
            value={form.item.frequency}
          />
        </label>
        <label>
          天数
          <input
            min="1"
            onChange={(event) =>
              setForm({ ...form, item: { ...form.item, days: event.target.value } })
            }
            type="number"
            value={form.item.days}
          />
        </label>
        <label>
          数量
          <input
            min="1"
            onChange={(event) =>
              setForm({ ...form, item: { ...form.item, quantity: event.target.value } })
            }
            type="number"
            value={form.item.quantity}
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
          {submitting ? "提交中..." : "确认开药"}
        </button>
      </form>
    </section>
  );
}

export default PrescribePage;
