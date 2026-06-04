import { useEffect, useState } from "react";
import Pagination from "../components/Pagination";
import FeedbackMessage from "../components/FeedbackMessage";
import { queryInventoryRecords } from "../api/inventories";
import useFlashMessage from "../hooks/useFlashMessage";

function InventoryRecordsPage() {
  const [pageNum, setPageNum] = useState(1);
  const [recordData, setRecordData] = useState({ records: [], total: 0, pageSize: 10 });
  const [loading, setLoading] = useState(false);
  const { message, showError } = useFlashMessage();

  async function loadRecords(targetPage = pageNum) {
    setLoading(true);
    try {
      const data = await queryInventoryRecords({ pageNum: targetPage, pageSize: 10 });
      setPageNum(data.pageNum);
      setRecordData(data);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRecords(1);
  }, []);

  return (
    <section className="panel">
      <p className="eyebrow">Inventory Records</p>
      <h2>库存流水</h2>
      <FeedbackMessage message={message} />
      {loading ? <p>加载中...</p> : null}
      <div className="table-list">
        {recordData.records.map((item) => (
          <article className="list-card" key={item.id}>
            <div>
              <strong>{item.recordType}</strong>
              <p>
                {item.drugCode} / {item.batchNo} / 前 {item.beforeQuantity} / 后 {item.afterQuantity}
              </p>
            </div>
          </article>
        ))}
      </div>
      <Pagination
        onChange={(nextPage) => loadRecords(nextPage)}
        pageNum={pageNum}
        pageSize={recordData.pageSize || 10}
        total={recordData.total}
      />
    </section>
  );
}

export default InventoryRecordsPage;
