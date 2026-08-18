package com.example.cms.menu.repository;

import com.example.cms.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<Menu> MENU_ROW_MAPPER = (rs, rowNum) -> {
        Menu menu = new Menu();
        menu.setId(rs.getLong("id"));
        menu.setParentId(rs.getLong("parent_id"));
        menu.setName(rs.getString("name"));
        menu.setTitle(rs.getString("title"));
        menu.setPath(rs.getString("path"));
        menu.setComponent(rs.getString("component"));
        menu.setIcon(rs.getString("icon"));
        menu.setType(rs.getString("type"));
        menu.setPermission(rs.getString("permission"));
        menu.setSort(rs.getInt("sort"));
        menu.setVisible(rs.getInt("visible"));
        menu.setStatus(rs.getInt("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        menu.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        menu.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return menu;
    };

    public Optional<Menu> findById(Long id) {
        String sql = """
                SELECT id, parent_id, name, title, path, component, icon, type, permission, sort, visible, status, created_at, updated_at
                FROM sys_menu WHERE id = ? AND deleted = 0
                """;
        List<Menu> menus = jdbcTemplate.query(sql, MENU_ROW_MAPPER, id);
        return menus.isEmpty() ? Optional.empty() : Optional.of(menus.get(0));
    }

    public List<Menu> findAll() {
        String sql = """
                SELECT id, parent_id, name, title, path, component, icon, type, permission, sort, visible, status, created_at, updated_at
                FROM sys_menu WHERE deleted = 0 ORDER BY sort ASC, id ASC
                """;
        return jdbcTemplate.query(sql, MENU_ROW_MAPPER);
    }

    public List<Menu> findByUserId(Long userId) {
        String sql = """
                SELECT DISTINCT m.id, m.parent_id, m.name, m.title, m.path, m.component, m.icon, m.type, m.permission, m.sort, m.visible, m.status, m.created_at, m.updated_at
                FROM sys_user_role ur
                INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id
                INNER JOIN sys_menu m ON rm.menu_id = m.id
                WHERE ur.user_id = ? AND m.deleted = 0 AND m.status = 1 AND m.type != 'BUTTON'
                ORDER BY m.sort ASC, m.id ASC
                """;
        return jdbcTemplate.query(sql, MENU_ROW_MAPPER, userId);
    }

    public List<Menu> findButtonsByUserId(Long userId) {
        String sql = """
                SELECT DISTINCT m.id, m.parent_id, m.name, m.title, m.path, m.component, m.icon, m.type, m.permission, m.sort, m.visible, m.status, m.created_at, m.updated_at
                FROM sys_user_role ur
                INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id
                INNER JOIN sys_menu m ON rm.menu_id = m.id
                WHERE ur.user_id = ? AND m.deleted = 0 AND m.status = 1 AND m.type = 'BUTTON'
                """;
        return jdbcTemplate.query(sql, MENU_ROW_MAPPER, userId);
    }

    public Long insert(Menu menu) {
        String sql = """
                INSERT INTO sys_menu (parent_id, name, title, path, component, icon, type, permission, sort, visible, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, menu.getParentId() != null ? menu.getParentId() : 0);
            ps.setString(2, menu.getName());
            ps.setString(3, menu.getTitle());
            ps.setString(4, menu.getPath() != null ? menu.getPath() : "");
            ps.setString(5, menu.getComponent() != null ? menu.getComponent() : "");
            ps.setString(6, menu.getIcon() != null ? menu.getIcon() : "");
            ps.setString(7, menu.getType() != null ? menu.getType() : "MENU");
            ps.setString(8, menu.getPermission() != null ? menu.getPermission() : "");
            ps.setInt(9, menu.getSort() != null ? menu.getSort() : 0);
            ps.setInt(10, menu.getVisible() != null ? menu.getVisible() : 1);
            ps.setInt(11, menu.getStatus() != null ? menu.getStatus() : 1);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Menu menu) {
        String sql = """
                UPDATE sys_menu
                SET parent_id = ?, name = ?, title = ?, path = ?, component = ?, icon = ?, type = ?, permission = ?, sort = ?, visible = ?, status = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql,
                menu.getParentId(), menu.getName(), menu.getTitle(), menu.getPath(),
                menu.getComponent(), menu.getIcon(), menu.getType(), menu.getPermission(),
                menu.getSort(), menu.getVisible(), menu.getStatus(), menu.getId());
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update("UPDATE sys_menu SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
    }

    public boolean hasChildren(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_menu WHERE parent_id = ? AND deleted = 0", Long.class, id);
        return count != null && count > 0;
    }

    public void deleteRoleMenusByMenuId(Long menuId) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE menu_id = ?", menuId);
    }
}
