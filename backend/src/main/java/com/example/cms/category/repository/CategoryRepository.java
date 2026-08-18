package com.example.cms.category.repository;

import com.example.cms.category.entity.Category;
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
public class CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) -> {
        Category cat = new Category();
        cat.setId(rs.getLong("id"));
        cat.setParentId(rs.getLong("parent_id"));
        cat.setName(rs.getString("name"));
        cat.setSlug(rs.getString("slug"));
        cat.setSort(rs.getInt("sort"));
        cat.setStatus(rs.getInt("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        cat.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        cat.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return cat;
    };

    public Optional<Category> findById(Long id) {
        String sql = """
                SELECT id, parent_id, name, slug, sort, status, created_at, updated_at
                FROM cms_category WHERE id = ? AND deleted = 0
                """;
        List<Category> list = jdbcTemplate.query(sql, CATEGORY_ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Category> findAll() {
        String sql = """
                SELECT id, parent_id, name, slug, sort, status, created_at, updated_at
                FROM cms_category WHERE deleted = 0 ORDER BY sort ASC, id ASC
                """;
        return jdbcTemplate.query(sql, CATEGORY_ROW_MAPPER);
    }

    public Long insert(Category category) {
        String sql = """
                INSERT INTO cms_category (parent_id, name, slug, sort, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, category.getParentId() != null ? category.getParentId() : 0);
            ps.setString(2, category.getName());
            ps.setString(3, category.getSlug() != null ? category.getSlug() : "");
            ps.setInt(4, category.getSort() != null ? category.getSort() : 0);
            ps.setInt(5, category.getStatus() != null ? category.getStatus() : 1);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Category category) {
        String sql = """
                UPDATE cms_category
                SET parent_id = ?, name = ?, slug = ?, sort = ?, status = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql, category.getParentId(), category.getName(),
                category.getSlug(), category.getSort(), category.getStatus(), category.getId());
    }

    public int updateStatus(Long id, int status) {
        return jdbcTemplate.update(
                "UPDATE cms_category SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0",
                status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(
                "UPDATE cms_category SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
    }

    public boolean hasChildren(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_category WHERE parent_id = ? AND deleted = 0",
                Long.class, id);
        return count != null && count > 0;
    }

    public boolean hasArticles(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_article WHERE category_id = ? AND deleted = 0",
                Long.class, id);
        return count != null && count > 0;
    }

    public long countAll() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_category WHERE deleted = 0 AND status = 1", Long.class);
    }

    public List<Object[]> countArticlesByCategory() {
        String sql = """
                SELECT c.name, COUNT(a.id) as article_count
                FROM cms_category c
                LEFT JOIN cms_article a ON c.id = a.category_id AND a.deleted = 0
                WHERE c.deleted = 0
                GROUP BY c.id, c.name
                ORDER BY article_count DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{rs.getString("name"), rs.getLong("article_count")});
    }
}
