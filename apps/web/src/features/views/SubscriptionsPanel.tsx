import type { Subscription } from "../../shared/types";

type SubscriptionsPanelProps = {
  subscriptions: Subscription[];
};

export function SubscriptionsPanel({ subscriptions }: SubscriptionsPanelProps) {
  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Subscription sources</p>
          <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
            Import flow placeholders for the local API contract.
          </h2>
        </div>
        <button className="rounded-full border border-slate-200 bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Refresh all
        </button>
      </div>

      <div className="space-y-4">
        {subscriptions.map((subscription) => (
          <article
            key={subscription.name}
            className="flex flex-col gap-4 rounded-[24px] border border-slate-200 bg-slate-50/75 p-5 md:flex-row md:items-center md:justify-between"
          >
            <div>
              <h3 className="text-lg font-semibold text-slate-950">{subscription.name}</h3>
              <p className="mt-1 text-sm text-slate-500">Last sync: {subscription.lastSync}</p>
            </div>
            <span className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-sm text-emerald-800">
              {subscription.status}
            </span>
          </article>
        ))}
      </div>
    </section>
  );
}
