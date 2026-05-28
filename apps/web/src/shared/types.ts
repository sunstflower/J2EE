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

export type ImportedProxyNode = {
  id: number;
  subscriptionId: number;
  nodeName: string;
  nodeType: string;
  server: string;
  port: number;
  importedAt: string;
};

export type ProxyGroupSelection = {
  groupName: string;
  selectedNodeName: string;
  availableNodeNames: string[];
  updatedAt: string;
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
  systemProxyConfirmedServices: string;
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

export type RuntimeLogLine = {
  lineNumber: number;
  content: string;
};

export type RuntimeLogs = {
  logFile: string;
  available: boolean;
  lineCount: number;
  lines: RuntimeLogLine[];
};

export type RuntimeError = {
  source: string;
  severity: string;
  message: string;
};

export type RuntimeErrors = {
  errorCount: number;
  errors: RuntimeError[];
};

export type SystemProxyStatus = {
  enabled: boolean;
  managed: boolean;
  mode: string;
  statusLabel: string;
  capability: string;
  scope: "ALL_ENABLED" | "SELECTED";
  selectedServices: string[];
  confirmedServices: string[];
  recommendedServices: string[];
  availableServices: string[];
  activeServices: string[];
  recommendationPending: boolean;
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
  mixedPort: number;
  controllerPort: number;
  lastAction: string;
  lastStartedAt: string;
  lastError: string;
};

export type ApiSuccessResponse<T> = {
  success: true;
  data: T;
};
