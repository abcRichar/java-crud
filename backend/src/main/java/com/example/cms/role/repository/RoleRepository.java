package com.example.cms.role.repository;

import com.example.cms.role.entity.Role;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setCode(rs.getString("code"));
        role.setRemark(rs.getString("remark"));
        role.setStatus(rs.getInt("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        role.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        role.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return role;
    };

    public Optional<Role> findById(Long id) {
        String sql = """
                SELECT id, name, code, remark, status, created_at, updated_at
                FROM sys_role WHERE id = ? AND deleted = 0
                """;
        List<Role> roles = jdbcTemplate.query(sql, ROLE_ROW_MAPPER, id);
        return roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0));
    }

    public Optional<Role> findByCode(String code) {
        String sql = """
                SELECT id, name, code, remark, status, created_at, updated_at
                FROM sys_role WHERE code = ? AND deleted = 0
                """;
        List<Role> roles = jdbcTemplate.query(sql, ROLE_ROW_MAPPER, code);
        return roles.isEmpty() ? Optional.empty() : Optional.of(roles.get(0));
    }

    public List<Role> findPage(String keyword, Integer status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, name, code, remark, status, created_at, updated_at
                FROM sys_role WHERE deleted = 0
                """);
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (name LIKE ? OR code LIKE ?)");
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(sql.toString(), ROLE_ROW_MAPPER, params.toArray());
    }

    public long count(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_role WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (name LIKE ? OR code LIKE ?)");
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    public List<Role> findAll() {
        String sql = """
                SELECT id, name, code, remark, status, created_at, updated_at
                FROM sys_role WHERE deleted = 0 AND status = 1 ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(sql, ROLE_ROW_MAPPER);
    }

    public Long insert(Role role) {
        String sql = """
                INSERT INTO sys_role (name, code, remark, status)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getName());
            ps.setString(2, role.getCode());
            ps.setString(3, role.getRemark() != null ? role.getRemark() : "");
            ps.setInt(4, role.getStatus() != null ? role.getStatus() : 1);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Role role) {
        String sql = """
                UPDATE sys_role
                SET name = ?, code = ?, remark = ?, status = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql, role.getName(), role.getCode(),
                role.getRemark(), role.getStatus(), role.getId());
    }

    public int updateStatus(Long id, int status) {
        return jdbcTemplate.update("UPDATE sys_role SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0",
                status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE sys_role SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
    }

    // --- Role-Menu relations ---

    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return jdbcTemplate.queryForList("SELECT menu_id FROM sys_role_menu WHERE role_id = ?", Long.class, roleId);
    }

    public void deleteRoleMenus(Long roleId) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
    }

    public void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        String sql = "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, menuIds, menuIds.size(), (ps, menuId) -> {
            ps.setLong(1, roleId);
            ps.setLong(2, menuId);
        });
    }

    public boolean existsUserByRoleId(Long roleId) {
        String sql = "SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, roleId);
        return count != null && count > 0;
    }
}
