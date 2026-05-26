const sections = [
  {
    title: "Desktop shell",
    description: "Electron owns startup, session bootstrap, and macOS integration."
  },
  {
    title: "Local API",
    description: "Spring Boot will manage Clash.Meta, settings, subscriptions, and runtime state."
  },
  {
    title: "Web UI",
    description: "React consumes the local REST API through desktop-provided connection context."
  }
];

export default function App() {
  return (
    <div className="app-shell">
      <header className="hero">
        <p className="eyebrow">macOS proxy client scaffold</p>
        <h1>Control plane first, data plane delegated to Clash.Meta.</h1>
        <p className="lead">
          This UI is a formal scaffold for the local desktop client. Business features will be
          added on top of the documented Electron, React, and Spring Boot boundaries.
        </p>
      </header>

      <main className="grid">
        {sections.map((section) => (
          <section className="card" key={section.title}>
            <h2>{section.title}</h2>
            <p>{section.description}</p>
          </section>
        ))}
      </main>
    </div>
  );
}
