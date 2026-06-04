import { useState } from "react";

function useApiAction() {
  const [submitting, setSubmitting] = useState("");
  const [message, setMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  function resetFeedback() {
    setMessage("");
    setErrorMessage("");
  }

  async function runAction(type, executor, successMessage = "") {
    setSubmitting(type);
    resetFeedback();

    try {
      const result = await executor();
      if (successMessage) {
        setMessage(successMessage);
      }
      return result;
    } catch (error) {
      setErrorMessage(error.message);
      throw error;
    } finally {
      setSubmitting("");
    }
  }

  return {
    errorMessage,
    message,
    resetFeedback,
    runAction,
    setErrorMessage,
    setMessage,
    submitting,
  };
}

export default useApiAction;
