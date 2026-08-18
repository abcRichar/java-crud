package com.example.cms.menu.service;

import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.common.utils.SecurityUtils;
import com.example.cms.menu.dto.MenuCreateDTO;
import com.example.cms.menu.dto.MenuUpdateDTO;
import com.example.cms.menu.entity.Menu;
import com.example.cms.menu.repository.MenuRepository;
import com.example.cms.menu.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    /**
     * Get full menu tree (admin view)
     */
    public List<MenuVO> getTree() {
        List<Menu> allMenus = menuRepository.findAll();
        return buildTree(allMenus);
    }

    /**
     * Get menus for the current logged-in user (for dynamic routing)
     */
    public List<MenuVO> getCurrentUserMenuTree() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        List<Menu> userMenus = menuRepository.findByUserId(userId);
        // Filter: only visible menus
        List<Menu> visibleMenus = userMenus.stream()
                .filter(m -> m.getVisible() == 1)
                .toList();
        return buildTree(visibleMenus);
    }

    public MenuVO getById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.MENU_NOT_FOUND));
        return toVO(menu);
    }

    @Transactional
    public Long create(MenuCreateDTO dto) {
        Menu menu = new Menu();
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        menu.setName(dto.getName());
        menu.setTitle(dto.getTitle());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setIcon(dto.getIcon());
        menu.setType(dto.getType());
        menu.setPermission(dto.getPermission());
        menu.setSort(dto.getSort());
        menu.setVisible(dto.getVisible());
        menu.setStatus(dto.getStatus());
        return menuRepository.insert(menu);
    }

    @Transactional
    public void update(MenuUpdateDTO dto) {
        Menu existing = menuRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.MENU_NOT_FOUND));

        Menu menu = new Menu();
        menu.setId(dto.getId());
        menu.setParentId(dto.getParentId() != null ? dto.getParentId() : existing.getParentId());
        menu.setName(dto.getName() != null ? dto.getName() : existing.getName());
        menu.setTitle(dto.getTitle() != null ? dto.getTitle() : existing.getTitle());
        menu.setPath(dto.getPath() != null ? dto.getPath() : existing.getPath());
        menu.setComponent(dto.getComponent() != null ? dto.getComponent() : existing.getComponent());
        menu.setIcon(dto.getIcon() != null ? dto.getIcon() : existing.getIcon());
        menu.setType(dto.getType() != null ? dto.getType() : existing.getType());
        menu.setPermission(dto.getPermission() != null ? dto.getPermission() : existing.getPermission());
        menu.setSort(dto.getSort() != null ? dto.getSort() : existing.getSort());
        menu.setVisible(dto.getVisible() != null ? dto.getVisible() : existing.getVisible());
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());

        menuRepository.update(menu);
    }

    @Transactional
    public void delete(Long id) {
        menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.MENU_NOT_FOUND));

        if (menuRepository.hasChildren(id)) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该菜单下有子菜单，无法删除");
        }

        menuRepository.deleteRoleMenusByMenuId(id);
        menuRepository.softDelete(id);
    }

    // --- Tree building ---

    private List<MenuVO> buildTree(List<Menu> menus) {
        Map<Long, MenuVO> voMap = menus.stream()
                .map(this::toVO)
                .collect(Collectors.toMap(MenuVO::getId, vo -> vo));

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : voMap.values()) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                roots.add(vo);
            } else {
                MenuVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    // Parent not in list, treat as root
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    private MenuVO toVO(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setTitle(menu.getTitle());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setType(menu.getType());
        vo.setPermission(menu.getPermission());
        vo.setSort(menu.getSort());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        vo.setCreatedAt(menu.getCreatedAt());
        vo.setUpdatedAt(menu.getUpdatedAt());
        return vo;
    }
}
