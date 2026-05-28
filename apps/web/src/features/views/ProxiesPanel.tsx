import { useImportedProxyNodes } from "../proxies/useImportedProxyNodes";
import { useProxyGroups } from "../proxies/useProxyGroups";

export function ProxiesPanel() {
  const { data, loading, error } = useImportedProxyNodes();
  const {
    data: groups,
    loading: groupsLoading,
    saving: groupsSaving,
    error: groupsError,
    updateSelection
  } = useProxyGroups();

  return (
    <section className="rounded-[30px] border border-white/60 bg-white/75 p-8 shadow-[0_28px_70px_rgba(26,57,92,0.10)] backdrop-blur-xl">
      <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Imported proxy nodes</p>
          <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
            Real subscription-backed nodes available to the local control plane.
          </h2>
        </div>
        <div className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sm text-sky-800">
          Mode: system proxy only
        </div>
      </div>

      {loading ? <p className="text-sm text-slate-600">Loading imported nodes...</p> : null}
      {error ? <p className="text-sm text-rose-700">{error}</p> : null}
      {groupsLoading ? <p className="mt-2 text-sm text-slate-600">Loading proxy groups...</p> : null}
      {groupsError ? <p className="mt-2 text-sm text-rose-700">{groupsError}</p> : null}

      {groups.length ? (
        <div className="mb-6 grid gap-4 lg:grid-cols-2">
          {groups.map((group) => (
            <article key={group.groupName} className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="mb-2 text-sm text-slate-500">Proxy group</p>
                  <h3 className="text-xl font-semibold text-slate-950">{group.groupName}</h3>
                </div>
                <span className="rounded-full border border-slate-200 bg-white px-3 py-1 text-xs text-slate-600">
                  {group.availableNodeNames.length} nodes
                </span>
              </div>

              <label className="mt-4 block text-sm text-slate-600">
                <span className="mb-2 block">Selected node</span>
                <select
                  className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900"
                  disabled={groupsSaving || !group.availableNodeNames.length}
                  onChange={(event) => {
                    void updateSelection(group.groupName, event.target.value);
                  }}
                  value={group.selectedNodeName}
                >
                  <option value="">Choose a node</option>
                  {group.availableNodeNames.map((nodeName) => (
                    <option key={nodeName} value={nodeName}>
                      {nodeName}
                    </option>
                  ))}
                </select>
              </label>

              <p className="mt-3 text-xs text-slate-500">
                {group.updatedAt ? `Updated at ${group.updatedAt}` : "Selection not set yet"}
              </p>
            </article>
          ))}
        </div>
      ) : null}

      {!loading && !data.length ? (
        <div className="rounded-[24px] border border-dashed border-slate-300 bg-slate-50/75 p-6 text-sm text-slate-600">
          No imported nodes yet. Refresh an enabled subscription to pull proxy entries into the local database.
        </div>
      ) : null}

      {data.length ? (
        <div className="grid gap-4 lg:grid-cols-2">
          {data.map((node) => (
            <article key={node.id} className="rounded-[24px] border border-slate-200 bg-slate-50/75 p-6">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="mb-2 text-sm text-slate-500">{node.nodeType.toUpperCase()}</p>
                  <h3 className="mb-2 truncate text-xl font-semibold text-slate-950">{node.nodeName}</h3>
                  <p className="text-sm text-slate-600">
                    {node.server}:{node.port}
                  </p>
                </div>
                <span className="rounded-full border border-slate-200 bg-white px-3 py-1 text-xs text-slate-600">
                  Sub #{node.subscriptionId}
                </span>
              </div>
              <div className="mt-4 rounded-2xl border border-slate-200 bg-white px-4 py-3">
                <p className="text-xs uppercase tracking-[0.16em] text-slate-400">Imported at</p>
                <p className="mt-2 text-sm font-medium text-slate-800">{node.importedAt}</p>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}
