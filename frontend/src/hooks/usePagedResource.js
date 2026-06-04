import { useState } from "react";

const DEFAULT_PAGE = {
  records: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
};

function usePagedResource(initialState = DEFAULT_PAGE) {
  const [data, setData] = useState(initialState);
  const [loading, setLoading] = useState(false);

  async function loadResource(loader) {
    setLoading(true);

    try {
      const nextData = await loader();
      setData(nextData);
      return nextData;
    } finally {
      setLoading(false);
    }
  }

  return {
    data,
    loadResource,
    loading,
    setData,
  };
}

export default usePagedResource;
