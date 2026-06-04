import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FeedbackMessage from "../components/FeedbackMessage";
import { queryDrugs } from "../api/drugs";
import { createPrescription } from "../api/prescriptions";
import { loadCurrentUser } from "../auth";
import useFlashMessage from "../hooks/useFlashMessage";

function buildCreateForm(currentUser) {
  return {
    patientName: "",
    doctorId: currentUser?.userId || "",
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

function PrescriptionCreatePage() {
  const currentUser = loadCurrentUser();
  const navigate = useNavigate();
  const [drugs, setDrugs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState(() => buildCreateForm(currentUser));
  const { message, showError, showSuccess } = useFlashMessage();
  const currentItem = form.items[0];

  useEffect(() => {
    async function loadDrugOptions() {
      try {
        const data = await queryDrugs({ pageNum: 1, pageSize: 100 });
        setDrugs(data.records || []);
      } catch (error) {
        showError(error.message);
      }
    }

    loadDrugOptions();
  }, []);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);

    try {
      const newId = await createPrescription({
        ...form,
        doctorId: Number(form.doctorId),
        createdByUserId: Number(form.createdByUserId),
        items: form.items.map((item) => ({
          ...item,
          drugId: Number(item.drugId),
          days: Number(item.days),
          quantity: Number(item.quantity),
        })),
      });
      showSuccess("处方创建成功，正在跳转详情页。");
      window.setTimeout(() => navigate(`/prescriptions/${newId}`), 600);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="panel form-panel" onSubmit={handleSubmit}>
      <p className="eyebrow">Prescription Create</p>
      <h2>新建处方</h2>
      <FeedbackMessage message={message} />
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
      <button disabled={loading} type="submit">
        {loading ? "创建中..." : "创建处方"}
      </button>
    </form>
  );
}

export default PrescriptionCreatePage;
