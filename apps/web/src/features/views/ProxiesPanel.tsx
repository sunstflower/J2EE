import type { ProxyGroup } from "../../shared/types";

type ProxiesPanelProps = {
  proxyGroups: ProxyGroup[];
};

export function ProxiesPanel({ proxyGroups }: ProxiesPanelProps) {
  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Proxy groups</p>
          <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
            Selection surfaces for the future core runtime.
          </h2>
        </div>
        <div className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sm text-sky-800">
          Mode: system proxy only
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {proxyGroups.map((group) => (
          <article key={group.name} className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
            <p className="mb-2 text-sm text-slate-500">{group.policy}</p>
            <h3 className="mb-4 text-xl font-semibold text-slate-950">{group.name}</h3>
            <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3">
              <p className="text-xs uppercase tracking-[0.16em] text-slate-400">Active selection</p>
              <p className="mt-2 text-base font-medium text-slate-800">{group.active}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
