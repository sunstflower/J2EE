import type { Metric } from "../types";

type MetricCardsProps = {
  metrics: Metric[];
  compact?: boolean;
};

export function MetricCards({ metrics, compact = false }: MetricCardsProps) {
  return (
    <div className={`grid gap-3 text-sm ${compact ? "md:grid-cols-3" : "md:grid-cols-3"}`}>
      {metrics.map((metric) => (
        <div key={metric.label} className="rounded-2xl border border-slate-200 bg-slate-50/75 px-4 py-3">
          <p className="text-slate-500">{metric.label}</p>
          {compact ? (
            <p className="mt-2 font-medium text-slate-900">{metric.value}</p>
          ) : (
            <span className={`mt-3 inline-flex rounded-full border px-3 py-1 text-sm font-medium ${metric.tone}`}>
              {metric.value}
            </span>
          )}
        </div>
      ))}
    </div>
  );
}
