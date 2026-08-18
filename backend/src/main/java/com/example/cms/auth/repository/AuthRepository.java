package com.example.cms.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertLoginLog(String username, String ip, String userAgent, int status, String message) {
        String sql = """
                INSERT INTO sys_login_log (username, ip, user_agent, status, message)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, username, ip, userAgent, status, message);
    }
}
