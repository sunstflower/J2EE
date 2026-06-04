import PrescriptionPage from "./PrescriptionPage";

function PrescriptionCreatePage() {
  return (
    <section className="route-wrapper">
      <div className="route-header">
        <p className="section-kicker">Prescription Create</p>
        <h2>处方创建页</h2>
        <p className="lead">当前版本先复用处方工作台承载建方流程，后续可继续拆成独立新建页。</p>
      </div>
      <PrescriptionPage />
    </section>
  );
}

export default PrescriptionCreatePage;
