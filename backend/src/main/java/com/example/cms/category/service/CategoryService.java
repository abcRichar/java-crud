package com.example.cms.category.service;

import com.example.cms.category.dto.CategoryCreateDTO;
import com.example.cms.category.dto.CategoryUpdateDTO;
import com.example.cms.category.entity.Category;
import com.example.cms.category.repository.CategoryRepository;
import com.example.cms.category.vo.CategoryVO;
import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryVO> getTree() {
        List<Category> all = categoryRepository.findAll();
        return buildTree(all);
    }

    public List<CategoryVO> getAll() {
        return categoryRepository.findAll().stream().map(this::toVO).toList();
    }

    public CategoryVO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));
        return toVO(category);
    }

    @Transactional
    public Long create(CategoryCreateDTO dto) {
        Category category = new Category();
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        return categoryRepository.insert(category);
    }

    @Transactional
    public void update(CategoryUpdateDTO dto) {
        Category existing = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));

        Category category = new Category();
        category.setId(dto.getId());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : existing.getParentId());
        category.setName(dto.getName() != null ? dto.getName() : existing.getName());
        category.setSlug(dto.getSlug() != null ? dto.getSlug() : existing.getSlug());
        category.setSort(dto.getSort() != null ? dto.getSort() : existing.getSort());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        categoryRepository.update(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.hasChildren(id)) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }
        if (categoryRepository.hasArticles(id)) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该分类下有文章，无法删除");
        }

        categoryRepository.softDelete(id);
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.CATEGORY_NOT_FOUND));
        categoryRepository.updateStatus(id, status);
    }

    private List<CategoryVO> buildTree(List<Category> categories) {
        Map<Long, CategoryVO> voMap = categories.stream()
                .map(this::toVO)
                .collect(Collectors.toMap(CategoryVO::getId, vo -> vo));

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : voMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                CategoryVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        vo.setCreatedAt(category.getCreatedAt());
        vo.setUpdatedAt(category.getUpdatedAt());
        return vo;
    }
}
