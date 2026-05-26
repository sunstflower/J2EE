import { MetricCards } from "../../shared/components/MetricCards";
import type { Metric } from "../../shared/types";

type OverviewPanelProps = {
  metrics: Metric[];
};

export function OverviewPanel({ metrics }: OverviewPanelProps) {
  return (
    <div className="grid gap-5 xl:grid-cols-[1.6fr_1fr]">
      <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
        <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-sky-900/60">Runtime brief</p>
        <h2 className="mb-4 text-4xl font-semibold tracking-tight text-slate-950">
          Control plane first, with room for real network operations later.
        </h2>
        <p className="max-w-2xl text-base leading-8 text-slate-700">
          The current shell is designed around the fixed project decisions: bundled Clash.Meta, system proxy only, Spring Boot core ownership, and a desktop-managed local API session.
        </p>

        <div className="mt-8">
          <MetricCards metrics={metrics} />
        </div>
      </section>

      <section className="rounded-[30px] border border-slate-200/70 bg-linear-to-br from-slate-950 via-slate-900 to-sky-950 p-8 text-slate-100 shadow-[0_28px_70px_rgba(15,23,42,0.22)]">
        <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-sky-200/70">Project lock-ins</p>
        <div className="space-y-3 text-sm leading-7 text-slate-300">
          <p>Clash.Meta is the only target core for the first implementation.</p>
          <p>Spring Boot remains the only process allowed to manage core lifecycle.</p>
          <p>Electron will hand off random port and session token context to the UI.</p>
        </div>
      </section>
    </div>
  );
}
