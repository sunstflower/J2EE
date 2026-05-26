import type { NavItem, ViewId } from "../../shared/types";

type SidebarProps = {
  activeView: ViewId;
  navItems: NavItem[];
  onSelect: (view: ViewId) => void;
};

export function Sidebar({ activeView, navItems, onSelect }: SidebarProps) {
  return (
    <aside className="hidden w-[280px] shrink-0 rounded-[32px] border border-white/60 bg-white/72 p-6 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl lg:flex lg:flex-col">
      <div className="mb-8">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">mac-proxy-client</p>
        <h1 className="text-2xl font-semibold tracking-tight text-slate-950">Desktop control shell</h1>
      </div>

      <nav className="space-y-2">
        {navItems.map((item) => {
          const active = item.id === activeView;

          return (
            <button
              key={item.id}
              className={`flex w-full items-center justify-between rounded-2xl px-4 py-3 text-left text-sm transition ${
                active ? "bg-slate-950 text-white shadow-lg" : "bg-slate-50/70 text-slate-700 hover:bg-white"
              }`}
              onClick={() => onSelect(item.id)}
              type="button"
            >
              <span>{item.label}</span>
              <span className={`h-2.5 w-2.5 rounded-full ${active ? "bg-sky-300" : "bg-slate-300"}`} />
            </button>
          );
        })}
      </nav>

      <div className="mt-auto rounded-[28px] bg-linear-to-br from-sky-500 to-cyan-400 p-5 text-sky-950">
        <p className="text-xs font-bold uppercase tracking-[0.16em] text-sky-950/70">Current scope</p>
        <p className="mt-3 text-sm leading-7">
          System proxy only, bundled Clash.Meta, Spring Boot owned runtime, Electron managed session bootstrap.
        </p>
      </div>
    </aside>
  );
}
