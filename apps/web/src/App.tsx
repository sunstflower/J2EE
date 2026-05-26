const sections = [
  {
    title: "Desktop shell",
    description: "Electron owns startup, session bootstrap, and macOS integration.",
    accent: "from-sky-400/25 to-cyan-300/10"
  },
  {
    title: "Local API",
    description: "Spring Boot will manage Clash.Meta, settings, subscriptions, and runtime state.",
    accent: "from-amber-300/25 to-orange-200/10"
  },
  {
    title: "Web UI",
    description: "React consumes the local REST API through desktop-provided connection context.",
    accent: "from-emerald-300/25 to-lime-200/10"
  }
];

export default function App() {
  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(255,196,87,0.18),_transparent_24%),radial-gradient(circle_at_top_right,_rgba(58,151,255,0.14),_transparent_28%),linear-gradient(180deg,_#f7fbff_0%,_#ebf1f8_100%)] text-slate-900">
      <div className="mx-auto w-[min(1120px,calc(100vw-48px))] py-18">
        <header className="mb-10 max-w-3xl">
          <p className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-sky-900/70">
            macOS proxy client scaffold
          </p>
          <h1 className="mb-5 text-5xl leading-none font-semibold tracking-tight text-slate-950 md:text-7xl">
            Control plane first, data plane delegated to Clash.Meta.
          </h1>
          <p className="max-w-2xl text-lg leading-8 text-slate-700">
            This UI is a formal scaffold for the local desktop client. Business features will be
            added on top of the documented Electron, React, and Spring Boot boundaries.
          </p>
        </header>

        <section className="mb-8 flex flex-wrap gap-3 text-sm">
          <span className="rounded-full border border-sky-200 bg-white/70 px-4 py-2 text-sky-900 shadow-sm backdrop-blur">
            Electron shell
          </span>
          <span className="rounded-full border border-amber-200 bg-white/70 px-4 py-2 text-amber-900 shadow-sm backdrop-blur">
            Spring Boot local API
          </span>
          <span className="rounded-full border border-emerald-200 bg-white/70 px-4 py-2 text-emerald-900 shadow-sm backdrop-blur">
            React + Tailwind CSS
          </span>
        </section>

        <main className="grid gap-5 md:grid-cols-3">
        {sections.map((section) => (
            <section
              className={`rounded-[24px] border border-slate-200/70 bg-linear-to-br ${section.accent} p-7 shadow-[0_20px_50px_rgba(26,57,92,0.08)] backdrop-blur-sm`}
              key={section.title}
            >
              <div className="mb-6 h-12 w-12 rounded-2xl border border-white/60 bg-white/70 shadow-sm" />
              <h2 className="mb-3 text-xl font-semibold text-slate-950">{section.title}</h2>
              <p className="leading-7 text-slate-700">{section.description}</p>
            </section>
        ))}
        </main>

        <section className="mt-8 rounded-[28px] border border-slate-200/70 bg-white/78 p-8 shadow-[0_28px_60px_rgba(26,57,92,0.09)] backdrop-blur-xl">
          <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
            <div className="max-w-2xl">
              <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">
                Design direction
              </p>
              <h3 className="mb-3 text-3xl font-semibold tracking-tight text-slate-950">
                Tailwind is now part of the frontend foundation.
              </h3>
              <p className="text-base leading-7 text-slate-700">
                The web client can now evolve with utility-first styling instead of hand-maintained
                component CSS. This keeps scaffolding fast while still leaving room for a more
                opinionated visual system later.
              </p>
            </div>

            <div className="grid min-w-[240px] gap-3 text-sm text-slate-700">
              <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                Runtime mode: system proxy only
              </div>
              <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                Core target: bundled Clash.Meta
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
