package com.example.cms.article.service;

import com.example.cms.article.dto.ArticleCreateDTO;
import com.example.cms.article.dto.ArticleUpdateDTO;
import com.example.cms.article.entity.Article;
import com.example.cms.article.repository.ArticleRepository;
import com.example.cms.article.vo.ArticleVO;
import com.example.cms.category.entity.Category;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.common.response.PageResult;
import com.example.cms.common.utils.SecurityUtils;
import com.example.cms.user.entity.User;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public PageResult<ArticleVO> getPage(String keyword, Long categoryId, String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Article> articles = articleRepository.findPage(keyword, categoryId, status, offset, pageSize);
        long total = articleRepository.count(keyword, categoryId, status);

        // Batch load category names
        Map<Long, String> categoryNames = loadCategoryNames(articles);

        List<ArticleVO> voList = articles.stream()
                .map(a -> toVO(a, categoryNames.get(a.getCategoryId())))
                .toList();
        return PageResult.of(voList, total, page, pageSize);
    }

    public ArticleVO getById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));
        String categoryName = categoryRepository.findById(article.getCategoryId())
                .map(Category::getName)
                .orElse("");
        return toVO(article, categoryName);
    }

    @Transactional
    public Long create(ArticleCreateDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");
        article.setSeoTitle(dto.getSeoTitle());
        article.setSeoDescription(dto.getSeoDescription());

        // Set author info from current user
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            article.setAuthorId(userId);
            userRepository.findById(userId).ifPresent(user -> article.setAuthorName(user.getNickname()));
        }

        return articleRepository.insert(article);
    }

    @Transactional
    public void update(ArticleUpdateDTO dto) {
        Article existing = articleRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));

        Article article = new Article();
        article.setId(dto.getId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setSeoTitle(dto.getSeoTitle());
        article.setSeoDescription(dto.getSeoDescription());
        articleRepository.update(article);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));
        articleRepository.softDelete(id);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        articleRepository.batchSoftDelete(ids);
    }

    @Transactional
    public void publish(Long id) {
        articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));
        articleRepository.updateStatus(id, "PUBLISHED");
    }

    @Transactional
    public void offline(Long id) {
        articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));
        articleRepository.updateStatus(id, "OFFLINE");
    }

    @Transactional
    public void incrementViewCount(Long id) {
        articleRepository.incrementViewCount(id);
    }

    private Map<Long, String> loadCategoryNames(List<Article> articles) {
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .distinct()
                .filter(id -> id != null && id > 0)
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private ArticleVO toVO(Article article, String categoryName) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCover(article.getCover());
        vo.setCategoryId(article.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setAuthorId(article.getAuthorId());
        vo.setAuthorName(article.getAuthorName());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setSeoTitle(article.getSeoTitle());
        vo.setSeoDescription(article.getSeoDescription());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        return vo;
    }
}
