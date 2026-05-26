package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.SettingsDao;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsRecord;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsRequest;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private static final SettingsRecord DEFAULT_SETTINGS = new SettingsRecord(false, false, "INFO");

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
                    launch_at_login INTEGER NOT NULL,
                    log_level TEXT NOT NULL
                )
                """);

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
                request.launchAtLogin(),
                request.logLevel()
        );
        settingsDao.updateSettings(updated);
        return toResponse(updated);
    }

    private SettingsResponse toResponse(SettingsRecord record) {
        return new SettingsResponse(
                record.systemProxyEnabled(),
                record.launchAtLogin(),
                record.logLevel()
        );
    }
}
