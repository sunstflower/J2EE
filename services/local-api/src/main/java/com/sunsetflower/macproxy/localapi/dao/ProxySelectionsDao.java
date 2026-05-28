package com.sunsetflower.macproxy.localapi.dao;

import com.sunsetflower.macproxy.localapi.service.dto.ProxySelectionRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProxySelectionsDao {

    @Select("""
            SELECT group_name AS groupName,
                   selected_node_name AS selectedNodeName,
                   updated_at AS updatedAt
            FROM proxy_selections
            ORDER BY group_name ASC
            """)
    List<ProxySelectionRecord> findAll();

    @Select("""
            SELECT group_name AS groupName,
                   selected_node_name AS selectedNodeName,
                   updated_at AS updatedAt
            FROM proxy_selections
            WHERE group_name = #{groupName}
            """)
    ProxySelectionRecord findByGroupName(String groupName);

    @Insert("""
            INSERT INTO proxy_selections (group_name, selected_node_name, updated_at)
            VALUES (#{groupName}, #{selectedNodeName}, #{updatedAt})
            """)
    void insert(ProxySelectionRecord record);

    @Update("""
            UPDATE proxy_selections
            SET selected_node_name = #{selectedNodeName},
                updated_at = #{updatedAt}
            WHERE group_name = #{groupName}
            """)
    void update(ProxySelectionRecord record);
}
