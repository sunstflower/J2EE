import type { NavItem, ViewId } from "../../shared/types";

type MobileNavProps = {
  activeView: ViewId;
  navItems: NavItem[];
  onSelect: (view: ViewId) => void;
};

export function MobileNav({ activeView, navItems, onSelect }: MobileNavProps) {
  return (
    <div className="mt-6 flex gap-3 overflow-x-auto pb-1 lg:hidden">
      {navItems.map((item) => {
        const active = item.id === activeView;

        return (
          <button
            key={item.id}
            className={`rounded-full border px-4 py-2 text-sm whitespace-nowrap ${
              active ? "border-slate-900 bg-slate-900 text-white" : "border-slate-200 bg-white/75 text-slate-700"
            }`}
            onClick={() => onSelect(item.id)}
            type="button"
          >
            {item.label}
          </button>
        );
      })}
    </div>
  );
}
