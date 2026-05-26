import type { DashboardState } from "../../shared/types";

const mockDashboardState: DashboardState = {
  navItems: [
    { id: "overview", label: "Overview" },
    { id: "proxies", label: "Proxies" },
    { id: "subscriptions", label: "Subscriptions" },
    { id: "settings", label: "Settings" }
  ],
  metrics: [
    { label: "Core status", value: "Idle", tone: "text-emerald-700 bg-emerald-50 border-emerald-200" },
    { label: "System proxy", value: "Disabled", tone: "text-amber-800 bg-amber-50 border-amber-200" },
    { label: "Local API", value: "Scaffolded", tone: "text-sky-700 bg-sky-50 border-sky-200" }
  ],
  proxyGroups: [
    { name: "Auto Select", active: "JP-03 Tokyo", policy: "Latency probe" },
    { name: "Global", active: "US-01 Los Angeles", policy: "Manual" },
    { name: "Streaming", active: "SG-02 Singapore", policy: "Rule-based" }
  ],
  subscriptions: [
    {
      id: 1,
      name: "Primary feed",
      sourceUrl: "https://example.com/primary",
      enabled: true,
      status: "Healthy",
      lastSync: "5 min ago"
    },
    {
      id: 2,
      name: "Fallback nodes",
      sourceUrl: "https://example.com/fallback",
      enabled: false,
      status: "Pending",
      lastSync: "Not synced yet"
    }
  ]
};

export async function getMockDashboardState(): Promise<DashboardState> {
  return Promise.resolve(mockDashboardState);
}
