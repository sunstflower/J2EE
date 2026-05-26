import { useState } from "react";
import { useDashboardState } from "./features/app-shell/useDashboardState";
import { AppHeader } from "./features/shell/AppHeader";
import { MobileNav } from "./features/shell/MobileNav";
import { Sidebar } from "./features/shell/Sidebar";
import { OverviewPanel } from "./features/views/OverviewPanel";
import { ProxiesPanel } from "./features/views/ProxiesPanel";
import { SettingsPanel } from "./features/views/SettingsPanel";
import { SubscriptionsPanel } from "./features/views/SubscriptionsPanel";
import type { ViewId } from "./shared/types";

function renderView(activeView: ViewId, state: NonNullable<ReturnType<typeof useDashboardState>["data"]>) {
  if (activeView === "overview") {
    return <OverviewPanel metrics={state.metrics} />;
  }

  if (activeView === "proxies") {
    return <ProxiesPanel proxyGroups={state.proxyGroups} />;
  }

  if (activeView === "subscriptions") {
    return <SubscriptionsPanel />;
  }

  return <SettingsPanel />;
}

export default function App() {
  const { data, loading, error } = useDashboardState();
  const [activeView, setActiveView] = useState<ViewId>("overview");

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top_left,_rgba(255,196,87,0.18),_transparent_24%),radial-gradient(circle_at_top_right,_rgba(58,151,255,0.14),_transparent_28%),linear-gradient(180deg,_#f7fbff_0%,_#ebf1f8_100%)] px-6 text-slate-900">
        <div className="rounded-[28px] border border-white/60 bg-white/75 px-8 py-6 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
          <p className="text-sm font-medium text-slate-700">Loading dashboard scaffold...</p>
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top_left,_rgba(255,196,87,0.18),_transparent_24%),radial-gradient(circle_at_top_right,_rgba(58,151,255,0.14),_transparent_28%),linear-gradient(180deg,_#f7fbff_0%,_#ebf1f8_100%)] px-6 text-slate-900">
        <div className="max-w-lg rounded-[28px] border border-rose-200 bg-white/80 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
          <p className="mb-3 text-xs font-bold uppercase tracking-[0.18em] text-rose-700">Dashboard load failure</p>
          <h1 className="mb-3 text-2xl font-semibold tracking-tight text-slate-950">The frontend shell could not load its startup state.</h1>
          <p className="text-sm leading-7 text-slate-700">{error ?? "Unknown startup error."}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(255,196,87,0.18),_transparent_24%),radial-gradient(circle_at_top_right,_rgba(58,151,255,0.14),_transparent_28%),linear-gradient(180deg,_#f7fbff_0%,_#ebf1f8_100%)] text-slate-900">
      <div className="mx-auto flex w-[min(1380px,calc(100vw-40px))] gap-6 py-6">
        <Sidebar activeView={activeView} navItems={data.navItems} onSelect={setActiveView} />

        <div className="min-w-0 flex-1">
          <AppHeader metrics={data.metrics} />
          <MobileNav activeView={activeView} navItems={data.navItems} onSelect={setActiveView} />
          <main className="mt-6">{renderView(activeView, data)}</main>
        </div>
      </div>
    </div>
  );
}
