export type ViewId = "overview" | "proxies" | "subscriptions" | "settings";

export type NavItem = {
  id: ViewId;
  label: string;
};

export type Metric = {
  label: string;
  value: string;
  tone: string;
};

export type ProxyGroup = {
  name: string;
  active: string;
  policy: string;
};

export type Subscription = {
  id: number;
  name: string;
  sourceUrl: string;
  enabled: boolean;
  status: string;
  lastSync: string;
};

export type DashboardState = {
  navItems: NavItem[];
  metrics: Metric[];
  proxyGroups: ProxyGroup[];
  subscriptions: Subscription[];
};

export type AppSettings = {
  systemProxyEnabled: boolean;
  systemProxyScope: "ALL_ENABLED" | "SELECTED";
  systemProxyServices: string;
  launchAtLogin: boolean;
  logLevel: string;
};

export type RuntimeSummary = {
  backendStatus: string;
  coreStatus: string;
  systemProxyStatus: string;
  subscriptionCount: number;
  logLevel: string;
};

export type SystemProxyStatus = {
  enabled: boolean;
  managed: boolean;
  mode: string;
  statusLabel: string;
  capability: string;
  scope: "ALL_ENABLED" | "SELECTED";
  selectedServices: string[];
  recommendedServices: string[];
  availableServices: string[];
  targetHost: string;
  targetPort: number;
  serviceCount: number;
  services: string[];
  lastAction: string;
  lastError: string;
};

export type CoreStatus = {
  state: string;
  configuredPath: string;
  binaryExists: boolean;
  lastAction: string;
  lastStartedAt: string;
  lastError: string;
};

export type ApiSuccessResponse<T> = {
  success: true;
  data: T;
};
