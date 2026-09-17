package com.example.cms.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userSql = """
                SELECT id, username, password, status
                FROM sys_user
                WHERE username = ? AND deleted = 0
                """;

        try {
            return jdbcTemplate.queryForObject(userSql, (rs, rowNum) -> {
                Long userId = rs.getLong("id");
                int status = rs.getInt("status");
                Set<String> permissions = loadPermissions(userId);
                return new SecurityUser(
                        userId,
                        rs.getString("username"),
                        rs.getString("password"),
                        status == 1,
                        permissions,
                        null
                );
            }, username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
    }

    public UserDetails loadUserByUserId(Long userId) {
        String userSql = """
                SELECT id, username, password, status
                FROM sys_user
                WHERE id = ? AND deleted = 0
                """;

        try {
            return jdbcTemplate.queryForObject(userSql, (rs, rowNum) -> {
                Set<String> permissions = loadPermissions(userId);
                return new SecurityUser(
                        userId,
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("status") == 1,
                        permissions,
                        null
                );
            }, userId);
        } catch (Exception e) {
            throw new UsernameNotFoundException("用户不存在, id=" + userId);
        }
    }

    private Set<String> loadPermissions(Long userId) {
        String sql = """
                SELECT DISTINCT m.permission
                FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.id
                INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id
                INNER JOIN sys_menu m ON rm.menu_id = m.id
                WHERE ur.user_id = ?
                  AND r.deleted = 0 AND r.status = 1
                  AND m.deleted = 0 AND m.permission != '' AND m.status = 1
                """;

        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, userId));
    }
}
