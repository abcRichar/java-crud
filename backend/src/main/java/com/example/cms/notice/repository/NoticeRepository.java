package com.example.cms.notice.repository;

import com.example.cms.notice.entity.Notice;
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
public class NoticeRepository {

    private final JdbcTemplate jdbcTemplate;

    public static final RowMapper<Notice> NOTICE_ROW_MAPPER = (rs, rowNum) -> {
        Notice notice = new Notice();
        notice.setId(rs.getLong("id"));
        notice.setTitle(rs.getString("title"));
        notice.setContent(rs.getString("content"));
        notice.setType(rs.getString("type"));
        notice.setStatus(rs.getString("status"));
        notice.setCreatedBy(rs.getLong("created_by"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        notice.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        notice.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return notice;
    };

    public Optional<Notice> findById(Long id) {
        String sql = """
                SELECT id, title, content, type, status, created_by, created_at, updated_at
                FROM sys_notice WHERE id = ? AND deleted = 0
                """;
        List<Notice> list = jdbcTemplate.query(sql, NOTICE_ROW_MAPPER, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Notice> findPage(String keyword, String type, String status, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, title, content, type, status, created_by, created_at, updated_at
                FROM sys_notice WHERE deleted = 0
                """);
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (StringUtils.hasText(type)) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbcTemplate.query(sql.toString(), NOTICE_ROW_MAPPER, params.toArray());
    }

    public long count(String keyword, String type, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_notice WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND title LIKE ?");
            params.add("%" + keyword + "%");
        }
        if (StringUtils.hasText(type)) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (StringUtils.hasText(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    public Long insert(Notice notice) {
        String sql = """
                INSERT INTO sys_notice (title, content, type, status, created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getContent());
            ps.setString(3, notice.getType() != null ? notice.getType() : "NOTICE");
            ps.setString(4, notice.getStatus() != null ? notice.getStatus() : "DRAFT");
            ps.setLong(5, notice.getCreatedBy() != null ? notice.getCreatedBy() : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Notice notice) {
        String sql = """
                UPDATE sys_notice
                SET title = ?, content = ?, type = ?, updated_at = NOW()
                WHERE id = ? AND deleted = 0
                """;
        return jdbcTemplate.update(sql, notice.getTitle(), notice.getContent(),
                notice.getType(), notice.getId());
    }

    public int updateStatus(Long id, String status) {
        return jdbcTemplate.update(
                "UPDATE sys_notice SET status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0",
                status, id);
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(
                "UPDATE sys_notice SET deleted = 1, updated_at = NOW() WHERE id = ?", id);
    }

    // --- Notice-User relations ---

    public void insertNoticeUsers(Long noticeId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        String sql = "INSERT INTO sys_notice_user (notice_id, user_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, userIds, userIds.size(), (ps, userId) -> {
            ps.setLong(1, noticeId);
            ps.setLong(2, userId);
        });
    }

    public void deleteNoticeUsers(Long noticeId) {
        jdbcTemplate.update("DELETE FROM sys_notice_user WHERE notice_id = ?", noticeId);
    }

    public void markAsRead(Long noticeId, Long userId) {
        String sql = """
                INSERT INTO sys_notice_user (notice_id, user_id, is_read, read_at)
                VALUES (?, ?, 1, NOW())
                ON DUPLICATE KEY UPDATE is_read = 1, read_at = NOW()
                """;
        jdbcTemplate.update(sql, noticeId, userId);
    }

    public List<Notice> findUserNotices(Long userId, int offset, int limit) {
        String sql = """
                SELECT n.id, n.title, n.content, n.type, n.status, n.created_by, n.created_at, n.updated_at,
                       nu.is_read, nu.read_at
                FROM sys_notice n
                INNER JOIN sys_notice_user nu ON n.id = nu.notice_id
                WHERE nu.user_id = ? AND n.deleted = 0 AND n.status = 'PUBLISHED'
                ORDER BY n.created_at DESC LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, NOTICE_ROW_MAPPER, userId, limit, offset);
    }

    public long countUserNotices(Long userId) {
        String sql = """
                SELECT COUNT(*) FROM sys_notice n
                INNER JOIN sys_notice_user nu ON n.id = nu.notice_id
                WHERE nu.user_id = ? AND n.deleted = 0 AND n.status = 'PUBLISHED'
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }

    public long countUnread(Long userId) {
        String sql = """
                SELECT COUNT(*) FROM sys_notice n
                INNER JOIN sys_notice_user nu ON n.id = nu.notice_id
                WHERE nu.user_id = ? AND nu.is_read = 0 AND n.deleted = 0 AND n.status = 'PUBLISHED'
                """;
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }
}
