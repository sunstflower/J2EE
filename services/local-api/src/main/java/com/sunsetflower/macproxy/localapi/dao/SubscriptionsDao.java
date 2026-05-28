package com.sunsetflower.macproxy.localapi.dao;

import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SubscriptionsDao {

    @Select("""
            SELECT id,
                   name,
                   source_url AS sourceUrl,
                   enabled,
                   status,
                   last_sync AS lastSync
            FROM subscriptions
            ORDER BY id DESC
            """)
    List<SubscriptionRecord> findAll();

    @Select("""
            SELECT id,
                   name,
                   source_url AS sourceUrl,
                   enabled,
                   status,
                   last_sync AS lastSync
            FROM subscriptions
            WHERE id = #{id}
            """)
    SubscriptionRecord findById(long id);

    @Insert("""
            INSERT INTO subscriptions (name, source_url, enabled, status, last_sync)
            VALUES (#{name}, #{sourceUrl}, #{enabled}, #{status}, #{lastSync})
            """)
    void insert(SubscriptionRecord record);

    @Select("SELECT last_insert_rowid()")
    long lastInsertedId();

    @Update("""
            UPDATE subscriptions
            SET name = #{name},
                source_url = #{sourceUrl},
                enabled = #{enabled},
                status = #{status},
                last_sync = #{lastSync}
            WHERE id = #{id}
            """)
    void update(SubscriptionRecord record);

    @Delete("""
            DELETE FROM subscriptions
            WHERE id = #{id}
            """)
    void delete(long id);
}
