package com.example.cms.article.repository;

import com.example.cms.article.entity.Article;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<Article> ARTICLE_ROW_MAPPER = (rs, rowNum) -> {
        Article article = new Article();
        article.setId(rs.getLong("id"));
        article.setTitle(rs.getString("title"));
        article.setSummary(rs.getString("summary"));
        article.setContent(rs.getString("content"));
        article.setCover(rs.getString("cover"));
        article.setCategoryId(rs.getLong("category_id"));
        article.setAuthorId(rs.getLong("author_id"));
        article.setAuthorName(rs.getString("author_name"));
        article.setStatus(rs.getString("status"));
        article.setViewCount(rs.getInt("view_count"));
        article.setLikeCount(rs.getInt("like_count"));
        article.setSeoTitle(rs.getString("seo_title"));
        article.setSeoDescription(rs.getString("seo_description"));
        Timestamp publishedAt = rs.getTimestamp("published_at");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        article.setPublishedAt(publishedAt != null ? publishedAt.toLocalDateTime() : null);
        article.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        article.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return article;
    };

    // Lightweight row mapper for list queries (no content field)
    public static final RowMapper<Article> ARTICLE_LIST_ROW_MAPPER = (rs, rowNum) -> {
        Article article = new Article();
        article.setId(rs.getLong("id"));
        article.setTitle(rs.getString("title"));
        article.setSummary(rs.getString("summary"));
        article.setCover(rs.getString("cover"));
        article.setCategoryId(rs.getLong("category_id"));
        article.setAuthorId(rs.getLong("author_id"));
        article.setAuthorName(rs.getString("author_name"));
        article.setStatus(rs.getString("status"));
        article.setViewCount(rs.getInt("view_count"));
        article.setLikeCount(rs.getInt("like_count"));
        article.setSeoTitle(rs.getString("seo_title"));
        Timestamp publishedAt = rs.getTimestamp("published_at");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        article.setPublishedAt(publishedAt != null ? publishedAt.toLocalDateTime() : null);
        article.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        article.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return article;
    };

    public Optional<Article> findById(Long id) {
        String sql = """
                SELECT id, title, summary, content, cover, category_id, author_id, author_name,
                       status, view_count, like_count, seo_title, seo_description, published_at, created_at, updated_at
                FROM cms_article WHERE id = ? AND deleted = 0
                """;
        List<Article> list = jdbcTemplate.query(sql, ARTICLE_ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Article> findPage(String keyword, Long categoryId, String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, title, summary, cover, category_id, author_id, author_name,
                       status, view_count, like_count, seo_title, published_at, created_at, updated_at
                FROM cms_article WHERE deleted = 0
                """);
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(sql.toString(), ARTICLE_LIST_ROW_MAPPER, params.toArray());
    }

    public long count(String keyword, Long categoryId, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cms_article WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    public Long insert(Article article) {
        String sql = """
                INSERT INTO cms_article (title, summary, content, cover, category_id, author_id, author_name,
                    status, view_count, like_count, seo_title, seo_description, published_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, article.getTitle());
            ps.setString(2, article.getSummary() != null ? article.getSummary() : "");
            ps.setString(3, article.getContent() != null ? article.getContent() : "");
            ps.setString(4, article.getCover() != null ? article.getCover() : "");
            ps.setLong(5, article.getCategoryId() != null ? article.getCategoryId() : 0);
            ps.setLong(6, article.getAuthorId() != null ? article.getAuthorId() : 0);
            ps.setString(7, article.getAuthorName() != null ? article.getAuthorName() : "");
            ps.setString(8, article.getStatus() != null ? article.getStatus() : "DRAFT");
            ps.setInt(9, 0);
            ps.setInt(10, 0);
            ps.setString(11, article.getSeoTitle() != null ? article.getSeoTitle() : "");
            ps.setString(12, article.getSeoDescription() != null ? article.getSeoDescription() : "");
            ps.setTimestamp(13, article.getPublishedAt() != null ? Timestamp.valueOf(article.getPublishedAt()) : null);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Article article) {
        String sql = """
                UPDATE cms_article
                SET title = ?, summary = ?, content = ?, cover = ?, category_id = ?,
                    seo_title = ?, seo_description = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql, article.getTitle(),
                article.getSummary() != null ? article.getSummary() : "",
                article.getContent() != null ? article.getContent() : "",
                article.getCover() != null ? article.getCover() : "",
                article.getCategoryId() != null ? article.getCategoryId() : 0,
                article.getSeoTitle() != null ? article.getSeoTitle() : "",
                article.getSeoDescription() != null ? article.getSeoDescription() : "",
                article.getId());
    }

    public int updateStatus(Long id, String status) {
        if ("PUBLISHED".equals(status)) {
            return jdbcTemplate.update(
                    "UPDATE cms_article SET status = ?, published_at = NOW(), updated_at = NOW() WHERE id = ? AND deleted = 0",
                    status, id);
        }
        return jdbcTemplate.update(
                "UPDATE cms_article SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0",
                status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(
                "UPDATE cms_article SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
    }

    public int batchSoftDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        String placeholders = String.join(",", ids.stream().map(i -> "?").toList());
        return jdbcTemplate.update(
                "UPDATE cms_article SET deleted = 1, updated_at = NOW() WHERE id IN (" + placeholders + ")",
                ids.toArray());
    }

    public int incrementViewCount(Long id) {
        return jdbcTemplate.update(
                "UPDATE cms_article SET view_count = view_count + 1 WHERE id = ? AND deleted = 0", id);
    }

    // --- Dashboard stats ---

    public long countAll() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_article WHERE deleted = 0", Long.class);
    }

    public long countByStatus(String status) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_article WHERE deleted = 0 AND status = ?", Long.class, status);
    }

    public long countToday() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cms_article WHERE deleted = 0 AND DATE(created_at) = CURDATE()", Long.class);
    }

    public List<Object[]> countByDay(int days) {
        String sql = """
                SELECT DATE(created_at) as date, COUNT(*) as cnt
                FROM cms_article
                WHERE deleted = 0 AND created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE(created_at)
                ORDER BY date ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Object[]{rs.getDate("date").toString(), rs.getLong("cnt")}, days);
    }
}
