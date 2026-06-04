function Pagination({ pageNum, pageSize, total, onChange }) {
  const totalPages = Math.max(1, Math.ceil((total || 0) / (pageSize || 10)));

  return (
    <div className="pagination">
      <button
        disabled={pageNum <= 1}
        onClick={() => onChange(pageNum - 1)}
        type="button"
      >
        上一页
      </button>
      <span>
        第 {pageNum} / {totalPages} 页，共 {total || 0} 条
      </span>
      <button
        disabled={pageNum >= totalPages}
        onClick={() => onChange(pageNum + 1)}
        type="button"
      >
        下一页
      </button>
    </div>
  );
}

export default Pagination;
