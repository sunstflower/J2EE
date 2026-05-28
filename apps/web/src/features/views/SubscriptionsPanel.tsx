import { useState } from "react";
import { useSubscriptionsState } from "../subscriptions/useSubscriptionsState";

export function SubscriptionsPanel() {
  const { data, loading, saving, refreshing, error, create, update, remove, refresh, refreshAll } = useSubscriptionsState();
  const [name, setName] = useState("");
  const [sourceUrl, setSourceUrl] = useState("");

  if (loading) {
    return (
      <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
        <p className="text-sm text-slate-600">Loading subscriptions...</p>
      </section>
    );
  }

  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Subscription sources</p>
          <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
            First real local-api backed subscription list.
          </h2>
        </div>
        <div className="flex flex-wrap gap-3">
          <button
            className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={refreshing || saving || !data.some((subscription) => subscription.enabled)}
            onClick={() => {
              void refreshAll();
            }}
            type="button"
          >
            {refreshing ? "Refreshing..." : "Refresh enabled"}
          </button>
          <button
            className="rounded-full border border-slate-200 bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
            disabled={saving || refreshing || !name.trim() || !sourceUrl.trim()}
            onClick={async () => {
              const nextName = name.trim();
              const nextSourceUrl = sourceUrl.trim();
              try {
                await create({
                  name: nextName,
                  sourceUrl: nextSourceUrl,
                  enabled: true
                });
                setName("");
                setSourceUrl("");
              } catch {
              }
            }}
            type="button"
          >
            Add subscription
          </button>
        </div>
      </div>

      <div className="mb-5 grid gap-4 lg:grid-cols-[1fr_1.6fr]">
        <input
          className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900"
          onChange={(event) => setName(event.target.value)}
          placeholder="Subscription name"
          value={name}
        />
        <input
          className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900"
          onChange={(event) => setSourceUrl(event.target.value)}
          placeholder="https://example.com/subscription"
          value={sourceUrl}
        />
      </div>

      <div className="space-y-4">
        {data.map((subscription) => (
          <article
            key={subscription.id}
            className="flex flex-col gap-4 rounded-[24px] border border-slate-200 bg-slate-50/75 p-5"
          >
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div className="min-w-0">
                <h3 className="text-lg font-semibold text-slate-950">{subscription.name}</h3>
                <p className="mt-1 truncate text-sm text-slate-500">{subscription.sourceUrl}</p>
                <p className="mt-1 text-sm text-slate-500">Last sync: {subscription.lastSync}</p>
              </div>
              <span className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-sm text-emerald-800">
                {subscription.status}
              </span>
            </div>

            <div className="flex flex-wrap gap-3">
              <button
                className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-800"
                disabled={saving || refreshing}
                onClick={() => {
                  void refresh(subscription.id);
                }}
                type="button"
              >
                Refresh
              </button>
              <button
                className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-800"
                disabled={saving || refreshing}
                onClick={() =>
                  void update(subscription.id, {
                    name: subscription.name,
                    sourceUrl: subscription.sourceUrl,
                    enabled: !subscription.enabled
                  })
                }
                type="button"
              >
                {subscription.enabled ? "Disable" : "Enable"}
              </button>
              <button
                className="rounded-full border border-rose-200 bg-rose-50 px-4 py-2 text-sm text-rose-700"
                disabled={saving || refreshing}
                onClick={() => void remove(subscription.id)}
                type="button"
              >
                Delete
              </button>
            </div>
          </article>
        ))}
      </div>

      {error ? (
        <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      ) : null}
    </section>
  );
}
