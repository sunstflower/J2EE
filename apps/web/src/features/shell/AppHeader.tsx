import { MetricCards } from "../../shared/components/MetricCards";
import type { Metric } from "../../shared/types";

type AppHeaderProps = {
  metrics: Metric[];
};

export function AppHeader({ metrics }: AppHeaderProps) {
  return (
    <header className="rounded-[32px] border border-white/60 bg-white/72 p-6 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
        <div className="max-w-3xl">
          <p className="mb-3 text-xs font-bold uppercase tracking-[0.2em] text-sky-900/70">
            macOS proxy client scaffold
          </p>
          <h2 className="mb-4 text-4xl leading-none font-semibold tracking-tight text-slate-950 md:text-6xl">
            Local-first desktop orchestration for a Clash-style client.
          </h2>
          <p className="text-base leading-8 text-slate-700">
            The frontend is now structured as an application shell instead of a single landing screen, so the next API-backed features can slot into stable surfaces.
          </p>
        </div>

        <div className="xl:min-w-[420px]">
          <MetricCards compact metrics={metrics} />
        </div>
      </div>
    </header>
  );
}
