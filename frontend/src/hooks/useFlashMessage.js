import { useEffect, useState } from "react";

function useFlashMessage() {
  const [message, setMessage] = useState(null);

  useEffect(() => {
    if (!message) {
      return undefined;
    }

    const timer = window.setTimeout(() => {
      setMessage(null);
    }, 3000);

    return () => window.clearTimeout(timer);
  }, [message]);

  function showMessage(type, text) {
    setMessage({ type, text });
  }

  return {
    message,
    showError(text) {
      showMessage("error", text);
    },
    showSuccess(text) {
      showMessage("success", text);
    },
    clearMessage() {
      setMessage(null);
    },
  };
}

export default useFlashMessage;
