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
  name: string;
  status: string;
  lastSync: string;
};

export type DashboardState = {
  navItems: NavItem[];
  metrics: Metric[];
  proxyGroups: ProxyGroup[];
  subscriptions: Subscription[];
};

export type ApiSuccessResponse<T> = {
  success: true;
  data: T;
};
