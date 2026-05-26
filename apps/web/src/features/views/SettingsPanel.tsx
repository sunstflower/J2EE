export function SettingsPanel() {
  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Application settings</p>
      <h2 className="mb-6 text-3xl font-semibold tracking-tight text-slate-950">Backend-driven settings surface.</h2>

      <div className="grid gap-4 lg:grid-cols-2">
        <div className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
          <p className="text-sm text-slate-500">Connectivity</p>
          <h3 className="mt-2 text-lg font-semibold text-slate-950">Local API session</h3>
          <p className="mt-3 text-sm leading-7 text-slate-700">
            Random localhost port plus session token will be surfaced from Electron, not discovered by the renderer directly.
          </p>
        </div>
        <div className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
          <p className="text-sm text-slate-500">Runtime files</p>
          <h3 className="mt-2 text-lg font-semibold text-slate-950">Writable application directory</h3>
          <p className="mt-3 text-sm leading-7 text-slate-700">
            Clash.Meta configs, logs, and state will live outside bundled assets, aligned with the runtime documentation.
          </p>
        </div>
      </div>
    </section>
  );
}
