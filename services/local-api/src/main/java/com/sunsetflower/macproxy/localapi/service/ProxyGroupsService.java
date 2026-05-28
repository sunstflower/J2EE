package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.ProxySelectionsDao;
import com.sunsetflower.macproxy.localapi.service.dto.ProxyGroupResponse;
import com.sunsetflower.macproxy.localapi.service.dto.ProxyGroupSelectionResponse;
import com.sunsetflower.macproxy.localapi.service.dto.ProxySelectionRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProxyGroupsService {

    private static final List<String> FIXED_GROUPS = List.of("Auto Select", "Global");

    private final JdbcTemplate jdbcTemplate;
    private final ProxySelectionsDao proxySelectionsDao;
    private final ImportedProxyNodesService importedProxyNodesService;

    public ProxyGroupsService(
            JdbcTemplate jdbcTemplate,
            ProxySelectionsDao proxySelectionsDao,
            ImportedProxyNodesService importedProxyNodesService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.proxySelectionsDao = proxySelectionsDao;
        this.importedProxyNodesService = importedProxyNodesService;
    }

    @PostConstruct
    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS proxy_selections (
                    group_name TEXT PRIMARY KEY,
                    selected_node_name TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
                """);
    }

    public List<ProxyGroupSelectionResponse> getGroups() {
        List<String> availableNodeNames = importedProxyNodesService.getAllNodes().stream()
                .map(node -> node.nodeName())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        return FIXED_GROUPS.stream()
                .map(groupName -> toSelectionResponse(groupName, availableNodeNames))
                .toList();
    }

    public ProxyGroupSelectionResponse updateSelection(String groupName, String selectedNodeName) {
        String normalizedGroupName = normalizeGroupName(groupName);
        List<String> availableNodeNames = importedProxyNodesService.getAllNodes().stream()
                .map(node -> node.nodeName())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        if (availableNodeNames.isEmpty()) {
            throw new IllegalArgumentException("No imported proxy nodes are available");
        }

        if (selectedNodeName == null || selectedNodeName.isBlank()) {
            throw new IllegalArgumentException("Selected node name is required");
        }

        if (!availableNodeNames.contains(selectedNodeName)) {
            throw new IllegalArgumentException("Selected node is not available: " + selectedNodeName);
        }

        ProxySelectionRecord existing = proxySelectionsDao.findByGroupName(normalizedGroupName);
        ProxySelectionRecord updated = new ProxySelectionRecord(
                normalizedGroupName,
                selectedNodeName,
                OffsetDateTime.now().toString()
        );

        if (existing == null) {
            proxySelectionsDao.insert(updated);
        } else {
            proxySelectionsDao.update(updated);
        }

        return new ProxyGroupSelectionResponse(
                normalizedGroupName,
                selectedNodeName,
                availableNodeNames,
                updated.updatedAt()
        );
    }

    public List<ProxyGroupResponse> buildDashboardGroups() {
        List<String> availableNodeNames = importedProxyNodesService.getAllNodes().stream()
                .map(node -> node.nodeName())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        if (availableNodeNames.isEmpty()) {
            return List.of(
                    new ProxyGroupResponse("Imported nodes", "No imported proxies yet", "Refresh a subscription to import nodes")
            );
        }

        return getGroups().stream()
                .map(group -> new ProxyGroupResponse(
                        group.groupName(),
                        group.selectedNodeName().isBlank() ? "(not selected)" : group.selectedNodeName(),
                        group.availableNodeNames().size() + " available nodes"
                ))
                .toList();
    }

    private ProxyGroupSelectionResponse toSelectionResponse(String groupName, List<String> availableNodeNames) {
        ProxySelectionRecord existing = proxySelectionsDao.findByGroupName(groupName);
        String selectedNodeName = existing == null ? "" : existing.selectedNodeName();
        String updatedAt = existing == null ? "" : existing.updatedAt();

        return new ProxyGroupSelectionResponse(
                groupName,
                selectedNodeName,
                availableNodeNames,
                updatedAt
        );
    }

    private String normalizeGroupName(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Proxy group name is required");
        }

        return FIXED_GROUPS.stream()
                .filter(candidate -> candidate.equalsIgnoreCase(groupName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown proxy group: " + groupName));
    }
}
