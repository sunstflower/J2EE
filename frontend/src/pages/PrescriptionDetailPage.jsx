import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import FeedbackMessage from "../components/FeedbackMessage";
import { getPrescription } from "../api/prescriptions";
import useFlashMessage from "../hooks/useFlashMessage";

function PrescriptionDetailPage() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const { message, showError } = useFlashMessage();

  useEffect(() => {
    async function loadDetail() {
      setLoading(true);
      try {
        const data = await getPrescription(id);
        setDetail(data);
      } catch (error) {
        showError(error.message);
      } finally {
        setLoading(false);
      }
    }

    loadDetail();
  }, [id]);

  return (
    <section className="panel">
      <p className="eyebrow">Prescription Detail</p>
      <h2>处方详情</h2>
      <FeedbackMessage message={message} />
      {loading ? <p>加载中...</p> : null}
      {detail ? (
        <article className="detail-card">
          <p>编号：{detail.prescriptionNo}</p>
          <p>患者：{detail.patientName}</p>
          <p>状态：{detail.status}</p>
          <p>医生：{detail.doctorName}</p>
          <ul className="detail-list">
            {(detail.items || []).map((item) => (
              <li key={item.id || `${item.drugId}-${item.drugName}`}>
                {item.drugName} / {item.dosage} / {item.frequency} / {item.quantity}
              </li>
            ))}
          </ul>
        </article>
      ) : null}
    </section>
  );
}

export default PrescriptionDetailPage;
