import { MetricCards } from "../../shared/components/MetricCards";
import { useCoreStatus } from "../core/useCoreStatus";
import { useRuntimeSummary } from "../runtime/useRuntimeSummary";
import { useSystemProxyStatus } from "../system-proxy/useSystemProxyStatus";
import type { Metric } from "../../shared/types";

type OverviewPanelProps = {
  metrics: Metric[];
};

export function OverviewPanel({ metrics }: OverviewPanelProps) {
  const { data: runtime, loading, error } = useRuntimeSummary();
  const { data: core, acting, error: coreError, start, stop, reload } = useCoreStatus();
  const {
    data: systemProxy,
    acting: systemProxyActing,
    error: systemProxyError,
    enable: enableSystemProxy,
    disable: disableSystemProxy
  } = useSystemProxyStatus();
  const runtimeMetrics = runtime
    ? [
        {
          label: "Backend status",
          value: runtime.backendStatus,
          tone: "text-sky-700 bg-sky-50 border-sky-200"
        },
        {
          label: "Core status",
          value: runtime.coreStatus,
          tone: "text-emerald-700 bg-emerald-50 border-emerald-200"
        },
        {
          label: "System proxy",
          value: runtime.systemProxyStatus,
          tone: "text-amber-800 bg-amber-50 border-amber-200"
        },
        {
          label: "Subscriptions",
          value: String(runtime.subscriptionCount),
          tone: "text-violet-700 bg-violet-50 border-violet-200"
        },
        {
          label: "Log level",
          value: runtime.logLevel,
          tone: "text-slate-700 bg-slate-100 border-slate-200"
        }
      ]
    : metrics;

  return (
    <div className="grid gap-5 xl:grid-cols-[1.2fr_0.9fr_0.9fr]">
      <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
        <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-sky-900/60">Runtime brief</p>
        <h2 className="mb-4 text-4xl font-semibold tracking-tight text-slate-950">
          Control plane first, with room for real network operations later.
        </h2>
        <p className="max-w-2xl text-base leading-8 text-slate-700">
          The current shell is designed around the fixed project decisions: bundled Clash.Meta, system proxy only, Spring Boot core ownership, and a desktop-managed local API session.
        </p>

        <div className="mt-8">
          <MetricCards metrics={runtimeMetrics} />
          {loading ? <p className="mt-4 text-sm text-slate-500">Loading runtime summary...</p> : null}
          {error ? <p className="mt-4 text-sm text-rose-700">{error}</p> : null}
        </div>
      </section>

      <section className="rounded-[30px] border border-slate-200/70 bg-linear-to-br from-slate-950 via-slate-900 to-sky-950 p-8 text-slate-100 shadow-[0_28px_70px_rgba(15,23,42,0.22)]">
        <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-sky-200/70">Core manager</p>
        <div className="space-y-3 text-sm leading-7 text-slate-300">
          <p>State: <span className="font-medium text-white">{core?.state ?? "Loading"}</span></p>
          <p>Binary present: <span className="font-medium text-white">{core?.binaryExists ? "Yes" : "No"}</span></p>
          <p className="break-all">Configured path: <span className="font-medium text-white">{core?.configuredPath || "(not configured)"}</span></p>
          <p>Last action: <span className="font-medium text-white">{core?.lastAction ?? "NONE"}</span></p>
          {core?.lastError ? <p className="text-rose-300">Error: {core.lastError}</p> : null}
          {coreError ? <p className="text-rose-300">Error: {coreError}</p> : null}
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <button
            className="rounded-full border border-sky-300/40 bg-white/10 px-4 py-2 text-sm text-white disabled:opacity-50"
            disabled={acting}
            onClick={start}
            type="button"
          >
            Start
          </button>
          <button
            className="rounded-full border border-sky-300/40 bg-white/10 px-4 py-2 text-sm text-white disabled:opacity-50"
            disabled={acting}
            onClick={stop}
            type="button"
          >
            Stop
          </button>
          <button
            className="rounded-full border border-sky-300/40 bg-white/10 px-4 py-2 text-sm text-white disabled:opacity-50"
            disabled={acting}
            onClick={reload}
            type="button"
          >
            Reload
          </button>
        </div>
      </section>

      <section className="rounded-[30px] border border-white/60 bg-white/78 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
        <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">System proxy</p>
        <div className="space-y-3 text-sm leading-7 text-slate-700">
          <p>Status: <span className="font-medium text-slate-950">{systemProxy?.statusLabel ?? "Loading"}</span></p>
          <p>Mode: <span className="font-medium text-slate-950">{systemProxy?.mode ?? "Unknown"}</span></p>
          <p>Capability: <span className="font-medium text-slate-950">{systemProxy?.capability ?? "Unknown"}</span></p>
          <p>Scope: <span className="font-medium text-slate-950">{systemProxy?.scope ?? "Unknown"}</span></p>
          <p>Target: <span className="font-medium text-slate-950">{systemProxy ? `${systemProxy.targetHost}:${systemProxy.targetPort}` : "Unknown"}</span></p>
          <p>Services: <span className="font-medium text-slate-950">{systemProxy?.serviceCount ?? 0}</span></p>
          <p>Applied to macOS: <span className="font-medium text-slate-950">{systemProxy?.managed ? "Yes" : "No"}</span></p>
          <p>Last action: <span className="font-medium text-slate-950">{systemProxy?.lastAction ?? "NONE"}</span></p>
          {systemProxy?.services?.length ? (
            <p className="text-slate-500">Network services: {systemProxy.services.join(", ")}</p>
          ) : null}
          {systemProxy?.recommendedServices?.length ? (
            <p className="text-slate-500">Recommended services: {systemProxy.recommendedServices.join(", ")}</p>
          ) : null}
          {systemProxy?.lastError ? <p className="text-rose-700">Error: {systemProxy.lastError}</p> : null}
          {systemProxyError ? <p className="text-rose-700">Error: {systemProxyError}</p> : null}
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <button
            className="rounded-full border border-slate-200 bg-slate-900 px-4 py-2 text-sm text-white disabled:opacity-50"
            disabled={systemProxyActing}
            onClick={() => {
              void enableSystemProxy();
            }}
            type="button"
          >
            Configure On
          </button>
          <button
            className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-900 disabled:opacity-50"
            disabled={systemProxyActing}
            onClick={() => {
              void disableSystemProxy();
            }}
            type="button"
          >
            Configure Off
          </button>
        </div>

        <p className="mt-5 text-sm leading-7 text-slate-500">
          The backend now drives macOS proxy state through `networksetup`, prefers likely primary interfaces using macOS service order plus interface heuristics, and restores prior settings from a runtime snapshot when turned off.
        </p>
      </section>
    </div>
  );
}
