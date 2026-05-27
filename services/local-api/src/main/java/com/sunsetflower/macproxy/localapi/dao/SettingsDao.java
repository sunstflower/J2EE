package com.sunsetflower.macproxy.localapi.dao;

import com.sunsetflower.macproxy.localapi.service.dto.SettingsRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SettingsDao {

    @Select("""
            SELECT system_proxy_enabled AS systemProxyEnabled,
                   system_proxy_scope AS systemProxyScope,
                   system_proxy_services AS systemProxyServices,
                   launch_at_login AS launchAtLogin,
                   log_level AS logLevel
            FROM app_settings
            WHERE id = 1
            """)
    SettingsRecord findSettings();

    @Insert("""
            INSERT INTO app_settings (id, system_proxy_enabled, system_proxy_scope, system_proxy_services, launch_at_login, log_level)
            VALUES (1, #{systemProxyEnabled}, #{systemProxyScope}, #{systemProxyServices}, #{launchAtLogin}, #{logLevel})
            """)
    void insertSettings(SettingsRecord record);

    @Update("""
            UPDATE app_settings
            SET system_proxy_enabled = #{systemProxyEnabled},
                system_proxy_scope = #{systemProxyScope},
                system_proxy_services = #{systemProxyServices},
                launch_at_login = #{launchAtLogin},
                log_level = #{logLevel}
            WHERE id = 1
            """)
    void updateSettings(SettingsRecord record);
}
