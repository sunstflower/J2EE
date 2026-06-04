import { useParams } from "react-router-dom";
import PrescriptionPage from "./PrescriptionPage";

function PrescriptionDetailPage() {
  const { id } = useParams();

  return (
    <section className="route-wrapper">
      <div className="route-header">
        <p className="section-kicker">Prescription Detail</p>
        <h2>处方详情页</h2>
        <p className="lead">
          当前正在查看处方路由参数 ID：{id}。当前版本先复用处方工作台承载详情与动作，后续可继续拆分为独立详情页。
        </p>
      </div>
      <PrescriptionPage />
    </section>
  );
}

export default PrescriptionDetailPage;
