package com.sunsetflower.macproxy.localapi.dao;

import com.sunsetflower.macproxy.localapi.service.dto.ImportedProxyNodeRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImportedProxyNodesDao {

    @Select("""
            SELECT id,
                   subscription_id AS subscriptionId,
                   node_name AS nodeName,
                   node_type AS nodeType,
                   server,
                   port,
                   cipher,
                   password,
                   uuid,
                   alter_id AS alterId,
                   tls,
                   network,
                   server_name AS serverName,
                   ws_path AS wsPath,
                   ws_host AS wsHost,
                   imported_at AS importedAt
            FROM imported_proxy_nodes
            ORDER BY subscription_id ASC, node_name COLLATE NOCASE ASC
            """)
    List<ImportedProxyNodeRecord> findAll();

    @Select("""
            SELECT id,
                   subscription_id AS subscriptionId,
                   node_name AS nodeName,
                   node_type AS nodeType,
                   server,
                   port,
                   cipher,
                   password,
                   uuid,
                   alter_id AS alterId,
                   tls,
                   network,
                   server_name AS serverName,
                   ws_path AS wsPath,
                   ws_host AS wsHost,
                   imported_at AS importedAt
            FROM imported_proxy_nodes
            WHERE subscription_id = #{subscriptionId}
            ORDER BY node_name COLLATE NOCASE ASC
            """)
    List<ImportedProxyNodeRecord> findBySubscriptionId(long subscriptionId);

    @Delete("""
            DELETE FROM imported_proxy_nodes
            WHERE subscription_id = #{subscriptionId}
            """)
    void deleteBySubscriptionId(long subscriptionId);

    @Insert("""
            INSERT INTO imported_proxy_nodes (
                subscription_id,
                node_name,
                node_type,
                server,
                port,
                cipher,
                password,
                uuid,
                alter_id,
                tls,
                network,
                server_name,
                ws_path,
                ws_host,
                imported_at
            )
            VALUES (
                #{subscriptionId},
                #{nodeName},
                #{nodeType},
                #{server},
                #{port},
                #{cipher},
                #{password},
                #{uuid},
                #{alterId},
                #{tls},
                #{network},
                #{serverName},
                #{wsPath},
                #{wsHost},
                #{importedAt}
            )
            """)
    void insert(ImportedProxyNodeRecord record);
}
