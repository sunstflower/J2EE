function FeedbackMessage({ message }) {
  if (!message) {
    return null;
  }

  return <p className={`message ${message.type}`}>{message.text}</p>;
}

export default FeedbackMessage;
