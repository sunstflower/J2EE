import { useEffect, useState } from "react";
import { useSettingsState } from "../settings/useSettingsState";
import { useSystemProxyStatus } from "../system-proxy/useSystemProxyStatus";
import type { AppSettings } from "../../shared/types";

export function SettingsPanel() {
  const { data, loading, saving, error, save } = useSettingsState();
  const { data: systemProxy, acting: systemProxyActing, error: systemProxyError, setEnabled } = useSystemProxyStatus();
  const [draft, setDraft] = useState<AppSettings | null>(null);

  useEffect(() => {
    if (data) {
      setDraft(data);
    }
  }, [data]);

  if (loading || !draft) {
    return (
      <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
        <p className="text-sm text-slate-600">Loading settings...</p>
      </section>
    );
  }

  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Application settings</p>
          <h2 className="text-3xl font-semibold tracking-tight text-slate-950">First real local-api backed settings surface.</h2>
        </div>
        <button
          className="rounded-full border border-slate-200 bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
          disabled={saving || systemProxyActing}
          onClick={() =>
            save({
              ...draft,
              systemProxyEnabled: systemProxy?.enabled ?? draft.systemProxyEnabled
            })
          }
          type="button"
        >
          {saving ? "Saving..." : "Save settings"}
        </button>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <label className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm text-slate-500">Network behavior</p>
              <h3 className="mt-2 text-lg font-semibold text-slate-950">System proxy enabled</h3>
              <p className="mt-3 text-sm leading-7 text-slate-700">
                Controls whether the backend applies macOS proxy settings through `networksetup` for the detected enabled network services.
              </p>
              <p className="mt-3 text-sm leading-7 text-slate-500">
                Current runtime status: {systemProxy?.statusLabel ?? "Loading"}
                {systemProxy ? ` · ${systemProxy.serviceCount} services · ${systemProxy.targetHost}:${systemProxy.targetPort}` : ""}
              </p>
            </div>
            <input
              checked={draft.systemProxyEnabled}
              className="mt-1 h-5 w-5"
              disabled={systemProxyActing}
              onChange={async (event) => {
                const nextValue = event.target.checked;
                const nextScope = draft.systemProxyScope;
                const nextServices = draft.systemProxyServices
                  .split(",")
                  .map((value) => value.trim())
                  .filter(Boolean);
                setDraft((current) =>
                  current ? { ...current, systemProxyEnabled: nextValue } : current
                );
                await setEnabled(nextValue, {
                  scope: nextScope,
                  services: nextServices
                });
              }}
              type="checkbox"
            />
          </div>
        </label>

        <label className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm text-slate-500">Desktop startup</p>
              <h3 className="mt-2 text-lg font-semibold text-slate-950">Launch at login</h3>
              <p className="mt-3 text-sm leading-7 text-slate-700">
                Persists whether the application should later request launch-at-login behavior.
              </p>
            </div>
            <input
              checked={draft.launchAtLogin}
              className="mt-1 h-5 w-5"
              onChange={(event) =>
                setDraft((current) =>
                  current ? { ...current, launchAtLogin: event.target.checked } : current
                )
              }
              type="checkbox"
            />
          </div>
        </label>
      </div>

      <div className="mt-4 rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
        <p className="text-sm text-slate-500">System proxy targeting</p>
        <h3 className="mt-2 text-lg font-semibold text-slate-950">Managed network services</h3>
        <p className="mt-3 text-sm leading-7 text-slate-700">
          Choose whether system proxy should apply to every enabled macOS network service or only to explicitly selected services.
        </p>
        {systemProxy?.recommendedServices?.length ? (
          <p className="mt-3 text-sm leading-7 text-slate-500">
            Recommended by the backend from macOS service order and interface heuristics: {systemProxy.recommendedServices.join(", ")}
          </p>
        ) : null}

        <div className="mt-4 flex flex-wrap gap-3">
          <button
            className={`rounded-full border px-4 py-2 text-sm ${
              draft.systemProxyScope === "ALL_ENABLED"
                ? "border-slate-900 bg-slate-900 text-white"
                : "border-slate-300 bg-white text-slate-800"
            }`}
            onClick={() =>
              setDraft((current) =>
                current ? { ...current, systemProxyScope: "ALL_ENABLED" } : current
              )
            }
            type="button"
          >
            All enabled services
          </button>
          <button
            className={`rounded-full border px-4 py-2 text-sm ${
              draft.systemProxyScope === "SELECTED"
                ? "border-slate-900 bg-slate-900 text-white"
                : "border-slate-300 bg-white text-slate-800"
            }`}
            onClick={() =>
              setDraft((current) =>
                current ? { ...current, systemProxyScope: "SELECTED" } : current
              )
            }
            type="button"
          >
            Selected services only
          </button>
        </div>

        <div className="mt-5 grid gap-3 sm:grid-cols-2">
          {(systemProxy?.availableServices ?? []).map((serviceName) => {
            const selected = draft.systemProxyServices
              .split(",")
              .map((value) => value.trim())
              .filter(Boolean);
            const checked = selected.includes(serviceName);
            const recommended = systemProxy?.recommendedServices.includes(serviceName) ?? false;

            return (
              <label
                className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-800"
                key={serviceName}
              >
                <span>
                  {serviceName}
                  {recommended ? " (Recommended)" : ""}
                </span>
                <input
                  checked={checked}
                  className="h-4 w-4"
                  disabled={draft.systemProxyScope !== "SELECTED"}
                  onChange={(event) => {
                    const nextSelected = event.target.checked
                      ? [...selected, serviceName]
                      : selected.filter((value) => value !== serviceName);

                    setDraft((current) =>
                      current
                        ? { ...current, systemProxyServices: Array.from(new Set(nextSelected)).join(",") }
                        : current
                    );
                  }}
                  type="checkbox"
                />
              </label>
            );
          })}
        </div>

        <p className="mt-4 text-sm leading-7 text-slate-500">
          Current scope: {systemProxy?.scope ?? draft.systemProxyScope}. Selected services are persisted and used by the backend when scope is `SELECTED`.
        </p>
      </div>

      <div className="mt-4 rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
        <p className="text-sm text-slate-500">Diagnostics</p>
        <h3 className="mt-2 text-lg font-semibold text-slate-950">Log level</h3>
        <p className="mt-3 text-sm leading-7 text-slate-700">
          Stored locally for future runtime logging controls.
        </p>

        <select
          className="mt-4 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900"
          onChange={(event) =>
            setDraft((current) =>
              current ? { ...current, logLevel: event.target.value } : current
            )
          }
          value={draft.logLevel}
        >
          <option value="DEBUG">DEBUG</option>
          <option value="INFO">INFO</option>
          <option value="WARN">WARN</option>
          <option value="ERROR">ERROR</option>
        </select>
      </div>

      {error ? (
        <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      ) : null}

      {systemProxyError ? (
        <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {systemProxyError}
        </div>
      ) : null}
    </section>
  );
}
