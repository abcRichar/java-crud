package com.example.cms.user.repository;

import com.example.cms.user.entity.User;
import com.example.cms.user.vo.UserOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setNickname(rs.getString("nickname"));
        user.setAvatar(rs.getString("avatar"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setStatus(rs.getInt("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        user.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return user;
    };

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT id, username, nickname, avatar, email, phone, password, status, created_at, updated_at
                FROM sys_user
                WHERE username = ? AND deleted = 0
                """;
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findById(Long id) {
        String sql = """
                SELECT id, username, nickname, avatar, email, phone, password, status, created_at, updated_at
                FROM sys_user
                WHERE id = ? AND deleted = 0
                """;
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public List<User> findPage(String keyword, Integer status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, username, nickname, avatar, email, phone, password, status, created_at, updated_at
                FROM sys_user
                WHERE deleted = 0
                """);
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (username LIKE ? OR nickname LIKE ? OR email LIKE ?)");
        }
        if (status != null) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

        List<Object> params = new java.util.ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null) {
            params.add(status);
        }
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), USER_ROW_MAPPER, params.toArray());
    }

    public long count(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_user WHERE deleted = 0");
        List<Object> params = new java.util.ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (username LIKE ? OR nickname LIKE ? OR email LIKE ?)");
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    public long countToday() {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND DATE(created_at) = CURDATE()";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND status = 1";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long insert(User user) {
        String sql = """
                INSERT INTO sys_user (username, nickname, avatar, email, phone, password, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getNickname());
            ps.setString(3, user.getAvatar());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getPassword());
            ps.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(User user) {
        String sql = """
                UPDATE sys_user
                SET nickname = ?, avatar = ?, email = ?, phone = ?, status = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql,
                user.getNickname(), user.getAvatar(), user.getEmail(),
                user.getPhone(), user.getStatus(), user.getId());
    }

    public int updatePassword(Long id, String hashedPassword) {
        String sql = "UPDATE sys_user SET password = ?, updated_at = NOW() WHERE id = ? AND deleted = 0";
        return jdbcTemplate.update(sql, hashedPassword, id);
    }

    public int updateStatus(Long id, int status) {
        String sql = "UPDATE sys_user SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0";
        return jdbcTemplate.update(sql, status, id);
    }

    public int softDelete(Long id) {
        String sql = "UPDATE sys_user SET deleted = 1, updated_at = NOW() WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // --- User-Role relations ---

    public List<Long> findRoleIdsByUserId(Long userId) {
        String sql = "SELECT role_id FROM sys_user_role WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    public void deleteUserRoles(Long userId) {
        String sql = "DELETE FROM sys_user_role WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void insertUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, roleIds, roleIds.size(), (ps, roleId) -> {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
        });
    }

    public List<String> findRoleCodesByUserId(Long userId) {
        String sql = """
                SELECT r.code
                FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.id
                WHERE ur.user_id = ? AND r.deleted = 0 AND r.status = 1
                """;
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    public List<String> findPermissionsByUserId(Long userId) {
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
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    public boolean hasRoleCode(Long userId, String roleCode) {
        String sql = """
                SELECT COUNT(*)
                FROM sys_user_role ur
                INNER JOIN sys_role r ON ur.role_id = r.id
                WHERE ur.user_id = ? AND r.code = ? AND r.deleted = 0
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, roleCode);
        return count != null && count > 0;
    }

    public long countEnabledUsersByRoleCodeExcept(String roleCode, Long exceptUserId) {
        String sql = """
                SELECT COUNT(DISTINCT u.id)
                FROM sys_user u
                INNER JOIN sys_user_role ur ON u.id = ur.user_id
                INNER JOIN sys_role r ON ur.role_id = r.id
                WHERE u.deleted = 0 AND u.status = 1
                  AND r.deleted = 0 AND r.status = 1 AND r.code = ?
                  AND u.id != ?
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, roleCode, exceptUserId);
        return count != null ? count : 0;
    }

    public List<UserOptionVO> findActiveOptions() {
        String sql = """
                SELECT id, username, nickname
                FROM sys_user
                WHERE deleted = 0 AND status = 1
                ORDER BY nickname ASC, id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new UserOptionVO(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("nickname")
        ));
    }
}
