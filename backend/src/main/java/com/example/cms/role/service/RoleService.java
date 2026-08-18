package com.example.cms.role.service;

import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.common.response.PageResult;
import com.example.cms.role.dto.RoleCreateDTO;
import com.example.cms.role.dto.RoleUpdateDTO;
import com.example.cms.role.entity.Role;
import com.example.cms.role.repository.RoleRepository;
import com.example.cms.role.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public PageResult<RoleVO> getPage(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Role> roles = roleRepository.findPage(keyword, status, offset, pageSize);
        long total = roleRepository.count(keyword, status);
        List<RoleVO> voList = roles.stream().map(this::toVO).toList();
        return PageResult.of(voList, total, page, pageSize);
    }

    public List<RoleVO> getAll() {
        return roleRepository.findAll().stream().map(this::toVO).toList();
    }

    public RoleVO getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));
        return toVO(role);
    }

    @Transactional
    public Long create(RoleCreateDTO dto) {
        if (roleRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setRemark(dto.getRemark());
        role.setStatus(1);

        Long roleId = roleRepository.insert(role);

        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            roleRepository.insertRoleMenus(roleId, dto.getMenuIds());
        }

        return roleId;
    }

    @Transactional
    public void update(RoleUpdateDTO dto) {
        Role existing = roleRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));

        // Check code uniqueness if changed
        if (dto.getCode() != null && !dto.getCode().equals(existing.getCode())) {
            roleRepository.findByCode(dto.getCode()).ifPresent(r -> {
                throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
            });
        }

        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName() != null ? dto.getName() : existing.getName());
        role.setCode(dto.getCode() != null ? dto.getCode() : existing.getCode());
        role.setRemark(dto.getRemark() != null ? dto.getRemark() : existing.getRemark());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());

        roleRepository.update(role);

        // Reassign menus if provided
        if (dto.getMenuIds() != null) {
            roleRepository.deleteRoleMenus(dto.getId());
            if (!dto.getMenuIds().isEmpty()) {
                roleRepository.insertRoleMenus(dto.getId(), dto.getMenuIds());
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));

        if (roleRepository.existsUserByRoleId(id)) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该角色已分配给用户，无法删除");
        }

        roleRepository.deleteRoleMenus(id);
        roleRepository.softDelete(id);
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));
        roleRepository.updateStatus(id, status);
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getName());
        vo.setCode(role.getCode());
        vo.setRemark(role.getRemark());
        vo.setStatus(role.getStatus());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        vo.setMenuIds(roleRepository.findMenuIdsByRoleId(role.getId()));
        return vo;
    }
}
