package com.example.cms.log.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final JdbcTemplate jdbcTemplate;

    // --- Login Log ---

    public List<Map<String, Object>> findLoginLogs(String keyword, Integer status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, username, ip, user_agent, status, message, login_time
                FROM sys_login_log WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND username LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY login_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public long countLoginLogs(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_login_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND username LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    // --- Operation Log ---

    public void insertOperationLog(Long userId, String username, String method, String url,
                                    String params, String operation, String ip, long costTime) {
        String sql = """
                INSERT INTO sys_operation_log (user_id, username, method, url, params, operation, ip, cost_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, userId, username, method, url, params, operation, ip, costTime);
    }

    public List<Map<String, Object>> findOperationLogs(String keyword, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, user_id, username, method, url, params, operation, ip, cost_time, created_at
                FROM sys_operation_log WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (username LIKE ? OR operation LIKE ? OR url LIKE ?)");
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public long countOperationLogs(String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (username LIKE ? OR operation LIKE ? OR url LIKE ?)");
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}
