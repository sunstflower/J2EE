package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.SettingsDao;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsRecord;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsRequest;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SettingsService {

    private static final SettingsRecord DEFAULT_SETTINGS = new SettingsRecord(false, "ALL_ENABLED", "", false, "INFO");

    private final JdbcTemplate jdbcTemplate;
    private final SettingsDao settingsDao;

    public SettingsService(JdbcTemplate jdbcTemplate, SettingsDao settingsDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.settingsDao = settingsDao;
    }

    @PostConstruct
    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER PRIMARY KEY,
                    system_proxy_enabled INTEGER NOT NULL,
                    system_proxy_scope TEXT NOT NULL DEFAULT 'ALL_ENABLED',
                    system_proxy_services TEXT NOT NULL DEFAULT '',
                    launch_at_login INTEGER NOT NULL,
                    log_level TEXT NOT NULL
                )
                """);

        ensureSettingsColumns();

        if (settingsDao.findSettings() == null) {
            settingsDao.insertSettings(DEFAULT_SETTINGS);
        }
    }

    public SettingsResponse getSettings() {
        return toResponse(settingsDao.findSettings());
    }

    public SettingsResponse updateSettings(SettingsRequest request) {
        SettingsRecord updated = new SettingsRecord(
                request.systemProxyEnabled(),
                normalizeScope(request.systemProxyScope()),
                normalizeServices(request.systemProxyServices()),
                request.launchAtLogin(),
                request.logLevel()
        );
        settingsDao.updateSettings(updated);
        return toResponse(updated);
    }

    public SettingsResponse updateSystemProxyEnabled(boolean enabled) {
        SettingsResponse current = getSettings();
        SettingsRecord updated = new SettingsRecord(
                enabled,
                current.systemProxyScope(),
                current.systemProxyServices(),
                current.launchAtLogin(),
                current.logLevel()
        );
        settingsDao.updateSettings(updated);
        return toResponse(updated);
    }

    public SettingsResponse updateSystemProxyPreferences(String scope, String services) {
        SettingsResponse current = getSettings();
        SettingsRecord updated = new SettingsRecord(
                current.systemProxyEnabled(),
                normalizeScope(scope),
                normalizeServices(services),
                current.launchAtLogin(),
                current.logLevel()
        );
        settingsDao.updateSettings(updated);
        return toResponse(updated);
    }

    private SettingsResponse toResponse(SettingsRecord record) {
        return new SettingsResponse(
                record.systemProxyEnabled(),
                normalizeScope(record.systemProxyScope()),
                normalizeServices(record.systemProxyServices()),
                record.launchAtLogin(),
                record.logLevel()
        );
    }

    private String normalizeScope(String scope) {
        if ("SELECTED".equalsIgnoreCase(scope)) {
            return "SELECTED";
        }
        return "ALL_ENABLED";
    }

    private String normalizeServices(String services) {
        return services == null ? "" : services.trim();
    }

    private void ensureSettingsColumns() {
        List<String> existingColumns = jdbcTemplate.query(
                "PRAGMA table_info(app_settings)",
                (resultSet, rowNum) -> resultSet.getString("name")
        );
        Set<String> columns = new HashSet<>(existingColumns);

        if (!columns.contains("system_proxy_scope")) {
            jdbcTemplate.execute("ALTER TABLE app_settings ADD COLUMN system_proxy_scope TEXT NOT NULL DEFAULT 'ALL_ENABLED'");
        }

        if (!columns.contains("system_proxy_services")) {
            jdbcTemplate.execute("ALTER TABLE app_settings ADD COLUMN system_proxy_services TEXT NOT NULL DEFAULT ''");
        }
    }
}
