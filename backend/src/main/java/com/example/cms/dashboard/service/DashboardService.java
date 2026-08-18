package com.example.cms.dashboard.service;

import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userCount", userRepository.countAll());
        summary.put("articleCount", articleRepository.countAll());
        summary.put("publishedCount", articleRepository.countByStatus("PUBLISHED"));
        summary.put("categoryCount", categoryRepository.countAll());
        summary.put("todayNewUsers", userRepository.countToday());
        summary.put("todayNewArticles", articleRepository.countToday());
        return summary;
    }

    public Map<String, Object> getUserGrowthTrend(int days) {
        String sql = """
                SELECT DATE(created_at) as date, COUNT(*) as cnt
                FROM sys_user
                WHERE deleted = 0 AND created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE(created_at)
                ORDER BY date ASC
                """;
        List<Object[]> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Object[]{rs.getDate("date").toString(), rs.getLong("cnt")}, days);

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] row : rows) {
            dates.add((String) row[0]);
            counts.add((Long) row[1]);
        }

        return Map.of("dates", dates, "counts", counts);
    }

    public Map<String, Object> getArticleTrend(int days) {
        List<Object[]> rows = articleRepository.countByDay(days);
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] row : rows) {
            dates.add((String) row[0]);
            counts.add((Long) row[1]);
        }
        return Map.of("dates", dates, "counts", counts);
    }

    public Map<String, Object> getCategoryStats() {
        List<Object[]> rows = categoryRepository.countArticlesByCategory();
        List<String> names = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Object[] row : rows) {
            names.add((String) row[0]);
            counts.add((Long) row[1]);
        }
        return Map.of("names", names, "counts", counts);
    }
}
