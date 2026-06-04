function Pagination({ pageNum, pageSize, total, onPageChange }) {
  const safePageNum = Number(pageNum) || 1;
  const safePageSize = Number(pageSize) || 10;
  const safeTotal = Number(total) || 0;
  const totalPages = Math.max(1, Math.ceil(safeTotal / safePageSize));
  const isFirstPage = safePageNum <= 1;
  const isLastPage = safePageNum >= totalPages;

  return (
    <div className="pagination-bar">
      <span className="pagination-meta">
        第 {safePageNum} / {totalPages} 页，共 {safeTotal} 条
      </span>
      <div className="button-row">
        <button
          className="secondary-action"
          disabled={isFirstPage}
          onClick={() => onPageChange(safePageNum - 1)}
          type="button"
        >
          上一页
        </button>
        <button
          className="secondary-action"
          disabled={isLastPage}
          onClick={() => onPageChange(safePageNum + 1)}
          type="button"
        >
          下一页
        </button>
      </div>
    </div>
  );
}

export default Pagination;
