package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.ImportedProxyNodesDao;
import com.sunsetflower.macproxy.localapi.service.dto.ImportedProxyNodeRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ImportedProxyNodesService {

    private final JdbcTemplate jdbcTemplate;
    private final ImportedProxyNodesDao importedProxyNodesDao;

    public ImportedProxyNodesService(JdbcTemplate jdbcTemplate, ImportedProxyNodesDao importedProxyNodesDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.importedProxyNodesDao = importedProxyNodesDao;
    }

    @PostConstruct
    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS imported_proxy_nodes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subscription_id INTEGER NOT NULL,
                    node_name TEXT NOT NULL,
                    node_type TEXT NOT NULL,
                    server TEXT NOT NULL,
                    port INTEGER NOT NULL,
                    cipher TEXT,
                    password TEXT,
                    uuid TEXT,
                    alter_id INTEGER,
                    tls INTEGER,
                    network TEXT,
                    server_name TEXT,
                    ws_path TEXT,
                    ws_host TEXT,
                    imported_at TEXT NOT NULL
                )
                """);
        ensureColumnExists("cipher", "TEXT");
        ensureColumnExists("password", "TEXT");
        ensureColumnExists("uuid", "TEXT");
        ensureColumnExists("alter_id", "INTEGER");
        ensureColumnExists("tls", "INTEGER");
        ensureColumnExists("network", "TEXT");
        ensureColumnExists("server_name", "TEXT");
        ensureColumnExists("ws_path", "TEXT");
        ensureColumnExists("ws_host", "TEXT");
    }

    public List<ImportedProxyNodeRecord> getAllNodes() {
        return importedProxyNodesDao.findAll();
    }

    public List<ImportedProxyNodeRecord> getNodesForSubscription(long subscriptionId) {
        return importedProxyNodesDao.findBySubscriptionId(subscriptionId);
    }

    public void replaceNodesForSubscription(long subscriptionId, List<ImportedProxyNodeRecord> records) {
        importedProxyNodesDao.deleteBySubscriptionId(subscriptionId);
        for (ImportedProxyNodeRecord record : records) {
            importedProxyNodesDao.insert(record);
        }
    }

    private void ensureColumnExists(String columnName, String columnType) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('imported_proxy_nodes') WHERE name = ?",
                Integer.class,
                columnName
        );
        if (!Objects.equals(count, 0)) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE imported_proxy_nodes ADD COLUMN " + columnName + " " + columnType);
    }
}
