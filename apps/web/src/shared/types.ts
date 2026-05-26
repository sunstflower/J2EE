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
  launchAtLogin: boolean;
  logLevel: string;
};

export type ApiSuccessResponse<T> = {
  success: true;
  data: T;
};
